package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppRole
import com.example.data.model.SyncMetadataState
import com.example.ui.theme.DeepNavyWave
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanLight
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * Custom geometric S monogram emblem matching the SAB Connect UI Showcase.
 */
@Composable
fun SabSMonogramLogo(
    tint: Color = RoyalBluePrimary,
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = w * 0.17f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Upper ribbon curve of stylized "S"
        val topPath = Path().apply {
            moveTo(w * 0.84f, h * 0.22f)
            lineTo(w * 0.36f, h * 0.22f)
            cubicTo(w * 0.16f, h * 0.22f, w * 0.14f, h * 0.48f, w * 0.34f, h * 0.48f)
            lineTo(w * 0.64f, h * 0.48f)
        }
        drawPath(path = topPath, color = tint, style = stroke)

        // Lower ribbon curve of stylized "S"
        val bottomPath = Path().apply {
            moveTo(w * 0.16f, h * 0.78f)
            lineTo(w * 0.64f, h * 0.78f)
            cubicTo(w * 0.84f, h * 0.78f, w * 0.86f, h * 0.52f, w * 0.66f, h * 0.52f)
            lineTo(w * 0.36f, h * 0.52f)
        }
        drawPath(path = bottomPath, color = tint, style = stroke)
    }
}

@Composable
fun SabShowcaseLogoLockup(
    tint: Color = RoyalBluePrimary,
    textColor: Color = MidnightNavy,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        SabSMonogramLogo(tint = tint, size = 34.dp)
        Spacer(Modifier.width(10.dp))
        Text(
            text = "SAB CONNECT",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            color = textColor
        )
    }
}

@Composable
fun SabWaveSplashScreen(
    onFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1150L)
        onFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightNavy, DeepNavyWave, MidnightNavy)
                )
            )
            .clickable { onFinished() }
    ) {
        // Flowing cyan digital light waves matching the showcase Splash screen
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ElectricCyan.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.42f),
                    radius = w * 0.75f
                )
            )

            for (lineIdx in 0..7) {
                val path = Path()
                val baseY = h * (0.70f + lineIdx * 0.022f)
                val amp = 18.dp.toPx() + lineIdx * 3.dp.toPx()
                val steps = 48
                for (i in 0..steps) {
                    val t = i / steps.toFloat()
                    val x = w * t
                    val y = baseY + sin(t * 5.2f + phase + lineIdx * 0.38f) * amp
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = ElectricCyanLight.copy(alpha = (0.45f - lineIdx * 0.045f).coerceAtLeast(0.08f)),
                    style = Stroke(width = (2.2f - lineIdx * 0.16f).dp.toPx())
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SabSMonogramLogo(tint = Color.White, size = 76.dp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "SAB CONNECT",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Business. Technology. Connection.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFBBD7FA),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SabCitySkylineBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val backSkyline = Color(0xFFE7EFFA)
        val frontSkyline = Color(0xFFDCE7F7)

        val backHeights = listOf(0.55f, 0.35f, 0.75f, 0.45f, 0.85f, 0.40f, 0.65f, 0.90f, 0.50f, 0.70f, 0.35f, 0.60f)
        val slotW = w / backHeights.size
        backHeights.forEachIndexed { idx, ratio ->
            val bh = h * ratio
            drawRect(
                color = backSkyline,
                topLeft = Offset(idx * slotW, h - bh),
                size = Size(slotW + 1f, bh)
            )
        }

        val frontHeights = listOf(0.30f, 0.62f, 0.42f, 0.58f, 0.28f, 0.72f, 0.38f, 0.52f, 0.68f, 0.36f)
        val fSlotW = w / frontHeights.size
        frontHeights.forEachIndexed { idx, ratio ->
            val bh = h * ratio
            drawRect(
                color = frontSkyline,
                topLeft = Offset(idx * fSlotW + fSlotW * 0.1f, h - bh),
                size = Size(fSlotW * 0.84f, bh)
            )
        }
    }
}

@Composable
fun SabShowcaseTopBar(
    activeRole: AppRole,
    syncState: SyncMetadataState,
    unreadCount: Int,
    onNotificationClick: () -> Unit,
    onSwitchRole: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    SabSMonogramLogo(tint = RoyalBluePrimary, size = 28.dp)
                    Column {
                        Text(
                            text = "SAB CONNECT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MidnightNavy,
                            letterSpacing = 0.6.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (syncState.isFromCache) ElectricCyan else StatusSuccess)
                            )
                            Text(
                                text = when {
                                    syncState.hasPendingWrites -> "Syncing live..."
                                    syncState.isFromCache -> "Connecting cloud..."
                                    else -> "Online • ${activeRole.label}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = onSwitchRole,
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.testTag("switch_role_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = activeRole.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = RoyalBluePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Rounded.SwapHoriz,
                                contentDescription = "Switch role",
                                tint = RoyalBluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.testTag("notification_bell_button")
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications & Updates",
                                tint = MidnightNavy,
                                modifier = Modifier.size(25.dp)
                            )
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-1).dp, y = 1.dp)
                                        .clip(CircleShape)
                                        .background(StatusDanger)
                                )
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE9EEF5))
            )
        }
    }
}

@Composable
fun PastelStatCard(
    label: String,
    value: String,
    containerColor: Color,
    valueColor: Color = MidnightNavy,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF334155),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ShowcaseMenuRowCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = RoyalBluePrimary,
    iconBg: Color = Color(0xFFEBF3FE),
    badgeText: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconBg,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MidnightNavy
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!badgeText.isNullOrBlank()) {
                Surface(
                    shape = CircleShape,
                    color = iconBg
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = iconTint,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Open $title",
                tint = Color(0xFF64748B)
            )
        }
    }
}
