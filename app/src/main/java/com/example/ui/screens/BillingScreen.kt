package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Customer
import com.example.data.model.Invoice
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.data.model.UiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatTimestampShort
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.BillingCartState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BillingScreen(
    storeProfile: StoreProfile?,
    productsState: UiState<List<Product>>,
    customersState: UiState<List<Customer>>,
    invoicesState: UiState<List<Invoice>>,
    cartState: BillingCartState,
    onAddToCart: (Product) -> Unit,
    onUpdateCartQty: (String, Long) -> Unit,
    onSelectCustomer: (Customer?) -> Unit,
    onDiscountChange: (Double) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onPaymentStatusChange: (PaymentStatus) -> Unit,
    onNotesChange: (String) -> Unit,
    onClearCart: () -> Unit,
    onGenerateInvoice: () -> Unit,
    onMarkInvoicePaid: (Invoice) -> Unit,
    onSelectInvoiceForReceipt: (Invoice) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = storeProfile?.currencySymbol ?: "₹"
    val taxPercent = storeProfile?.taxRatePercent ?: 18.0
    var activeTab by remember { mutableIntStateOf(0) } // 0 = New Invoice, 1 = Invoices History
    var productSearch by remember { mutableStateOf("") }
    var invoiceSearch by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<PaymentStatus?>(null) }
    var showCustomerPicker by remember { mutableStateOf(false) }

    val products = (productsState as? UiState.Success)?.data.orEmpty()
    val customers = (customersState as? UiState.Success)?.data.orEmpty()
    val invoices = (invoicesState as? UiState.Success)?.data.orEmpty()

    val filteredProducts = remember(products, productSearch) {
        val q = productSearch.trim().lowercase()
        if (q.isEmpty()) products
        else products.filter {
            it.name.lowercase().contains(q) || it.sku.lowercase().contains(q)
        }
    }

    val filteredInvoices = remember(invoices, invoiceSearch, statusFilter) {
        val q = invoiceSearch.trim().lowercase()
        invoices.filter { inv ->
            val matchesQ = q.isEmpty() ||
                inv.invoiceNumber.lowercase().contains(q) ||
                inv.customerName.lowercase().contains(q)
            val matchesStatus = statusFilter == null ||
                PaymentStatus.fromRaw(inv.paymentStatus) == statusFilter
            matchesQ && matchesStatus
        }
    }

    val subtotal = remember(cartState.items) { cartState.items.sumOf { it.lineTotal } }
    val discountAmount = subtotal * (cartState.discountPercent / 100.0)
    val taxable = (subtotal - discountAmount).coerceAtLeast(0.0)
    val taxAmount = taxable * (taxPercent / 100.0)
    val grandTotal = taxable + taxAmount

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("Create Invoice", "Invoices (${invoices.size})").forEachIndexed { idx, label ->
                    SegmentedButton(
                        selected = activeTab == idx,
                        onClick = { activeTab = idx },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = 2),
                        modifier = Modifier.testTag("billing_tab_$idx")
                    ) {
                        Text(label)
                    }
                }
            }
        }

        if (activeTab == 0) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Customer & Cart Card
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
                                Text("Customer", style = MaterialTheme.typography.titleMedium)
                                OutlinedButton(
                                    onClick = { showCustomerPicker = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("pick_customer_button")
                                ) {
                                    Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(cartState.selectedCustomer?.fullName ?: "Walk-in Customer")
                                }
                            }

                            if (cartState.items.isNotEmpty()) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Bill Items (${cartState.items.sumOf { it.quantity }})",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    TextButton(onClick = onClearCart) {
                                        Icon(Icons.Rounded.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Clear")
                                    }
                                }

                                cartState.items.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.product.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${formatCurrency(item.product.sellingPrice, currency)} × ${item.quantity}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onUpdateCartQty(item.product.id, -1L) },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(Icons.Rounded.Remove, contentDescription = "Decrease quantity")
                                            }
                                            Text(
                                                text = "${item.quantity}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily)
                                            )
                                            IconButton(
                                                onClick = { onUpdateCartQty(item.product.id, 1L) },
                                                enabled = item.quantity < item.product.stockQuantity,
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(Icons.Rounded.Add, contentDescription = "Increase quantity")
                                            }
                                            Text(
                                                text = formatCurrency(item.lineTotal, currency),
                                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                var discountInput by remember(cartState.discountPercent) {
                                    mutableStateOf(
                                        if (cartState.discountPercent == 0.0) "" else cartState.discountPercent.toString()
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = discountInput,
                                        onValueChange = { raw ->
                                            discountInput = raw
                                            onDiscountChange((raw.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0))
                                        },
                                        label = { Text("Discount %") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = cartState.notes,
                                        onValueChange = onNotesChange,
                                        label = { Text("Receipt Note") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1.5f)
                                    )
                                }

                                Text("Payment Method", style = MaterialTheme.typography.labelMedium)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PaymentMethod.entries.forEach { method ->
                                        FilterChip(
                                            selected = cartState.paymentMethod == method,
                                            onClick = { onPaymentMethodChange(method) },
                                            label = { Text(method.label) }
                                        )
                                    }
                                    PaymentStatus.entries.forEach { st ->
                                        FilterChip(
                                            selected = cartState.paymentStatus == st,
                                            onClick = { onPaymentStatusChange(st) },
                                            label = { Text("Status: ${st.label}") }
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        SummaryLine("Subtotal", formatCurrency(subtotal, currency))
                                        if (discountAmount > 0.0) {
                                            SummaryLine("Discount (${cartState.discountPercent}%)", "-${formatCurrency(discountAmount, currency)}")
                                        }
                                        SummaryLine("GST / Tax (${taxPercent}%)", formatCurrency(taxAmount, currency))
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Total Payable", style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                text = formatCurrency(grandTotal, currency),
                                                style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFamily),
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = onGenerateInvoice,
                                    enabled = !cartState.isSubmitting,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("generate_invoice_button")
                                ) {
                                    if (cartState.isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Generating...")
                                    } else {
                                        Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Generate Invoice • ${formatCurrency(grandTotal, currency)}")
                                    }
                                }
                            } else {
                                Text(
                                    text = "Tap products below to add items to this invoice.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Product picker search
                item {
                    OutlinedTextField(
                        value = productSearch,
                        onValueChange = { productSearch = it },
                        placeholder = { Text("Search catalog to add to bill...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("billing_product_search")
                    )
                }

                if (filteredProducts.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No Billable Products",
                            subtitle = "Add products in the Stock tab first to generate customer invoices."
                        )
                    }
                } else {
                    items(filteredProducts, key = { it.id }) { product ->
                        val inCartQty = cartState.items.find { it.product.id == product.id }?.quantity ?: 0L
                        val outOfStock = product.stockQuantity <= 0L
                        Card(
                            onClick = { onAddToCart(product) },
                            enabled = !outOfStock,
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bill_product_${product.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${product.sku} • ${product.stockQuantity} ${product.unit} left",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (outOfStock) StatusDanger else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (inCartQty > 0L) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = "×$inCartQty",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = formatCurrency(product.sellingPrice, currency),
                                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Invoices History Tab
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = invoiceSearch,
                        onValueChange = { invoiceSearch = it },
                        placeholder = { Text("Search invoice # or customer...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = statusFilter == null,
                                onClick = { statusFilter = null },
                                label = { Text("All") }
                            )
                        }
                        items(PaymentStatus.entries) { st ->
                            FilterChip(
                                selected = statusFilter == st,
                                onClick = { statusFilter = st },
                                label = { Text(st.label) }
                            )
                        }
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (filteredInvoices.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Invoices Found",
                                subtitle = "Generated invoices appear here with digital receipt previews and payment tracking."
                            )
                        }
                    } else {
                        items(filteredInvoices, key = { it.id }) { inv ->
                            val st = PaymentStatus.fromRaw(inv.paymentStatus)
                            val method = PaymentMethod.fromRaw(inv.paymentMethod)
                            Card(
                                onClick = { onSelectInvoiceForReceipt(inv) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = inv.invoiceNumber,
                                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (st == PaymentStatus.PAID) StatusSuccess.copy(alpha = 0.14f) else StatusWarning.copy(alpha = 0.16f)
                                            ) {
                                                Text(
                                                    text = st.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (st == PaymentStatus.PAID) StatusSuccess else StatusWarning,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = formatCurrency(inv.totalAmount, currency),
                                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(inv.customerName, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                "${inv.items.sumOf { it.quantity }} items • ${method.label} • ${formatTimestampShort(inv.createdAt)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (st == PaymentStatus.PENDING) {
                                            OutlinedButton(
                                                onClick = { onMarkInvoicePaid(inv) },
                                                modifier = Modifier.height(48.dp)
                                            ) {
                                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Mark Paid")
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

    if (showCustomerPicker) {
        BackHandler { showCustomerPicker = false }
        AlertDialog(
            onDismissRequest = { showCustomerPicker = false },
            title = { Text("Select Customer") },
            text = {
                LazyColumn(
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Surface(
                            onClick = { onSelectCustomer(null); showCustomerPicker = false },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Walk-in Customer",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                    items(customers, key = { it.id }) { customer ->
                        Surface(
                            onClick = { onSelectCustomer(customer); showCustomerPicker = false },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(customer.fullName, style = MaterialTheme.typography.titleSmall)
                                if (customer.phone.isNotBlank()) {
                                    Text(
                                        customer.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomerPicker = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily))
    }
}

@Composable
fun InvoiceReceiptDialog(
    invoice: Invoice,
    storeProfile: StoreProfile?,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    val context = LocalContext.current
    val currency = storeProfile?.currencySymbol ?: "₹"
    val storeName = storeProfile?.storeName?.ifBlank { "SAB Connect Retail" } ?: "SAB Connect Retail"

    val receiptShareText = remember(invoice, storeProfile) {
        buildString {
            appendLine("=== $storeName ===")
            appendLine("Invoice: ${invoice.invoiceNumber}")
            appendLine("Date: ${formatTimestampShort(invoice.createdAt)}")
            appendLine("Customer: ${invoice.customerName}")
            appendLine("--------------------------------")
            invoice.items.forEach { item ->
                appendLine("${item.productName} x${item.quantity}  ${formatCurrency(item.lineTotal, currency)}")
            }
            appendLine("--------------------------------")
            appendLine("Subtotal: ${formatCurrency(invoice.subtotal, currency)}")
            if (invoice.discountAmount > 0.0) {
                appendLine("Discount (${invoice.discountPercent}%): -${formatCurrency(invoice.discountAmount, currency)}")
            }
            appendLine("GST / Tax (${invoice.taxPercent}%): ${formatCurrency(invoice.taxAmount, currency)}")
            appendLine("TOTAL: ${formatCurrency(invoice.totalAmount, currency)}")
            appendLine("Payment: ${PaymentMethod.fromRaw(invoice.paymentMethod).label} (${PaymentStatus.fromRaw(invoice.paymentStatus).label})")
            appendLine("Powered by SAB Connect • SAB Digital Technologies (sabdigitech.com)")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.invoiceNumber,
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFamily)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = PaymentStatus.fromRaw(invoice.paymentStatus).label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(storeName, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Customer: ${invoice.customerName}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Date: ${formatTimestampShort(invoice.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                invoice.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.productName} ×${item.quantity}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatCurrency(item.lineTotal, currency),
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily)
                        )
                    }
                }
                HorizontalDivider()
                SummaryLine("Subtotal", formatCurrency(invoice.subtotal, currency))
                if (invoice.discountAmount > 0.0) {
                    SummaryLine("Discount", "-${formatCurrency(invoice.discountAmount, currency)}")
                }
                SummaryLine("GST / Tax (${invoice.taxPercent}%)", formatCurrency(invoice.taxAmount, currency))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = formatCurrency(invoice.totalAmount, currency),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (invoice.notes.isNotBlank()) {
                    Text(
                        text = "Note: ${invoice.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Invoice ${invoice.invoiceNumber} - $storeName")
                        putExtra(Intent.EXTRA_TEXT, receiptShareText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Invoice Receipt"))
                }
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
