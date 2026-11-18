package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ClockAction
import com.example.data.model.ClockLog
import com.example.data.model.Employee
import com.example.data.model.ShiftStatus
import com.example.data.model.UiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LivePulsingDot
import com.example.ui.components.formatTimestampShort
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

@Composable
fun TeamScreen(
    employeesState: UiState<List<Employee>>,
    clockLogsState: UiState<List<ClockLog>>,
    onSaveEmployee: (
        employeeId: String?,
        fullName: String,
        role: String,
        phone: String,
        activeRecord: Boolean
    ) -> Unit,
    onDeleteEmployee: (Employee) -> Unit,
    onClockAction: (Employee, ClockAction, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Live Status, 1 = Clock-In Logs
    var showEditor by remember { mutableStateOf(false) }
    var editingEmployee by remember { mutableStateOf<Employee?>(null) }
    var deleteConfirmTarget by remember { mutableStateOf<Employee?>(null) }
    var notePromptTarget by remember { mutableStateOf<Pair<Employee, ClockAction>?>(null) }

    val employees = (employeesState as? UiState.Success)?.data.orEmpty()
    val logs = (clockLogsState as? UiState.Success)?.data.orEmpty()

    val clockedInCount = remember(employees) {
        employees.count { ShiftStatus.fromRaw(it.shiftStatus) == ShiftStatus.CLOCKED_IN }
    }
    val onBreakCount = remember(employees) {
        employees.count { ShiftStatus.fromRaw(it.shiftStatus) == ShiftStatus.ON_BREAK }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("Live Staff (${employees.size})", "Clock-In Logs (${logs.size})").forEachIndexed { idx, label ->
                        SegmentedButton(
                            selected = activeTab == idx,
                            onClick = { activeTab = idx },
                            shape = SegmentedButtonDefaults.itemShape(index = idx, count = 2),
                            modifier = Modifier.testTag("team_tab_$idx")
                        ) {
                            Text(label)
                        }
                    }
                }

                // Live Attendance Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LivePulsingDot()
                            Column {
                                Text("Real-Time Attendance", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Shift: Mon–Sat • 9:30 AM – 6:30 PM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "$clockedInCount In",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                color = StatusSuccess
                            )
                            Text(
                                text = "$onBreakCount Break",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                color = StatusWarning
                            )
                        }
                    }
                }
            }

            if (activeTab == 0) {
                when (employeesState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(employeesState.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is UiState.Success -> {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (employees.isEmpty()) {
                                item {
                                    EmptyStateView(
                                        title = "No Team Members Added",
                                        subtitle = "Add cashiers, stock clerks, and managers to track real-time shift statuses and clock-in logs.",
                                        actionLabel = "Add Team Member",
                                        onAction = { editingEmployee = null; showEditor = true }
                                    )
                                }
                            } else {
                                items(employees, key = { it.id }) { emp ->
                                    EmployeeShiftCard(
                                        employee = emp,
                                        onEdit = { editingEmployee = emp; showEditor = true },
                                        onDelete = { deleteConfirmTarget = emp },
                                        onClockAction = { action ->
                                            notePromptTarget = emp to action
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                when (clockLogsState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(clockLogsState.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is UiState.Success -> {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (logs.isEmpty()) {
                                item {
                                    EmptyStateView(
                                        title = "No Clock-In Logs Yet",
                                        subtitle = "Clock-in, break, and clock-out events stream here in real time."
                                    )
                                }
                            } else {
                                items(logs, key = { it.id }) { log ->
                                    ClockLogItemCard(log = log)
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { editingEmployee = null; showEditor = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_employee_fab")
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add employee")
        }
    }

    if (showEditor) {
        EmployeeEditorDialog(
            employee = editingEmployee,
            onDismiss = { showEditor = false },
            onSave = { name, role, phone, active ->
                onSaveEmployee(editingEmployee?.id, name, role, phone, active)
                showEditor = false
            }
        )
    }

    notePromptTarget?.let { (emp, action) ->
        ClockActionNoteDialog(
            employee = emp,
            action = action,
            onDismiss = { notePromptTarget = null },
            onConfirm = { note ->
                onClockAction(emp, action, note)
                notePromptTarget = null
            }
        )
    }

    deleteConfirmTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteConfirmTarget = null },
            title = { Text("Remove Employee?") },
            text = { Text("Remove ${target.fullName} from active staff list? Past clock-in logs remain archived.") },
            confirmButton = {
                Button(onClick = {
                    onDeleteEmployee(target)
                    deleteConfirmTarget = null
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmployeeShiftCard(
    employee: Employee,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClockAction: (ClockAction) -> Unit
) {
    val status = ShiftStatus.fromRaw(employee.shiftStatus)
    val statusColor = when (status) {
        ShiftStatus.CLOCKED_IN -> StatusSuccess
        ShiftStatus.ON_BREAK -> StatusWarning
        ShiftStatus.CLOCKED_OUT -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LivePulsingDot(color = statusColor)
                        Text(
                            text = employee.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = buildString {
                            append(employee.role)
                            if (employee.phone.isNotBlank()) append(" • ${employee.phone}")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = status.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit ${employee.fullName}")
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = "Remove ${employee.fullName}",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (employee.lastClockActionAt != null) {
                Text(
                    text = "Last activity: ${formatTimestampShort(employee.lastClockActionAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (status) {
                    ShiftStatus.CLOCKED_OUT -> {
                        Button(
                            onClick = { onClockAction(ClockAction.CLOCK_IN) },
                            enabled = employee.activeRecord,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("clock_in_${employee.id}")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Clock In")
                        }
                    }
                    ShiftStatus.CLOCKED_IN -> {
                        FilledTonalButton(
                            onClick = { onClockAction(ClockAction.BREAK_START) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Rounded.Coffee, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Start Break")
                        }
                        OutlinedButton(
                            onClick = { onClockAction(ClockAction.CLOCK_OUT) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDanger),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Clock Out")
                        }
                    }
                    ShiftStatus.ON_BREAK -> {
                        Button(
                            onClick = { onClockAction(ClockAction.BREAK_END) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Resume Shift")
                        }
                        OutlinedButton(
                            onClick = { onClockAction(ClockAction.CLOCK_OUT) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDanger),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Clock Out")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClockLogItemCard(log: ClockLog) {
    val action = ClockAction.fromRaw(log.action)
    val badgeColor = when (action) {
        ClockAction.CLOCK_IN, ClockAction.BREAK_END -> StatusSuccess
        ClockAction.BREAK_START -> StatusWarning
        ClockAction.CLOCK_OUT -> StatusDanger
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(log.employeeName, style = MaterialTheme.typography.titleSmall)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = action.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = log.employeeRole + if (log.note.isNotBlank()) " • ${log.note}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Text(
                text = formatTimestampShort(log.loggedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmployeeEditorDialog(
    employee: Employee?,
    onDismiss: () -> Unit,
    onSave: (fullName: String, role: String, phone: String, activeRecord: Boolean) -> Unit
) {
    BackHandler(onBack = onDismiss)
    var fullName by remember(employee) { mutableStateOf(employee?.fullName ?: "") }
    var role by remember(employee) { mutableStateOf(employee?.role ?: "Cashier") }
    var phone by remember(employee) { mutableStateOf(employee?.phone ?: "") }
    var activeRecord by remember(employee) { mutableStateOf(employee?.activeRecord ?: true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val roleSuggestions = listOf("Cashier", "Store Manager", "Mobile Dev", "Web Developer", "SEO Executive", "Stock Clerk")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (employee == null) "Add Team Member" else "Edit Team Member") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; errorMsg = null },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("employee_name_input")
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it; errorMsg = null },
                    label = { Text("Role") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("employee_role_input")
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    roleSuggestions.forEach { preset ->
                        FilterChip(
                            selected = role.equals(preset, ignoreCase = true),
                            onClick = { role = preset },
                            label = { Text(preset) }
                        )
                    }
                }
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active Staff Member", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = activeRecord, onCheckedChange = { activeRecord = it })
                }
                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.trim().isEmpty()) {
                        errorMsg = "Employee name is required."
                    } else {
                        onSave(
                            fullName.trim(),
                            role.trim().ifEmpty { "Cashier" },
                            phone.trim(),
                            activeRecord
                        )
                    }
                },
                modifier = Modifier.testTag("save_employee_button")
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ClockActionNoteDialog(
    employee: Employee,
    action: ClockAction,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    BackHandler(onBack = onDismiss)
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${employee.fullName}: ${action.label}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Confirm real-time clock log entry for ${employee.role}.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 160) note = it },
                    label = { Text("Shift Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(note.trim()) },
                modifier = Modifier.testTag("confirm_clock_action_button")
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
