package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RequestQuote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ClientProject
import com.example.data.model.CompanyService
import com.example.data.model.Customer
import com.example.data.model.EnquiryLead
import com.example.data.model.EnquiryStatus
import com.example.data.model.EnquiryType
import com.example.data.model.PortfolioItem
import com.example.data.model.ProjectStatus
import com.example.data.model.StoreProfile
import com.example.data.model.UiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SabDigitechInfo
import com.example.ui.components.formatCurrency
import com.example.ui.theme.DeepNavyWave
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PastelBlueCard
import com.example.ui.theme.PastelIndigoCard
import com.example.ui.theme.PastelMintCard
import com.example.ui.theme.PastelPurpleCard
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

@Composable
fun CustomerHomeScreen(
    userDisplayName: String,
    servicesState: UiState<List<CompanyService>>,
    portfolioState: UiState<List<PortfolioItem>>,
    enquiriesState: UiState<List<EnquiryLead>>,
    projectsState: UiState<List<ClientProject>>,
    onOpenServicesTab: (initialPortfolio: Boolean) -> Unit,
    onOpenEnquiriesTab: () -> Unit,
    onOpenEnquiryDialog: (EnquiryType, String) -> Unit,
    onOpenSupportUpdates: () -> Unit,
    onOpenAboutUs: () -> Unit,
    onSelectEnquiry: (EnquiryLead) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstName = remember(userDisplayName) {
        userDisplayName.trim().substringBefore(" ").ifBlank { "Guest" }
    }
    val services = (servicesState as? UiState.Success)?.data.orEmpty().filter { it.activeService }
    val portfolio = (portfolioState as? UiState.Success)?.data.orEmpty()
    val enquiries = (enquiriesState as? UiState.Success)?.data.orEmpty()
    val projects = (projectsState as? UiState.Success)?.data.orEmpty()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting Header matching Customer Home showcase
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Hello, $firstName 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy
                )
                Text(
                    text = "Discover our solutions for your business",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Deep Navy Promotional Hero Card ("Build, Grow & Digitize Your Business with SAB Digitech")
        item {
            Surface(
                onClick = { onOpenServicesTab(false) },
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MidnightNavy, DeepNavyWave, RoyalBluePrimary)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Build, Grow & Digitize\nYour Business with\nSAB Digitech",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = CircleShape,
                                color = ElectricCyan
                            ) {
                                Text(
                                    text = "Book Free Consultation",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        // Stylized Laptop Graphic matching the showcase hero banner
                        HeroLaptopArtwork(modifier = Modifier.size(width = 112.dp, height = 84.dp))
                    }
                }
            }
        }

        // 3x2 Showcase Quick Action Grid (Services, Portfolio, Get Quote / Track Enquiry, Chat Support, About Us)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomerGridActionTile(
                        title = "Services",
                        icon = Icons.Rounded.Apps,
                        iconBg = PastelBlueCard,
                        iconTint = RoyalBluePrimary,
                        testTag = "tile_services",
                        onClick = { onOpenServicesTab(false) },
                        modifier = Modifier.weight(1f)
                    )
                    CustomerGridActionTile(
                        title = "Portfolio",
                        icon = Icons.Rounded.Workspaces,
                        iconBg = PastelPurpleCard,
                        iconTint = Color(0xFF6D28D9),
                        testTag = "tile_portfolio",
                        onClick = { onOpenServicesTab(true) },
                        modifier = Modifier.weight(1f)
                    )
                    CustomerGridActionTile(
                        title = "Get Quote",
                        icon = Icons.Rounded.RequestQuote,
                        iconBg = PastelMintCard,
                        iconTint = StatusSuccess,
                        testTag = "tile_get_quote",
                        onClick = { onOpenEnquiryDialog(EnquiryType.QUOTATION, "") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomerGridActionTile(
                        title = "Track Enquiry",
                        icon = Icons.Rounded.AssignmentTurnedIn,
                        iconBg = Color(0xFFE0F2FE),
                        iconTint = Color(0xFF0284C7),
                        testTag = "tile_track_enquiry",
                        onClick = onOpenEnquiriesTab,
                        modifier = Modifier.weight(1f)
                    )
                    CustomerGridActionTile(
                        title = "Chat Support",
                        icon = Icons.Rounded.Chat,
                        iconBg = PastelIndigoCard,
                        iconTint = Color(0xFF4338CA),
                        testTag = "tile_chat_support",
                        onClick = onOpenSupportUpdates,
                        modifier = Modifier.weight(1f)
                    )
                    CustomerGridActionTile(
                        title = "About Us",
                        icon = Icons.Rounded.Info,
                        iconBg = Color(0xFFF3E8FF),
                        iconTint = Color(0xFF7E22CE),
                        testTag = "tile_about_us",
                        onClick = onOpenAboutUs,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Consultation & Enquiry Action Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onOpenEnquiryDialog(EnquiryType.ENQUIRY, "") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("submit_project_enquiry_btn")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Submit Enquiry", maxLines = 1)
                }
                OutlinedButton(
                    onClick = { onOpenEnquiryDialog(EnquiryType.CONSULTATION, "") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("book_consultation_btn")
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Book Call", maxLines = 1)
                }
            }
        }

        // Active Enquiry / Project Status Preview
        if (enquiries.isNotEmpty() || projects.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onOpenEnquiriesTab) {
                        Text("View All (${enquiries.size + projects.size})")
                    }
                }
            }
            items(enquiries.take(3), key = { it.id }) { enq ->
                EnquirySummaryCard(enquiry = enq, currencySymbol = "₹", onClick = { onSelectEnquiry(enq) })
            }
        }

        // Featured Company Services Preview
        if (services.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Featured Solutions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { onOpenServicesTab(false) }) {
                        Text("Explore ${services.size}")
                    }
                }
            }
            items(services.take(3), key = { it.id }) { svc ->
                Card(
                    onClick = { onOpenEnquiryDialog(EnquiryType.QUOTATION, svc.title) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(svc.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Get Quote", style = MaterialTheme.typography.labelMedium, color = RoyalBluePrimary, fontWeight = FontWeight.Bold)
                        }
                        Text(svc.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (portfolio.isNotEmpty() && svc.timeline.isNotBlank()) {
                            Text("Delivery: ${svc.timeline} • ${svc.deliverables}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF475569), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroLaptopArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Screen frame
        drawRoundRect(
            color = Color(0xFF0B254A),
            topLeft = Offset(w * 0.14f, h * 0.08f),
            size = Size(w * 0.72f, h * 0.62f),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )
        // Bright cyan display
        drawRoundRect(
            brush = Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF1D4ED8))),
            topLeft = Offset(w * 0.18f, h * 0.13f),
            size = Size(w * 0.64f, h * 0.52f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        // Dashboard chart bars inside laptop screen
        val barColors = listOf(0.42f, 0.72f, 0.55f, 0.88f)
        barColors.forEachIndexed { idx, ratio ->
            val bw = w * 0.08f
            val bx = w * 0.25f + idx * (bw + w * 0.04f)
            val bh = h * 0.34f * ratio
            drawRoundRect(
                color = Color.White.copy(alpha = 0.88f),
                topLeft = Offset(bx, h * 0.58f - bh),
                size = Size(bw, bh),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
        // Laptop base deck
        drawRoundRect(
            color = Color(0xFF93C5FD),
            topLeft = Offset(w * 0.05f, h * 0.73f),
            size = Size(w * 0.90f, h * 0.12f),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
    }
}

@Composable
private fun CustomerGridActionTile(
    title: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8EEF6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBg,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MidnightNavy,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomerServicesAndPortfolioScreen(
    storeProfile: StoreProfile?,
    servicesState: UiState<List<CompanyService>>,
    portfolioState: UiState<List<PortfolioItem>>,
    initialShowPortfolio: Boolean,
    onRequestQuoteOrEnquiry: (EnquiryType, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = storeProfile?.currencySymbol ?: "₹"
    var showPortfolio by remember(initialShowPortfolio) { mutableStateOf(initialShowPortfolio) }
    var search by remember { mutableStateOf("") }
    val services = (servicesState as? UiState.Success)?.data.orEmpty().filter { it.activeService }
    val portfolio = (portfolioState as? UiState.Success)?.data.orEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !showPortfolio,
                onClick = { showPortfolio = false },
                label = { Text("Services (${services.size})") }
            )
            FilterChip(
                selected = showPortfolio,
                onClick = { showPortfolio = true },
                label = { Text("Projects & Portfolio (${portfolio.size})") }
            )
        }

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text(if (showPortfolio) "Search portfolio case studies..." else "Search company services...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!showPortfolio) {
                if (servicesState is UiState.Loading) {
                    item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                } else {
                    val q = search.trim().lowercase()
                    val filtered = services.filter {
                        q.isEmpty() || it.title.lowercase().contains(q) || it.category.lowercase().contains(q)
                    }
                    if (filtered.isEmpty()) {
                        item { EmptyStateView("No Services Found", "Try a different keyword or request a custom solution.") }
                    } else {
                        items(filtered, key = { it.id }) { svc ->
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Surface(shape = RoundedCornerShape(8.dp), color = PastelBlueCard) {
                                            Text(
                                                text = svc.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = RoyalBluePrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        if (svc.startingPrice > 0.0) {
                                            Text(
                                                text = "From ${formatCurrency(svc.startingPrice, currency)}",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = StatusSuccess
                                            )
                                        }
                                    }
                                    Text(svc.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MidnightNavy)
                                    Text(svc.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                    if (svc.deliverables.isNotBlank()) {
                                        Text("Stack: ${svc.deliverables}", style = MaterialTheme.typography.labelSmall, color = RoyalBluePrimary)
                                    }
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { onRequestQuoteOrEnquiry(EnquiryType.QUOTATION, svc.title) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary)
                                        ) {
                                            Text("Request Quote")
                                        }
                                        FilledTonalButton(
                                            onClick = { onRequestQuoteOrEnquiry(EnquiryType.CONSULTATION, svc.title) },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Book Consultation")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (portfolioState is UiState.Loading) {
                    item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                } else {
                    val q = search.trim().lowercase()
                    val filtered = portfolio.filter {
                        q.isEmpty() || it.title.lowercase().contains(q) || it.category.lowercase().contains(q)
                    }
                    if (filtered.isEmpty()) {
                        item { EmptyStateView("No Portfolio Items Yet", "Published company case studies will appear here.") }
                    } else {
                        items(filtered, key = { it.id }) { item ->
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Surface(shape = RoundedCornerShape(8.dp), color = PastelPurpleCard) {
                                            Text(
                                                text = item.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF6D28D9),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        if (item.impactMetric.isNotBlank()) {
                                            Surface(shape = RoundedCornerShape(8.dp), color = PastelMintCard) {
                                                Text(
                                                    text = item.impactMetric,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = StatusSuccess,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MidnightNavy)
                                    Text("Client: ${item.clientName}", style = MaterialTheme.typography.labelMedium, color = Color(0xFF64748B))
                                    Text(item.summary, style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                    if (item.techStack.isNotBlank()) {
                                        Text("Tech: ${item.techStack}", style = MaterialTheme.typography.labelSmall, color = RoyalBluePrimary)
                                    }
                                    OutlinedButton(
                                        onClick = { onRequestQuoteOrEnquiry(EnquiryType.ENQUIRY, item.title) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Enquire Similar Solution")
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

@Composable
fun CustomerEnquiriesAndProjectsScreen(
    storeProfile: StoreProfile?,
    enquiriesState: UiState<List<EnquiryLead>>,
    projectsState: UiState<List<ClientProject>>,
    onNewEnquiry: (EnquiryType) -> Unit,
    onSelectEnquiry: (EnquiryLead) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = storeProfile?.currencySymbol ?: "₹"
    val enquiries = (enquiriesState as? UiState.Success)?.data.orEmpty()
    val projects = (projectsState as? UiState.Success)?.data.orEmpty()

    LazyColumn(
        modifier = modifier.fillMaxSize().widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onNewEnquiry(EnquiryType.ENQUIRY) },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("New Enquiry")
                }
                OutlinedButton(
                    onClick = { onNewEnquiry(EnquiryType.QUOTATION) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Get Quote")
                }
            }
        }

        if (projects.isNotEmpty()) {
            item {
                Text("Active Delivery Projects (${projects.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(projects, key = { it.id }) { proj ->
                ClientProjectCard(project = proj, currencySymbol = currency)
            }
        }

        item {
            Text("Enquiries, Quotations & Consultations (${enquiries.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (enquiries.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No Enquiries Submitted Yet",
                    subtitle = "Submit a project enquiry, request a quotation, or book a consultation to track live updates here.",
                    actionLabel = "Submit Enquiry",
                    onAction = { onNewEnquiry(EnquiryType.ENQUIRY) }
                )
            }
        } else {
            items(enquiries, key = { it.id }) { enq ->
                EnquirySummaryCard(enquiry = enq, currencySymbol = currency, onClick = { onSelectEnquiry(enq) })
            }
        }
    }
}

@Composable
fun EnquirySummaryCard(
    enquiry: EnquiryLead,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val st = EnquiryStatus.fromRaw(enquiry.status)
    val badgeBg = when (st) {
        EnquiryStatus.NEW -> PastelBlueCard
        EnquiryStatus.IN_PROGRESS -> Color(0xFFFEF3C7)
        EnquiryStatus.QUOTED -> PastelPurpleCard
        EnquiryStatus.CONVERTED -> PastelMintCard
        EnquiryStatus.CLOSED -> Color(0xFFF1F5F9)
    }
    val badgeFg = when (st) {
        EnquiryStatus.NEW -> RoyalBluePrimary
        EnquiryStatus.IN_PROGRESS -> StatusWarning
        EnquiryStatus.QUOTED -> Color(0xFF6D28D9)
        EnquiryStatus.CONVERTED -> StatusSuccess
        EnquiryStatus.CLOSED -> Color(0xFF64748B)
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${enquiry.enquiryCode} • ${EnquiryType.fromRaw(enquiry.enquiryType).label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = RoyalBluePrimary,
                    fontWeight = FontWeight.Bold
                )
                Surface(shape = CircleShape, color = badgeBg) {
                    Text(
                        text = st.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeFg,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }
            Text(enquiry.projectTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MidnightNavy)
            Text(
                text = "${enquiry.serviceTitle} • ${enquiry.customerName}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )
            if (enquiry.latestNote.isNotBlank()) {
                Text(
                    text = "Update: ${enquiry.latestNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF334155),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (enquiry.assignedEmployeeName.isNotBlank()) "Lead Owner: ${enquiry.assignedEmployeeName}" else "Awaiting Assignment",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
                if (enquiry.quotedAmount > 0.0) {
                    Text(
                        text = "Quote: ${formatCurrency(enquiry.quotedAmount, currencySymbol)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = StatusSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ClientProjectCard(
    project: ClientProject,
    currencySymbol: String,
    onClick: (() -> Unit)? = null
) {
    val status = ProjectStatus.fromRaw(project.status)
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(project.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Surface(shape = CircleShape, color = PastelMintCard) {
                    Text(
                        text = status.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusSuccess,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text("${project.serviceTitle} • Client: ${project.clientName}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
            LinearProgressIndicator(
                progress = { (project.progressPercent.coerceIn(0L, 100L)) / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = RoyalBluePrimary,
                trackColor = Color(0xFFE2E8F0)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${project.progressPercent}% Complete • ${project.assignedEmployeeName}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF475569))
                if (project.projectValue > 0.0) {
                    Text(formatCurrency(project.projectValue, currencySymbol), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = RoyalBluePrimary)
                }
            }
            if (project.milestoneNote.isNotBlank()) {
                Text("Milestone: ${project.milestoneNote}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF334155))
            }
        }
    }
}

@Composable
fun CustomerProfileScreen(
    userDisplayName: String,
    userEmail: String,
    customers: List<Customer>,
    onSaveProfile: (String?, String, String, String, String, String) -> Unit,
    onSwitchRole: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val existing = customers.firstOrNull()
    var fullName by remember(existing, userDisplayName) {
        mutableStateOf(existing?.fullName?.ifBlank { userDisplayName } ?: userDisplayName)
    }
    var phone by remember(existing) { mutableStateOf(existing?.phone ?: "") }
    var email by remember(existing, userEmail) { mutableStateOf(existing?.email?.ifBlank { userEmail } ?: userEmail) }
    var address by remember(existing) { mutableStateOf(existing?.address ?: "") }

    LazyColumn(
        modifier = modifier.fillMaxSize().widthIn(max = 840.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = CircleShape, color = PastelBlueCard, modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = RoyalBluePrimary)
                            }
                        }
                        Column {
                            Text(fullName.ifBlank { "Customer Profile" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(userEmail.ifBlank { "Verified Google Account" }, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                        }
                    }
                    HorizontalDivider()
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Company / City") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (fullName.trim().isNotEmpty()) {
                                onSaveProfile(existing?.id, fullName.trim(), phone.trim(), email.trim(), address.trim(), "Customer Portal Profile")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Save Profile")
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE6ECF4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SAB Digital Technologies Support", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { SabDigitechInfo.dialSupport(context) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Call")
                        }
                        OutlinedButton(onClick = { SabDigitechInfo.emailSupport(context) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Email")
                        }
                        OutlinedButton(onClick = { SabDigitechInfo.openWebsite(context) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Site")
                        }
                    }
                    HorizontalDivider()
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onSwitchRole, modifier = Modifier.weight(1f).height(48.dp)) {
                            Icon(Icons.Rounded.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Switch Role")
                        }
                        OutlinedButton(onClick = onSignOut, modifier = Modifier.weight(1f).height(48.dp)) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Sign Out")
                        }
                    }
                }
            }
        }
    }
}
