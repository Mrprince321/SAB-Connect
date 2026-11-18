package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AppRole
import com.example.data.model.ClientProject
import com.example.data.model.CompanyService
import com.example.data.model.Employee
import com.example.data.model.EnquiryLead
import com.example.data.model.EnquiryStatus
import com.example.data.model.EnquiryType
import com.example.data.model.PortfolioItem
import com.example.data.model.ProjectStatus
import com.example.ui.components.formatCurrency
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.StatusSuccess
import kotlin.math.roundToLong

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnquirySubmitDialog(
    initialType: EnquiryType,
    initialServiceTitle: String,
    services: List<CompanyService>,
    defaultCustomerName: String,
    defaultCustomerEmail: String,
    onDismiss: () -> Unit,
    onSubmit: (
        enquiryType: EnquiryType,
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        serviceTitle: String,
        projectTitle: String,
        requirements: String,
        budgetRange: String,
        preferredDate: String
    ) -> Unit
) {
    BackHandler(onBack = onDismiss)

    var selectedType by remember(initialType) { mutableStateOf(initialType) }
    var customerName by remember { mutableStateOf(defaultCustomerName.ifBlank { "" }) }
    var customerPhone by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf(defaultCustomerEmail) }
    var serviceTitle by remember(initialServiceTitle) {
        mutableStateOf(
            initialServiceTitle.ifBlank { services.firstOrNull()?.title ?: "Web & E-Commerce Development" }
        )
    }
    var projectTitle by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var budgetRange by remember { mutableStateOf("₹25,000 - ₹50,000") }
    var preferredDate by remember { mutableStateOf("This Week • 11:00 AM") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = selectedType.label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EnquiryType.entries.forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t.label) }
                        )
                    }
                }

                val serviceChoices = remember(services) {
                    services.map { it.title }.ifEmpty {
                        listOf(
                            "Web & E-Commerce Development",
                            "Mobile App Engineering",
                            "SAB Billing & Cloud POS Suite",
                            "SEO & Performance Growth",
                            "Creative & OTT Post-Production"
                        )
                    }
                }
                Text("Select Service", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    serviceChoices.take(6).forEach { title ->
                        FilterChip(
                            selected = serviceTitle == title,
                            onClick = { serviceTitle = title },
                            label = { Text(title, maxLines = 1) }
                        )
                    }
                }

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it; errorText = null },
                    label = { Text("Your Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("enquiry_name_input")
                )
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Phone / WhatsApp Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = customerEmail,
                    onValueChange = { customerEmail = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = projectTitle,
                    onValueChange = { projectTitle = it },
                    label = { Text("Project / Business Title") },
                    placeholder = { Text("e.g. Multi-Branch Store App") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("enquiry_project_title_input")
                )
                OutlinedTextField(
                    value = requirements,
                    onValueChange = { requirements = it; errorText = null },
                    label = { Text("Requirements & Goals *") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("enquiry_requirements_input")
                )
                if (selectedType == EnquiryType.QUOTATION || selectedType == EnquiryType.ENQUIRY) {
                    OutlinedTextField(
                        value = budgetRange,
                        onValueChange = { budgetRange = it },
                        label = { Text("Estimated Budget Range") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (selectedType == EnquiryType.CONSULTATION) {
                    OutlinedTextField(
                        value = preferredDate,
                        onValueChange = { preferredDate = it },
                        label = { Text("Preferred Consultation Date & Slot") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorText != null) {
                    Text(
                        text = errorText ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        customerName.trim().isEmpty() -> errorText = "Please enter your name."
                        requirements.trim().isEmpty() -> errorText = "Please describe your requirements."
                        else -> onSubmit(
                            selectedType,
                            customerName.trim(),
                            customerPhone.trim(),
                            customerEmail.trim(),
                            serviceTitle,
                            projectTitle.trim(),
                            requirements.trim(),
                            budgetRange.trim(),
                            preferredDate.trim()
                        )
                    }
                },
                modifier = Modifier.testTag("submit_enquiry_button")
            ) {
                Text("Submit ${selectedType.label}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnquiryDetailAndCommsDialog(
    enquiry: EnquiryLead,
    viewerRole: AppRole,
    viewerName: String,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onUpdateProgress: (
        status: EnquiryStatus,
        quotedAmount: Double,
        followUpDate: String,
        followUpNote: String,
        noteText: String
    ) -> Unit,
    onSendMessage: (String) -> Unit,
    onConvertToProject: (() -> Unit)? = null
) {
    BackHandler(onBack = onDismiss)
    val context = LocalContext.current

    var selectedStatus by remember(enquiry) { mutableStateOf(EnquiryStatus.fromRaw(enquiry.status)) }
    var quoteText by remember(enquiry) {
        mutableStateOf(if (enquiry.quotedAmount > 0.0) enquiry.quotedAmount.toLong().toString() else "")
    }
    var followUpDate by remember(enquiry) { mutableStateOf(enquiry.followUpDate) }
    var followUpNote by remember(enquiry) { mutableStateOf(enquiry.followUpNote) }
    var callMeetingNote by remember { mutableStateOf("") }
    var chatReply by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("${enquiry.enquiryCode} • ${enquiry.projectTitle}", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${enquiry.serviceTitle} • ${EnquiryType.fromRaw(enquiry.enquiryType).label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = RoyalBluePrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF4F7FC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Client: ${enquiry.customerName}", style = MaterialTheme.typography.titleSmall)
                        if (enquiry.customerPhone.isNotBlank()) {
                            Text("Phone: ${enquiry.customerPhone}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (enquiry.customerEmail.isNotBlank()) {
                            Text("Email: ${enquiry.customerEmail}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("Requirements: ${enquiry.requirements}", style = MaterialTheme.typography.bodySmall)
                        if (enquiry.assignedEmployeeName.isNotBlank()) {
                            Text(
                                text = "Assigned To: ${enquiry.assignedEmployeeName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = RoyalBluePrimary
                            )
                        }
                        if (enquiry.quotedAmount > 0.0) {
                            Text(
                                text = "Quoted Amount: ${formatCurrency(enquiry.quotedAmount, currencySymbol)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = StatusSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (enquiry.followUpDate.isNotBlank()) {
                            Text(
                                text = "Next Follow-up: ${enquiry.followUpDate} ${if (enquiry.followUpNote.isNotBlank()) "• ${enquiry.followUpNote}" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF9A3412)
                            )
                        }
                    }
                }

                // Employee / Admin quick contact client buttons
                if (viewerRole != AppRole.CUSTOMER &&
                    (enquiry.customerPhone.isNotBlank() || enquiry.customerEmail.isNotBlank())
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (enquiry.customerPhone.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${enquiry.customerPhone}")))
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Call Client")
                            }
                        }
                        if (enquiry.customerEmail.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${enquiry.customerEmail}")).apply {
                                                putExtra(Intent.EXTRA_SUBJECT, "Update on ${enquiry.enquiryCode} - SAB Connect")
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Email Client")
                            }
                        }
                    }
                }

                // Communications & Updates timeline
                Text("Communications & Activity Timeline", style = MaterialTheme.typography.titleSmall)
                if (enquiry.updates.isEmpty()) {
                    Text(
                        text = "No updates logged yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        enquiry.updates.takeLast(8).forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${item.authorName} (${item.authorRole})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = RoyalBluePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.timestampLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    Text(item.message, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // Direct reply box for all roles
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = chatReply,
                        onValueChange = { chatReply = it },
                        placeholder = { Text("Send message / update...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (chatReply.isNotBlank()) {
                                onSendMessage(chatReply.trim())
                                chatReply = ""
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send update", tint = RoyalBluePrimary)
                    }
                }

                if (viewerRole != AppRole.CUSTOMER) {
                    HorizontalDivider()
                    Text("Update Lead Stage, Quote & Follow-up", style = MaterialTheme.typography.titleSmall)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EnquiryStatus.entries.forEach { st ->
                            FilterChip(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st },
                                label = { Text(st.label) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = quoteText,
                        onValueChange = { quoteText = it },
                        label = { Text("Quotation Amount ($currencySymbol)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = followUpDate,
                        onValueChange = { followUpDate = it },
                        label = { Text("Schedule Follow-up (Date & Time)") },
                        placeholder = { Text("e.g. Tomorrow 3:00 PM") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = followUpNote,
                        onValueChange = { followUpNote = it },
                        label = { Text("Follow-up Agenda") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = callMeetingNote,
                        onValueChange = { callMeetingNote = it },
                        label = { Text("Record Call / Meeting Note") },
                        placeholder = { Text("Summary of client call or demo") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (viewerRole == AppRole.ADMIN && onConvertToProject != null &&
                        enquiry.status != EnquiryStatus.CONVERTED.name
                    ) {
                        OutlinedButton(
                            onClick = onConvertToProject,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Convert Lead to Active Project")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (viewerRole != AppRole.CUSTOMER) {
                Button(
                    onClick = {
                        onUpdateProgress(
                            selectedStatus,
                            quoteText.toDoubleOrNull() ?: enquiry.quotedAmount,
                            followUpDate,
                            followUpNote,
                            callMeetingNote
                        )
                    }
                ) {
                    Text("Save Update")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
        dismissButton = {
            if (viewerRole != AppRole.CUSTOMER) {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    )
}

@Composable
fun AssignLeadDialog(
    enquiry: EnquiryLead,
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onAssign: (Employee) -> Unit
) {
    BackHandler(onBack = onDismiss)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign ${enquiry.enquiryCode}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select an employee to handle ${enquiry.customerName}'s ${enquiry.serviceTitle} lead:",
                    style = MaterialTheme.typography.bodySmall
                )
                if (employees.isEmpty()) {
                    Text(
                        text = "No employees found yet. Add an employee under User Management first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    employees.forEach { emp ->
                        Surface(
                            onClick = { onAssign(emp) },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF4F7FC),
                            border = BorderStroke(
                                1.dp,
                                if (enquiry.assignedEmployeeId == emp.id) RoyalBluePrimary else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(emp.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${emp.role} • ${emp.phone.ifBlank { "Staff" }}", style = MaterialTheme.typography.bodySmall)
                                }
                                Text(
                                    text = if (enquiry.assignedEmployeeId == emp.id) "Assigned" else "Select",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = RoyalBluePrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun ServiceEditorDialog(
    existing: CompanyService?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Double, String, Boolean) -> Unit
) {
    BackHandler(onBack = onDismiss)
    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var category by remember(existing) { mutableStateOf(existing?.category ?: "Web & Cloud") }
    var description by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var deliverables by remember(existing) { mutableStateOf(existing?.deliverables ?: "") }
    var priceText by remember(existing) { mutableStateOf((existing?.startingPrice ?: 15000.0).toLong().toString()) }
    var timeline by remember(existing) { mutableStateOf(existing?.timeline ?: "2-4 Weeks") }
    var activeService by remember(existing) { mutableStateOf(existing?.activeService ?: true) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Company Service" else "Edit Service") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it; error = null }, label = { Text("Service Title *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it; error = null }, label = { Text("Description *") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = deliverables, onValueChange = { deliverables = it }, label = { Text("Tech Stack / Deliverables") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Starting Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(value = timeline, onValueChange = { timeline = it }, label = { Text("Timeline") }, modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = activeService, onCheckedChange = { activeService = it })
                    Text("Visible to Customers")
                }
                if (error != null) Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    title.trim().isEmpty() -> error = "Title required"
                    description.trim().isEmpty() -> error = "Description required"
                    else -> onSave(title.trim(), category.trim(), description.trim(), deliverables.trim(), priceText.toDoubleOrNull() ?: 0.0, timeline.trim(), activeService)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun PortfolioEditorDialog(
    existing: PortfolioItem?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, Boolean) -> Unit
) {
    BackHandler(onBack = onDismiss)
    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var clientName by remember(existing) { mutableStateOf(existing?.clientName ?: "") }
    var category by remember(existing) { mutableStateOf(existing?.category ?: "Mobile App") }
    var summary by remember(existing) { mutableStateOf(existing?.summary ?: "") }
    var techStack by remember(existing) { mutableStateOf(existing?.techStack ?: "") }
    var impact by remember(existing) { mutableStateOf(existing?.impactMetric ?: "") }
    var featured by remember(existing) { mutableStateOf(existing?.featured ?: true) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Portfolio Project" else "Edit Portfolio Project") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it; error = null }, label = { Text("Project Title *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = clientName, onValueChange = { clientName = it }, label = { Text("Client / Sector") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = summary, onValueChange = { summary = it; error = null }, label = { Text("Outcome Summary *") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = techStack, onValueChange = { techStack = it }, label = { Text("Tech Stack") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = impact, onValueChange = { impact = it }, label = { Text("Impact Metric (e.g. 3x Leads)") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = featured, onCheckedChange = { featured = it })
                    Text("Featured Showcase Project")
                }
                if (error != null) Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    title.trim().isEmpty() -> error = "Title required"
                    summary.trim().isEmpty() -> error = "Summary required"
                    else -> onSave(title.trim(), clientName.trim(), category.trim(), summary.trim(), techStack.trim(), impact.trim(), featured)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectEditorDialog(
    existing: ClientProject?,
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, ProjectStatus, Long, String, String, Double) -> Unit
) {
    BackHandler(onBack = onDismiss)
    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var clientName by remember(existing) { mutableStateOf(existing?.clientName ?: "") }
    var serviceTitle by remember(existing) { mutableStateOf(existing?.serviceTitle ?: "Mobile App Engineering") }
    var selectedEmpId by remember(existing) { mutableStateOf(existing?.assignedEmployeeId ?: "") }
    var selectedEmpName by remember(existing) { mutableStateOf(existing?.assignedEmployeeName ?: "") }
    var status by remember(existing) { mutableStateOf(ProjectStatus.fromRaw(existing?.status)) }
    var progress by remember(existing) { mutableFloatStateOf((existing?.progressPercent ?: 20L).toFloat()) }
    var milestoneNote by remember(existing) { mutableStateOf(existing?.milestoneNote ?: "") }
    var dueDate by remember(existing) { mutableStateOf(existing?.dueDate ?: "Next Month") }
    var valueText by remember(existing) { mutableStateOf((existing?.projectValue ?: 35000.0).toLong().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Create Client Project" else "Update Project") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Project Title *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = clientName, onValueChange = { clientName = it }, label = { Text("Client Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = serviceTitle, onValueChange = { serviceTitle = it }, label = { Text("Service") }, modifier = Modifier.fillMaxWidth())
                if (employees.isNotEmpty()) {
                    Text("Assigned Employee", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        employees.forEach { emp ->
                            FilterChip(
                                selected = selectedEmpId == emp.id,
                                onClick = { selectedEmpId = emp.id; selectedEmpName = emp.fullName },
                                label = { Text(emp.fullName) }
                            )
                        }
                    }
                }
                Text("Project Status", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ProjectStatus.entries.forEach { st ->
                        FilterChip(selected = status == st, onClick = { status = st }, label = { Text(st.label) })
                    }
                }
                Text("Completion Progress: ${progress.roundToLong()}%", style = MaterialTheme.typography.labelMedium)
                Slider(value = progress, onValueChange = { progress = it }, valueRange = 0f..100f)
                OutlinedTextField(value = milestoneNote, onValueChange = { milestoneNote = it }, label = { Text("Current Milestone Note") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Target Date") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = valueText, onValueChange = { valueText = it }, label = { Text("Project Value") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.trim().isNotEmpty()) {
                    onSave(
                        title.trim(),
                        clientName.trim().ifEmpty { "Client" },
                        serviceTitle.trim().ifEmpty { "Digital Project" },
                        selectedEmpId,
                        selectedEmpName.ifBlank { "SAB Delivery Team" },
                        status,
                        progress.roundToLong(),
                        milestoneNote.trim(),
                        dueDate.trim(),
                        valueText.toDoubleOrNull() ?: 0.0
                    )
                }
            }) { Text("Save Project") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun NotificationsUpdatesDialog(
    enquiries: List<EnquiryLead>,
    projects: List<ClientProject>,
    onDismiss: () -> Unit,
    onSelectEnquiry: (EnquiryLead) -> Unit
) {
    BackHandler(onBack = onDismiss)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = RoyalBluePrimary)
                Text("Communications & Updates")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (enquiries.isEmpty() && projects.isEmpty()) {
                    Text(
                        text = "You're all caught up! New enquiries, quote replies, follow-ups, and milestone updates appear here in real time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                } else {
                    enquiries.take(6).forEach { enq ->
                        Surface(
                            onClick = { onDismiss(); onSelectEnquiry(enq) },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF4F7FC),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "${enq.enquiryCode} • ${EnquiryStatus.fromRaw(enq.status).label}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = RoyalBluePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(enq.projectTitle, style = MaterialTheme.typography.titleSmall, color = MidnightNavy)
                                Text(
                                    text = enq.latestNote.ifBlank { enq.requirements },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }
                    projects.take(4).forEach { proj ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE1F7EB),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Project • ${ProjectStatus.fromRaw(proj.status).label} (${proj.progressPercent}%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = StatusSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(proj.title, style = MaterialTheme.typography.titleSmall, color = MidnightNavy)
                                if (proj.milestoneNote.isNotBlank()) {
                                    Text(proj.milestoneNote, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
