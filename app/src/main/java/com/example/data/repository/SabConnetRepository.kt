package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CartItem
import com.example.data.model.ClockAction
import com.example.data.model.ClockLog
import com.example.data.model.Customer
import com.example.data.model.Employee
import com.example.data.model.Invoice
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.Product
import com.example.data.model.ShiftStatus
import com.example.data.model.StoreProfile
import com.example.data.model.SyncMetadataState
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
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

object FirestoreInstanceProvider {
    @Volatile
    private var configured = false

    fun get(context: Context): FirebaseFirestore {
        val appContext = context.applicationContext
        val databaseId = appContext.getString(R.string.firestore_database_id)
        val db = FirebaseFirestore.getInstance(databaseId)
        if (!configured) {
            synchronized(this) {
                if (!configured) {
                    try {
                        val settings = FirebaseFirestoreSettings.Builder()
                            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                            .build()
                        db.firestoreSettings = settings
                    } catch (_: IllegalStateException) {
                        // Already started
                    }
                    configured = true
                }
            }
        }
        return db
    }
}

class SabConnetRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(FirestoreInstanceProvider.get(context))

    private fun requireAuthorizedUser(expectedUserId: String): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        check(uid == expectedUserId) { "Signed-in user changed; refusing cross-user operation." }
        return uid
    }

    private fun userDoc(userId: String) = db.collection("users").document(userId)
    private fun productsCol(userId: String) = userDoc(userId).collection("products")
    private fun customersCol(userId: String) = userDoc(userId).collection("customers")
    private fun invoicesCol(userId: String) = userDoc(userId).collection("invoices")
    private fun employeesCol(userId: String) = userDoc(userId).collection("employees")
    private fun clockLogsCol(userId: String) = userDoc(userId).collection("clock_logs")

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

    fun observeStoreProfile(userId: String): Flow<StoreProfile?> =
        userDoc(userId)
            .snapshots(MetadataChanges.INCLUDE)
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(StoreProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .handleStreamErrors(userId, OperationType.GET, "users/$userId")

    fun observeProducts(userId: String): Flow<List<Product>> =
        productsCol(userId)
            .snapshots(MetadataChanges.INCLUDE)
            .map { snapshot ->
                snapshot.toObjects(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.name.lowercase() }
            }
            .handleStreamErrors(userId, OperationType.LIST, "users/$userId/products")

    fun observeCustomers(userId: String): Flow<List<Customer>> =
        customersCol(userId)
            .snapshots(MetadataChanges.INCLUDE)
            .map { snapshot ->
                snapshot.toObjects(Customer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.fullName.lowercase() }
            }
            .handleStreamErrors(userId, OperationType.LIST, "users/$userId/customers")

    fun observeInvoices(userId: String): Flow<List<Invoice>> =
        invoicesCol(userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots(MetadataChanges.INCLUDE)
            .map { snapshot ->
                snapshot.toObjects(Invoice::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .handleStreamErrors(userId, OperationType.LIST, "users/$userId/invoices")

    fun observeEmployees(userId: String): Flow<List<Employee>> =
        employeesCol(userId)
            .snapshots(MetadataChanges.INCLUDE)
            .map { snapshot ->
                snapshot.toObjects(Employee::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.fullName.lowercase() }
            }
            .handleStreamErrors(userId, OperationType.LIST, "users/$userId/employees")

    fun observeClockLogs(userId: String): Flow<List<ClockLog>> =
        clockLogsCol(userId)
            .orderBy("loggedAt", Query.Direction.DESCENDING)
            .limit(80)
            .snapshots(MetadataChanges.INCLUDE)
            .map { snapshot ->
                snapshot.toObjects(ClockLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .handleStreamErrors(userId, OperationType.LIST, "users/$userId/clock_logs")

    fun observeSyncStatus(userId: String): Flow<SyncMetadataState> {
        val productsMeta = productsCol(userId).snapshots(MetadataChanges.INCLUDE).map { it.metadata }
        val invoicesMeta = invoicesCol(userId).snapshots(MetadataChanges.INCLUDE).map { it.metadata }
        val logsMeta = clockLogsCol(userId).snapshots(MetadataChanges.INCLUDE).map { it.metadata }
        return combine(productsMeta, invoicesMeta, logsMeta) { p, i, l ->
            SyncMetadataState(
                isFromCache = p.isFromCache || i.isFromCache || l.isFromCache,
                hasPendingWrites = p.hasPendingWrites() || i.hasPendingWrites() || l.hasPendingWrites()
            )
        }.catch { error ->
            if (error is CancellationException) throw error
            emit(SyncMetadataState(isFromCache = true, hasPendingWrites = false))
        }
    }

    suspend fun saveStoreProfile(
        userId: String,
        existingProfile: StoreProfile?,
        storeName: String,
        ownerName: String,
        email: String,
        phone: String,
        address: String,
        currencySymbol: String,
        taxRatePercent: Double
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireAuthorizedUser(userId)
            val cleanStore = storeName.trim().take(80)
            val cleanOwner = ownerName.trim().take(80)
            require(cleanStore.isNotEmpty()) { "Store name is required" }
            require(cleanOwner.isNotEmpty()) { "Owner name is required" }
            val cleanCurrency = currencySymbol.trim().ifEmpty { "$" }.take(5)
            val cleanTax = taxRatePercent.coerceIn(0.0, 50.0)

            if (existingProfile == null) {
                val payload = mapOf(
                    "userId" to uid,
                    "storeName" to cleanStore,
                    "ownerName" to cleanOwner,
                    "email" to email.trim().take(120),
                    "phone" to phone.trim().take(30),
                    "address" to address.trim().take(160),
                    "currencySymbol" to cleanCurrency,
                    "taxRatePercent" to cleanTax,
                    "invoiceCounter" to 1001L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                awaitOnline(userDoc(uid).set(payload), OperationType.CREATE, path)
            } else {
                val payload = mapOf(
                    "storeName" to cleanStore,
                    "ownerName" to cleanOwner,
                    "email" to email.trim().take(120),
                    "phone" to phone.trim().take(30),
                    "address" to address.trim().take(160),
                    "currencySymbol" to cleanCurrency,
                    "taxRatePercent" to cleanTax,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                awaitOnline(userDoc(uid).update(payload), OperationType.UPDATE, path)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun saveProduct(
        userId: String,
        productId: String?,
        name: String,
        sku: String,
        category: String,
        unit: String,
        sellingPrice: Double,
        costPrice: Double,
        stockQuantity: Long,
        lowStockThreshold: Long
    ): Result<Unit> {
        val isCreate = productId.isNullOrBlank()
        val path = "users/$userId/products/${productId ?: "new"}"
        return try {
            val uid = requireAuthorizedUser(userId)
            val cleanName = name.trim().take(100)
            val cleanSku = sku.trim().take(40)
            val cleanCategory = category.trim().ifEmpty { "General" }.take(50)
            val cleanUnit = unit.trim().ifEmpty { "pcs" }.take(20)
            require(cleanName.isNotEmpty()) { "Product name is required" }
            require(cleanSku.isNotEmpty()) { "SKU is required" }
            require(sellingPrice >= 0.0 && sellingPrice <= 10000000.0) { "Invalid selling price" }
            require(costPrice >= 0.0 && costPrice <= 10000000.0) { "Invalid cost price" }
            require(stockQuantity >= 0L && stockQuantity <= 10000000L) { "Stock cannot be negative" }
            require(lowStockThreshold >= 0L && lowStockThreshold <= 100000L) { "Invalid threshold" }

            if (isCreate) {
                val docRef = productsCol(uid).document()
                val payload = mapOf(
                    "userId" to uid,
                    "name" to cleanName,
                    "sku" to cleanSku,
                    "category" to cleanCategory,
                    "unit" to cleanUnit,
                    "sellingPrice" to sellingPrice,
                    "costPrice" to costPrice,
                    "stockQuantity" to stockQuantity,
                    "lowStockThreshold" to lowStockThreshold,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                awaitOnline(docRef.set(payload), OperationType.CREATE, docRef.path)
            } else {
                val docRef = productsCol(uid).document(productId!!)
                val payload = mapOf(
                    "name" to cleanName,
                    "sku" to cleanSku,
                    "category" to cleanCategory,
                    "unit" to cleanUnit,
                    "sellingPrice" to sellingPrice,
                    "costPrice" to costPrice,
                    "stockQuantity" to stockQuantity,
                    "lowStockThreshold" to lowStockThreshold,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                awaitOnline(docRef.update(payload), OperationType.UPDATE, docRef.path)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun adjustProductStock(
        userId: String,
        productId: String,
        newQuantity: Long
    ): Result<Unit> {
        val path = "users/$userId/products/$productId"
        return try {
            val uid = requireAuthorizedUser(userId)
            require(productId.isNotBlank()) { "Product ID required" }
            val clamped = newQuantity.coerceIn(0L, 10000000L)
            val docRef = productsCol(uid).document(productId)
            awaitOnline(
                docRef.update(
                    mapOf(
                        "stockQuantity" to clamped,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.UPDATE,
                docRef.path
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(userId: String, productId: String): Result<Unit> {
        val path = "users/$userId/products/$productId"
        return try {
            val uid = requireAuthorizedUser(userId)
            require(productId.isNotBlank()) { "Product ID required" }
            val docRef = productsCol(uid).document(productId)
            awaitOnline(docRef.delete(), OperationType.DELETE, docRef.path)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun saveCustomer(
        userId: String,
        customerId: String?,
        fullName: String,
        phone: String,
        email: String,
        address: String,
        notes: String
    ): Result<Unit> {
        val isCreate = customerId.isNullOrBlank()
        val path = "users/$userId/customers/${customerId ?: "new"}"
        return try {
            val uid = requireAuthorizedUser(userId)
            val cleanName = fullName.trim().take(80)
            require(cleanName.isNotEmpty()) { "Customer name is required" }

            if (isCreate) {
                val docRef = customersCol(uid).document()
                val payload = mapOf(
                    "userId" to uid,
                    "fullName" to cleanName,
                    "phone" to phone.trim().take(30),
                    "email" to email.trim().take(120),
                    "address" to address.trim().take(180),
                    "notes" to notes.trim().take(250),
                    "totalOrders" to 0L,
                    "totalSpent" to 0.0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                awaitOnline(docRef.set(payload), OperationType.CREATE, docRef.path)
            } else {
                val docRef = customersCol(uid).document(customerId!!)
                val payload = mapOf(
                    "fullName" to cleanName,
                    "phone" to phone.trim().take(30),
                    "email" to email.trim().take(120),
                    "address" to address.trim().take(180),
                    "notes" to notes.trim().take(250),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                awaitOnline(docRef.update(payload), OperationType.UPDATE, docRef.path)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteCustomer(userId: String, customerId: String): Result<Unit> {
        val path = "users/$userId/customers/$customerId"
        return try {
            val uid = requireAuthorizedUser(userId)
            require(customerId.isNotBlank()) { "Customer ID required" }
            val docRef = customersCol(uid).document(customerId)
            awaitOnline(docRef.delete(), OperationType.DELETE, docRef.path)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun createInvoice(
        userId: String,
        storeProfile: StoreProfile?,
        cartItems: List<CartItem>,
        customer: Customer?,
        discountPercent: Double,
        taxPercent: Double,
        paymentMethod: PaymentMethod,
        paymentStatus: PaymentStatus,
        notes: String
    ): Result<Invoice> {
        val path = "users/$userId/invoices"
        return try {
            val uid = requireAuthorizedUser(userId)
            require(cartItems.isNotEmpty()) { "Add at least one product to the invoice" }
            require(cartItems.size <= 50) { "Maximum 50 line items per invoice" }

            val cleanDiscountPct = discountPercent.coerceIn(0.0, 100.0)
            val cleanTaxPct = taxPercent.coerceIn(0.0, 50.0)
            val subtotal = cartItems.sumOf { it.lineTotal }
            val totalCost = cartItems.sumOf { it.lineCost }
            val discountAmount = subtotal * (cleanDiscountPct / 100.0)
            val taxableAmount = (subtotal - discountAmount).coerceAtLeast(0.0)
            val taxAmount = taxableAmount * (cleanTaxPct / 100.0)
            val totalAmount = taxableAmount + taxAmount
            val estimatedProfit = taxableAmount - totalCost

            val nextNumber = storeProfile?.invoiceCounter ?: (1001L + (System.currentTimeMillis() % 8999L))
            val invoiceNumber = "INV-$nextNumber"

            val lineMaps = cartItems.map { item ->
                mapOf(
                    "productId" to item.product.id,
                    "productName" to item.product.name.take(100),
                    "sku" to item.product.sku.take(40),
                    "quantity" to item.quantity.coerceAtLeast(1L),
                    "unitPrice" to item.product.sellingPrice,
                    "costPrice" to item.product.costPrice,
                    "lineTotal" to item.lineTotal
                )
            }

            val invoiceDocRef = invoicesCol(uid).document()
            val invoicePayload = mapOf(
                "userId" to uid,
                "invoiceNumber" to invoiceNumber,
                "customerId" to (customer?.id ?: ""),
                "customerName" to (customer?.fullName?.take(80) ?: "Walk-in Customer"),
                "customerPhone" to (customer?.phone?.take(30) ?: ""),
                "items" to lineMaps,
                "subtotal" to subtotal,
                "discountPercent" to cleanDiscountPct,
                "discountAmount" to discountAmount,
                "taxPercent" to cleanTaxPct,
                "taxAmount" to taxAmount,
                "totalAmount" to totalAmount,
                "estimatedProfit" to estimatedProfit,
                "paymentMethod" to paymentMethod.name,
                "paymentStatus" to paymentStatus.name,
                "notes" to notes.trim().take(200),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val batch = db.batch()
            batch.set(invoiceDocRef, invoicePayload)

            // Decrement stock for each sold product safely (clamped >= 0)
            cartItems.forEach { cartItem ->
                if (cartItem.product.id.isNotBlank()) {
                    val newStock = (cartItem.product.stockQuantity - cartItem.quantity).coerceAtLeast(0L)
                    batch.update(
                        productsCol(uid).document(cartItem.product.id),
                        mapOf(
                            "stockQuantity" to newStock,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }

            // Update customer lifetime metrics if a registered customer was selected
            if (customer != null && customer.id.isNotBlank()) {
                batch.update(
                    customersCol(uid).document(customer.id),
                    mapOf(
                        "totalOrders" to (customer.totalOrders + 1L),
                        "totalSpent" to (customer.totalSpent + totalAmount),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            // Increment store invoice counter if store profile exists
            if (storeProfile != null) {
                batch.update(
                    userDoc(uid),
                    mapOf(
                        "invoiceCounter" to (storeProfile.invoiceCounter + 1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            awaitOnline(batch.commit(), OperationType.CREATE, invoiceDocRef.path)

            val createdInvoice = Invoice(
                id = invoiceDocRef.id,
                userId = uid,
                invoiceNumber = invoiceNumber,
                customerId = customer?.id ?: "",
                customerName = customer?.fullName ?: "Walk-in Customer",
                customerPhone = customer?.phone ?: "",
                items = cartItems.map {
                    com.example.data.model.InvoiceLineItem(
                        productId = it.product.id,
                        productName = it.product.name,
                        sku = it.product.sku,
                        quantity = it.quantity,
                        unitPrice = it.product.sellingPrice,
                        costPrice = it.product.costPrice,
                        lineTotal = it.lineTotal
                    )
                },
                subtotal = subtotal,
                discountPercent = cleanDiscountPct,
                discountAmount = discountAmount,
                taxPercent = cleanTaxPct,
                taxAmount = taxAmount,
                totalAmount = totalAmount,
                estimatedProfit = estimatedProfit,
                paymentMethod = paymentMethod.name,
                paymentStatus = paymentStatus.name,
                notes = notes.trim().take(200)
            )
            Result.success(createdInvoice)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun markInvoicePaid(userId: String, invoiceId: String): Result<Unit> {
        val path = "users/$userId/invoices/$invoiceId"
        return try {
            val uid = requireAuthorizedUser(userId)
            require(invoiceId.isNotBlank()) { "Invoice ID required" }
            val docRef = invoicesCol(uid).document(invoiceId)
            awaitOnline(
                docRef.update(
                    mapOf(
                        "paymentStatus" to PaymentStatus.PAID.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ),
                OperationType.UPDATE,
                docRef.path
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun saveEmployee(
        userId: String,
        employeeId: String?,
        fullName: String,
        role: String,
        phone: String,
        activeRecord: Boolean
    ): Result<Unit> {
        val isCreate = employeeId.isNullOrBlank()
        val path = "users/$userId/employees/${employeeId ?: "new"}"
        return try {
            val uid = requireAuthorizedUser(userId)
            val cleanName = fullName.trim().take(80)
            val cleanRole = role.trim().ifEmpty { "Cashier" }.take(40)
            require(cleanName.isNotEmpty()) { "Employee name is required" }

            if (isCreate) {
                val docRef = employeesCol(uid).document()
                val payload = mapOf(
                    "userId" to uid,
                    "fullName" to cleanName,
                    "role" to cleanRole,
                    "phone" to phone.trim().take(30),
                    "shiftStatus" to ShiftStatus.CLOCKED_OUT.name,
                    "activeRecord" to activeRecord,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                awaitOnline(docRef.set(payload), OperationType.CREATE, docRef.path)
            } else {
                val docRef = employeesCol(uid).document(employeeId!!)
                val payload = mapOf(
                    "fullName" to cleanName,
                    "role" to cleanRole,
                    "phone" to phone.trim().take(30),
                    "activeRecord" to activeRecord,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                awaitOnline(docRef.update(payload), OperationType.UPDATE, docRef.path)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isCreate) OperationType.CREATE else OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteEmployee(userId: String, employeeId: String): Result<Unit> {
        val path = "users/$userId/employees/$employeeId"
        return try {
            val uid = requireAuthorizedUser(userId)
            require(employeeId.isNotBlank()) { "Employee ID required" }
            val docRef = employeesCol(uid).document(employeeId)
            awaitOnline(docRef.delete(), OperationType.DELETE, docRef.path)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun recordClockAction(
        userId: String,
        employee: Employee,
        action: ClockAction,
        note: String = ""
    ): Result<Unit> {
        val path = "users/$userId/employees/${employee.id}"
        return try {
            val uid = requireAuthorizedUser(userId)
            require(employee.id.isNotBlank()) { "Employee ID required" }

            val nextStatus = when (action) {
                ClockAction.CLOCK_IN, ClockAction.BREAK_END -> ShiftStatus.CLOCKED_IN
                ClockAction.BREAK_START -> ShiftStatus.ON_BREAK
                ClockAction.CLOCK_OUT -> ShiftStatus.CLOCKED_OUT
            }

            val empRef = employeesCol(uid).document(employee.id)
            val logRef = clockLogsCol(uid).document()

            val batch = db.batch()
            batch.update(
                empRef,
                mapOf(
                    "shiftStatus" to nextStatus.name,
                    "lastClockActionAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                logRef,
                mapOf(
                    "userId" to uid,
                    "employeeId" to employee.id,
                    "employeeName" to employee.fullName.take(80),
                    "employeeRole" to employee.role.take(40),
                    "action" to action.name,
                    "note" to note.trim().take(160),
                    "loggedAt" to FieldValue.serverTimestamp()
                )
            )
            awaitOnline(batch.commit(), OperationType.WRITE, empRef.path)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }
}
