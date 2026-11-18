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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.data.model.UiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatCurrency
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

enum class StockFilter(val label: String) {
    ALL("All"),
    IN_STOCK("In Stock"),
    LOW_STOCK("Low Stock"),
    OUT_OF_STOCK("Out of Stock")
}

@Composable
fun InventoryScreen(
    storeProfile: StoreProfile?,
    productsState: UiState<List<Product>>,
    onSaveProduct: (
        productId: String?,
        name: String,
        sku: String,
        category: String,
        unit: String,
        sellingPrice: Double,
        costPrice: Double,
        stockQuantity: Long,
        lowStockThreshold: Long
    ) -> Unit,
    onAdjustStock: (Product, Long) -> Unit,
    onSetStock: (Product, Long) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    isAdminMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    val currency = storeProfile?.currencySymbol ?: "₹"
    var activeTab by remember(isAdminMode) { mutableIntStateOf(if (isAdminMode) 0 else 1) } // 0 = Products, 1 = Stock Levels
    var searchQuery by remember { mutableStateOf("") }
    var stockFilter by remember { mutableStateOf(StockFilter.ALL) }

    var showEditor by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var restockTarget by remember { mutableStateOf<Product?>(null) }
    var deleteConfirmTarget by remember { mutableStateOf<Product?>(null) }

    val products = (productsState as? UiState.Success)?.data.orEmpty()
    val filteredProducts = remember(products, searchQuery, stockFilter) {
        val q = searchQuery.trim().lowercase()
        products.filter { p ->
            val matchesQuery = q.isEmpty() ||
                p.name.lowercase().contains(q) ||
                p.sku.lowercase().contains(q) ||
                p.category.lowercase().contains(q)
            val matchesStock = when (stockFilter) {
                StockFilter.ALL -> true
                StockFilter.IN_STOCK -> p.stockQuantity > p.lowStockThreshold
                StockFilter.LOW_STOCK -> p.stockQuantity in 1..p.lowStockThreshold
                StockFilter.OUT_OF_STOCK -> p.stockQuantity <= 0L
            }
            matchesQuery && matchesStock
        }
    }

    val totalUnits = remember(products) { products.sumOf { it.stockQuantity } }
    val inventoryCostValue = remember(products) { products.sumOf { it.costPrice * it.stockQuantity } }
    val retailValue = remember(products) { products.sumOf { it.sellingPrice * it.stockQuantity } }

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
                if (isAdminMode) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        listOf("Products Catalog", "Inventory Stock").forEachIndexed { idx, label ->
                            SegmentedButton(
                                selected = activeTab == idx,
                                onClick = { activeTab = idx },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = 2),
                                modifier = Modifier.testTag("inventory_tab_$idx")
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
                            text = "Employee Stock Check • Cost margins, pricing edits & product deletions restricted to Admin",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search product name, SKU, or category") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_search_input")
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StockFilter.entries) { filter ->
                        FilterChip(
                            selected = stockFilter == filter,
                            onClick = { stockFilter = filter },
                            label = { Text(filter.label) },
                            modifier = Modifier.testTag("stock_filter_${filter.name.lowercase()}")
                        )
                    }
                }
            }

            when (productsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(productsState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (activeTab == 1) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Units On Hand", style = MaterialTheme.typography.labelMedium)
                                            Text(
                                                "$totalUnits",
                                                style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFamily),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (isAdminMode) {
                                            Column {
                                                Text("Cost Value", style = MaterialTheme.typography.labelMedium)
                                                Text(
                                                    formatCurrency(inventoryCostValue, currency),
                                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Retail Value", style = MaterialTheme.typography.labelMedium)
                                            Text(
                                                formatCurrency(retailValue, currency),
                                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredProducts.isEmpty()) {
                            item {
                                EmptyStateView(
                                    title = if (products.isEmpty()) "No Products in Catalog" else "No Matching Products",
                                    subtitle = if (products.isEmpty()) {
                                        "Add your retail products with SKUs, prices, and low-stock alert levels."
                                    } else {
                                        "Try adjusting your search or stock filter."
                                    },
                                    actionLabel = if (products.isEmpty()) "Add Product" else null,
                                    onAction = if (products.isEmpty()) {
                                        { editingProduct = null; showEditor = true }
                                    } else null
                                )
                            }
                        } else {
                            items(filteredProducts, key = { it.id }) { product ->
                                ProductInventoryCard(
                                    product = product,
                                    currency = currency,
                                    isStockMode = activeTab == 1,
                                    isAdminMode = isAdminMode,
                                    onEdit = { editingProduct = product; showEditor = true },
                                    onDelete = { deleteConfirmTarget = product },
                                    onAdjustStock = { delta -> onAdjustStock(product, delta) },
                                    onOpenRestock = { restockTarget = product }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isAdminMode) {
            FloatingActionButton(
                onClick = { editingProduct = null; showEditor = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("add_product_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add product")
            }
        }
    }

    if (showEditor) {
        ProductEditorDialog(
            product = editingProduct,
            currency = currency,
            onDismiss = { showEditor = false },
            onSave = { name, sku, cat, unit, sell, cost, stock, threshold ->
                onSaveProduct(editingProduct?.id, name, sku, cat, unit, sell, cost, stock, threshold)
                showEditor = false
            }
        )
    }

    restockTarget?.let { target ->
        RestockDialog(
            product = target,
            onDismiss = { restockTarget = null },
            onConfirm = { newQty ->
                onSetStock(target, newQty)
                restockTarget = null
            }
        )
    }

    deleteConfirmTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteConfirmTarget = null },
            title = { Text("Delete Product?") },
            text = { Text("Remove ${target.name} (${target.sku}) from your product catalog?") },
            confirmButton = {
                Button(onClick = {
                    onDeleteProduct(target)
                    deleteConfirmTarget = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProductInventoryCard(
    product: Product,
    currency: String,
    isStockMode: Boolean,
    isAdminMode: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdjustStock: (Long) -> Unit,
    onOpenRestock: () -> Unit
) {
    val stockColor = when {
        product.stockQuantity <= 0L -> StatusDanger
        product.stockQuantity <= product.lowStockThreshold -> StatusWarning
        else -> StatusSuccess
    }
    val stockLabel = when {
        product.stockQuantity <= 0L -> "Out of Stock"
        product.stockQuantity <= product.lowStockThreshold -> "Low Stock"
        else -> "In Stock"
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Text(
                                text = product.sku,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = stockColor.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "$stockLabel • ${product.stockQuantity} ${product.unit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = stockColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sell: ${formatCurrency(product.sellingPrice, currency)} / ${product.unit}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFamily),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isAdminMode) {
                            "Cost: ${formatCurrency(product.costPrice, currency)} • Alert ≤ ${product.lowStockThreshold}"
                        } else {
                            "Low-stock alert ≤ ${product.lowStockThreshold} ${product.unit}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isStockMode) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { onAdjustStock(-1L) },
                            enabled = product.stockQuantity > 0L,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Remove, contentDescription = "Decrease stock by 1")
                        }
                        IconButton(
                            onClick = { onAdjustStock(1L) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Increase stock by 1")
                        }
                        OutlinedButton(
                            onClick = onOpenRestock,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Set")
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit ${product.name}")
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete ${product.name}",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductEditorDialog(
    product: Product?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        sku: String,
        category: String,
        unit: String,
        sellingPrice: Double,
        costPrice: Double,
        stockQuantity: Long,
        lowStockThreshold: Long
    ) -> Unit
) {
    BackHandler(onBack = onDismiss)

    var name by remember(product) { mutableStateOf(product?.name ?: "") }
    var sku by remember(product) { mutableStateOf(product?.sku ?: "") }
    var category by remember(product) { mutableStateOf(product?.category ?: "General") }
    var unit by remember(product) { mutableStateOf(product?.unit ?: "pcs") }
    var sellingPrice by remember(product) { mutableStateOf(product?.sellingPrice?.toString() ?: "") }
    var costPrice by remember(product) { mutableStateOf(product?.costPrice?.toString() ?: "0.0") }
    var stockQty by remember(product) { mutableStateOf(product?.stockQuantity?.toString() ?: "10") }
    var lowThreshold by remember(product) { mutableStateOf(product?.lowStockThreshold?.toString() ?: "5") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    label = { Text("Product Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it; errorMsg = null },
                        label = { Text("SKU / Code *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("product_sku_input")
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f)
                    )
                }
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it; errorMsg = null },
                        label = { Text("Sell ($currency) *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("product_sell_price_input")
                    )
                    OutlinedTextField(
                        value = costPrice,
                        onValueChange = { costPrice = it },
                        label = { Text("Cost ($currency)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockQty,
                        onValueChange = { stockQty = it; errorMsg = null },
                        label = { Text("Stock Qty *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("product_stock_input")
                    )
                    OutlinedTextField(
                        value = lowThreshold,
                        onValueChange = { lowThreshold = it },
                        label = { Text("Low Alert") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sell = sellingPrice.toDoubleOrNull()
                    val cost = costPrice.toDoubleOrNull() ?: 0.0
                    val stock = stockQty.toLongOrNull()
                    val threshold = lowThreshold.toLongOrNull() ?: 5L
                    when {
                        name.trim().isEmpty() -> errorMsg = "Product name is required."
                        sku.trim().isEmpty() -> errorMsg = "SKU is required."
                        sell == null || sell < 0.0 -> errorMsg = "Enter a valid selling price."
                        cost < 0.0 -> errorMsg = "Cost price cannot be negative."
                        stock == null || stock < 0L -> errorMsg = "Enter a valid stock quantity."
                        threshold < 0L -> errorMsg = "Threshold cannot be negative."
                        else -> onSave(
                            name.trim(),
                            sku.trim(),
                            category.trim().ifEmpty { "General" },
                            unit.trim().ifEmpty { "pcs" },
                            sell,
                            cost,
                            stock,
                            threshold
                        )
                    }
                },
                modifier = Modifier.testTag("save_product_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RestockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    BackHandler(onBack = onDismiss)
    var qtyText by remember(product) { mutableStateOf(product.stockQuantity.toString()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Inventory2, contentDescription = null) },
        title = { Text("Update Stock: ${product.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current stock: ${product.stockQuantity} ${product.unit}")
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it; errorMsg = null },
                    label = { Text("New Stock Quantity") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsed = qtyText.toLongOrNull()
                if (parsed == null || parsed < 0L) {
                    errorMsg = "Enter a valid non-negative integer."
                } else {
                    onConfirm(parsed)
                }
            }) {
                Text("Update Stock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
