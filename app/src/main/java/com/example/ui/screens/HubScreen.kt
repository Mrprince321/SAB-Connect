package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import com.example.data.model.Customer
import com.example.data.model.Invoice
import com.example.data.model.PaymentMethod
import com.example.data.model.StoreProfile
import com.example.data.model.UiState
import com.example.ui.components.ChartBarPoint
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricKpiCard
import com.example.ui.components.RevenueBarChart
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatTimestampShort
import com.example.ui.theme.CobaltAccent
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusSuccess
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HubScreen(
    storeProfile: StoreProfile?,
    invoicesState: UiState<List<Invoice>>,
    customersState: UiState<List<Customer>>,
    onSaveCustomer: (
        customerId: String?,
        fullName: String,
        phone: String,
        email: String,
        address: String,
        notes: String
    ) -> Unit,
    onDeleteCustomer: (Customer) -> Unit,
    onSelectInvoice: (Invoice) -> Unit,
    isAdminMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    val currency = storeProfile?.currencySymbol ?: "₹"
    var activeTab by remember(isAdminMode) { mutableIntStateOf(if (isAdminMode) 0 else 1) } // 0 = Reports, 1 = Customers
    var reportPeriod by remember { mutableIntStateOf(0) } // 0 = Daily (7d), 1 = Monthly (6m)
    var customerSearch by remember { mutableStateOf("") }

    var showCustomerEditor by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerDetailTarget by remember { mutableStateOf<Customer?>(null) }
    var deleteCustomerTarget by remember { mutableStateOf<Customer?>(null) }

    val invoices = (invoicesState as? UiState.Success)?.data.orEmpty()
    val customers = (customersState as? UiState.Success)?.data.orEmpty()

    val filteredCustomers = remember(customers, customerSearch) {
        val q = customerSearch.trim().lowercase()
        if (q.isEmpty()) customers
        else customers.filter {
            it.fullName.lowercase().contains(q) ||
                it.phone.lowercase().contains(q) ||
                it.email.lowercase().contains(q)
        }
    }

    val totalRevenue = remember(invoices) { invoices.sumOf { it.totalAmount } }
    val totalProfit = remember(invoices) { invoices.sumOf { it.estimatedProfit } }
    val avgTicket = if (invoices.isEmpty()) 0.0 else totalRevenue / invoices.size

    val dailyPoints = remember(invoices) {
        val dayFmt = SimpleDateFormat("MMM d", Locale.getDefault())
        val cal = Calendar.getInstance()
        (6 downTo 0).map { daysAgo ->
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val y = cal.get(Calendar.YEAR)
            val d = cal.get(Calendar.DAY_OF_YEAR)
            val rev = invoices.filter { inv ->
                val dt = inv.createdAt?.toDate() ?: return@filter false
                val c = Calendar.getInstance().apply { time = dt }
                c.get(Calendar.YEAR) == y && c.get(Calendar.DAY_OF_YEAR) == d
            }.sumOf { it.totalAmount }
            ChartBarPoint(dayFmt.format(cal.time), rev)
        }
    }

    val monthlyPoints = remember(invoices) {
        val monthFmt = SimpleDateFormat("MMM", Locale.getDefault())
        val cal = Calendar.getInstance()
        (5 downTo 0).map { monthsAgo ->
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.MONTH, -monthsAgo)
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val rev = invoices.filter { inv ->
                val dt = inv.createdAt?.toDate() ?: return@filter false
                val c = Calendar.getInstance().apply { time = dt }
                c.get(Calendar.YEAR) == y && c.get(Calendar.MONTH) == m
            }.sumOf { it.totalAmount }
            ChartBarPoint(monthFmt.format(cal.time), rev)
        }
    }

    val topProducts = remember(invoices) {
        invoices.flatMap { it.items }
            .groupBy { it.productName }
            .map { (name, lines) ->
                Triple(name, lines.sumOf { it.quantity }, lines.sumOf { it.lineTotal })
            }
            .sortedByDescending { it.third }
            .take(5)
    }

    val paymentBreakdown = remember(invoices, totalRevenue) {
        PaymentMethod.entries.map { method ->
            val sum = invoices.filter { PaymentMethod.fromRaw(it.paymentMethod) == method }
                .sumOf { it.totalAmount }
            val ratio = if (totalRevenue > 0.0) (sum / totalRevenue).toFloat() else 0f
            Triple(method, sum, ratio)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .align(Alignment.TopCenter)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                if (isAdminMode) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        listOf("Sales Reports", "Customers (${customers.size})").forEachIndexed { idx, label ->
                            SegmentedButton(
                                selected = activeTab == idx,
                                onClick = { activeTab = idx },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = 2),
                                modifier = Modifier.testTag("hub_tab_$idx")
                            ) {
                                Text(label)
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Employee Customer Desk • Add or update customers (Financial reports & deletions restricted to Admin)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (activeTab == 0) {
                // Sales Reports Tab
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            listOf("Daily (7 Days)", "Monthly (6 Months)").forEachIndexed { idx, label ->
                                SegmentedButton(
                                    selected = reportPeriod == idx,
                                    onClick = { reportPeriod = idx },
                                    shape = SegmentedButtonDefaults.itemShape(index = idx, count = 2),
                                    modifier = Modifier.testTag("report_period_$idx")
                                ) {
                                    Text(label)
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
                                title = "Gross Revenue",
                                value = formatCurrency(totalRevenue, currency),
                                subtitle = "${invoices.size} total bills",
                                icon = Icons.Rounded.AttachMoney,
                                accentColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            MetricKpiCard(
                                title = "Est. Net Profit",
                                value = formatCurrency(totalProfit, currency),
                                subtitle = "Avg ${formatCurrency(avgTicket, currency)}/bill",
                                icon = Icons.AutoMirrored.Rounded.TrendingUp,
                                accentColor = StatusSuccess,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = if (reportPeriod == 0) "Daily Sales Performance" else "Monthly Sales Performance",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                RevenueBarChart(
                                    points = if (reportPeriod == 0) dailyPoints else monthlyPoints,
                                    currencySymbol = currency
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Payment Methods", style = MaterialTheme.typography.titleMedium)
                                paymentBreakdown.forEach { (method, sum, ratio) ->
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(method.label, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                text = formatCurrency(sum, currency),
                                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily)
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = { ratio },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp),
                                            color = CobaltAccent,
                                            trackColor = MaterialTheme.colorScheme.surfaceContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Top Selling Products", style = MaterialTheme.typography.titleMedium)
                                if (topProducts.isEmpty()) {
                                    Text(
                                        text = "Generate invoices to see top product analytics.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    topProducts.forEachIndexed { idx, (name, qty, rev) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    "${idx + 1}. $name",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    "$qty units sold",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = formatCurrency(rev, currency),
                                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        if (idx < topProducts.lastIndex) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Customers Directory Tab
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = customerSearch,
                        onValueChange = { customerSearch = it },
                        placeholder = { Text("Search customer name, phone, or email...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    when (customersState) {
                        is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is UiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text(customersState.message, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        is UiState.Success -> {
                            LazyColumn(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (filteredCustomers.isEmpty()) {
                                    item {
                                        EmptyStateView(
                                            title = if (customers.isEmpty()) "No Customers Yet" else "No Matching Customers",
                                            subtitle = "Save customer details to track order history and total lifetime spend.",
                                            actionLabel = if (customers.isEmpty()) "Add Customer" else null,
                                            onAction = if (customers.isEmpty()) {
                                                { editingCustomer = null; showCustomerEditor = true }
                                            } else null
                                        )
                                    }
                                } else {
                                    items(filteredCustomers, key = { it.id }) { cust ->
                                        Card(
                                            onClick = { customerDetailTarget = cust },
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
                                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                    Text(cust.fullName, style = MaterialTheme.typography.titleMedium)
                                                    val contactLine = listOf(cust.phone, cust.email)
                                                        .filter { it.isNotBlank() }
                                                        .joinToString(" • ")
                                                    if (contactLine.isNotBlank()) {
                                                        Text(
                                                            text = contactLine,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    Text(
                                                        text = "${cust.totalOrders} orders • Spent ${formatCurrency(cust.totalSpent, currency)}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Row {
                                                    IconButton(
                                                        onClick = { editingCustomer = cust; showCustomerEditor = true },
                                                        modifier = Modifier.size(48.dp)
                                                    ) {
                                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit ${cust.fullName}")
                                                    }
                                                    if (isAdminMode) {
                                                        IconButton(
                                                            onClick = { deleteCustomerTarget = cust },
                                                            modifier = Modifier.size(48.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Rounded.DeleteOutline,
                                                                contentDescription = "Delete ${cust.fullName}",
                                                                tint = MaterialTheme.colorScheme.error
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
                    }
                }
            }
        }

        if (activeTab == 1) {
            FloatingActionButton(
                onClick = { editingCustomer = null; showCustomerEditor = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("add_customer_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add customer")
            }
        }
    }

    if (showCustomerEditor) {
        CustomerEditorDialog(
            customer = editingCustomer,
            onDismiss = { showCustomerEditor = false },
            onSave = { name, phone, email, address, notes ->
                onSaveCustomer(editingCustomer?.id, name, phone, email, address, notes)
                showCustomerEditor = false
            }
        )
    }

    customerDetailTarget?.let { customer ->
        CustomerDetailDialog(
            customer = customer,
            invoices = invoices.filter { it.customerId == customer.id },
            currency = currency,
            onDismiss = { customerDetailTarget = null },
            onSelectInvoice = { inv ->
                customerDetailTarget = null
                onSelectInvoice(inv)
            }
        )
    }

    deleteCustomerTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteCustomerTarget = null },
            title = { Text("Delete Customer?") },
            text = { Text("Delete ${target.fullName} from customer records?") },
            confirmButton = {
                Button(onClick = {
                    onDeleteCustomer(target)
                    deleteCustomerTarget = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteCustomerTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CustomerEditorDialog(
    customer: Customer?,
    onDismiss: () -> Unit,
    onSave: (fullName: String, phone: String, email: String, address: String, notes: String) -> Unit
) {
    BackHandler(onBack = onDismiss)
    var fullName by remember(customer) { mutableStateOf(customer?.fullName ?: "") }
    var phone by remember(customer) { mutableStateOf(customer?.phone ?: "") }
    var email by remember(customer) { mutableStateOf(customer?.email ?: "") }
    var address by remember(customer) { mutableStateOf(customer?.address ?: "") }
    var notes by remember(customer) { mutableStateOf(customer?.notes ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (customer == null) "Add Customer" else "Edit Customer") },
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
                    modifier = Modifier.fillMaxWidth().testTag("customer_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("customer_phone_input")
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Preferences") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.trim().isEmpty()) {
                        errorMsg = "Customer name is required."
                    } else {
                        onSave(fullName.trim(), phone.trim(), email.trim(), address.trim(), notes.trim())
                    }
                },
                modifier = Modifier.testTag("save_customer_button")
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CustomerDetailDialog(
    customer: Customer,
    invoices: List<Invoice>,
    currency: String,
    onDismiss: () -> Unit,
    onSelectInvoice: (Invoice) -> Unit
) {
    BackHandler(onBack = onDismiss)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(customer.fullName) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (customer.phone.isNotBlank()) Text("Phone: ${customer.phone}")
                if (customer.email.isNotBlank()) Text("Email: ${customer.email}")
                if (customer.address.isNotBlank()) Text("Address: ${customer.address}")
                if (customer.notes.isNotBlank()) Text("Notes: ${customer.notes}", style = MaterialTheme.typography.bodySmall)
                HorizontalDivider()
                Text(
                    "Purchase History (${invoices.size})",
                    style = MaterialTheme.typography.titleSmall
                )
                if (invoices.isEmpty()) {
                    Text(
                        "No invoices linked to this customer yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    invoices.take(8).forEach { inv ->
                        Surface(
                            onClick = { onSelectInvoice(inv) },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        inv.invoiceNumber,
                                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily)
                                    )
                                    Text(
                                        formatTimestampShort(inv.createdAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    formatCurrency(inv.totalAmount, currency),
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                    color = MaterialTheme.colorScheme.primary
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
