package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppRole
import com.example.data.model.StoreProfile
import com.example.data.model.SyncMetadataState
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanLight
import com.example.ui.theme.HeaderActivePill
import com.example.ui.theme.HeroGlassBorder
import com.example.ui.theme.HeroGlassSurface
import com.example.ui.theme.HeroTextMuted
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.SapphirePrimary
import com.example.ui.theme.SpaceNavy
import com.example.ui.theme.SpaceNavyElevated
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.google.firebase.Timestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

fun formatCurrency(amount: Double, currencySymbol: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val prefix = if (amount < 0) "-" else ""
    return "$prefix$currencySymbol${formatter.format(kotlin.math.abs(amount))}"
}

fun formatTimestampTime(timestamp: Timestamp?): String {
    val date = (timestamp ?: Timestamp.now()).toDate()
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
}

fun formatTimestampShort(timestamp: Timestamp?): String {
    val date = (timestamp ?: Timestamp.now()).toDate()
    return SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()).format(date)
}

fun formatTimestampDate(timestamp: Timestamp?): String {
    val date = (timestamp ?: Timestamp.now()).toDate()
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
}

@Composable
fun SabUtilityContactStrip(modifier: Modifier = Modifier) {
    Surface(
        color = MidnightNavy,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "A Unit of SAB Creations",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "+91 81253 99937 • sab@sabdigitech.com",
                style = MaterialTheme.typography.labelSmall,
                color = HeroTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SabLogoTile(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = ElectricCyan,
        modifier = modifier.size(width = 48.dp, height = 38.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 3.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SAB",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                lineHeight = 14.sp
            )
            Text(
                text = "DIGITECH",
                color = Color.White.copy(alpha = 0.92f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 6.5.sp,
                lineHeight = 8.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SabNeuralHeroContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        SpaceNavy,
                        SpaceNavyElevated,
                        SpaceNavy
                    )
                )
            )
            .drawBehind {
                // Soft electric blue constellation glow (matching sabdigitech.com)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricCyan.copy(alpha = 0.24f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.82f, size.height * 0.45f),
                        radius = size.width * 0.55f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SapphirePrimary.copy(alpha = 0.28f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.85f),
                        radius = size.width * 0.48f
                    )
                )

                // Constellation nodes & glowing edges
                val nodes = listOf(
                    Offset(size.width * 0.62f, size.height * 0.14f),
                    Offset(size.width * 0.78f, size.height * 0.08f),
                    Offset(size.width * 0.93f, size.height * 0.22f),
                    Offset(size.width * 0.72f, size.height * 0.34f),
                    Offset(size.width * 0.88f, size.height * 0.46f),
                    Offset(size.width * 0.64f, size.height * 0.62f),
                    Offset(size.width * 0.81f, size.height * 0.72f),
                    Offset(size.width * 0.95f, size.height * 0.68f),
                    Offset(size.width * 0.53f, size.height * 0.84f),
                    Offset(size.width * 0.74f, size.height * 0.90f),
                    Offset(size.width * 0.90f, size.height * 0.92f)
                )
                val edges = listOf(
                    0 to 1, 1 to 2, 0 to 3, 1 to 3, 2 to 4, 3 to 4,
                    3 to 5, 4 to 6, 4 to 7, 5 to 6, 6 to 7,
                    5 to 8, 5 to 9, 6 to 9, 6 to 10, 7 to 10, 8 to 9, 9 to 10
                )
                edges.forEach { (a, b) ->
                    drawLine(
                        color = ElectricCyanLight.copy(alpha = 0.28f),
                        start = nodes[a],
                        end = nodes[b],
                        strokeWidth = 1.4.dp.toPx()
                    )
                }
                nodes.forEachIndexed { idx, node ->
                    drawCircle(
                        color = ElectricCyan.copy(alpha = 0.28f),
                        radius = (if (idx % 3 == 0) 7.dp else 5.dp).toPx(),
                        center = node
                    )
                    drawCircle(
                        color = ElectricCyanLight.copy(alpha = 0.88f),
                        radius = (if (idx % 3 == 0) 3.dp else 2.dp).toPx(),
                        center = node
                    )
                }
            },
        content = content
    )
}

@Composable
fun SabConnetTopBar(
    storeName: String,
    activeRole: AppRole?,
    syncState: SyncMetadataState,
    onSwitchRole: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SabLogoTile()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = storeName.ifBlank { "SAB Connect" },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    SyncStatusPill(syncState = syncState, onDarkHeader = true)
                }

                if (activeRole != null) {
                    Surface(
                        onClick = onSwitchRole,
                        shape = CircleShape,
                        color = HeaderActivePill,
                        border = BorderStroke(1.dp, HeroGlassBorder),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("switch_role_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = activeRole.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Rounded.SwapHoriz,
                                contentDescription = "Switch role",
                                tint = ElectricCyanLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onOpenSupport,
                    modifier = Modifier.testTag("sab_support_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SupportAgent,
                        contentDescription = "SAB Digital Technologies Help Desk",
                        tint = Color.White
                    )
                }

                if (activeRole == AppRole.ADMIN) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("store_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Store settings",
                            tint = Color.White
                        )
                    }
                }

                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier.testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = "Sign out",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SyncStatusPill(
    syncState: SyncMetadataState,
    onDarkHeader: Boolean = false
) {
    val (label, icon, tint) = when {
        syncState.hasPendingWrites -> Triple(
            "Syncing...",
            Icons.Rounded.CloudUpload,
            if (onDarkHeader) Color(0xFFFDE68A) else StatusWarning
        )
        syncState.isFromCache -> Triple(
            "Connecting...",
            Icons.Rounded.CloudOff,
            if (onDarkHeader) HeroTextMuted else MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> Triple(
            "Online • Live",
            Icons.Rounded.CloudDone,
            if (onDarkHeader) Color(0xFF6EE7B7) else StatusSuccess
        )
    }
    val animatedTint by animateColorAsState(targetValue = tint, label = "sync_tint")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = animatedTint,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = animatedTint
        )
    }
}

@Composable
fun LivePulsingDot(color: Color = StatusSuccess) {
    val transition = rememberInfiniteTransition(label = "live_pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    Box(
        modifier = Modifier
            .size(9.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
fun MetricKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val cardContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.14f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFamily),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = modifier
        ) { cardContent() }
    } else {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = modifier
        ) { cardContent() }
    }
}

data class ChartBarPoint(
    val label: String,
    val value: Double
)

@Composable
fun RevenueBarChart(
    points: List<ChartBarPoint>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val maxVal = (points.maxOfOrNull { it.value } ?: 0.0).coerceAtLeast(1.0)
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.primaryContainer
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
        ) {
            val barCount = points.size.coerceAtLeast(1)
            val totalWidth = size.width
            val chartHeight = size.height

            // Subtle horizontal guide lines
            for (i in 0..3) {
                val y = chartHeight * (i / 3f)
                drawLine(
                    color = gridColor.copy(alpha = 0.55f),
                    start = Offset(0f, y),
                    end = Offset(totalWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val slotWidth = totalWidth / barCount
            val barWidth = (slotWidth * 0.54f).coerceAtMost(36.dp.toPx())

            points.forEachIndexed { index, point ->
                val ratio = (point.value / maxVal).toFloat().coerceIn(0f, 1f)
                val barHeight = (chartHeight - 10.dp.toPx()) * ratio
                val left = index * slotWidth + (slotWidth - barWidth) / 2f
                val top = chartHeight - barHeight

                // Track background
                drawRoundRect(
                    color = secondaryColor.copy(alpha = 0.25f),
                    topLeft = Offset(left, 6.dp.toPx()),
                    size = Size(barWidth, chartHeight - 6.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                if (barHeight > 2f) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor, primaryColor.copy(alpha = 0.72f))
                        ),
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { point ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = if (point.value >= 1000) {
                            "$currencySymbol${String.format(Locale.US, "%.1fk", point.value / 1000.0)}"
                        } else {
                            "$currencySymbol${point.value.toInt()}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_empty_shelf),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(140.dp, 105.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("empty_state_action_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun StoreProfileDialog(
    existingProfile: StoreProfile?,
    defaultEmail: String,
    defaultOwnerName: String,
    isFirstTimeSetup: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        storeName: String,
        ownerName: String,
        email: String,
        phone: String,
        address: String,
        currencySymbol: String,
        taxRatePercent: Double
    ) -> Unit
) {
    if (!isFirstTimeSetup) {
        BackHandler(onBack = onDismiss)
    }

    var storeName by remember(existingProfile) { mutableStateOf(existingProfile?.storeName ?: "") }
    var ownerName by remember(existingProfile) {
        mutableStateOf(existingProfile?.ownerName ?: defaultOwnerName.ifBlank { "Store Owner" })
    }
    var email by remember(existingProfile) { mutableStateOf(existingProfile?.email ?: defaultEmail) }
    var phone by remember(existingProfile) { mutableStateOf(existingProfile?.phone ?: "") }
    var address by remember(existingProfile) { mutableStateOf(existingProfile?.address ?: "") }
    var currencySymbol by remember(existingProfile) { mutableStateOf(existingProfile?.currencySymbol ?: "₹") }
    var taxRateText by remember(existingProfile) {
        mutableStateOf((existingProfile?.taxRatePercent ?: 18.0).toString())
    }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isFirstTimeSetup) onDismiss() },
        title = {
            Text(
                text = if (isFirstTimeSetup) "Register Your SAB Connect Store" else "Store Profile & GST Settings",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isFirstTimeSetup) {
                        "Set up your SAB Digital Technologies POS workspace for GST billing, inventory, and live staff logs."
                    } else {
                        "Update business details & GST rate printed on customer invoices."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it; validationError = null },
                    label = { Text("Store / Business Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("store_name_input")
                )
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it; validationError = null },
                    label = { Text("Owner / Manager Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("owner_name_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currencySymbol,
                        onValueChange = { if (it.length <= 5) currencySymbol = it },
                        label = { Text("Currency") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("currency_input")
                    )
                    OutlinedTextField(
                        value = taxRateText,
                        onValueChange = { taxRateText = it },
                        label = { Text("GST / Tax %") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tax_rate_input")
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("₹", "$", "€").forEach { sym ->
                        FilterChip(
                            selected = currencySymbol == sym,
                            onClick = { currencySymbol = sym },
                            label = { Text(sym) }
                        )
                    }
                    listOf(0.0, 5.0, 12.0, 18.0).forEach { gst ->
                        FilterChip(
                            selected = taxRateText.toDoubleOrNull() == gst,
                            onClick = { taxRateText = gst.toString() },
                            label = { Text("${gst.toInt()}%") }
                        )
                    }
                }
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Store Phone") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Business Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Store Address") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tax = taxRateText.toDoubleOrNull()
                    when {
                        storeName.trim().isEmpty() -> validationError = "Please enter your store name."
                        ownerName.trim().isEmpty() -> validationError = "Please enter the owner name."
                        tax == null || tax < 0.0 || tax > 50.0 -> validationError = "Tax rate must be between 0% and 50%."
                        else -> onSave(
                            storeName.trim(),
                            ownerName.trim(),
                            email.trim(),
                            phone.trim(),
                            address.trim(),
                            currencySymbol.trim().ifEmpty { "$" },
                            tax
                        )
                    }
                },
                modifier = Modifier.testTag("save_store_profile_button")
            ) {
                Text(if (isFirstTimeSetup) "Complete Setup" else "Save Changes")
            }
        },
        dismissButton = {
            if (!isFirstTimeSetup) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
