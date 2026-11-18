package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Web
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class SabDigitalService(
    val title: String,
    val subtitle: String,
    val stackSummary: String,
    val icon: ImageVector
)

object SabDigitechInfo {
    const val BRAND_NAME = "SAB Digital Technologies"
    const val SHORT_BRAND = "SAB Digitech"
    const val PARENT_ENTITY = "A Strategic Unit of SAB Creations"
    const val WEB_URL = "https://sabdigitech.com/"
    const val EMAIL = "sab@sabdigitech.com"
    const val PHONE_PRIMARY = "+918125399937"
    const val PHONE_DISPLAY = "+91 81253 99937 / +91 91600 01922"
    const val HOURS = "Mon – Sat • 9:30 AM – 6:30 PM"
    const val HYDERABAD_HQ =
        "2nd & 3rd Floor, 8-3-318/6/5/2, Yousufguda Main Rd, Opp. Metro Pillar 1490, Madhura Nagar, Hyderabad 500045"
    const val NELLORE_OFFICE =
        "26-3-506, 3rd Street, Jyothi Nagar, Vedayapalem, Nellore 524004, AP"

    val deliverySteps = listOf(
        "1. Requirement Analysis",
        "2. UI/UX & Architecture",
        "3. Agile Engineering",
        "4. Quality Assurance",
        "5. Deploy & 1-Yr Care"
    )

    val services = listOf(
        SabDigitalService(
            title = "Web & E-Commerce",
            subtitle = "Responsive websites, corporate portals & WooCommerce stores",
            stackSummary = "React • WordPress • PHP/MySQL • SSL",
            icon = Icons.Rounded.Web
        ),
        SabDigitalService(
            title = "Mobile App Engineering",
            subtitle = "Cross-platform Android & iOS apps with REST API sync",
            stackSummary = "Android • iOS • React Native • APIs",
            icon = Icons.Rounded.PhoneAndroid
        ),
        SabDigitalService(
            title = "SAB Billing & POS Suite",
            subtitle = "GST accounting, inventory control & thermal receipt billing",
            stackSummary = "GST Engine • Inventory • Cloud POS",
            icon = Icons.Rounded.PointOfSale
        ),
        SabDigitalService(
            title = "SEO & Digital Growth",
            subtitle = "Technical & Local SEO, Google Business, Ads & WhatsApp API",
            stackSummary = "GA4 • GSC • Meta/Google Ads • IVR",
            icon = Icons.Rounded.Campaign
        ),
        SabDigitalService(
            title = "Creative & OTT Media",
            subtitle = "4K video editing, VFX, dubbing, reels & vertical OTT workflows",
            stackSummary = "AI Media • DaVinci • Premiere • VFX",
            icon = Icons.Rounded.Movie
        )
    )

    fun openWebsite(context: Context) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WEB_URL)))
        }
    }

    fun dialSupport(context: Context) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$PHONE_PRIMARY")))
        }
    }

    fun emailSupport(context: Context, subject: String = "SAB Connect Digital Support Inquiry") {
        runCatching {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$EMAIL")
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
            context.startActivity(intent)
        }
    }
}

@Composable
fun SabDigitalServicesCarousel(
    onSelectService: (SabDigitalService) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(SabDigitechInfo.services) { service ->
            Card(
                onClick = { onSelectService(service) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.width(220.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = service.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = service.title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = service.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Text(
                            text = service.stackSummary,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SabDigitechAboutDialog(
    initialService: SabDigitalService? = null,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(SabDigitechInfo.BRAND_NAME, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = SabDigitechInfo.PARENT_ENTITY,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (initialService != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Selected Service: ${initialService.title}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = initialService.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Text(
                    text = "Full-stack digital partner helping startups, retail chains, and local businesses scale with custom web & mobile apps, GST POS billing, local SEO, performance ads, and OTT media production.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Hyderabad Operations HQ", style = MaterialTheme.typography.labelMedium)
                        Text(
                            SabDigitechInfo.HYDERABAD_HQ,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("Nellore Registered Office", style = MaterialTheme.typography.labelMedium)
                        Text(
                            SabDigitechInfo.NELLORE_OFFICE,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(SabDigitechInfo.HOURS, style = MaterialTheme.typography.bodySmall)
                }

                Text("5-Step Delivery Framework", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SabDigitechInfo.deliverySteps.forEach { step ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Text(
                                text = step,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { SabDigitechInfo.dialSupport(context) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sab_call_support_button")
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Call / WhatsApp: ${SabDigitechInfo.PHONE_DISPLAY}")
                    }
                    OutlinedButton(
                        onClick = {
                            val subj = if (initialService != null) {
                                "Inquiry: ${initialService.title} via SAB Connect"
                            } else {
                                "SAB Connect Digital Support Inquiry"
                            }
                            SabDigitechInfo.emailSupport(context, subj)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sab_email_support_button")
                    ) {
                        Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(SabDigitechInfo.EMAIL)
                    }
                    Button(
                        onClick = { SabDigitechInfo.openWebsite(context) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sab_website_button")
                    ) {
                        Icon(Icons.Rounded.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Visit sabdigitech.com")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
