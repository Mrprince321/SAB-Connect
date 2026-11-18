package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class SyncMetadataState(
    val isFromCache: Boolean = false,
    val hasPendingWrites: Boolean = false
)

enum class AppRole(
    val label: String,
    val badge: String,
    val subtitle: String,
    val summary: String,
    val specifications: List<String>
) {
    CUSTOMER(
        label = "Customer",
        badge = "CLIENT PORTAL",
        subtitle = "Sign in to discover businesses and services.",
        summary = "Browse SAB Digitech services & portfolio, request quotes, book consultations, and track project status.",
        specifications = listOf(
            "Browse Services & Portfolio",
            "Submit Enquiries, Request Quotes & Book Consultations",
            "Track Enquiry & Project Status",
            "View Communications & Manage Profile"
        )
    ),
    EMPLOYEE(
        label = "Employee",
        badge = "STAFF DESK",
        subtitle = "Sign in with your company account.",
        summary = "Manage assigned leads, contact clients, update enquiry stages, log call/meeting notes, and schedule follow-ups.",
        specifications = listOf(
            "View Assigned Leads & Client Details",
            "Contact Clients & Update Enquiry Status",
            "Record Call/Meeting Notes & Schedule Follow-ups",
            "View Tasks & Basic Performance Reports"
        )
    ),
    ADMIN(
        label = "Admin",
        badge = "ADMIN CONSOLE",
        subtitle = "Authorized administrators only.",
        summary = "Manage customers & employees, assign incoming leads, curate services & portfolio, and monitor reports & projects.",
        specifications = listOf(
            "View All Incoming Leads & Assign to Employees",
            "User Management (Customers & Employees)",
            "Manage Services, Portfolio, Quotations & Projects",
            "Monitor Employee Performance & Conversion Reports"
        )
    )
}

enum class EnquiryType(val label: String) {
    ENQUIRY("Project Enquiry"),
    QUOTATION("Quotation Request"),
    CONSULTATION("Consultation");

    companion object {
        fun fromRaw(raw: String?): EnquiryType =
            entries.firstOrNull { it.name == raw } ?: ENQUIRY
    }
}

enum class EnquiryStatus(val label: String) {
    NEW("New Lead"),
    IN_PROGRESS("In Progress"),
    QUOTED("Quoted"),
    CONVERTED("Converted"),
    CLOSED("Closed");

    companion object {
        fun fromRaw(raw: String?): EnquiryStatus =
            entries.firstOrNull { it.name == raw } ?: NEW
    }
}

enum class ProjectStatus(val label: String) {
    PLANNING("Planning"),
    IN_PROGRESS("In Progress"),
    REVIEW("In Review"),
    COMPLETED("Completed");

    companion object {
        fun fromRaw(raw: String?): ProjectStatus =
            entries.firstOrNull { it.name == raw } ?: PLANNING
    }
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    CARD("Card"),
    DIGITAL("Digital / UPI");

    companion object {
        fun fromRaw(raw: String?): PaymentMethod =
            entries.firstOrNull { it.name == raw } ?: CASH
    }
}

enum class PaymentStatus(val label: String) {
    PAID("Paid"),
    PENDING("Pending");

    companion object {
        fun fromRaw(raw: String?): PaymentStatus =
            entries.firstOrNull { it.name == raw } ?: PAID
    }
}

enum class ShiftStatus(val label: String) {
    CLOCKED_IN("Clocked In"),
    ON_BREAK("On Break"),
    CLOCKED_OUT("Clocked Out");

    companion object {
        fun fromRaw(raw: String?): ShiftStatus =
            entries.firstOrNull { it.name == raw } ?: CLOCKED_OUT
    }
}

enum class ClockAction(val label: String) {
    CLOCK_IN("Clocked In"),
    BREAK_START("Started Break"),
    BREAK_END("Ended Break"),
    CLOCK_OUT("Clocked Out");

    companion object {
        fun fromRaw(raw: String?): ClockAction =
            entries.firstOrNull { it.name == raw } ?: CLOCK_IN
    }
}

@Keep
@IgnoreExtraProperties
data class StoreProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val storeName: String = "",
    val ownerName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val currencySymbol: String = "₹",
    val taxRatePercent: Double = 18.0,
    val invoiceCounter: Long = 1001L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CompanyService(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val deliverables: String = "",
    val startingPrice: Double = 0.0,
    val timeline: String = "",
    val activeService: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PortfolioItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val clientName: String = "",
    val category: String = "",
    val summary: String = "",
    val techStack: String = "",
    val impactMetric: String = "",
    val featured: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CommUpdate(
    val authorRole: String = "",
    val authorName: String = "",
    val message: String = "",
    val timestampLabel: String = ""
)

@Keep
@IgnoreExtraProperties
data class EnquiryLead(
    @DocumentId val id: String = "",
    val userId: String = "",
    val enquiryCode: String = "",
    val enquiryType: String = EnquiryType.ENQUIRY.name,
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val serviceTitle: String = "",
    val projectTitle: String = "",
    val requirements: String = "",
    val budgetRange: String = "",
    val preferredDate: String = "",
    val status: String = EnquiryStatus.NEW.name,
    val assignedEmployeeId: String = "",
    val assignedEmployeeName: String = "",
    val quotedAmount: Double = 0.0,
    val followUpDate: String = "",
    val followUpNote: String = "",
    val latestNote: String = "",
    val updates: List<CommUpdate> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ClientProject(
    @DocumentId val id: String = "",
    val userId: String = "",
    val enquiryId: String = "",
    val title: String = "",
    val clientName: String = "",
    val serviceTitle: String = "",
    val assignedEmployeeId: String = "",
    val assignedEmployeeName: String = "",
    val status: String = ProjectStatus.PLANNING.name,
    val progressPercent: Long = 0L,
    val milestoneNote: String = "",
    val dueDate: String = "",
    val projectValue: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val sku: String = "",
    val category: String = "",
    val unit: String = "pcs",
    val sellingPrice: Double = 0.0,
    val costPrice: Double = 0.0,
    val stockQuantity: Long = 0L,
    val lowStockThreshold: Long = 5L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Customer(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val totalOrders: Long = 0L,
    val totalSpent: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class InvoiceLineItem(
    val productId: String = "",
    val productName: String = "",
    val sku: String = "",
    val quantity: Long = 1L,
    val unitPrice: Double = 0.0,
    val costPrice: Double = 0.0,
    val lineTotal: Double = 0.0
)

@Keep
@IgnoreExtraProperties
data class Invoice(
    @DocumentId val id: String = "",
    val userId: String = "",
    val invoiceNumber: String = "",
    val customerId: String = "",
    val customerName: String = "Walk-in Customer",
    val customerPhone: String = "",
    val items: List<InvoiceLineItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxPercent: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val estimatedProfit: Double = 0.0,
    val paymentMethod: String = PaymentMethod.CASH.name,
    val paymentStatus: String = PaymentStatus.PAID.name,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Employee(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val role: String = "Solutions Consultant",
    val phone: String = "",
    val shiftStatus: String = ShiftStatus.CLOCKED_OUT.name,
    @ServerTimestamp val lastClockActionAt: Timestamp? = null,
    val activeRecord: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ClockLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val employeeRole: String = "",
    val action: String = ClockAction.CLOCK_IN.name,
    val note: String = "",
    @ServerTimestamp val loggedAt: Timestamp? = null
)

data class CartItem(
    val product: Product,
    val quantity: Long = 1L
) {
    val lineTotal: Double get() = product.sellingPrice * quantity
    val lineCost: Double get() = product.costPrice * quantity
}
