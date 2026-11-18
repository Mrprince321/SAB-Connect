package com.example.data.repository

import com.example.data.model.ClientProject
import com.example.data.model.CommUpdate
import com.example.data.model.CompanyService
import com.example.data.model.Employee
import com.example.data.model.EnquiryLead
import com.example.data.model.EnquiryStatus
import com.example.data.model.EnquiryType
import com.example.data.model.PortfolioItem
import com.example.data.model.ProjectStatus
import com.example.data.model.SyncMetadataState
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AgencyCrmRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    private fun requireAuthorizedUser(expectedUserId: String): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        check(uid == expectedUserId) { "Signed-in user changed; refusing cross-user operation." }
        return uid
    }

    private fun userDoc(userId: String) = db.collection("users").document(userId)
    private fun servicesCol(userId: String) = userDoc(userId).collection("services")
    private fun portfolioCol(userId: String) = userDoc(userId).collection("portfolio_projects")
    private fun enquiriesCol(userId: String) = userDoc(userId).collection("enquiries")
    private fun projectsCol(userId: String) = userDoc(userId).collection("client_projects")

    private suspend fun <T> awaitOnline(
        task: Task<T>,
        operationType: OperationType,
        path: String
    ) {
        try {
            withTimeout(15000L) {
                task.await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, operationType, path)
            throw e
        }
    }

    private fun <T> Flow<T>.handleStreamErrors(
        userId: String,
        operationType: OperationType,
        path: String
    ): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
        throw error
    }

    fun observeServices(userId: String): Flow<List<CompanyService>> =
        servicesCol(userId)
            .snapshots(MetadataChanges.INCLUDE)
            .map { snapshot ->
                snapshot.toObjects(CompanyService::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.title.lowercase() }
            }
            .handleStreamErrors(userId, OperationType.LIST, "users/$userId/services")

    fun observePortfolio(userId: String): Flow<List<PortfolioItem>> =
        portfolioCol(userId)
            .snapshots(MetadataChanges.INCLUDE)
            .map { snapshot ->
                snapshot.toObjects(PortfolioItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.featured }
            }
            .handleStreamErrors(userId, OperationType.LIST, "users/$userId/portfolio_projects")

    fun observeEnquiries(userId: String): Flow<List<EnquiryLead>> =
        enquiriesCol(userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots(MetadataChanges.INCLUDE)
            .map { snapshot ->
                snapshot.toObjects(EnquiryLead::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .handleStreamErrors(userId, OperationType.LIST, "users/$userId/enquiries")

    fun observeClientProjects(userId: String): Flow<List<ClientProject>> =
        projectsCol(userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .snapshots(MetadataChanges.INCLUDE)
            .map { snapshot ->
                snapshot.toObjects(ClientProject::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .handleStreamErrors(userId, OperationType.LIST, "users/$userId/client_projects")

    fun observeAgencySyncStatus(userId: String): Flow<SyncMetadataState> {
        val svcMeta = servicesCol(userId).snapshots(MetadataChanges.INCLUDE).map { it.metadata }
        val enqMeta = enquiriesCol(userId).snapshots(MetadataChanges.INCLUDE).map { it.metadata }
        val projMeta = projectsCol(userId).snapshots(MetadataChanges.INCLUDE).map { it.metadata }
        return combine(svcMeta, enqMeta, projMeta) { s, e, p ->
            SyncMetadataState(
                isFromCache = s.isFromCache || e.isFromCache || p.isFromCache,
                hasPendingWrites = s.hasPendingWrites() || e.hasPendingWrites() || p.hasPendingWrites()
            )
        }.catch { error ->
            if (error is CancellationException) throw error
            emit(SyncMetadataState(isFromCache = true, hasPendingWrites = false))
        }
    }

    suspend fun seedSabDigitechCatalogIfEmpty(userId: String): Result<Int> {
        val path = "users/$userId/services"
        return try {
            val uid = requireAuthorizedUser(userId)
            val existingServices = withTimeout(15000L) { servicesCol(uid).limit(1).get().await() }
            val existingPortfolio = withTimeout(15000L) { portfolioCol(uid).limit(1).get().await() }
            if (!existingServices.isEmpty && !existingPortfolio.isEmpty) {
                return Result.success(0)
            }
            val batch = db.batch()
            var count = 0
            if (existingServices.isEmpty) {
                val starterServices = listOf(
                    Triple("Web & E-Commerce Development", "Web Engineering", 25000.0) to
                        Triple("Responsive corporate websites, custom SaaS dashboards, and WooCommerce portals.", "React • PHP/MySQL • WordPress • SSL", "3-4 Weeks"),
                    Triple("Mobile App Engineering", "Mobile Apps", 45000.0) to
                        Triple("Android, iOS, and React Native apps with live Firebase & REST API integration.", "Kotlin • React Native • Firebase • Push", "4-6 Weeks"),
                    Triple("SAB Billing & Cloud POS Suite", "Retail & ERP", 18000.0) to
                        Triple("GST billing, multi-branch stock tracking, customer records & employee shift logs.", "GST Engine • Cloud POS • Thermal Print", "2 Weeks"),
                    Triple("SEO & Performance Growth", "Digital Marketing", 12000.0) to
                        Triple("Local & technical SEO, Google Business optimization, Meta/Google Ads & WhatsApp API.", "GA4 • GSC • Ads • WhatsApp CRM", "Monthly"),
                    Triple("Creative & OTT Post-Production", "Media Studio", 15000.0) to
                        Triple("4K video editing, motion graphics, VFX, reels & vertical OTT launch workflows.", "DaVinci • Premiere • AI Media • VFX", "1-2 Weeks")
                )
                starterServices.forEach { (head, body) ->
                    val ref = servicesCol(uid).document()
                    batch.set(
                        ref,
                        mapOf(
                            "userId" to uid,
                            "title" to head.first,
                            "category" to head.second,
                            "description" to body.first,
                            "deliverables" to body.second,
                            "startingPrice" to head.third,
                            "timeline" to body.third,
                            "activeService" to true,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    count++
                }
            }
            if (existingPortfolio.isEmpty) {
                val starterPortfolio = listOf(
                    Triple("SAB Retail Cloud POS Platform", "Multi-Store Retail Network", "Mobile & Cloud") to
                        Triple("Real-time multi-outlet inventory, GST invoice engine, and staff clock-in telemetry.", "Kotlin Compose • Firestore • Cloud Sync", "42% Faster Checkout"),
                    Triple("Hyderabad D2C Commerce & SEO Engine", "Regional Lifestyle Brand", "Web & Growth") to
                        Triple("Custom e-commerce storefront paired with technical SEO & Meta Ads lead funnels.", "Next.js • WooCommerce • GA4", "3.4x Organic Leads"),
                    Triple("OTT Vertical Series & Brand Launch", "Media Production Partner", "Post Production") to
                        Triple("Full 4K trailer suite, color grading, sound mix, and social campaign creatives.", "DaVinci Resolve • After Effects", "1.2M Campaign Views")
                )
                starterPortfolio.forEach { (head, body) ->
                    val ref = portfolioCol(uid).document()
                    batch.set(
                        ref,
                        mapOf(
                            "userId" to uid,
                            "title" to head.first,
                            "clientName" to head.second,
                            "category" to head.third,
                            "summary" to body.first,
                            "techStack" to body.second,
                            "impactMetric" to body.third,
                            "featured" to true,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    count++
                }
            }
            if (count > 0) {
                awaitOnline(batch.commit(), OperationType.CREATE, path)
            }
            Result.success(count)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun saveService(
        userId: String,
        serviceId: String?,
        title: String,
        category: String,
        description: String,
        deliverables: String,
        startingPrice: Double,
        timeline: String,
        activeService: Boolean
    ): Result<Unit> {
        val isCreate = serviceId.isNullOrBlank()
        val path = "users/$userId/services/${serviceId ?: "new"}"
        return try {
            val uid = requireAuthorizedUser(userId)
            val cleanTitle = title.trim().take(80)
            val cleanCategory = category.trim().ifEmpty { "Digital Service" }.take(50)
            val cleanDesc = description.trim().take(400)
            require(cleanTitle.isNotEmpty()) { "Service title is required" }
            require(cleanDesc.isNotEmpty()) { "Description is required" }
            val cleanPrice = startingPrice.coerceIn(0.0, 100000000.0)

            if (isCreate) {
                val ref = servicesCol(uid).document()
                awaitOnline(
                    ref.set(
                        mapOf(
                            "userId" to uid,
                            "title" to cleanTitle,
                            "category" to cleanCategory,
                            "description" to cleanDesc,
                            "deliverables" to deliverables.trim().take(200),
                            "startingPrice" to cleanPrice,
                            "timeline" to timeline.trim().take(60),
                            "activeService" to activeService,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ),
                    OperationType.CREATE,
                    ref.path
                )
            } else {
                val ref = servicesCol(uid).document(serviceId!!)
                awaitOnline(
                    ref.update(
                        mapOf(
                            "title" to cleanTitle,
                            "category" to cleanCategory,
                            "description" to cleanDesc,
                            "deliverables" to deliverables.trim().take(200),
                            "startingPrice" to cleanPrice,
                            "timeline" to timeline.trim().take(60),
                            "activeService" to activeService,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ),
                    OperationType.UPDATE,
                    ref.path
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteService(userId: String, serviceId: String): Result<Unit> {
        val path = "users/$userId/services/$serviceId"
        return try {
            val uid = requireAuthorizedUser(userId)
            awaitOnline(servicesCol(uid).document(serviceId).delete(), OperationType.DELETE, path)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun savePortfolioItem(
        userId: String,
        itemId: String?,
        title: String,
        clientName: String,
        category: String,
        summary: String,
        techStack: String,
        impactMetric: String,
        featured: Boolean
    ): Result<Unit> {
        val isCreate = itemId.isNullOrBlank()
        val path = "users/$userId/portfolio_projects/${itemId ?: "new"}"
        return try {
            val uid = requireAuthorizedUser(userId)
            val cleanTitle = title.trim().take(100)
            val cleanClient = clientName.trim().ifEmpty { "Enterprise Client" }.take(80)
            val cleanCat = category.trim().ifEmpty { "Case Study" }.take(50)
            val cleanSummary = summary.trim().take(400)
            require(cleanTitle.isNotEmpty()) { "Project title is required" }
            require(cleanSummary.isNotEmpty()) { "Summary is required" }

            if (isCreate) {
                val ref = portfolioCol(uid).document()
                awaitOnline(
                    ref.set(
                        mapOf(
                            "userId" to uid,
                            "title" to cleanTitle,
                            "clientName" to cleanClient,
                            "category" to cleanCat,
                            "summary" to cleanSummary,
                            "techStack" to techStack.trim().take(160),
                            "impactMetric" to impactMetric.trim().take(80),
                            "featured" to featured,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ),
                    OperationType.CREATE,
                    ref.path
                )
            } else {
                val ref = portfolioCol(uid).document(itemId!!)
                awaitOnline(
                    ref.update(
                        mapOf(
                            "title" to cleanTitle,
                            "clientName" to cleanClient,
                            "category" to cleanCat,
                            "summary" to cleanSummary,
                            "techStack" to techStack.trim().take(160),
                            "impactMetric" to impactMetric.trim().take(80),
                            "featured" to featured,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ),
                    OperationType.UPDATE,
                    ref.path
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deletePortfolioItem(userId: String, itemId: String): Result<Unit> {
        val path = "users/$userId/portfolio_projects/$itemId"
        return try {
            val uid = requireAuthorizedUser(userId)
            awaitOnline(portfolioCol(uid).document(itemId).delete(), OperationType.DELETE, path)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun submitEnquiry(
        userId: String,
        enquiryType: EnquiryType,
        customerId: String,
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        serviceTitle: String,
        projectTitle: String,
        requirements: String,
        budgetRange: String,
        preferredDate: String
    ): Result<String> {
        val path = "users/$userId/enquiries"
        return try {
            val uid = requireAuthorizedUser(userId)
            val cleanCustomer = customerName.trim().ifEmpty { "SAB Client" }.take(80)
            val cleanService = serviceTitle.trim().ifEmpty { "Digital Consulting" }.take(80)
            val cleanTitle = projectTitle.trim().ifEmpty { "$cleanService ${enquiryType.label}" }.take(120)
            val cleanReq = requirements.trim().take(600)
            require(cleanReq.isNotEmpty()) { "Please describe your project requirements" }

            val codeNum = 1000L + (System.currentTimeMillis() % 8999L)
            val prefix = when (enquiryType) {
                EnquiryType.ENQUIRY -> "ENQ"
                EnquiryType.QUOTATION -> "QUO"
                EnquiryType.CONSULTATION -> "CON"
            }
            val code = "$prefix-$codeNum"
            val nowLabel = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()).format(Date())
            val initialUpdate = mapOf(
                "authorRole" to "CUSTOMER",
                "authorName" to cleanCustomer,
                "message" to "Submitted ${enquiryType.label}: $cleanTitle",
                "timestampLabel" to nowLabel
            )

            val ref = enquiriesCol(uid).document()
            awaitOnline(
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "enquiryCode" to code,
                        "enquiryType" to enquiryType.name,
                        "customerId" to customerId.take(128),
                        "customerName" to cleanCustomer,
                        "customerPhone" to customerPhone.trim().take(30),
                        "customerEmail" to customerEmail.trim().take(120),
                        "serviceTitle" to cleanService,
                        "projectTitle" to cleanTitle,
                        "requirements" to cleanReq,
                        "budgetRange" to budgetRange.trim().take(60),
                        "preferredDate" to preferredDate.trim().take(60),
                        "status" to EnquiryStatus.NEW.name,
                        "assignedEmployeeId" to "",
                        "assignedEmployeeName" to "",
                        "quotedAmount" to 0.0,
                        "followUpDate" to "",
                        "followUpNote" to "",
                        "latestNote" to "Submitted ${enquiryType.label} ($code)",
                        "updates" to listOf(initialUpdate),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.CREATE,
                ref.path
            )
            Result.success(code)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun assignEnquiryToEmployee(
        userId: String,
        enquiry: EnquiryLead,
        employee: Employee
    ): Result<Unit> {
        val path = "users/$userId/enquiries/${enquiry.id}"
        return try {
            val uid = requireAuthorizedUser(userId)
            val nowLabel = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()).format(Date())
            val nextUpdates = (enquiry.updates + CommUpdate(
                authorRole = "ADMIN",
                authorName = "Admin",
                message = "Assigned lead to ${employee.fullName} (${employee.role})",
                timestampLabel = nowLabel
            )).takeLast(30).map { it.toMap() }
            val nextStatus = if (enquiry.status == EnquiryStatus.NEW.name) {
                EnquiryStatus.IN_PROGRESS.name
            } else {
                enquiry.status
            }
            awaitOnline(
                enquiriesCol(uid).document(enquiry.id).update(
                    mapOf(
                        "assignedEmployeeId" to employee.id,
                        "assignedEmployeeName" to employee.fullName.take(80),
                        "status" to nextStatus,
                        "latestNote" to "Assigned to ${employee.fullName}",
                        "updates" to nextUpdates,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.UPDATE,
                path
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateEnquiryProgress(
        userId: String,
        enquiry: EnquiryLead,
        status: EnquiryStatus,
        quotedAmount: Double,
        followUpDate: String,
        followUpNote: String,
        noteText: String,
        authorRole: String,
        authorName: String
    ): Result<Unit> {
        val path = "users/$userId/enquiries/${enquiry.id}"
        return try {
            val uid = requireAuthorizedUser(userId)
            val nowLabel = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()).format(Date())
            val cleanNote = noteText.trim().take(260)
            val summaryMessage = buildString {
                if (cleanNote.isNotEmpty()) append(cleanNote)
                else append("Status updated to ${status.label}")
                if (quotedAmount > 0.0) append(" • Quote: ${quotedAmount.toLong()}")
                if (followUpDate.isNotBlank()) append(" • Follow-up: $followUpDate")
            }.take(300)

            val nextUpdates = (enquiry.updates + CommUpdate(
                authorRole = authorRole.take(20),
                authorName = authorName.ifBlank { authorRole }.take(60),
                message = summaryMessage,
                timestampLabel = nowLabel
            )).takeLast(30).map { it.toMap() }

            awaitOnline(
                enquiriesCol(uid).document(enquiry.id).update(
                    mapOf(
                        "status" to status.name,
                        "quotedAmount" to quotedAmount.coerceIn(0.0, 1000000000.0),
                        "followUpDate" to followUpDate.trim().take(60),
                        "followUpNote" to followUpNote.trim().take(250),
                        "latestNote" to summaryMessage,
                        "updates" to nextUpdates,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.UPDATE,
                path
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun postCommunicationMessage(
        userId: String,
        enquiry: EnquiryLead,
        authorRole: String,
        authorName: String,
        message: String
    ): Result<Unit> {
        val path = "users/$userId/enquiries/${enquiry.id}"
        return try {
            val uid = requireAuthorizedUser(userId)
            val cleanMsg = message.trim().take(260)
            require(cleanMsg.isNotEmpty()) { "Message cannot be empty" }
            val nowLabel = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()).format(Date())
            val nextUpdates = (enquiry.updates + CommUpdate(
                authorRole = authorRole.take(20),
                authorName = authorName.ifBlank { authorRole }.take(60),
                message = cleanMsg,
                timestampLabel = nowLabel
            )).takeLast(30).map { it.toMap() }

            awaitOnline(
                enquiriesCol(uid).document(enquiry.id).update(
                    mapOf(
                        "updates" to nextUpdates,
                        "latestNote" to cleanMsg.take(300),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.UPDATE,
                path
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteEnquiry(userId: String, enquiryId: String): Result<Unit> {
        val path = "users/$userId/enquiries/$enquiryId"
        return try {
            val uid = requireAuthorizedUser(userId)
            awaitOnline(enquiriesCol(uid).document(enquiryId).delete(), OperationType.DELETE, path)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun saveClientProject(
        userId: String,
        projectId: String?,
        enquiryId: String,
        title: String,
        clientName: String,
        serviceTitle: String,
        assignedEmployeeId: String,
        assignedEmployeeName: String,
        status: ProjectStatus,
        progressPercent: Long,
        milestoneNote: String,
        dueDate: String,
        projectValue: Double
    ): Result<Unit> {
        val isCreate = projectId.isNullOrBlank()
        val path = "users/$userId/client_projects/${projectId ?: "new"}"
        return try {
            val uid = requireAuthorizedUser(userId)
            val cleanTitle = title.trim().take(120)
            val cleanClient = clientName.trim().ifEmpty { "Client" }.take(80)
            val cleanService = serviceTitle.trim().ifEmpty { "Digital Project" }.take(80)
            require(cleanTitle.isNotEmpty()) { "Project title is required" }

            if (isCreate) {
                val ref = projectsCol(uid).document()
                awaitOnline(
                    ref.set(
                        mapOf(
                            "userId" to uid,
                            "enquiryId" to enquiryId.take(128),
                            "title" to cleanTitle,
                            "clientName" to cleanClient,
                            "serviceTitle" to cleanService,
                            "assignedEmployeeId" to assignedEmployeeId.take(128),
                            "assignedEmployeeName" to assignedEmployeeName.take(80),
                            "status" to status.name,
                            "progressPercent" to progressPercent.coerceIn(0L, 100L),
                            "milestoneNote" to milestoneNote.trim().take(300),
                            "dueDate" to dueDate.trim().take(60),
                            "projectValue" to projectValue.coerceIn(0.0, 1000000000.0),
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ),
                    OperationType.CREATE,
                    ref.path
                )
            } else {
                val ref = projectsCol(uid).document(projectId!!)
                awaitOnline(
                    ref.update(
                        mapOf(
                            "title" to cleanTitle,
                            "clientName" to cleanClient,
                            "serviceTitle" to cleanService,
                            "assignedEmployeeId" to assignedEmployeeId.take(128),
                            "assignedEmployeeName" to assignedEmployeeName.take(80),
                            "status" to status.name,
                            "progressPercent" to progressPercent.coerceIn(0L, 100L),
                            "milestoneNote" to milestoneNote.trim().take(300),
                            "dueDate" to dueDate.trim().take(60),
                            "projectValue" to projectValue.coerceIn(0.0, 1000000000.0),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ),
                    OperationType.UPDATE,
                    ref.path
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteClientProject(userId: String, projectId: String): Result<Unit> {
        val path = "users/$userId/client_projects/$projectId"
        return try {
            val uid = requireAuthorizedUser(userId)
            awaitOnline(projectsCol(uid).document(projectId).delete(), OperationType.DELETE, path)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    private fun CommUpdate.toMap(): Map<String, Any> = mapOf(
        "authorRole" to authorRole,
        "authorName" to authorName,
        "message" to message,
        "timestampLabel" to timestampLabel
    )
}
