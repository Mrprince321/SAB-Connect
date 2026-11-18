package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ClientProject
import com.example.data.model.CompanyService
import com.example.data.model.Employee
import com.example.data.model.PortfolioItem
import com.example.data.model.ProjectStatus
import com.example.data.model.StoreProfile
import com.example.data.model.UiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatCurrency
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PastelBlueCard
import com.example.ui.theme.PastelPurpleCard
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess

@Composable
fun AdminServicesPortfolioSubScreen(
    storeProfile: StoreProfile?,
    servicesState: UiState<List<CompanyService>>,
    portfolioState: UiState<List<PortfolioItem>>,
    onBack: () -> Unit,
    onSaveService: (String?, String, String, String, String, Double, String, Boolean) -> Unit,
    onDeleteService: (CompanyService) -> Unit,
    onSavePortfolio: (String?, String, String, String, String, String, String, Boolean) -> Unit,
    onDeletePortfolio: (PortfolioItem) -> Unit,
    onSeedCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val currency = storeProfile?.currencySymbol ?: "₹"
    var showPortfolio by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<CompanyService?>(null) }
    var showServiceDialog by remember { mutableStateOf(false) }
    var editingPortfolio by remember { mutableStateOf<PortfolioItem?>(null) }
    var showPortfolioDialog by remember { mutableStateOf(false) }

    val services = (servicesState as? UiState.Success)?.data.orEmpty()
    val portfolio = (portfolioState as? UiState.Success)?.data.orEmpty()

    Column(modifier = modifier.fillMaxSize().widthIn(max = 840.dp)) {
        Surface(color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Text("Services & Portfolio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        if (!showPortfolio) {
                            editingService = null; showServiceDialog = true
                        } else {
                            editingPortfolio = null; showPortfolioDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (!showPortfolio) "Add Service" else "Add Project")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(selected = !showPortfolio, onClick = { showPortfolio = false }, label = { Text("Services (${services.size})") })
            FilterChip(selected = showPortfolio, onClick = { showPortfolio = true }, label = { Text("Portfolio (${portfolio.size})") })
            if (services.isEmpty() || portfolio.isEmpty()) {
                OutlinedButton(onClick = onSeedCatalog, shape = RoundedCornerShape(10.dp)) {
                    Text("Load SAB Catalog")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!showPortfolio) {
                if (services.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No Services Configured",
                            subtitle = "Add company services or load SAB Digitech's starter digital catalog.",
                            actionLabel = "Add Service",
                            onAction = { editingService = null; showServiceDialog = true }
                        )
                    }
                } else {
                    items(services, key = { it.id }) { svc ->
                        Card(
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
                                    Surface(shape = RoundedCornerShape(6.dp), color = PastelBlueCard) {
                                        Text(svc.category, style = MaterialTheme.typography.labelSmall, color = RoyalBluePrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                    }
                                    Row {
                                        IconButton(onClick = { editingService = svc; showServiceDialog = true }) {
                                            Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                                        }
                                        IconButton(onClick = { onDeleteService(svc) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = StatusDanger)
                                        }
                                    }
                                }
                                Text(svc.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MidnightNavy)
                                Text(svc.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                Text(
                                    text = "${formatCurrency(svc.startingPrice, currency)} • ${svc.timeline} • ${if (svc.activeService) "Published" else "Hidden"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusSuccess
                                )
                            }
                        }
                    }
                }
            } else {
                if (portfolio.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No Portfolio Projects",
                            subtitle = "Showcase completed web, mobile, and digital marketing projects for customers.",
                            actionLabel = "Add Portfolio Project",
                            onAction = { editingPortfolio = null; showPortfolioDialog = true }
                        )
                    }
                } else {
                    items(portfolio, key = { it.id }) { item ->
                        Card(
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
                                    Surface(shape = RoundedCornerShape(6.dp), color = PastelPurpleCard) {
                                        Text(item.category, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6D28D9), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                    }
                                    Row {
                                        IconButton(onClick = { editingPortfolio = item; showPortfolioDialog = true }) {
                                            Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                                        }
                                        IconButton(onClick = { onDeletePortfolio(item) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = StatusDanger)
                                        }
                                    }
                                }
                                Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Client: ${item.clientName} • ${item.impactMetric}", style = MaterialTheme.typography.labelMedium, color = StatusSuccess)
                                Text(item.summary, style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showServiceDialog) {
        ServiceEditorDialog(
            existing = editingService,
            onDismiss = { showServiceDialog = false },
            onSave = { title, cat, desc, deliv, price, time, active ->
                onSaveService(editingService?.id, title, cat, desc, deliv, price, time, active)
                showServiceDialog = false
            }
        )
    }

    if (showPortfolioDialog) {
        PortfolioEditorDialog(
            existing = editingPortfolio,
            onDismiss = { showPortfolioDialog = false },
            onSave = { title, client, cat, sum, stack, impact, feat ->
                onSavePortfolio(editingPortfolio?.id, title, client, cat, sum, stack, impact, feat)
                showPortfolioDialog = false
            }
        )
    }
}

@Composable
fun AdminProjectsSubScreen(
    storeProfile: StoreProfile?,
    projectsState: UiState<List<ClientProject>>,
    employeesState: UiState<List<Employee>>,
    onBack: () -> Unit,
    onSaveProject: (String?, String, String, String, String, String, String, ProjectStatus, Long, String, String, Double) -> Unit,
    onDeleteProject: (ClientProject) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val currency = storeProfile?.currencySymbol ?: "₹"
    val projects = (projectsState as? UiState.Success)?.data.orEmpty()
    val employees = (employeesState as? UiState.Success)?.data.orEmpty()
    var editingProject by remember { mutableStateOf<ClientProject?>(null) }
    var showProjectDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().widthIn(max = 840.dp)) {
        Surface(color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Text("Assigned Projects (${projects.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { editingProject = null; showProjectDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New Project")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (projects.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Active Projects",
                        subtitle = "Convert any quoted lead to a project or create an assigned project directly.",
                        actionLabel = "New Project",
                        onAction = { editingProject = null; showProjectDialog = true }
                    )
                }
            } else {
                items(projects, key = { it.id }) { proj ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ClientProjectCard(
                            project = proj,
                            currencySymbol = currency,
                            onClick = { editingProject = proj; showProjectDialog = true }
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            OutlinedButton(onClick = { editingProject = proj; showProjectDialog = true }) {
                                Text("Update Progress")
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = { onDeleteProject(proj) }) {
                                Text("Delete", color = StatusDanger)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showProjectDialog) {
        ProjectEditorDialog(
            existing = editingProject,
            employees = employees,
            onDismiss = { showProjectDialog = false },
            onSave = { title, client, svc, empId, empName, status, prog, note, due, value ->
                onSaveProject(
                    editingProject?.id,
                    editingProject?.enquiryId ?: "",
                    title,
                    client,
                    svc,
                    empId,
                    empName,
                    status,
                    prog,
                    note,
                    due,
                    value
                )
                showProjectDialog = false
            }
        )
    }
}
