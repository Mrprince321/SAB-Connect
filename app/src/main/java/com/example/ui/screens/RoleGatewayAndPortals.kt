package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AppRole
import com.example.data.model.ClockAction
import com.example.data.model.ClockLog
import com.example.data.model.Employee
import com.example.data.model.Invoice
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.Product
import com.example.data.model.ShiftStatus
import com.example.data.model.StoreProfile
import com.example.data.model.UiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LivePulsingDot
import com.example.ui.components.MetricKpiCard
import com.example.ui.components.SabDigitalService
import com.example.ui.components.SabDigitalServicesCarousel
import com.example.ui.components.SabDigitechInfo
import com.example.ui.components.SabLogoTile
import com.example.ui.components.SabNeuralHeroContainer
import com.example.ui.components.SabUtilityContactStrip
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatTimestampShort
import com.example.ui.components.formatTimestampTime
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanLight
import com.example.ui.theme.HeroGlassBorder
import com.example.ui.theme.HeroGlassSurface
import com.example.ui.theme.HeroTextMuted
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SapphirePrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.BillingCartState
import java.util.Calendar

@Composable
fun RoleSelectionScreen(
    userDisplayName: String,
    userEmail: String,
    storeProfile: StoreProfile?,
    onSelectRole: (AppRole) -> Unit,
    onOpenSupport: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SabUtilityContactStrip()

        Surface(
            color = SapphirePrimary,
            contentColor = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SabLogoTile()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = storeProfile?.storeName?.ifBlank { "SAB Connect" } ?: "SAB Connect",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = userEmail.ifBlank { "Online Cloud Workspace" },
                        style = MaterialTheme.typography.labelSmall,
                        color = HeroTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onOpenSupport) {
                    Icon(
                        imageVector = Icons.Rounded.SupportAgent,
                        contentDescription = "SAB Digital Technologies Support",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onSignOut) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = "Sign out",
                        tint = Color.White
                    )
                }
            }
        }

        SabNeuralHeroContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = HeroGlassSurface,
                    border = BorderStroke(1.dp, HeroGlassBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        LivePulsingDot(color = ElectricCyanLight)
                        Text(
                            text = "Online Workspace Ready • Choose Access Role",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
                Text(
                    text = "Welcome${if (userDisplayName.isNotBlank()) ", $userDisplayName" else ""}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select your role to launch a tailored interface with role-specific permissions and workflows.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HeroTextMuted
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppRole.entries.forEach { role ->
                val (icon, accent) = when (role) {
                    AppRole.ADMIN -> Icons.Rounded.AdminPanelSettings to SapphirePrimary
                    AppRole.EMPLOYEE -> Icons.Rounded.Badge to ElectricCyan
                    AppRole.CUSTOMER -> Icons.Rounded.ShoppingBag to StatusSuccess
                }
                RoleSpecificationCard(
                    role = role,
                    icon = icon,
                    accentColor = accent,
                    onEnterRole = { onSelectRole(role) }
                )
            }
        }
    }
}

@Composable
private fun RoleSpecificationCard(
    role: AppRole,
    icon: ImageVector,
    accentColor: Color,
    onEnterRole: () -> Unit
) {
    Card(
        onClick = onEnterRole,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("select_role_${role.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = accentColor.copy(alpha = 0.14f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = role.label,
                                tint = accentColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = role.label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = role.badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = accentColor
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Enter",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = role.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                role.specifications.forEach { spec ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = spec,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmployeeShiftDeskScreen(
    storeProfile: StoreProfile?,
    userDisplayName: String,
    productsState: UiState<List<Product>>,
    invoicesState: UiState<List<Invoice>>,
    employeesState: UiState<List<Employee>>,
    clockLogsState: UiState<List<ClockLog>>,
    onClockAction: (Employee, ClockAction, String) -> Unit,
    onQuickRegisterStaff: (String) -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToStock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products = (productsState as? UiState.Success)?.data.orEmpty()
    val invoices = (invoicesState as? UiState.Success)?.data.orEmpty()
    val employees = (employeesState as? UiState.Success)?.data.orEmpty()
    val logs = (clockLogsState as? UiState.Success)?.data.orEmpty()

    var selectedEmpId by remember(employees) {
        mutableStateOf(employees.firstOrNull()?.id)
    }
    val selectedEmployee = employees.firstOrNull { it.id == selectedEmpId } ?: employees.firstOrNull()

    val todayBillsCount = remember(invoices) {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val d = cal.get(Calendar.DAY_OF_YEAR)
        invoices.count { inv ->
            val dt = inv.createdAt?.toDate() ?: return@count false
            cal.time = dt
            cal.get(Calendar.YEAR) == y && cal.get(Calendar.DAY_OF_YEAR) == d
        }
    }
    val lowStockCount = remember(products) {
        products.count { it.stockQuantity <= it.lowStockThreshold }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SabNeuralHeroContainer(modifier = Modifier) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HeroGlassSurface,
                            border = BorderStroke(1.dp, HeroGlassBorder)
                        ) {
                            Text(
                                text = "EMPLOYEE DESK • FLOOR OPERATIONS",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyanLight,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        LivePulsingDot(color = StatusSuccess)
                    }
                    Text(
                        text = storeProfile?.storeName?.ifBlank { "SAB Connect Counter" } ?: "SAB Connect Counter",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "POS Billing, Shelf Stock Checks & Live Shift Logs • Admin margins & GST settings restricted",
                        style = MaterialTheme.typography.bodySmall,
                        color = HeroTextMuted
                    )
                }
            }
        }

        // Personal Live Clock-In Widget
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("My Live Shift Clock-In", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = SabDigitechInfo.HOURS,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (employees.isEmpty()) {
                        Text(
                            text = "No staff profile linked yet. Create your counter staff profile to start logging shifts online.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { onQuickRegisterStaff(userDisplayName.ifBlank { "Counter Staff" }) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Create Staff Profile (${userDisplayName.ifBlank { "Staff" }})")
                        }
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            employees.forEach { emp ->
                                FilterChip(
                                    selected = selectedEmployee?.id == emp.id,
                                    onClick = { selectedEmpId = emp.id },
                                    label = { Text("${emp.fullName} (${emp.role})") }
                                )
                            }
                        }

                        selectedEmployee?.let { emp ->
                            val status = ShiftStatus.fromRaw(emp.shiftStatus)
                            val statusColor = when (status) {
                                ShiftStatus.CLOCKED_IN -> StatusSuccess
                                ShiftStatus.ON_BREAK -> StatusWarning
                                ShiftStatus.CLOCKED_OUT -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = statusColor.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        LivePulsingDot(color = statusColor)
                                        Column {
                                            Text(emp.fullName, style = MaterialTheme.typography.titleSmall)
                                            Text(
                                                text = "Status: ${status.label}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = statusColor
                                            )
                                        }
                                    }
                                    if (emp.lastClockActionAt != null) {
                                        Text(
                                            text = formatTimestampTime(emp.lastClockActionAt),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                when (status) {
                                    ShiftStatus.CLOCKED_OUT -> {
                                        Button(
                                            onClick = { onClockAction(emp, ClockAction.CLOCK_IN, "Shift started") },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Rounded.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Clock In Now")
                                        }
                                    }
                                    ShiftStatus.CLOCKED_IN -> {
                                        FilledTonalButton(
                                            onClick = { onClockAction(emp, ClockAction.BREAK_START, "Short break") },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Icon(Icons.Rounded.Coffee, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Start Break")
                                        }
                                        OutlinedButton(
                                            onClick = { onClockAction(emp, ClockAction.CLOCK_OUT, "Shift ended") },
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
                                            onClick = { onClockAction(emp, ClockAction.BREAK_END, "Resumed shift") },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Resume Shift")
                                        }
                                        OutlinedButton(
                                            onClick = { onClockAction(emp, ClockAction.CLOCK_OUT, "Shift ended") },
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
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricKpiCard(
                    title = "Counter Bills Today",
                    value = "$todayBillsCount",
                    subtitle = "Open POS Billing",
                    icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                    accentColor = SapphirePrimary,
                    onClick = onNavigateToBilling,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "Low Stock Alerts",
                    value = "$lowStockCount",
                    subtitle = "${products.size} SKUs on floor",
                    icon = Icons.Rounded.WarningAmber,
                    accentColor = if (lowStockCount > 0) StatusWarning else StatusSuccess,
                    onClick = onNavigateToStock,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToBilling,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Rounded.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("POS Checkout")
                }
                OutlinedButton(
                    onClick = onNavigateToStock,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Rounded.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Stock Check")
                }
            }
        }

        item {
            Text("Live Shift Activity", style = MaterialTheme.typography.titleMedium)
        }

        if (logs.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No Shift Logs Yet",
                    subtitle = "Clock-in and break events appear here live for the team."
                )
            }
        } else {
            items(logs.take(8), key = { it.id }) { log ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.employeeName, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${ClockAction.fromRaw(log.action).label} • ${log.employeeRole}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = formatTimestampShort(log.loggedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerShopHomeScreen(
    storeProfile: StoreProfile?,
    userDisplayName: String,
    productsState: UiState<List<Product>>,
    invoicesState: UiState<List<Invoice>>,
    cartState: BillingCartState,
    onAddToOrderBag: (Product) -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToServices: () -> Unit,
    onOpenSupport: (SabDigitalService?) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = storeProfile?.currencySymbol ?: "₹"
    val products = (productsState as? UiState.Success)?.data.orEmpty()
    val invoices = (invoicesState as? UiState.Success)?.data.orEmpty()
    val inStockProducts = remember(products) { products.filter { it.stockQuantity > 0L }.take(6) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SabNeuralHeroContainer {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = HeroGlassSurface,
                        border = BorderStroke(1.dp, HeroGlassBorder)
                    ) {
                        Text(
                            text = "CUSTOMER PORTAL • ONLINE STOREFRONT",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyanLight,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Hello${if (userDisplayName.isNotBlank()) ", $userDisplayName" else ""}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Browse live stock at ${storeProfile?.storeName?.ifBlank { "SAB Connect Store" } ?: "SAB Connect Store"}, place orders online, and view digital GST bills.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HeroTextMuted
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onNavigateToCatalog,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Rounded.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Browse Catalog")
                        }
                        OutlinedButton(
                            onClick = onNavigateToBills,
                            border = BorderStroke(1.dp, HeroGlassBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("My Bills (${invoices.size})")
                        }
                    }
                }
            }
        }

        if (cartState.items.isNotEmpty()) {
            item {
                Card(
                    onClick = onNavigateToCatalog,
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Order Bag: ${cartState.items.sumOf { it.quantity }} item(s)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Tap to review & place online order",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ShoppingCart,
                            contentDescription = "Open order bag",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Available In Store", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onNavigateToCatalog, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("See All (${products.size})")
                }
            }
        }

        if (inStockProducts.isEmpty()) {
            item {
                EmptyStateView(
                    title = "Store Catalog Updating",
                    subtitle = "Ask store staff to add online products or check back shortly."
                )
            }
        } else {
            items(inStockProducts, key = { it.id }) { prod ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(prod.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${prod.category} • ${prod.stockQuantity} ${prod.unit} available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${formatCurrency(prod.sellingPrice, currency)} / ${prod.unit}",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        FilledTonalButton(
                            onClick = { onAddToOrderBag(prod) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Order")
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SAB Digitech Digital Services", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onNavigateToServices, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Explore")
                }
            }
        }

        item {
            SabDigitalServicesCarousel(onSelectService = onOpenSupport)
        }
    }
}

@Composable
fun CustomerCatalogScreen(
    storeProfile: StoreProfile?,
    productsState: UiState<List<Product>>,
    cartState: BillingCartState,
    onAddToCart: (Product) -> Unit,
    onUpdateCartQty: (String, Long) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onClearCart: () -> Unit,
    onPlaceOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = storeProfile?.currencySymbol ?: "₹"
    val taxRate = storeProfile?.taxRatePercent ?: 18.0
    var query by remember { mutableStateOf("") }
    val products = (productsState as? UiState.Success)?.data.orEmpty()
    val filtered = remember(products, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) products
        else products.filter {
            it.name.lowercase().contains(q) || it.category.lowercase().contains(q)
        }
    }

    val subtotal = cartState.items.sumOf { it.lineTotal }
    val taxAmount = subtotal * (taxRate / 100.0)
    val total = subtotal + taxAmount

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search catalog by product or category...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (productsState is UiState.Loading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (filtered.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Matching Items",
                        subtitle = "Try another product keyword."
                    )
                }
            } else {
                items(filtered, key = { it.id }) { product ->
                    val inBagQty = cartState.items.firstOrNull { it.product.id == product.id }?.quantity ?: 0L
                    val available = product.stockQuantity > 0L
                    val stockColor = when {
                        product.stockQuantity <= 0L -> StatusDanger
                        product.stockQuantity <= product.lowStockThreshold -> StatusWarning
                        else -> StatusSuccess
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(product.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${product.category} • ${if (available) "${product.stockQuantity} ${product.unit} in stock" else "Out of stock"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = stockColor
                                )
                                Text(
                                    text = "${formatCurrency(product.sellingPrice, currency)} / ${product.unit}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (inBagQty > 0L) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onUpdateCartQty(product.id, inBagQty - 1L) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Rounded.Remove, contentDescription = "Decrease quantity")
                                    }
                                    Text(
                                        text = "$inBagQty",
                                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily)
                                    )
                                    IconButton(
                                        onClick = { onUpdateCartQty(product.id, inBagQty + 1L) },
                                        enabled = inBagQty < product.stockQuantity,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = "Increase quantity")
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { onAddToCart(product) },
                                    enabled = available,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text(if (available) "Add" else "Sold Out")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (cartState.items.isNotEmpty()) {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
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
                        Text(
                            text = "Order Total (${cartState.items.sumOf { it.quantity }} items, incl. $taxRate% GST)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(total, currency),
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PaymentMethod.entries.forEach { method ->
                            FilterChip(
                                selected = cartState.paymentMethod == method,
                                onClick = { onPaymentMethodChange(method) },
                                label = { Text(method.label) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClearCart,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Clear")
                        }
                        Button(
                            onClick = onPlaceOrder,
                            enabled = !cartState.isSubmitting,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text(if (cartState.isSubmitting) "Placing Online Order..." else "Place Online Order")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerBillsScreen(
    storeProfile: StoreProfile?,
    invoicesState: UiState<List<Invoice>>,
    onSelectInvoice: (Invoice) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = storeProfile?.currencySymbol ?: "₹"
    val invoices = (invoicesState as? UiState.Success)?.data.orEmpty()
    var filterStatus by remember { mutableStateOf<PaymentStatus?>(null) }
    val filtered = remember(invoices, filterStatus) {
        if (filterStatus == null) invoices
        else invoices.filter { PaymentStatus.fromRaw(it.paymentStatus) == filterStatus }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterStatus == null,
                onClick = { filterStatus = null },
                label = { Text("All (${invoices.size})") }
            )
            FilterChip(
                selected = filterStatus == PaymentStatus.PAID,
                onClick = { filterStatus = PaymentStatus.PAID },
                label = { Text("Paid") }
            )
            FilterChip(
                selected = filterStatus == PaymentStatus.PENDING,
                onClick = { filterStatus = PaymentStatus.PENDING },
                label = { Text("Pending") }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Bills Yet",
                        subtitle = "Your online store orders and digital GST invoices appear here."
                    )
                }
            } else {
                items(filtered, key = { it.id }) { inv ->
                    val status = PaymentStatus.fromRaw(inv.paymentStatus)
                    val color = if (status == PaymentStatus.PAID) StatusSuccess else StatusWarning
                    Card(
                        onClick = { onSelectInvoice(inv) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = inv.invoiceNumber,
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily)
                                )
                                Text(
                                    text = "${inv.items.size} item(s) • ${formatTimestampShort(inv.createdAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = formatCurrency(inv.totalAmount, currency),
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = color.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = status.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = color,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerServicesScreen(
    onOpenSupport: (SabDigitalService?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SabNeuralHeroContainer {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = SabDigitechInfo.BRAND_NAME,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${SabDigitechInfo.PARENT_ENTITY} • Digital Marketing, Post Production, Web & Retail POS Engineering.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HeroTextMuted
                    )
                }
            }
        }

        items(SabDigitechInfo.services) { svc ->
            Card(
                onClick = { onOpenSupport(svc) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = svc.icon,
                                contentDescription = svc.title,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(svc.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            svc.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            svc.stackSummary,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Sales Enquiry & Regional Offices", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "HQ: ${SabDigitechInfo.HYDERABAD_HQ}\nRegd: ${SabDigitechInfo.NELLORE_OFFICE}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(SabDigitechInfo.HOURS, style = MaterialTheme.typography.bodySmall)
                    }
                    HorizontalDivider()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { SabDigitechInfo.dialSupport(context) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Call")
                        }
                        OutlinedButton(
                            onClick = { SabDigitechInfo.emailSupport(context, "Sales Enquiry via SAB Connect") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Enquiry")
                        }
                        Button(
                            onClick = { SabDigitechInfo.openWebsite(context) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Rounded.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Web")
                        }
                    }
                }
            }
        }
    }
}
