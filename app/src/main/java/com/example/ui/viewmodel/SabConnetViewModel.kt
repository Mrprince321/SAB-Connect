package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppRole
import com.example.data.model.CartItem
import com.example.data.model.ClientProject
import com.example.data.model.ClockAction
import com.example.data.model.ClockLog
import com.example.data.model.CompanyService
import com.example.data.model.Customer
import com.example.data.model.Employee
import com.example.data.model.EnquiryLead
import com.example.data.model.EnquiryStatus
import com.example.data.model.EnquiryType
import com.example.data.model.Invoice
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.PortfolioItem
import com.example.data.model.Product
import com.example.data.model.ProjectStatus
import com.example.data.model.StoreProfile
import com.example.data.model.SyncMetadataState
import com.example.data.model.UiState
import com.example.data.repository.AgencyCrmRepository
import com.example.data.repository.SabConnetRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BillingCartState(
    val items: List<CartItem> = emptyList(),
    val selectedCustomer: Customer? = null,
    val discountPercent: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val notes: String = "",
    val isSubmitting: Boolean = false
)

class SabConnetViewModel(
    private val repository: SabConnetRepository,
    private val agencyRepository: AgencyCrmRepository,
    val currentUserId: String,
    initialRole: AppRole? = null
) : ViewModel() {

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5000L
    }

    private fun <T> Flow<T>.toUiStateFlow(): StateFlow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                emit(UiState.Error(rootError.message ?: "Unable to sync cloud data"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val storeProfileState: StateFlow<UiState<StoreProfile?>> =
        repository.observeStoreProfile(currentUserId).toUiStateFlow()

    val productsState: StateFlow<UiState<List<Product>>> =
        repository.observeProducts(currentUserId).toUiStateFlow()

    val customersState: StateFlow<UiState<List<Customer>>> =
        repository.observeCustomers(currentUserId).toUiStateFlow()

    val invoicesState: StateFlow<UiState<List<Invoice>>> =
        repository.observeInvoices(currentUserId).toUiStateFlow()

    val employeesState: StateFlow<UiState<List<Employee>>> =
        repository.observeEmployees(currentUserId).toUiStateFlow()

    val clockLogsState: StateFlow<UiState<List<ClockLog>>> =
        repository.observeClockLogs(currentUserId).toUiStateFlow()

    val servicesState: StateFlow<UiState<List<CompanyService>>> =
        agencyRepository.observeServices(currentUserId).toUiStateFlow()

    val portfolioState: StateFlow<UiState<List<PortfolioItem>>> =
        agencyRepository.observePortfolio(currentUserId).toUiStateFlow()

    val enquiriesState: StateFlow<UiState<List<EnquiryLead>>> =
        agencyRepository.observeEnquiries(currentUserId).toUiStateFlow()

    val clientProjectsState: StateFlow<UiState<List<ClientProject>>> =
        agencyRepository.observeClientProjects(currentUserId).toUiStateFlow()

    val syncState: StateFlow<SyncMetadataState> =
        combine(
            repository.observeSyncStatus(currentUserId),
            agencyRepository.observeAgencySyncStatus(currentUserId)
        ) { a, b ->
            SyncMetadataState(
                isFromCache = a.isFromCache && b.isFromCache,
                hasPendingWrites = a.hasPendingWrites || b.hasPendingWrites
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SyncMetadataState()
        )

    private val _activeRole = MutableStateFlow(initialRole)
    val activeRole: StateFlow<AppRole?> = _activeRole.asStateFlow()

    fun selectRole(role: AppRole) {
        _activeRole.value = role
    }

    fun clearRole() {
        _activeRole.value = null
    }

    private val _cartState = MutableStateFlow(BillingCartState())
    val cartState: StateFlow<BillingCartState> = _cartState.asStateFlow()

    private val _lastGeneratedInvoice = MutableStateFlow<Invoice?>(null)
    val lastGeneratedInvoice: StateFlow<Invoice?> = _lastGeneratedInvoice.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 12)
    val messages = _messages.asSharedFlow()

    private var catalogSeedAttempted = false

    fun ensureCatalogSeeded() {
        if (catalogSeedAttempted) return
        catalogSeedAttempted = true
        viewModelScope.launch {
            agencyRepository.seedSabDigitechCatalogIfEmpty(currentUserId)
        }
    }

    fun seedStarterCatalog() {
        viewModelScope.launch {
            agencyRepository.seedSabDigitechCatalogIfEmpty(currentUserId)
                .onSuccess { count ->
                    if (count > 0) _messages.tryEmit("Loaded $count SAB Digitech services & portfolio items")
                    else _messages.tryEmit("Services & portfolio catalog is already populated")
                }
                .onFailure { err ->
                    _messages.tryEmit(err.localizedMessage ?: "Failed to load catalog")
                }
        }
    }

    // Store / Business Settings
    fun saveStoreProfile(
        existingProfile: StoreProfile?,
        storeName: String,
        ownerName: String,
        email: String,
        phone: String,
        address: String,
        currencySymbol: String,
        taxRatePercent: Double,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.saveStoreProfile(
                userId = currentUserId,
                existingProfile = existingProfile,
                storeName = storeName,
                ownerName = ownerName,
                email = email,
                phone = phone,
                address = address,
                currencySymbol = currencySymbol,
                taxRatePercent = taxRatePercent
            ).onSuccess {
                _messages.tryEmit("Workspace profile saved")
                onSuccess()
            }.onFailure { err ->
                _messages.tryEmit(err.localizedMessage ?: "Failed to save workspace profile")
            }
        }
    }

    // Company Services & Portfolio CRUD (Admin)
    fun saveService(
        serviceId: String?,
        title: String,
        category: String,
        description: String,
        deliverables: String,
        startingPrice: Double,
        timeline: String,
        activeService: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            agencyRepository.saveService(
                userId = currentUserId,
                serviceId = serviceId,
                title = title,
                category = category,
                description = description,
                deliverables = deliverables,
                startingPrice = startingPrice,
                timeline = timeline,
                activeService = activeService
            ).onSuccess {
                _messages.tryEmit(if (serviceId == null) "Service added" else "Service updated")
                onSuccess()
            }.onFailure { err ->
                _messages.tryEmit(err.localizedMessage ?: "Failed to save service")
            }
        }
    }

    fun deleteService(service: CompanyService) {
        viewModelScope.launch {
            agencyRepository.deleteService(currentUserId, service.id)
                .onSuccess { _messages.tryEmit("${service.title} removed") }
                .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Failed to delete service") }
        }
    }

    fun savePortfolioItem(
        itemId: String?,
        title: String,
        clientName: String,
        category: String,
        summary: String,
        techStack: String,
        impactMetric: String,
        featured: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            agencyRepository.savePortfolioItem(
                userId = currentUserId,
                itemId = itemId,
                title = title,
                clientName = clientName,
                category = category,
                summary = summary,
                techStack = techStack,
                impactMetric = impactMetric,
                featured = featured
            ).onSuccess {
                _messages.tryEmit(if (itemId == null) "Portfolio project added" else "Portfolio project updated")
                onSuccess()
            }.onFailure { err ->
                _messages.tryEmit(err.localizedMessage ?: "Failed to save portfolio project")
            }
        }
    }

    fun deletePortfolioItem(item: PortfolioItem) {
        viewModelScope.launch {
            agencyRepository.deletePortfolioItem(currentUserId, item.id)
                .onSuccess { _messages.tryEmit("${item.title} deleted") }
                .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Failed to delete portfolio project") }
        }
    }

    // Enquiries, Quotations & Consultations
    fun submitEnquiry(
        enquiryType: EnquiryType,
        customerId: String,
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        serviceTitle: String,
        projectTitle: String,
        requirements: String,
        budgetRange: String,
        preferredDate: String,
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            agencyRepository.submitEnquiry(
                userId = currentUserId,
                enquiryType = enquiryType,
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                customerEmail = customerEmail,
                serviceTitle = serviceTitle,
                projectTitle = projectTitle,
                requirements = requirements,
                budgetRange = budgetRange,
                preferredDate = preferredDate
            ).onSuccess { code ->
                _messages.tryEmit("${enquiryType.label} submitted ($code)")
                onSuccess(code)
            }.onFailure { err ->
                _messages.tryEmit(err.localizedMessage ?: "Failed to submit ${enquiryType.label}")
            }
        }
    }

    fun assignEnquiry(enquiry: EnquiryLead, employee: Employee, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            agencyRepository.assignEnquiryToEmployee(currentUserId, enquiry, employee)
                .onSuccess {
                    _messages.tryEmit("${enquiry.enquiryCode} assigned to ${employee.fullName}")
                    onSuccess()
                }
                .onFailure { err ->
                    _messages.tryEmit(err.localizedMessage ?: "Failed to assign lead")
                }
        }
    }

    fun updateEnquiryProgress(
        enquiry: EnquiryLead,
        status: EnquiryStatus,
        quotedAmount: Double,
        followUpDate: String,
        followUpNote: String,
        noteText: String,
        authorRole: String,
        authorName: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            agencyRepository.updateEnquiryProgress(
                userId = currentUserId,
                enquiry = enquiry,
                status = status,
                quotedAmount = quotedAmount,
                followUpDate = followUpDate,
                followUpNote = followUpNote,
                noteText = noteText,
                authorRole = authorRole,
                authorName = authorName
            ).onSuccess {
                _messages.tryEmit("${enquiry.enquiryCode} updated (${status.label})")
                onSuccess()
            }.onFailure { err ->
                _messages.tryEmit(err.localizedMessage ?: "Failed to update lead")
            }
        }
    }

    fun postCommunicationMessage(
        enquiry: EnquiryLead,
        authorRole: String,
        authorName: String,
        message: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            agencyRepository.postCommunicationMessage(
                userId = currentUserId,
                enquiry = enquiry,
                authorRole = authorRole,
                authorName = authorName,
                message = message
            ).onSuccess {
                _messages.tryEmit("Update posted to ${enquiry.enquiryCode}")
                onSuccess()
            }.onFailure { err ->
                _messages.tryEmit(err.localizedMessage ?: "Failed to send message")
            }
        }
    }

    fun convertEnquiryToProject(enquiry: EnquiryLead, dueDate: String = "30 Days", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val value = if (enquiry.quotedAmount > 0.0) enquiry.quotedAmount else 25000.0
            agencyRepository.saveClientProject(
                userId = currentUserId,
                projectId = null,
                enquiryId = enquiry.id,
                title = enquiry.projectTitle,
                clientName = enquiry.customerName,
                serviceTitle = enquiry.serviceTitle,
                assignedEmployeeId = enquiry.assignedEmployeeId,
                assignedEmployeeName = enquiry.assignedEmployeeName.ifBlank { "SAB Delivery Team" },
                status = ProjectStatus.PLANNING,
                progressPercent = 15L,
                milestoneNote = "Project kicked off from ${enquiry.enquiryCode}",
                dueDate = dueDate,
                projectValue = value
            ).onSuccess {
                agencyRepository.updateEnquiryProgress(
                    userId = currentUserId,
                    enquiry = enquiry,
                    status = EnquiryStatus.CONVERTED,
                    quotedAmount = value,
                    followUpDate = enquiry.followUpDate,
                    followUpNote = enquiry.followUpNote,
                    noteText = "Converted to active project",
                    authorRole = "ADMIN",
                    authorName = "Admin"
                )
                _messages.tryEmit("Converted ${enquiry.enquiryCode} to active project")
                onSuccess()
            }.onFailure { err ->
                _messages.tryEmit(err.localizedMessage ?: "Failed to create project")
            }
        }
    }

    fun saveClientProject(
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
        projectValue: Double,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            agencyRepository.saveClientProject(
                userId = currentUserId,
                projectId = projectId,
                enquiryId = enquiryId,
                title = title,
                clientName = clientName,
                serviceTitle = serviceTitle,
                assignedEmployeeId = assignedEmployeeId,
                assignedEmployeeName = assignedEmployeeName,
                status = status,
                progressPercent = progressPercent,
                milestoneNote = milestoneNote,
                dueDate = dueDate,
                projectValue = projectValue
            ).onSuccess {
                _messages.tryEmit(if (projectId == null) "Project created" else "Project updated")
                onSuccess()
            }.onFailure { err ->
                _messages.tryEmit(err.localizedMessage ?: "Failed to save project")
            }
        }
    }

    fun deleteClientProject(project: ClientProject) {
        viewModelScope.launch {
            agencyRepository.deleteClientProject(currentUserId, project.id)
                .onSuccess { _messages.tryEmit("${project.title} deleted") }
                .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Failed to delete project") }
        }
    }

    // Customers, Employees, Products, Invoices (Existing + User Management)
    fun saveCustomer(
        customerId: String?,
        fullName: String,
        phone: String,
        email: String,
        address: String,
        notes: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.saveCustomer(
                userId = currentUserId,
                customerId = customerId,
                fullName = fullName,
                phone = phone,
                email = email,
                address = address,
                notes = notes
            ).onSuccess {
                _messages.tryEmit(if (customerId == null) "Customer account added" else "Customer updated")
                onSuccess()
            }.onFailure { err ->
                _messages.tryEmit(err.localizedMessage ?: "Failed to save customer")
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(currentUserId, customer.id)
                .onSuccess { _messages.tryEmit("${customer.fullName} removed") }
                .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Failed to remove customer") }
        }
    }

    fun saveEmployee(
        employeeId: String?,
        fullName: String,
        role: String,
        phone: String,
        activeRecord: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.saveEmployee(
                userId = currentUserId,
                employeeId = employeeId,
                fullName = fullName,
                role = role,
                phone = phone,
                activeRecord = activeRecord
            ).onSuccess {
                _messages.tryEmit(if (employeeId == null) "Employee added" else "Employee updated")
                onSuccess()
            }.onFailure { err ->
                _messages.tryEmit(err.localizedMessage ?: "Failed to save employee")
            }
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.deleteEmployee(currentUserId, employee.id)
                .onSuccess { _messages.tryEmit("${employee.fullName} removed") }
                .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Failed to remove employee") }
        }
    }

    fun clockEmployee(employee: Employee, action: ClockAction, note: String = "") {
        viewModelScope.launch {
            repository.recordClockAction(
                userId = currentUserId,
                employee = employee,
                action = action,
                note = note
            ).onSuccess {
                _messages.tryEmit("${employee.fullName}: ${action.label}")
            }.onFailure { err ->
                _messages.tryEmit(err.localizedMessage ?: "Failed to record clock log")
            }
        }
    }

    fun saveProduct(
        productId: String?,
        name: String,
        sku: String,
        category: String,
        unit: String,
        sellingPrice: Double,
        costPrice: Double,
        stockQuantity: Long,
        lowStockThreshold: Long,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.saveProduct(
                userId = currentUserId,
                productId = productId,
                name = name,
                sku = sku,
                category = category,
                unit = unit,
                sellingPrice = sellingPrice,
                costPrice = costPrice,
                stockQuantity = stockQuantity,
                lowStockThreshold = lowStockThreshold
            ).onSuccess {
                _messages.tryEmit(if (productId == null) "Product added" else "Product updated")
                onSuccess()
            }.onFailure { err ->
                _messages.tryEmit(err.localizedMessage ?: "Failed to save product")
            }
        }
    }

    fun adjustStock(product: Product, delta: Long) {
        val next = (product.stockQuantity + delta).coerceAtLeast(0L)
        setStock(product, next)
    }

    fun setStock(product: Product, exactQuantity: Long) {
        viewModelScope.launch {
            repository.adjustProductStock(currentUserId, product.id, exactQuantity)
                .onSuccess { _messages.tryEmit("${product.name} stock updated") }
                .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Failed to update stock") }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(currentUserId, product.id)
                .onSuccess { _messages.tryEmit("${product.name} deleted") }
                .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Failed to delete product") }
        }
    }

    fun addToCart(product: Product) {
        if (product.stockQuantity <= 0L) {
            _messages.tryEmit("${product.name} is out of stock")
            return
        }
        _cartState.update { state ->
            val existing = state.items.find { it.product.id == product.id }
            val updatedItems = if (existing != null) {
                val nextQty = (existing.quantity + 1L).coerceAtMost(product.stockQuantity)
                state.items.map {
                    if (it.product.id == product.id) it.copy(product = product, quantity = nextQty) else it
                }
            } else {
                state.items + CartItem(product = product, quantity = 1L)
            }
            state.copy(items = updatedItems)
        }
    }

    fun updateCartQuantity(productId: String, delta: Long) {
        _cartState.update { state ->
            val updated = state.items.mapNotNull { item ->
                if (item.product.id != productId) item
                else {
                    val nextQty = item.quantity + delta
                    if (nextQty <= 0L) null
                    else item.copy(quantity = nextQty.coerceAtMost(item.product.stockQuantity.coerceAtLeast(1L)))
                }
            }
            state.copy(items = updated)
        }
    }

    fun selectCartCustomer(customer: Customer?) {
        _cartState.update { it.copy(selectedCustomer = customer) }
    }

    fun updateDiscountPercent(discount: Double) {
        _cartState.update { it.copy(discountPercent = discount.coerceIn(0.0, 100.0)) }
    }

    fun updatePaymentMethod(method: PaymentMethod) {
        _cartState.update { it.copy(paymentMethod = method) }
    }

    fun updatePaymentStatus(status: PaymentStatus) {
        _cartState.update { it.copy(paymentStatus = status) }
    }

    fun updateCartNotes(notes: String) {
        _cartState.update { it.copy(notes = notes.take(200)) }
    }

    fun clearCart() {
        _cartState.value = BillingCartState()
    }

    fun dismissGeneratedInvoice() {
        _lastGeneratedInvoice.value = null
    }

    fun generateInvoice(storeProfile: StoreProfile?) {
        val snapshot = _cartState.value
        if (snapshot.items.isEmpty() || snapshot.isSubmitting) return

        viewModelScope.launch {
            _cartState.update { it.copy(isSubmitting = true) }
            val taxPct = storeProfile?.taxRatePercent ?: 18.0
            repository.createInvoice(
                userId = currentUserId,
                storeProfile = storeProfile,
                cartItems = snapshot.items,
                customer = snapshot.selectedCustomer,
                discountPercent = snapshot.discountPercent,
                taxPercent = taxPct,
                paymentMethod = snapshot.paymentMethod,
                paymentStatus = snapshot.paymentStatus,
                notes = snapshot.notes
            ).onSuccess { created ->
                _cartState.value = BillingCartState()
                _lastGeneratedInvoice.value = created
                _messages.tryEmit("Invoice ${created.invoiceNumber} generated")
            }.onFailure { err ->
                _cartState.update { it.copy(isSubmitting = false) }
                _messages.tryEmit(err.localizedMessage ?: "Failed to create invoice")
            }
        }
    }

    fun markInvoicePaid(invoice: Invoice) {
        viewModelScope.launch {
            repository.markInvoicePaid(currentUserId, invoice.id)
                .onSuccess { _messages.tryEmit("${invoice.invoiceNumber} marked as Paid") }
                .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Failed to update invoice") }
        }
    }
}
