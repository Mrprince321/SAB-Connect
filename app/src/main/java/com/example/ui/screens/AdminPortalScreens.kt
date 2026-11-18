package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.AssignmentInd
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FolderCopy
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ClientProject
import com.example.data.model.ClockAction
import com.example.data.model.ClockLog
import com.example.data.model.Customer
import com.example.data.model.Employee
import com.example.data.model.EnquiryLead
import com.example.data.model.EnquiryStatus
import com.example.data.model.Invoice
import com.example.data.model.ProjectStatus
import com.example.data.model.ShiftStatus
import com.example.data.model.StoreProfile
import com.example.data.model.UiState
import com.example.ui.components.ChartBarPoint
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PastelStatCard
import com.example.ui.components.RevenueBarChart
import com.example.ui.components.ShowcaseMenuRowCard
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatTimestampShort
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PastelBlueCard
import com.example.ui.theme.PastelCoralCard
import com.example.ui.theme.PastelIndigoCard
import com.example.ui.theme.PastelMintCard
import com.example.ui.theme.PastelPurpleCard
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess

@Composable
fun AdminHomeScreen(
    customersState: UiState<List<Customer>>,
    employeesState: UiState<List<Employee>>,
    enquiriesState: UiState<List<EnquiryLead>>,
    projectsState: UiState<List<ClientProject>>,
    onOpenUsersTab: () -> Unit,
    onOpenLeadsTab: () -> Unit,
    onOpenServicesPortfolio: () -> Unit,
    onOpenProjects: () -> Unit,
    onOpenReportsTab: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customers = (customersState as? UiState.Success)?.data.orEmpty()
    val employees = (employeesState as? UiState.Success)?.data.orEmpty()
    val enquiries = (enquiriesState as? UiState.Success)?.data.orEmpty()
    val projects = (projectsState as? UiState.Success)?.data.orEmpty()

    val totalUsers = customers.size + employees.size + 1
    val unassignedLeads = enquiries.count { it.assignedEmployeeId.isBlank() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Admin Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MidnightNavy
            )
        }

        // 3 Pastel Stat Cards matching Admin Dashboard showcase
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PastelStatCard(
                    label = "Total Users",
                    value = "$totalUsers",
                    containerColor = PastelBlueCard,
                    onClick = onOpenUsersTab,
                    modifier = Modifier.weight(1f)
                )
                PastelStatCard(
                    label = "Employees",
                    value = "${employees.size}",
                    containerColor = PastelMintCard,
                    onClick = onOpenUsersTab,
                    modifier = Modifier.weight(1f)
                )
                PastelStatCard(
                    label = "Leads",
                    value = "${enquiries.size}",
                    containerColor = PastelCoralCard,
                    onClick = onOpenLeadsTab,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 6 Chevron Menu Rows matching Admin Dashboard showcase
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ShowcaseMenuRowCard(
                    title = "User Management",
                    subtitle = "${customers.size} customers • ${employees.size} employees",
                    icon = Icons.Rounded.ManageAccounts,
                    iconTint = RoyalBluePrimary,
                    iconBg = PastelBlueCard,
                    onClick = onOpenUsersTab,
                    modifier = Modifier.testTag("admin_menu_users")
                )
                ShowcaseMenuRowCard(
                    title = "Assign Leads",
                    subtitle = if (unassignedLeads > 0) "$unassignedLeads awaiting assignment" else "View all incoming leads & assign",
                    icon = Icons.Rounded.AssignmentInd,
                    iconTint = Color(0xFF0284C7),
                    iconBg = Color(0xFFE0F2FE),
                    badgeText = if (unassignedLeads > 0) "$unassignedLeads new" else null,
                    onClick = onOpenLeadsTab,
                    modifier = Modifier.testTag("admin_menu_assign_leads")
                )
                ShowcaseMenuRowCard(
                    title = "Services & Portfolio",
                    subtitle = "Manage company digital services & case studies",
                    icon = Icons.Rounded.Layers,
                    iconTint = Color(0xFF4338CA),
                    iconBg = PastelIndigoCard,
                    onClick = onOpenServicesPortfolio,
                    modifier = Modifier.testTag("admin_menu_services_portfolio")
                )
                ShowcaseMenuRowCard(
                    title = "Projects",
                    subtitle = "${projects.size} active & completed client projects",
                    icon = Icons.Rounded.FolderCopy,
                    iconTint = StatusSuccess,
                    iconBg = PastelMintCard,
                    badgeText = if (projects.isNotEmpty()) "${projects.size}" else null,
                    onClick = onOpenProjects,
                    modifier = Modifier.testTag("admin_menu_projects")
                )
                ShowcaseMenuRowCard(
                    title = "Reports & Analytics",
                    subtitle = "Employee performance, sales & conversion funnel",
                    icon = Icons.Rounded.Assessment,
                    iconTint = Color(0xFF6D28D9),
                    iconBg = PastelPurpleCard,
                    onClick = onOpenReportsTab,
                    modifier = Modifier.testTag("admin_menu_reports")
                )
                ShowcaseMenuRowCard(
                    title = "Settings",
                    subtitle = "Workspace profile, currency, GST & access control",
                    icon = Icons.Rounded.Settings,
                    iconTint = Color(0xFF475569),
                    iconBg = Color(0xFFF1F5F9),
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("admin_menu_settings")
                )
            }
        }
    }
}

@Composable
fun AdminUsersScreen(
    customersState: UiState<List<Customer>>,
    employeesState: UiState<List<Employee>>,
    clockLogsState: UiState<List<ClockLog>>,
    onSaveCustomer: (String?, String, String, String, String, String) -> Unit,
    onDeleteCustomer: (Customer) -> Unit,
    onSaveEmployee: (String?, String, String, String, Boolean) -> Unit,
    onDeleteEmployee: (Employee) -> Unit,
    onClockAction: (Employee, ClockAction, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0=Customers, 1=Employees, 2=Clock Logs
    var editingCustomer by remember { mutableStateOf<Customer?>(null) }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var editingEmployee by remember { mutableStateOf<Employee?>(null) }
    var showEmployeeDialog by remember { mutableStateOf(false) }

    val customers = (customersState as? UiState.Success)?.data.orEmpty()
    val employees = (employeesState as? UiState.Success)?.data.orEmpty()
    val logs = (clockLogsState as? UiState.Success)?.data.orEmpty()

    Column(modifier = modifier.fillMaxSize().widthIn(max = 840.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = selectedTab == 0, onClick = { selectedTab = 0 }, label = { Text("Customers (${customers.size})") })
                FilterChip(selected = selectedTab == 1, onClick = { selectedTab = 1 }, label = { Text("Employees (${employees.size})") })
                FilterChip(selected = selectedTab == 2, onClick = { selectedTab = 2 }, label = { Text("Shifts") })
            }
            if (selectedTab != 2) {
                Button(
                    onClick = {
                        if (selectedTab == 0) {
                            editingCustomer = null; showCustomerDialog = true
                        } else {
                            editingEmployee = null; showEmployeeDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(if (selectedTab == 0) Icons.Rounded.PersonAdd else Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    if (customers.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Customer Accounts",
                                subtitle = "Add customer accounts or invite customers to register via SAB Connect.",
                                actionLabel = "Add Customer",
                                onAction = { editingCustomer = null; showCustomerDialog = true }
                            )
                        }
                    } else {
                        items(customers, key = { it.id }) { cust ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(cust.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("${cust.phone.ifBlank { "No phone" }} • ${cust.email.ifBlank { "No email" }}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                                    }
                                    Row {
                                        IconButton(onClick = { editingCustomer = cust; showCustomerDialog = true }) {
                                            Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                                        }
                                        IconButton(onClick = { onDeleteCustomer(cust) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = StatusDanger)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (employees.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Employees Added",
                                subtitle = "Add team members so you can assign incoming leads, projects, and track attendance.",
                                actionLabel = "Add Employee",
                                onAction = { editingEmployee = null; showEmployeeDialog = true }
                            )
                        }
                    } else {
                        items(employees, key = { it.id }) { emp ->
                            val st = ShiftStatus.fromRaw(emp.shiftStatus)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(emp.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Text("${emp.role} • ${emp.phone.ifBlank { "Staff" }}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                                        }
                                        Row {
                                            IconButton(onClick = { editingEmployee = emp; showEmployeeDialog = true }) {
                                                Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                                            }
                                            IconButton(onClick = { onDeleteEmployee(emp) }) {
                                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = StatusDanger)
                                            }
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(shape = CircleShape, color = if (st == ShiftStatus.CLOCKED_IN) PastelMintCard else Color(0xFFF1F5F9)) {
                                            Text(
                                                text = st.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (st == ShiftStatus.CLOCKED_IN) StatusSuccess else Color(0xFF64748B),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val nextAction = if (st == ShiftStatus.CLOCKED_IN) ClockAction.CLOCK_OUT else ClockAction.CLOCK_IN
                                                onClockAction(emp, nextAction, "Admin shift toggle")
                                            }
                                        ) {
                                            Text(if (st == ShiftStatus.CLOCKED_IN) "Clock Out" else "Clock In")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    if (logs.isEmpty()) {
                        item { EmptyStateView("No Shift Clock Logs", "Live clock-in and clock-out events appear here.") }
                    } else {
                        items(logs, key = { it.id }) { log ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(log.employeeName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("${ClockAction.fromRaw(log.action).label} • ${log.employeeRole}", style = MaterialTheme.typography.bodySmall, color = RoyalBluePrimary)
                                    }
                                    Text(formatTimestampShort(log.loggedAt), style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCustomerDialog) {
        AdminCustomerDialog(
            existing = editingCustomer,
            onDismiss = { showCustomerDialog = false },
            onSave = { name, phone, email, addr, notes ->
                onSaveCustomer(editingCustomer?.id, name, phone, email, addr, notes)
                showCustomerDialog = false
            }
        )
    }
    if (showEmployeeDialog) {
        AdminEmployeeDialog(
            existing = editingEmployee,
            onDismiss = { showEmployeeDialog = false },
            onSave = { name, role, phone, active ->
                onSaveEmployee(editingEmployee?.id, name, role, phone, active)
                showEmployeeDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminLeadsScreen(
    storeProfile: StoreProfile?,
    enquiriesState: UiState<List<EnquiryLead>>,
    employeesState: UiState<List<Employee>>,
    onSelectEnquiry: (EnquiryLead) -> Unit,
    onAssignLead: (EnquiryLead, Employee) -> Unit,
    onConvertToProject: (EnquiryLead) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = storeProfile?.currencySymbol ?: "₹"
    val enquiries = (enquiriesState as? UiState.Success)?.data.orEmpty()
    val employees = (employeesState as? UiState.Success)?.data.orEmpty()
    var filterStatus by remember { mutableStateOf<EnquiryStatus?>(null) }
    var assigningEnquiry by remember { mutableStateOf<EnquiryLead?>(null) }

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
                FilterChip(selected = filterStatus == st, onClick = { filterStatus = st }, label = { Text(st.label) })
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
                        title = "No Incoming Leads",
                        subtitle = "Customer project enquiries, quotation requests, and consultation bookings appear here."
                    )
                }
            } else {
                items(filtered, key = { it.id }) { enq ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        EnquirySummaryCard(enquiry = enq, currencySymbol = currency, onClick = { onSelectEnquiry(enq) })
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { assigningEnquiry = enq },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (enq.assignedEmployeeName.isBlank()) "Assign Employee" else "Reassign (${enq.assignedEmployeeName})")
                            }
                            if (enq.status != EnquiryStatus.CONVERTED.name) {
                                Button(
                                    onClick = { onConvertToProject(enq) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Convert Project")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    assigningEnquiry?.let { target ->
        AssignLeadDialog(
            enquiry = target,
            employees = employees,
            onDismiss = { assigningEnquiry = null },
            onAssign = { emp ->
                onAssignLead(target, emp)
                assigningEnquiry = null
            }
        )
    }
}

@Composable
fun AdminReportsScreen(
    storeProfile: StoreProfile?,
    enquiriesState: UiState<List<EnquiryLead>>,
    employeesState: UiState<List<Employee>>,
    projectsState: UiState<List<ClientProject>>,
    invoicesState: UiState<List<Invoice>>,
    onOpenProjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = storeProfile?.currencySymbol ?: "₹"
    val enquiries = (enquiriesState as? UiState.Success)?.data.orEmpty()
    val employees = (employeesState as? UiState.Success)?.data.orEmpty()
    val projects = (projectsState as? UiState.Success)?.data.orEmpty()
    val invoices = (invoicesState as? UiState.Success)?.data.orEmpty()

    val convertedCount = enquiries.count { it.status == EnquiryStatus.CONVERTED.name }
    val conversionRate = if (enquiries.isNotEmpty()) (convertedCount * 100) / enquiries.size else 0
    val quotedPipeline = enquiries.sumOf { it.quotedAmount }
    val projectValueTotal = projects.sumOf { it.projectValue }
    val invoiceRevenue = invoices.sumOf { it.totalAmount }

    val stageBars = listOf(
        ChartBarPoint("New", enquiries.count { it.status == EnquiryStatus.NEW.name }.toDouble()),
        ChartBarPoint("Active", enquiries.count { it.status == EnquiryStatus.IN_PROGRESS.name }.toDouble()),
        ChartBarPoint("Quoted", enquiries.count { it.status == EnquiryStatus.QUOTED.name }.toDouble()),
        ChartBarPoint("Won", convertedCount.toDouble()),
        ChartBarPoint("Closed", enquiries.count { it.status == EnquiryStatus.CLOSED.name }.toDouble())
    )

    LazyColumn(
        modifier = modifier.fillMaxSize().widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Sales & Conversion Reports", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MidnightNavy)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PastelStatCard("Total Leads", "${enquiries.size}", PastelBlueCard, modifier = Modifier.weight(1f))
                PastelStatCard("Conversion", "$conversionRate%", PastelMintCard, modifier = Modifier.weight(1f))
                PastelStatCard("Projects", "${projects.size}", PastelPurpleCard, onClick = onOpenProjects, modifier = Modifier.weight(1f))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pipeline & Revenue Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Quoted Lead Value", color = Color(0xFF475569))
                        Text(formatCurrency(quotedPipeline, currency), fontWeight = FontWeight.Bold, color = RoyalBluePrimary)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active Project Portfolio Value", color = Color(0xFF475569))
                        Text(formatCurrency(projectValueTotal, currency), fontWeight = FontWeight.Bold, color = StatusSuccess)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Billed Invoices Total", color = Color(0xFF475569))
                        Text(formatCurrency(invoiceRevenue, currency), fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Lead Stage Distribution", style = MaterialTheme.typography.labelMedium, color = Color(0xFF64748B))
                    RevenueBarChart(points = stageBars, currencySymbol = "")
                }
            }
        }

        item {
            Text("Employee Performance Monitor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (employees.isEmpty()) {
            item { EmptyStateView("No Employees Yet", "Add employees to track assigned lead handling and project completion rates.") }
        } else {
            items(employees, key = { it.id }) { emp ->
                val empLeads = enquiries.filter { it.assignedEmployeeId == emp.id }
                val empWon = empLeads.count { it.status == EnquiryStatus.CONVERTED.name }
                val empProjects = projects.count { it.assignedEmployeeId == emp.id }
                val rate = if (empLeads.isNotEmpty()) (empWon * 100) / empLeads.size else 0

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Rounded.Badge, contentDescription = null, tint = RoyalBluePrimary)
                                Column {
                                    Text(emp.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(emp.role, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                                }
                            }
                            Text("$rate% Conv.", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = StatusSuccess)
                        }
                        LinearProgressIndicator(
                            progress = { (rate.coerceIn(0, 100)) / 100f },
                            modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                            color = StatusSuccess,
                            trackColor = Color(0xFFE2E8F0)
                        )
                        Text(
                            text = "${empLeads.size} assigned leads • $empWon converted • $empProjects active projects",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCustomerDialog(
    existing: Customer?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    BackHandler(onBack = onDismiss)
    var name by remember(existing) { mutableStateOf(existing?.fullName ?: "") }
    var phone by remember(existing) { mutableStateOf(existing?.phone ?: "") }
    var email by remember(existing) { mutableStateOf(existing?.email ?: "") }
    var address by remember(existing) { mutableStateOf(existing?.address ?: "") }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Customer Account" else "Edit Customer") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it; error = null }, label = { Text("Full Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Company / Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                if (error != null) Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.trim().isEmpty()) error = "Name required"
                else onSave(name.trim(), phone.trim(), email.trim(), address.trim(), notes.trim())
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AdminEmployeeDialog(
    existing: Employee?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean) -> Unit
) {
    BackHandler(onBack = onDismiss)
    var name by remember(existing) { mutableStateOf(existing?.fullName ?: "") }
    var role by remember(existing) { mutableStateOf(existing?.role ?: "Solutions Consultant") }
    var phone by remember(existing) { mutableStateOf(existing?.phone ?: "") }
    var activeRecord by remember(existing) { mutableStateOf(existing?.activeRecord ?: true) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Employee" else "Edit Employee") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it; error = null }, label = { Text("Employee Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Department / Role") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone / Email") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = activeRecord, onCheckedChange = { activeRecord = it })
                    Text("Active Staff Account")
                }
                if (error != null) Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.trim().isEmpty()) error = "Employee name required"
                else onSave(name.trim(), role.trim().ifEmpty { "Consultant" }, phone.trim(), activeRecord)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
