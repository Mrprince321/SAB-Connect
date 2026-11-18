package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ClientProject
import com.example.data.model.ClockAction
import com.example.data.model.Customer
import com.example.data.model.Employee
import com.example.data.model.EnquiryLead
import com.example.data.model.EnquiryStatus
import com.example.data.model.ShiftStatus
import com.example.data.model.StoreProfile
import com.example.data.model.UiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LivePulsingDot
import com.example.ui.components.PastelStatCard
import com.example.ui.components.ShowcaseMenuRowCard
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatTimestampTime
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PastelBlueCard
import com.example.ui.theme.PastelIndigoCard
import com.example.ui.theme.PastelMintCard
import com.example.ui.theme.PastelPurpleCard
import com.example.ui.theme.PastelRoseCard
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

enum class EmployeeTasksSubTab(val label: String) {
    FOLLOW_UPS("Follow-ups"),
    CLIENTS("Clients"),
    CALL_NOTES("Call Notes"),
    PROJECTS("Projects")
}

@Composable
fun EmployeeHomeScreen(
    userDisplayName: String,
    enquiriesState: UiState<List<EnquiryLead>>,
    customersState: UiState<List<Customer>>,
    employeesState: UiState<List<Employee>>,
    projectsState: UiState<List<ClientProject>>,
    onOpenLeads: () -> Unit,
    onOpenTasksTab: (EmployeeTasksSubTab) -> Unit,
    onOpenReportsProfile: () -> Unit,
    onClockAction: (Employee, ClockAction, String) -> Unit,
    onQuickRegisterStaff: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val enquiries = (enquiriesState as? UiState.Success)?.data.orEmpty()
    val customers = (customersState as? UiState.Success)?.data.orEmpty()
    val employees = (employeesState as? UiState.Success)?.data.orEmpty()
    val projects = (projectsState as? UiState.Success)?.data.orEmpty()

    val assignedLeadsCount = enquiries.count { it.assignedEmployeeId.isNotBlank() || it.status != EnquiryStatus.NEW.name }
    val inProgressCount = enquiries.count {
        it.status == EnquiryStatus.IN_PROGRESS.name || it.status == EnquiryStatus.QUOTED.name
    }
    val closedCount = enquiries.count {
        it.status == EnquiryStatus.CONVERTED.name || it.status == EnquiryStatus.CLOSED.name
    }
    val followUpsCount = enquiries.count { it.followUpDate.isNotBlank() }

    val primaryEmp = employees.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Hi, ${userDisplayName.trim().substringBefore(" ").ifBlank { "Employee" }}! 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )
                Text(
                    text = "Manage your assigned leads and tasks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
            }
        }

        // 3 Pastel Stat Cards matching Employee Home showcase
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PastelStatCard(
                    label = "Assigned Leads",
                    value = "$assignedLeadsCount",
                    containerColor = PastelBlueCard,
                    onClick = onOpenLeads,
                    modifier = Modifier.weight(1f)
                )
                PastelStatCard(
                    label = "In Progress",
                    value = "$inProgressCount",
                    containerColor = PastelRoseCard,
                    onClick = onOpenLeads,
                    modifier = Modifier.weight(1f)
                )
                PastelStatCard(
                    label = "Closed",
                    value = "$closedCount",
                    containerColor = PastelMintCard,
                    onClick = onOpenReportsProfile,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Chevron Menu Rows matching Employee Home showcase
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ShowcaseMenuRowCard(
                    title = "My Leads",
                    subtitle = "${enquiries.size} total • Contact clients & update stages",
                    icon = Icons.Rounded.Assignment,
                    iconTint = RoyalBluePrimary,
                    iconBg = PastelBlueCard,
                    badgeText = if (enquiries.isNotEmpty()) "${enquiries.size}" else null,
                    onClick = onOpenLeads,
                    modifier = Modifier.testTag("emp_menu_my_leads")
                )
                ShowcaseMenuRowCard(
                    title = "Follow-ups",
                    subtitle = "Scheduled callbacks & client reminders",
                    icon = Icons.Rounded.EventNote,
                    iconTint = Color(0xFF0284C7),
                    iconBg = Color(0xFFE0F2FE),
                    badgeText = if (followUpsCount > 0) "$followUpsCount" else null,
                    onClick = { onOpenTasksTab(EmployeeTasksSubTab.FOLLOW_UPS) },
                    modifier = Modifier.testTag("emp_menu_followups")
                )
                ShowcaseMenuRowCard(
                    title = "Clients",
                    subtitle = "Access customer phone, email & requirements",
                    icon = Icons.Rounded.Groups,
                    iconTint = Color(0xFF4338CA),
                    iconBg = PastelIndigoCard,
                    badgeText = if (customers.isNotEmpty()) "${customers.size}" else null,
                    onClick = { onOpenTasksTab(EmployeeTasksSubTab.CLIENTS) },
                    modifier = Modifier.testTag("emp_menu_clients")
                )
                ShowcaseMenuRowCard(
                    title = "Tasks",
                    subtitle = "Call notes, meeting minutes & ${projects.size} assigned projects",
                    icon = Icons.Rounded.TaskAlt,
                    iconTint = StatusSuccess,
                    iconBg = PastelMintCard,
                    onClick = { onOpenTasksTab(EmployeeTasksSubTab.CALL_NOTES) },
                    modifier = Modifier.testTag("emp_menu_tasks")
                )
                ShowcaseMenuRowCard(
                    title = "Reports",
                    subtitle = "Basic lead conversion & activity summary",
                    icon = Icons.Rounded.Assessment,
                    iconTint = Color(0xFF6D28D9),
                    iconBg = PastelPurpleCard,
                    onClick = onOpenReportsProfile,
                    modifier = Modifier.testTag("emp_menu_reports")
                )
            }
        }

        // Live Shift Check-In Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Live Shift Attendance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        LivePulsingDot()
                    }
                    if (primaryEmp == null) {
                        Button(
                            onClick = { onQuickRegisterStaff(userDisplayName.ifBlank { "Team Member" }) },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create Employee Profile to Clock In")
                        }
                    } else {
                        val st = ShiftStatus.fromRaw(primaryEmp.shiftStatus)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(primaryEmp.fullName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "${primaryEmp.role} • ${st.label}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (st == ShiftStatus.CLOCKED_IN) StatusSuccess else Color(0xFF64748B)
                                )
                            }
                            if (primaryEmp.lastClockActionAt != null) {
                                Text(formatTimestampTime(primaryEmp.lastClockActionAt), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            when (st) {
                                ShiftStatus.CLOCKED_OUT -> {
                                    Button(
                                        onClick = { onClockAction(primaryEmp, ClockAction.CLOCK_IN, "Started shift") },
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Clock In")
                                    }
                                }
                                ShiftStatus.CLOCKED_IN -> {
                                    FilledTonalButton(
                                        onClick = { onClockAction(primaryEmp, ClockAction.BREAK_START, "Break") },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Rounded.Coffee, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Break")
                                    }
                                    OutlinedButton(
                                        onClick = { onClockAction(primaryEmp, ClockAction.CLOCK_OUT, "Shift ended") },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Clock Out")
                                    }
                                }
                                ShiftStatus.ON_BREAK -> {
                                    Button(
                                        onClick = { onClockAction(primaryEmp, ClockAction.BREAK_END, "Resumed") },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Resume")
                                    }
                                    OutlinedButton(
                                        onClick = { onClockAction(primaryEmp, ClockAction.CLOCK_OUT, "Shift ended") },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Clock Out")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmployeeLeadsScreen(
    storeProfile: StoreProfile?,
    enquiriesState: UiState<List<EnquiryLead>>,
    onSelectEnquiry: (EnquiryLead) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = storeProfile?.currencySymbol ?: "₹"
    val enquiries = (enquiriesState as? UiState.Success)?.data.orEmpty()
    var filterStatus by remember { mutableStateOf<EnquiryStatus?>(null) }

    val filtered = remember(enquiries, filterStatus) {
        if (filterStatus == null) enquiries else enquiries.filter { it.status == filterStatus?.name }
    }

    Column(modifier = modifier.fillMaxSize().widthIn(max = 840.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(selected = filterStatus == null, onClick = { filterStatus = null }, label = { Text("All (${enquiries.size})") })
            EnquiryStatus.entries.forEach { st ->
                FilterChip(
                    selected = filterStatus == st,
                    onClick = { filterStatus = st },
                    label = { Text(st.label) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Leads in View",
                        subtitle = "Assigned customer enquiries and quote requests appear here for outreach and status updates."
                    )
                }
            } else {
                items(filtered, key = { it.id }) { enq ->
                    EnquirySummaryCard(enquiry = enq, currencySymbol = currency, onClick = { onSelectEnquiry(enq) })
                }
            }
        }
    }
}

@Composable
fun EmployeeTasksAndClientsScreen(
    initialTab: EmployeeTasksSubTab,
    storeProfile: StoreProfile?,
    enquiriesState: UiState<List<EnquiryLead>>,
    customersState: UiState<List<Customer>>,
    projectsState: UiState<List<ClientProject>>,
    onSelectEnquiry: (EnquiryLead) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currency = storeProfile?.currencySymbol ?: "₹"
    var activeTab by remember(initialTab) { mutableStateOf(initialTab) }
    val enquiries = (enquiriesState as? UiState.Success)?.data.orEmpty()
    val customers = (customersState as? UiState.Success)?.data.orEmpty()
    val projects = (projectsState as? UiState.Success)?.data.orEmpty()

    Column(modifier = modifier.fillMaxSize().widthIn(max = 840.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            EmployeeTasksSubTab.entries.forEach { tab ->
                FilterChip(
                    selected = activeTab == tab,
                    onClick = { activeTab = tab },
                    label = { Text(tab.label, maxLines = 1) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (activeTab) {
                EmployeeTasksSubTab.FOLLOW_UPS -> {
                    val followUps = enquiries.filter { it.followUpDate.isNotBlank() }
                    if (followUps.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Scheduled Follow-ups",
                                subtitle = "Open any lead and set a follow-up date and agenda to see your callback queue here."
                            )
                        }
                    } else {
                        items(followUps, key = { it.id }) { enq ->
                            Card(
                                onClick = { onSelectEnquiry(enq) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Due: ${enq.followUpDate}", style = MaterialTheme.typography.labelMedium, color = StatusWarning, fontWeight = FontWeight.Bold)
                                    Text("${enq.customerName} • ${enq.projectTitle}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(enq.followUpNote.ifBlank { "Tap to record call outcome" }, style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                }
                            }
                        }
                    }
                }
                EmployeeTasksSubTab.CLIENTS -> {
                    if (customers.isEmpty() && enquiries.isEmpty()) {
                        item { EmptyStateView("No Client Directory Yet", "Clients from customer registrations and enquiries appear here.") }
                    } else {
                        items(customers, key = { it.id }) { cust ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(cust.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    if (cust.phone.isNotBlank()) Text("Phone: ${cust.phone}", style = MaterialTheme.typography.bodySmall)
                                    if (cust.email.isNotBlank()) Text("Email: ${cust.email}", style = MaterialTheme.typography.bodySmall)
                                    if (cust.address.isNotBlank()) Text(cust.address, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (cust.phone.isNotBlank()) {
                                            OutlinedButton(onClick = {
                                                runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cust.phone}"))) }
                                            }) {
                                                Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Call")
                                            }
                                        }
                                        if (cust.email.isNotBlank()) {
                                            OutlinedButton(onClick = {
                                                runCatching { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${cust.email}"))) }
                                            }) {
                                                Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Email")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                EmployeeTasksSubTab.CALL_NOTES -> {
                    if (enquiries.isEmpty()) {
                        item { EmptyStateView("No Call & Meeting Notes", "Select a lead to record call summaries and meeting notes.") }
                    } else {
                        items(enquiries, key = { it.id }) { enq ->
                            Card(
                                onClick = { onSelectEnquiry(enq) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("${enq.enquiryCode} • ${enq.customerName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = enq.latestNote.ifBlank { "Tap to add call or meeting note" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF475569)
                                    )
                                    Text("${enq.updates.size} logged update(s)", style = MaterialTheme.typography.labelSmall, color = RoyalBluePrimary)
                                }
                            }
                        }
                    }
                }
                EmployeeTasksSubTab.PROJECTS -> {
                    if (projects.isEmpty()) {
                        item { EmptyStateView("No Assigned Projects Yet", "Projects converted from leads appear here.") }
                    } else {
                        items(projects, key = { it.id }) { proj ->
                            ClientProjectCard(project = proj, currencySymbol = currency)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeProfileAndReportsScreen(
    userDisplayName: String,
    userEmail: String,
    storeProfile: StoreProfile?,
    enquiriesState: UiState<List<EnquiryLead>>,
    projectsState: UiState<List<ClientProject>>,
    onSwitchRole: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = storeProfile?.currencySymbol ?: "₹"
    val enquiries = (enquiriesState as? UiState.Success)?.data.orEmpty()
    val projects = (projectsState as? UiState.Success)?.data.orEmpty()
    val converted = enquiries.count { it.status == EnquiryStatus.CONVERTED.name }
    val conversionPct = if (enquiries.isNotEmpty()) (converted * 100) / enquiries.size else 0
    val quotedPipeline = enquiries.sumOf { it.quotedAmount }

    LazyColumn(
        modifier = modifier.fillMaxSize().widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(shape = CircleShape, color = PastelBlueCard, modifier = Modifier.size(48.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Badge, contentDescription = null, tint = RoyalBluePrimary)
                        }
                    }
                    Column {
                        Text(userDisplayName.ifBlank { "SAB Employee" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(userEmail.ifBlank { "Company Account" }, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                    }
                }
            }
        }

        item {
            Text("Basic Lead & Activity Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PastelStatCard("Total Leads", "${enquiries.size}", PastelBlueCard, modifier = Modifier.weight(1f))
                PastelStatCard("Conversion", "$conversionPct%", PastelMintCard, modifier = Modifier.weight(1f))
                PastelStatCard("Projects", "${projects.size}", PastelPurpleCard, modifier = Modifier.weight(1f))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Quoted Pipeline Value", style = MaterialTheme.typography.bodyMedium)
                        Text(formatCurrency(quotedPipeline, currency), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RoyalBluePrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Scheduled Follow-ups", style = MaterialTheme.typography.bodyMedium)
                        Text("${enquiries.count { it.followUpDate.isNotBlank() }}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onSwitchRole, modifier = Modifier.weight(1f).height(48.dp)) {
                            Icon(Icons.Rounded.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Switch Role")
                        }
                        OutlinedButton(onClick = onSignOut, modifier = Modifier.weight(1f).height(48.dp)) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Sign Out")
                        }
                    }
                }
            }
        }
    }
}
