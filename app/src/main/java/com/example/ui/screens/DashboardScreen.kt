package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ClockAction
import com.example.data.model.ClockLog
import com.example.data.model.Employee
import com.example.data.model.Invoice
import com.example.data.model.PaymentStatus
import com.example.data.model.Product
import com.example.data.model.ShiftStatus
import com.example.data.model.StoreProfile
import com.example.data.model.UiState
import com.example.ui.components.ChartBarPoint
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LivePulsingDot
import com.example.ui.components.MetricKpiCard
import com.example.ui.components.RevenueBarChart
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import com.example.ui.components.SabDigitalService
import com.example.ui.components.SabDigitalServicesCarousel
import com.example.ui.components.SabDigitechInfo
import com.example.ui.components.SabNeuralHeroContainer
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatTimestampShort
import com.example.ui.components.formatTimestampTime
import com.example.ui.theme.CobaltAccent
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanLight
import com.example.ui.theme.HeroGlassBorder
import com.example.ui.theme.HeroGlassSurface
import com.example.ui.theme.HeroTextMuted
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardScreen(
    storeProfile: StoreProfile?,
    productsState: UiState<List<Product>>,
    invoicesState: UiState<List<Invoice>>,
    employeesState: UiState<List<Employee>>,
    clockLogsState: UiState<List<ClockLog>>,
    onNavigateToBilling: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToReports: () -> Unit,
    onSelectInvoice: (Invoice) -> Unit,
    onOpenSupport: (SabDigitalService?) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = storeProfile?.currencySymbol ?: "₹"
    val products = (productsState as? UiState.Success)?.data.orEmpty()
    val invoices = (invoicesState as? UiState.Success)?.data.orEmpty()
    val employees = (employeesState as? UiState.Success)?.data.orEmpty()
    val clockLogs = (clockLogsState as? UiState.Success)?.data.orEmpty()

    val isLoading = productsState is UiState.Loading && invoicesState is UiState.Loading

    val todayStats = remember(invoices) {
        val cal = Calendar.getInstance()
        val todayYear = cal.get(Calendar.YEAR)
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)
        val todayInvoices = invoices.filter { inv ->
            val ts = inv.createdAt?.toDate() ?: return@filter false
            cal.time = ts
            cal.get(Calendar.YEAR) == todayYear && cal.get(Calendar.DAY_OF_YEAR) == todayDay
        }
        Triple(
            todayInvoices.sumOf { it.totalAmount },
            todayInvoices.size,
            invoices.sumOf { it.totalAmount }
        )
    }

    val lowStockCount = remember(products) {
        products.count { it.stockQuantity <= it.lowStockThreshold }
    }
    val activeStaff = remember(employees) {
        employees.filter {
            ShiftStatus.fromRaw(it.shiftStatus) != ShiftStatus.CLOCKED_OUT
        }
    }

    val last7DaysChart = remember(invoices) {
        val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())
        val cal = Calendar.getInstance()
        (6 downTo 0).map { daysAgo ->
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val targetYear = cal.get(Calendar.YEAR)
            val targetDay = cal.get(Calendar.DAY_OF_YEAR)
            val label = dayFmt.format(cal.time)
            val dayRevenue = invoices.filter { inv ->
                val d = inv.createdAt?.toDate() ?: return@filter false
                val c2 = Calendar.getInstance().apply { time = d }
                c2.get(Calendar.YEAR) == targetYear && c2.get(Calendar.DAY_OF_YEAR) == targetDay
            }.sumOf { it.totalAmount }
            ChartBarPoint(label = label, value = dayRevenue)
        }
    }

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Authentic SAB Digital Technologies Neural Hero Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SabNeuralHeroContainer {
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
                                    text = "ADMIN CONSOLE • FULL ACCESS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCyanLight,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                onClick = { onOpenSupport(null) },
                                shape = CircleShape,
                                color = ElectricCyan,
                                modifier = Modifier.testTag("dashboard_help_desk_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Icon(Icons.Rounded.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("Digital Help", style = MaterialTheme.typography.labelMedium, color = Color.White)
                                }
                            }
                        }
                        Column {
                            Text(
                                text = storeProfile?.storeName?.ifBlank { "SAB Connect Workspace" } ?: "SAB Connect Workspace",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${SabDigitechInfo.BRAND_NAME} • GST ${storeProfile?.taxRatePercent ?: 18.0}% • Online Cloud",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyanLight
                            )
                        }
                        Text(
                            text = "${SabDigitechInfo.HOURS} • Hyderabad & Nellore Support Desk",
                            style = MaterialTheme.typography.bodySmall,
                            color = HeroTextMuted
                        )
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
                    title = "Today's Sales",
                    value = formatCurrency(todayStats.first, currency),
                    subtitle = "${todayStats.second} bills today",
                    icon = Icons.Rounded.AttachMoney,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToReports,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "Total Revenue",
                    value = formatCurrency(todayStats.third, currency),
                    subtitle = "${invoices.size} total invoices",
                    icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                    accentColor = CobaltAccent,
                    onClick = onNavigateToReports,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricKpiCard(
                    title = "Stock Alerts",
                    value = "$lowStockCount",
                    subtitle = "${products.size} products tracked",
                    icon = Icons.Rounded.WarningAmber,
                    accentColor = if (lowStockCount > 0) StatusWarning else StatusSuccess,
                    onClick = onNavigateToStock,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "Staff On Shift",
                    value = "${activeStaff.size}/${employees.size}",
                    subtitle = "Real-time status",
                    icon = Icons.Rounded.Badge,
                    accentColor = StatusSuccess,
                    onClick = onNavigateToTeam,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions Strip
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    QuickActionPill(
                        label = "New Invoice",
                        icon = Icons.Rounded.AddShoppingCart,
                        isPrimary = true,
                        testTag = "quick_new_invoice",
                        onClick = onNavigateToBilling
                    )
                }
                item {
                    QuickActionPill(
                        label = "Manage Stock",
                        icon = Icons.Rounded.Inventory2,
                        isPrimary = false,
                        testTag = "quick_manage_stock",
                        onClick = onNavigateToStock
                    )
                }
                item {
                    QuickActionPill(
                        label = "Clock In/Out",
                        icon = Icons.Rounded.Schedule,
                        isPrimary = false,
                        testTag = "quick_clock_staff",
                        onClick = onNavigateToTeam
                    )
                }
                item {
                    QuickActionPill(
                        label = "Sales Reports",
                        icon = Icons.Rounded.Analytics,
                        isPrimary = false,
                        testTag = "quick_sales_reports",
                        onClick = onNavigateToReports
                    )
                }
            }
        }

        // Real-Time Employee Status & Clock-In Logs Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LivePulsingDot()
                            Text(
                                text = "Live Employee Status",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        TextButton(
                            onClick = onNavigateToTeam,
                            modifier = Modifier.testTag("dashboard_view_team_button")
                        ) {
                            Text("Manage Team")
                        }
                    }

                    if (employees.isEmpty()) {
                        Text(
                            text = "No team members added yet. Add staff in the Team tab to monitor real-time clock-ins.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(employees, key = { it.id }) { emp ->
                                val status = ShiftStatus.fromRaw(emp.shiftStatus)
                                val statusColor = when (status) {
                                    ShiftStatus.CLOCKED_IN -> StatusSuccess
                                    ShiftStatus.ON_BREAK -> StatusWarning
                                    ShiftStatus.CLOCKED_OUT -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                Surface(
                                    onClick = onNavigateToTeam,
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            LivePulsingDot(color = statusColor)
                                            Text(
                                                text = emp.fullName,
                                                style = MaterialTheme.typography.labelLarge,
                                                maxLines = 1
                                            )
                                        }
                                        Text(
                                            text = "${emp.role} • ${status.label}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = statusColor
                                        )
                                    }
                                }
                            }
                        }

                        if (clockLogs.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = "Latest Clock-In Logs",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            clockLogs.take(3).forEach { log ->
                                val action = ClockAction.fromRaw(log.action)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${log.employeeName} — ${action.label}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = log.employeeRole,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = formatTimestampTime(log.loggedAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7-Day Revenue Pulse Chart
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                        Column {
                            Text(
                                text = "7-Day Sales Pulse",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Daily invoice revenue",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = onNavigateToReports) {
                            Text("Full Report")
                        }
                    }
                    RevenueBarChart(
                        points = last7DaysChart,
                        currencySymbol = currency
                    )
                }
            }
        }

        // Recent Invoices
        item {
            Text(
                text = "Recent Invoices",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (invoices.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No Sales Recorded Yet",
                    subtitle = "Add products to your catalog and generate your first customer invoice.",
                    actionLabel = "Create First Invoice",
                    onAction = onNavigateToBilling
                )
            }
        } else {
            items(invoices.take(5), key = { it.id }) { invoice ->
                val status = PaymentStatus.fromRaw(invoice.paymentStatus)
                Card(
                    onClick = { onSelectInvoice(invoice) },
                    shape = RoundedCornerShape(16.dp),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = invoice.invoiceNumber,
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (status == PaymentStatus.PAID) {
                                        StatusSuccess.copy(alpha = 0.14f)
                                    } else {
                                        StatusWarning.copy(alpha = 0.16f)
                                    }
                                ) {
                                    Text(
                                        text = status.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (status == PaymentStatus.PAID) StatusSuccess else StatusWarning,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = invoice.customerName,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatTimestampShort(invoice.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = formatCurrency(invoice.totalAmount, currency),
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // SAB Digital Technologies Client Services Hub
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAB Digitech Digital Services",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = { onOpenSupport(null) }) {
                        Icon(Icons.Rounded.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("About & Contact")
                    }
                }
                SabDigitalServicesCarousel(
                    onSelectService = { service -> onOpenSupport(service) }
                )
            }
        }
    }
}

@Composable
private fun QuickActionPill(
    label: String,
    icon: ImageVector,
    isPrimary: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    if (isPrimary) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .height(48.dp)
                .testTag(testTag)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(label)
        }
    } else {
        FilledTonalButton(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .height(48.dp)
                .testTag(testTag)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(label)
        }
    }
}
