package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AssignmentInd
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AssignmentInd
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.PendingRegistrationData
import com.example.auth.RoleLoginScreen
import com.example.auth.WelcomeRoleGatewayScreen
import com.example.auth.attemptAutoSignIn
import com.example.auth.signOut
import com.example.data.model.AppRole
import com.example.data.model.EnquiryLead
import com.example.data.model.EnquiryStatus
import com.example.data.model.EnquiryType
import com.example.data.model.StoreProfile
import com.example.data.model.UiState
import com.example.data.repository.AgencyCrmRepository
import com.example.data.repository.FirestoreInstanceProvider
import com.example.data.repository.SabConnetRepository
import com.example.ui.components.SabDigitechAboutDialog
import com.example.ui.components.SabShowcaseTopBar
import com.example.ui.components.SabWaveSplashScreen
import com.example.ui.components.StoreProfileDialog
import com.example.ui.screens.AdminHomeScreen
import com.example.ui.screens.AdminLeadsScreen
import com.example.ui.screens.AdminProjectsSubScreen
import com.example.ui.screens.AdminReportsScreen
import com.example.ui.screens.AdminServicesPortfolioSubScreen
import com.example.ui.screens.AdminUsersScreen
import com.example.ui.screens.CustomerEnquiriesAndProjectsScreen
import com.example.ui.screens.CustomerHomeScreen
import com.example.ui.screens.CustomerProfileScreen
import com.example.ui.screens.CustomerServicesAndPortfolioScreen
import com.example.ui.screens.EmployeeHomeScreen
import com.example.ui.screens.EmployeeLeadsScreen
import com.example.ui.screens.EmployeeProfileAndReportsScreen
import com.example.ui.screens.EmployeeTasksAndClientsScreen
import com.example.ui.screens.EmployeeTasksSubTab
import com.example.ui.screens.EnquiryDetailAndCommsDialog
import com.example.ui.screens.EnquirySubmitDialog
import com.example.ui.screens.NotificationsUpdatesDialog
import com.example.ui.theme.PastelBlueCard
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.SabConnetTheme
import com.example.ui.viewmodel.SabConnetViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

enum class AppDestination(
    val role: AppRole,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    // Customer Bottom Nav: Home, Services, Enquiries, Profile
    CUST_HOME(AppRole.CUSTOMER, "Home", Icons.Rounded.Home, Icons.Outlined.Home, "nav_cust_home"),
    CUST_SERVICES(AppRole.CUSTOMER, "Services", Icons.Rounded.Apps, Icons.Outlined.Apps, "nav_cust_services"),
    CUST_ENQUIRIES(AppRole.CUSTOMER, "Enquiries", Icons.Rounded.AssignmentTurnedIn, Icons.Outlined.AssignmentTurnedIn, "nav_cust_enquiries"),
    CUST_PROFILE(AppRole.CUSTOMER, "Profile", Icons.Rounded.Person, Icons.Outlined.Person, "nav_cust_profile"),

    // Employee Bottom Nav: Home, Leads, Tasks, Profile
    EMP_HOME(AppRole.EMPLOYEE, "Home", Icons.Rounded.Home, Icons.Outlined.Home, "nav_emp_home"),
    EMP_LEADS(AppRole.EMPLOYEE, "Leads", Icons.AutoMirrored.Rounded.Assignment, Icons.AutoMirrored.Outlined.Assignment, "nav_emp_leads"),
    EMP_TASKS(AppRole.EMPLOYEE, "Tasks", Icons.Rounded.CheckBox, Icons.Outlined.CheckBox, "nav_emp_tasks"),
    EMP_PROFILE(AppRole.EMPLOYEE, "Profile", Icons.Rounded.Person, Icons.Outlined.Person, "nav_emp_profile"),

    // Admin Bottom Nav: Home, Users, Leads, Reports
    ADMIN_HOME(AppRole.ADMIN, "Home", Icons.Rounded.Home, Icons.Outlined.Home, "nav_admin_home"),
    ADMIN_USERS(AppRole.ADMIN, "Users", Icons.Rounded.Group, Icons.Outlined.Group, "nav_admin_users"),
    ADMIN_LEADS(AppRole.ADMIN, "Leads", Icons.Rounded.AssignmentInd, Icons.Outlined.AssignmentInd, "nav_admin_leads"),
    ADMIN_REPORTS(AppRole.ADMIN, "Reports", Icons.Rounded.Analytics, Icons.Outlined.Analytics, "nav_admin_reports");

    companion object {
        fun forRole(role: AppRole): List<AppDestination> = entries.filter { it.role == role }
    }
}

private enum class AdminSubRoute {
    NONE,
    SERVICES_PORTFOLIO,
    PROJECTS
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            SabConnetTheme(darkTheme = false) {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    var showSplash by remember { mutableStateOf(true) }
    var pendingRoleForAuth by remember { mutableStateOf<AppRole?>(null) }
    var pendingRegistration by remember { mutableStateOf<PendingRegistrationData?>(null) }
    var localBypassEmail by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    if (showSplash) {
        SabWaveSplashScreen(onFinished = { showSplash = false })
        return
    }

    if (user == null && localBypassEmail == null) {
        val roleToLogin = pendingRoleForAuth
        if (roleToLogin == null) {
            WelcomeRoleGatewayScreen(
                isSignedIn = false,
                userEmail = "",
                userDisplayName = "",
                onChooseRole = { role, data ->
                    pendingRoleForAuth = role
                    pendingRegistration = data
                }
            )
        } else {
            RoleLoginScreen(
                role = roleToLogin,
                onBack = { pendingRoleForAuth = null },
                onAuthCompleted = { data ->
                    localBypassEmail = data.email.ifBlank { "testuser@sabconnect.com" }
                    pendingRegistration = data
                    pendingRoleForAuth = null
                }
            )
        }
    } else {
        val activeUid = user?.uid ?: "dev_bypass_user_id"
        key(activeUid) {
            SabConnetAppShell(
                currentUserId = activeUid,
                userEmail = user?.email ?: localBypassEmail ?: "testuser@sabconnect.com",
                userDisplayName = user?.displayName ?: pendingRegistration?.fullName?.ifBlank { "Test User" } ?: "Test User",
                initialRegistration = pendingRegistration,
                onConsumeRegistration = { pendingRegistration = null }
            )
        }
    }
}

@Composable
private fun SabConnetAppShell(
    currentUserId: String,
    userEmail: String,
    userDisplayName: String,
    initialRegistration: PendingRegistrationData?,
    onConsumeRegistration: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: SabConnetViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val db = FirestoreInstanceProvider.get(app)
                SabConnetViewModel(
                    repository = SabConnetRepository(db),
                    agencyRepository = AgencyCrmRepository(db),
                    currentUserId = currentUserId,
                    initialRole = initialRegistration?.role
                )
            }
        }
    )

    LaunchedEffect(currentUserId) {
        viewModel.ensureCatalogSeeded()
    }

    LaunchedEffect(initialRegistration) {
        if (initialRegistration != null) {
            viewModel.selectRole(initialRegistration.role)
            if (initialRegistration.role == AppRole.CUSTOMER && initialRegistration.fullName.isNotBlank()) {
                viewModel.saveCustomer(
                    customerId = null,
                    fullName = initialRegistration.fullName,
                    phone = initialRegistration.mobile,
                    email = initialRegistration.email.ifBlank { userEmail },
                    address = "",
                    notes = "Self-registered via Customer Login"
                )
            } else if (initialRegistration.role == AppRole.EMPLOYEE && initialRegistration.fullName.isNotBlank()) {
                viewModel.saveEmployee(
                    employeeId = null,
                    fullName = initialRegistration.fullName,
                    role = "Solutions Consultant",
                    phone = initialRegistration.mobile.ifBlank { initialRegistration.email },
                    activeRecord = true
                )
            }
            onConsumeRegistration()
        }
    }

    val activeRole by viewModel.activeRole.collectAsStateWithLifecycle()
    val storeProfileState by viewModel.storeProfileState.collectAsStateWithLifecycle()
    val customersState by viewModel.customersState.collectAsStateWithLifecycle()
    val employeesState by viewModel.employeesState.collectAsStateWithLifecycle()
    val clockLogsState by viewModel.clockLogsState.collectAsStateWithLifecycle()
    val invoicesState by viewModel.invoicesState.collectAsStateWithLifecycle()
    val servicesState by viewModel.servicesState.collectAsStateWithLifecycle()
    val portfolioState by viewModel.portfolioState.collectAsStateWithLifecycle()
    val enquiriesState by viewModel.enquiriesState.collectAsStateWithLifecycle()
    val projectsState by viewModel.clientProjectsState.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val storeProfile: StoreProfile? = (storeProfileState as? UiState.Success)?.data
    val servicesList = (servicesState as? UiState.Success)?.data.orEmpty()
    val customersList = (customersState as? UiState.Success)?.data.orEmpty()
    val enquiriesList = (enquiriesState as? UiState.Success)?.data.orEmpty()
    val projectsList = (projectsState as? UiState.Success)?.data.orEmpty()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var enquiryDialogRequest by remember { mutableStateOf<Pair<EnquiryType, String>?>(null) }
    var selectedEnquiryDetail by remember { mutableStateOf<EnquiryLead?>(null) }

    val selectedRole = activeRole
    if (selectedRole == null) {
        WelcomeRoleGatewayScreen(
            isSignedIn = true,
            userEmail = userEmail,
            userDisplayName = userDisplayName,
            onChooseRole = { role, regData ->
                viewModel.selectRole(role)
                if (role == AppRole.CUSTOMER && regData.fullName.isNotBlank()) {
                    viewModel.saveCustomer(
                        customerId = null,
                        fullName = regData.fullName,
                        phone = regData.mobile,
                        email = regData.email.ifBlank { userEmail },
                        address = "",
                        notes = "Self-registered via Customer Gateway"
                    )
                } else if (role == AppRole.EMPLOYEE && regData.fullName.isNotBlank()) {
                    viewModel.saveEmployee(
                        employeeId = regData.employeeId.ifBlank { null },
                        fullName = regData.fullName,
                        role = "Solutions Consultant",
                        phone = regData.mobile.ifBlank { regData.email },
                        activeRecord = true
                    )
                }
            },
            onSignOut = { signOut(credentialManager = credentialManager, scope = scope) }
        )
        return
    }

    val roleDestinations = remember(selectedRole) { AppDestination.forRole(selectedRole) }
    var currentDestination by remember(selectedRole) { mutableStateOf(roleDestinations.first()) }
    var customerShowPortfolioInitially by remember { mutableStateOf(false) }
    var employeeTasksInitialTab by remember { mutableStateOf(EmployeeTasksSubTab.FOLLOW_UPS) }
    var adminSubRoute by remember(selectedRole) { mutableStateOf(AdminSubRoute.NONE) }

    BackHandler {
        if (selectedRole == AppRole.ADMIN && adminSubRoute != AdminSubRoute.NONE) {
            adminSubRoute = AdminSubRoute.NONE
        } else if (currentDestination != roleDestinations.first()) {
            currentDestination = roleDestinations.first()
        } else {
            viewModel.clearRole()
        }
    }

    val unreadUpdatesCount = remember(enquiriesList) {
        enquiriesList.count { it.status == EnquiryStatus.NEW.name || it.status == EnquiryStatus.QUOTED.name }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.navigationBars,
            containerColor = Color(0xFFF7F9FC),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                SabShowcaseTopBar(
                    activeRole = selectedRole,
                    syncState = syncState,
                    unreadCount = unreadUpdatesCount,
                    onNotificationClick = { showNotificationsDialog = true },
                    onSwitchRole = { viewModel.clearRole() }
                )
            },
            bottomBar = {
                if (!useRail && adminSubRoute == AdminSubRoute.NONE) {
                    NavigationBar(containerColor = Color.White, tonalElevation = 2.dp) {
                        roleDestinations.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RoyalBluePrimary,
                                    selectedTextColor = RoyalBluePrimary,
                                    indicatorColor = PastelBlueCard,
                                    unselectedIconColor = Color(0xFF64748B),
                                    unselectedTextColor = Color(0xFF64748B)
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = Color.White,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        roleDestinations.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    adminSubRoute = AdminSubRoute.NONE
                                    currentDestination = dest
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (selectedRole == AppRole.ADMIN && adminSubRoute != AdminSubRoute.NONE) {
                        when (adminSubRoute) {
                            AdminSubRoute.SERVICES_PORTFOLIO -> AdminServicesPortfolioSubScreen(
                                storeProfile = storeProfile,
                                servicesState = servicesState,
                                portfolioState = portfolioState,
                                onBack = { adminSubRoute = AdminSubRoute.NONE },
                                onSaveService = { id, title, cat, desc, deliv, price, time, active ->
                                    viewModel.saveService(id, title, cat, desc, deliv, price, time, active)
                                },
                                onDeleteService = viewModel::deleteService,
                                onSavePortfolio = { id, title, client, cat, sum, stack, impact, feat ->
                                    viewModel.savePortfolioItem(id, title, client, cat, sum, stack, impact, feat)
                                },
                                onDeletePortfolio = viewModel::deletePortfolioItem,
                                onSeedCatalog = viewModel::seedStarterCatalog
                            )
                            AdminSubRoute.PROJECTS -> AdminProjectsSubScreen(
                                storeProfile = storeProfile,
                                projectsState = projectsState,
                                employeesState = employeesState,
                                onBack = { adminSubRoute = AdminSubRoute.NONE },
                                onSaveProject = { id, enqId, title, client, svc, empId, empName, st, prog, note, due, value ->
                                    viewModel.saveClientProject(id, enqId, title, client, svc, empId, empName, st, prog, note, due, value)
                                },
                                onDeleteProject = viewModel::deleteClientProject
                            )
                            AdminSubRoute.NONE -> Unit
                        }
                    } else {
                        Crossfade(targetState = currentDestination, label = "main_tab_crossfade") { dest ->
                            when (dest) {
                                // CUSTOMER SCREENS
                                AppDestination.CUST_HOME -> CustomerHomeScreen(
                                    userDisplayName = userDisplayName,
                                    servicesState = servicesState,
                                    portfolioState = portfolioState,
                                    enquiriesState = enquiriesState,
                                    projectsState = projectsState,
                                    onOpenServicesTab = { showPortfolio ->
                                        customerShowPortfolioInitially = showPortfolio
                                        currentDestination = AppDestination.CUST_SERVICES
                                    },
                                    onOpenEnquiriesTab = { currentDestination = AppDestination.CUST_ENQUIRIES },
                                    onOpenEnquiryDialog = { type, service -> enquiryDialogRequest = type to service },
                                    onOpenSupportUpdates = {
                                        val latest = enquiriesList.firstOrNull()
                                        if (latest != null) selectedEnquiryDetail = latest
                                        else showNotificationsDialog = true
                                    },
                                    onOpenAboutUs = { showAboutDialog = true },
                                    onSelectEnquiry = { selectedEnquiryDetail = it }
                                )
                                AppDestination.CUST_SERVICES -> CustomerServicesAndPortfolioScreen(
                                    storeProfile = storeProfile,
                                    servicesState = servicesState,
                                    portfolioState = portfolioState,
                                    initialShowPortfolio = customerShowPortfolioInitially,
                                    onRequestQuoteOrEnquiry = { type, serviceTitle ->
                                        enquiryDialogRequest = type to serviceTitle
                                    }
                                )
                                AppDestination.CUST_ENQUIRIES -> CustomerEnquiriesAndProjectsScreen(
                                    storeProfile = storeProfile,
                                    enquiriesState = enquiriesState,
                                    projectsState = projectsState,
                                    onNewEnquiry = { type -> enquiryDialogRequest = type to "" },
                                    onSelectEnquiry = { selectedEnquiryDetail = it }
                                )
                                AppDestination.CUST_PROFILE -> CustomerProfileScreen(
                                    userDisplayName = userDisplayName,
                                    userEmail = userEmail,
                                    customers = customersList,
                                    onSaveProfile = { id, name, phone, email, addr, notes ->
                                        viewModel.saveCustomer(id, name, phone, email, addr, notes)
                                    },
                                    onSwitchRole = { viewModel.clearRole() },
                                    onSignOut = { signOut(credentialManager = credentialManager, scope = scope) }
                                )

                                // EMPLOYEE SCREENS
                                AppDestination.EMP_HOME -> EmployeeHomeScreen(
                                    userDisplayName = userDisplayName,
                                    enquiriesState = enquiriesState,
                                    customersState = customersState,
                                    employeesState = employeesState,
                                    projectsState = projectsState,
                                    onOpenLeads = { currentDestination = AppDestination.EMP_LEADS },
                                    onOpenTasksTab = { tab ->
                                        employeeTasksInitialTab = tab
                                        currentDestination = AppDestination.EMP_TASKS
                                    },
                                    onOpenReportsProfile = { currentDestination = AppDestination.EMP_PROFILE },
                                    onClockAction = viewModel::clockEmployee,
                                    onQuickRegisterStaff = { name ->
                                        viewModel.saveEmployee(null, name, "Solutions Consultant", userEmail, true)
                                    }
                                )
                                AppDestination.EMP_LEADS -> EmployeeLeadsScreen(
                                    storeProfile = storeProfile,
                                    enquiriesState = enquiriesState,
                                    onSelectEnquiry = { selectedEnquiryDetail = it }
                                )
                                AppDestination.EMP_TASKS -> EmployeeTasksAndClientsScreen(
                                    initialTab = employeeTasksInitialTab,
                                    storeProfile = storeProfile,
                                    enquiriesState = enquiriesState,
                                    customersState = customersState,
                                    projectsState = projectsState,
                                    onSelectEnquiry = { selectedEnquiryDetail = it }
                                )
                                AppDestination.EMP_PROFILE -> EmployeeProfileAndReportsScreen(
                                    userDisplayName = userDisplayName,
                                    userEmail = userEmail,
                                    storeProfile = storeProfile,
                                    enquiriesState = enquiriesState,
                                    projectsState = projectsState,
                                    onSwitchRole = { viewModel.clearRole() },
                                    onSignOut = { signOut(credentialManager = credentialManager, scope = scope) }
                                )

                                // ADMIN SCREENS
                                AppDestination.ADMIN_HOME -> AdminHomeScreen(
                                    customersState = customersState,
                                    employeesState = employeesState,
                                    enquiriesState = enquiriesState,
                                    projectsState = projectsState,
                                    onOpenUsersTab = { currentDestination = AppDestination.ADMIN_USERS },
                                    onOpenLeadsTab = { currentDestination = AppDestination.ADMIN_LEADS },
                                    onOpenServicesPortfolio = { adminSubRoute = AdminSubRoute.SERVICES_PORTFOLIO },
                                    onOpenProjects = { adminSubRoute = AdminSubRoute.PROJECTS },
                                    onOpenReportsTab = { currentDestination = AppDestination.ADMIN_REPORTS },
                                    onOpenSettings = { showSettingsDialog = true }
                                )
                                AppDestination.ADMIN_USERS -> AdminUsersScreen(
                                    customersState = customersState,
                                    employeesState = employeesState,
                                    clockLogsState = clockLogsState,
                                    onSaveCustomer = { id, name, phone, email, addr, notes ->
                                        viewModel.saveCustomer(id, name, phone, email, addr, notes)
                                    },
                                    onDeleteCustomer = viewModel::deleteCustomer,
                                    onSaveEmployee = { id, name, role, phone, active ->
                                        viewModel.saveEmployee(id, name, role, phone, active)
                                    },
                                    onDeleteEmployee = viewModel::deleteEmployee,
                                    onClockAction = viewModel::clockEmployee
                                )
                                AppDestination.ADMIN_LEADS -> AdminLeadsScreen(
                                    storeProfile = storeProfile,
                                    enquiriesState = enquiriesState,
                                    employeesState = employeesState,
                                    onSelectEnquiry = { selectedEnquiryDetail = it },
                                    onAssignLead = { enq, emp -> viewModel.assignEnquiry(enq, emp) },
                                    onConvertToProject = { enq -> viewModel.convertEnquiryToProject(enq) }
                                )
                                AppDestination.ADMIN_REPORTS -> AdminReportsScreen(
                                    storeProfile = storeProfile,
                                    enquiriesState = enquiriesState,
                                    employeesState = employeesState,
                                    projectsState = projectsState,
                                    invoicesState = invoicesState,
                                    onOpenProjects = { adminSubRoute = AdminSubRoute.PROJECTS }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    enquiryDialogRequest?.let { (type, preselectedService) ->
        val primaryCust = customersList.firstOrNull()
        EnquirySubmitDialog(
            initialType = type,
            initialServiceTitle = preselectedService,
            services = servicesList,
            defaultCustomerName = primaryCust?.fullName?.ifBlank { userDisplayName } ?: userDisplayName,
            defaultCustomerEmail = primaryCust?.email?.ifBlank { userEmail } ?: userEmail,
            onDismiss = { enquiryDialogRequest = null },
            onSubmit = { reqType, cName, cPhone, cEmail, sTitle, pTitle, reqs, budget, prefDate ->
                viewModel.submitEnquiry(
                    enquiryType = reqType,
                    customerId = primaryCust?.id ?: "",
                    customerName = cName,
                    customerPhone = cPhone,
                    customerEmail = cEmail,
                    serviceTitle = sTitle,
                    projectTitle = pTitle,
                    requirements = reqs,
                    budgetRange = budget,
                    preferredDate = prefDate,
                    onSuccess = {
                        enquiryDialogRequest = null
                        if (selectedRole == AppRole.CUSTOMER) {
                            currentDestination = AppDestination.CUST_ENQUIRIES
                        }
                    }
                )
            }
        )
    }

    selectedEnquiryDetail?.let { enq ->
        val liveEnquiry = enquiriesList.firstOrNull { it.id == enq.id } ?: enq
        EnquiryDetailAndCommsDialog(
            enquiry = liveEnquiry,
            viewerRole = selectedRole,
            viewerName = userDisplayName.ifBlank { selectedRole.label },
            currencySymbol = storeProfile?.currencySymbol ?: "₹",
            onDismiss = { selectedEnquiryDetail = null },
            onUpdateProgress = { st, quote, followDate, followNote, callNote ->
                viewModel.updateEnquiryProgress(
                    enquiry = liveEnquiry,
                    status = st,
                    quotedAmount = quote,
                    followUpDate = followDate,
                    followUpNote = followNote,
                    noteText = callNote,
                    authorRole = selectedRole.name,
                    authorName = userDisplayName.ifBlank { selectedRole.label },
                    onSuccess = { selectedEnquiryDetail = null }
                )
            },
            onSendMessage = { msg ->
                viewModel.postCommunicationMessage(
                    enquiry = liveEnquiry,
                    authorRole = selectedRole.name,
                    authorName = userDisplayName.ifBlank { selectedRole.label },
                    message = msg
                )
            },
            onConvertToProject = if (selectedRole == AppRole.ADMIN) {
                {
                    viewModel.convertEnquiryToProject(liveEnquiry) {
                        selectedEnquiryDetail = null
                        adminSubRoute = AdminSubRoute.PROJECTS
                    }
                }
            } else null
        )
    }

    if (showNotificationsDialog) {
        NotificationsUpdatesDialog(
            enquiries = enquiriesList,
            projects = projectsList,
            onDismiss = { showNotificationsDialog = false },
            onSelectEnquiry = { selectedEnquiryDetail = it }
        )
    }

    if (showAboutDialog) {
        SabDigitechAboutDialog(
            initialService = null,
            onDismiss = { showAboutDialog = false }
        )
    }

    if (showSettingsDialog) {
        StoreProfileDialog(
            existingProfile = storeProfile,
            defaultEmail = userEmail,
            defaultOwnerName = userDisplayName,
            isFirstTimeSetup = false,
            onDismiss = { showSettingsDialog = false },
            onSave = { storeName, ownerName, email, phone, address, currencySymbol, taxRatePercent ->
                viewModel.saveStoreProfile(
                    existingProfile = storeProfile,
                    storeName = storeName,
                    ownerName = ownerName,
                    email = email,
                    phone = phone,
                    address = address,
                    currencySymbol = currencySymbol,
                    taxRatePercent = taxRatePercent,
                    onSuccess = { showSettingsDialog = false }
                )
            }
        )
    }
}
