package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.data.model.AppRole
import com.example.ui.components.SabCitySkylineBackdrop
import com.example.ui.components.SabShowcaseLogoLockup
import com.example.ui.theme.GoogleBlue
import com.example.ui.theme.GoogleGreen
import com.example.ui.theme.GoogleRed
import com.example.ui.theme.GoogleYellow
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.RoleAdminBg
import com.example.ui.theme.RoleAdminFg
import com.example.ui.theme.RoleCustomerBg
import com.example.ui.theme.RoleEmployeeBg
import com.example.ui.theme.RoleEmployeeFg
import com.example.ui.theme.RoyalBluePrimary
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.PasswordVisualTransformation

data class PendingRegistrationData(
    val role: AppRole,
    val fullName: String = "",
    val email: String = "",
    val mobile: String = "",
    val employeeId: String = ""
)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onUnauthenticated()
        return
    }
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    val activityOrContext = context.findActivity() ?: context
    scope.launch {
        try {
            val cred = credentialManager.getCredential(activityOrContext, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (_: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String, Boolean) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found", false)
        return
    }
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()
    scope.launch {
        try {
            val activityContext = context.findActivity()
                ?: throw IllegalStateException("Activity context required for Google Sign-In")
            val cred = credentialManager.getCredential(activityContext, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type returned", false)
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: NoCredentialException) {
            Log.w("Auth", "No Google credentials found on device/emulator: ${e.message}", e)
            onAuthError(
                "No Google account is signed in on this emulator/device yet. Add a Google account in device Settings and tap Sign In with Google again.",
                true
            )
        } catch (e: GetCredentialException) {
            Log.e("Auth", "Credential Manager Google Sign-In failed: ${e.type} ${e.errorMessage}", e)
            onAuthError(
                e.errorMessage?.toString()
                    ?: e.localizedMessage
                    ?: "Google Sign-In failed (${e.type})",
                true
            )
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed", false)
        }
    }
}

fun signOut(
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Failed to clear credential state: ${e.message}")
            } finally {
                Firebase.auth.signOut()
                onSignOutComplete()
            }
        }
    }
}

private fun openAddGoogleAccountSettings(context: Context) {
    val addAccountIntent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
        putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(addAccountIntent)
    } catch (_: Exception) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            // No settings activity handler available
        }
    }
}

@Composable
fun WelcomeRoleGatewayScreen(
    isSignedIn: Boolean,
    userEmail: String,
    userDisplayName: String = "",
    onChooseRole: (AppRole, PendingRegistrationData) -> Unit,
    onSignOut: (() -> Unit)? = null
) {
    var step by remember { mutableStateOf(if (isSignedIn) OnboardingStep.ROLE_SELECTION else OnboardingStep.LOGIN) }
    var selectedRole by remember { mutableStateOf(AppRole.CUSTOMER) }

    SABOnboardingFlow(
        initialStep = step,
        userEmail = userEmail,
        userDisplayName = userDisplayName,
        onAuthCompleted = { regData ->
            onChooseRole(regData.role, regData)
        },
        onSignOut = onSignOut
    )
}

enum class OnboardingStep {
    LOGIN,
    ROLE_SELECTION,
    CUSTOMER_PROFILE,
    EMPLOYEE_VERIFY,
    ADMIN_VERIFY
}

@Composable
fun SABOnboardingFlow(
    initialStep: OnboardingStep = OnboardingStep.LOGIN,
    userEmail: String = "",
    userDisplayName: String = "",
    onAuthCompleted: (PendingRegistrationData) -> Unit,
    onSignOut: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var currentStep by remember(initialStep) { mutableStateOf(initialStep) }
    var selectedRole by remember { mutableStateOf(AppRole.CUSTOMER) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Customer Form state
    var custFullName by remember(userDisplayName) { mutableStateOf(userDisplayName.ifBlank { "Kushwanth Kumar" }) }
    var custMobile by remember { mutableStateOf("+91 98765 43210") }
    var custCompany by remember { mutableStateOf("SAB Digitech") }
    var custInterest by remember { mutableStateOf("Web & App Development") }

    // Employee Form state
    var empId by remember { mutableStateOf("SAB-E1024") }
    var empFullName by remember(userDisplayName) { mutableStateOf(userDisplayName.ifBlank { "Kushwanth Kumar" }) }
    var empDepartment by remember { mutableStateOf("Business Development") }

    // Admin Form state
    var adminPasscode by remember { mutableStateOf("") }

    val darkBackground = Color(0xFF0F172A)
    val cardSurface = Color(0xFF1E293B)
    val cardBorder = Color(0xFF334155)
    val activeBorder = Color(0xFF38BDF8)
    val primaryBlue = Color(0xFF2563EB)

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = cardSurface,
        unfocusedContainerColor = cardSurface,
        focusedBorderColor = activeBorder,
        unfocusedBorderColor = cardBorder,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = activeBorder,
        unfocusedLabelColor = Color(0xFF94A3B8)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stepper Header (for steps after Login)
            if (currentStep != OnboardingStep.LOGIN) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentStepNumber = if (currentStep == OnboardingStep.ROLE_SELECTION) 1 else 2
                    StepperDot(stepNumber = 1, activeStep = currentStepNumber, completed = currentStepNumber > 1)
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.width(40.dp).height(2.dp).background(if (currentStepNumber > 1) Color(0xFF10B981) else Color(0xFF334155)))
                    Spacer(Modifier.width(8.dp))
                    StepperDot(stepNumber = 2, activeStep = currentStepNumber, completed = false)
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.width(40.dp).height(2.dp).background(Color(0xFF334155)))
                    Spacer(Modifier.width(8.dp))
                    StepperDot(stepNumber = 3, activeStep = currentStepNumber, completed = false)
                }
                Spacer(Modifier.height(16.dp))
            }

            when (currentStep) {
                // SCREEN 1: WELCOME BACK (SIGN IN)
                OnboardingStep.LOGIN -> {
                    Spacer(Modifier.height(20.dp))

                    // 3D Branding Rocket Card
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                                    radius = size.minDimension * 0.45f,
                                    center = center
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "🚀", style = MaterialTheme.typography.displayMedium)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "SAB CONNECT",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Business. People. Projects.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = "Welcome Back!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Sign in to continue to SAB Connect",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(Modifier.height(32.dp))

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Continue with Google Button
                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    currentStep = OnboardingStep.ROLE_SELECTION
                                },
                                onAuthError = { msg, _ ->
                                    isLoading = false
                                    errorMessage = msg
                                },
                                scope = scope
                            )
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            GoogleGLogo(modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Continue with Google", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(text = "— or —", style = MaterialTheme.typography.labelMedium, color = Color(0xFF64748B))
                    Spacer(Modifier.height(16.dp))

                    // Continue with Email / Dev Test Login Button
                    OutlinedButton(
                        onClick = {
                            currentStep = OnboardingStep.ROLE_SELECTION
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Icon(Icons.Rounded.LockOpen, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Continue with Email", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(Modifier.height(40.dp))
                    Text(
                        text = "By continuing, you agree to our Terms & Privacy Policy",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }

                // SCREEN 2: TELL US ABOUT YOU (ROLE SELECTION)
                OnboardingStep.ROLE_SELECTION -> {
                    Text(
                        text = "Tell us about you",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Choose the role that best fits you",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(Modifier.height(32.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        RoleSelectionCard(
                            title = "I am a Customer",
                            subtext = "I want to explore services, request quotes and work with SAB.",
                            icon = Icons.Rounded.Person,
                            isSelected = selectedRole == AppRole.CUSTOMER,
                            onClick = { selectedRole = AppRole.CUSTOMER }
                        )

                        RoleSelectionCard(
                            title = "I am an Employee",
                            subtext = "I work at SAB and want to access my work dashboard.",
                            icon = Icons.Rounded.Work,
                            isSelected = selectedRole == AppRole.EMPLOYEE,
                            onClick = { selectedRole = AppRole.EMPLOYEE }
                        )

                        RoleSelectionCard(
                            title = "I am an Admin",
                            subtext = "I manage the team, leads and business operations.",
                            icon = Icons.Rounded.AdminPanelSettings,
                            isSelected = selectedRole == AppRole.ADMIN,
                            onClick = { selectedRole = AppRole.ADMIN }
                        )
                    }

                    Spacer(Modifier.height(36.dp))

                    Button(
                        onClick = {
                            currentStep = when (selectedRole) {
                                AppRole.CUSTOMER -> OnboardingStep.CUSTOMER_PROFILE
                                AppRole.EMPLOYEE -> OnboardingStep.EMPLOYEE_VERIFY
                                AppRole.ADMIN -> OnboardingStep.ADMIN_VERIFY
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue, contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Text("Continue ➔", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }

                // SCREEN 3A: COMPLETE YOUR PROFILE (CUSTOMER)
                OnboardingStep.CUSTOMER_PROFILE -> {
                    Text(
                        text = "Complete Your Profile",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Help us personalize your experience",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(Modifier.height(28.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = custFullName,
                            onValueChange = { custFullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = inputColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = custMobile,
                            onValueChange = { custMobile = it },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null, tint = Color(0xFF94A3B8)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(14.dp),
                            colors = inputColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = custCompany,
                            onValueChange = { custCompany = it },
                            label = { Text("Company (Optional)") },
                            leadingIcon = { Icon(Icons.Rounded.Business, contentDescription = null, tint = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = inputColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = custInterest,
                            onValueChange = { custInterest = it },
                            label = { Text("Interested In") },
                            leadingIcon = { Icon(Icons.Rounded.Work, contentDescription = null, tint = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = inputColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            onAuthCompleted(
                                PendingRegistrationData(
                                    role = AppRole.CUSTOMER,
                                    fullName = custFullName.ifBlank { "Kushwanth Kumar" },
                                    email = userEmail.ifBlank { "kushwanthk55@gmail.com" },
                                    mobile = custMobile
                                )
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue, contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Text("Create Account", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }

                // SCREEN 3B: EMPLOYEE VERIFICATION
                OnboardingStep.EMPLOYEE_VERIFY -> {
                    Text(
                        text = "Employee Verification",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Enter your company details to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(Modifier.height(28.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = empId,
                            onValueChange = { empId = it },
                            label = { Text("Employee ID / ID Card Number") },
                            leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null, tint = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = inputColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = empFullName,
                            onValueChange = { empFullName = it },
                            label = { Text("Full Name (Auto from Google)") },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = inputColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = empDepartment,
                            onValueChange = { empDepartment = it },
                            label = { Text("Department") },
                            leadingIcon = { Icon(Icons.Rounded.Work, contentDescription = null, tint = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = inputColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            onAuthCompleted(
                                PendingRegistrationData(
                                    role = AppRole.EMPLOYEE,
                                    fullName = empFullName.ifBlank { "Kushwanth Kumar" },
                                    email = userEmail.ifBlank { "kushwanthk55@gmail.com" },
                                    employeeId = empId.ifBlank { "SAB-E1024" }
                                )
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue, contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Text("Verify & Continue ➔", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }

                // SCREEN 3C: ADMIN VERIFICATION
                OnboardingStep.ADMIN_VERIFY -> {
                    Text(
                        text = "Admin Authorization",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Enter administrator passcode to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(Modifier.height(28.dp))

                    OutlinedTextField(
                        value = adminPasscode,
                        onValueChange = { adminPasscode = it; errorMessage = null },
                        label = { Text("Admin Passcode") },
                        placeholder = { Text("Default: 1234") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color(0xFF94A3B8)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(14.dp),
                        colors = inputColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (adminPasscode.trim().ifEmpty { "1234" } != "1234") {
                                errorMessage = "Invalid passcode. Default is 1234."
                            } else {
                                onAuthCompleted(
                                    PendingRegistrationData(
                                        role = AppRole.ADMIN,
                                        fullName = userDisplayName.ifBlank { "Administrator" },
                                        email = userEmail.ifBlank { "kushwanthk55@gmail.com" }
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue, contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Text("Authorize & Enter ➔", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepperDot(stepNumber: Int, activeStep: Int, completed: Boolean) {
    val activeColor = Color(0xFF38BDF8)
    val completedColor = Color(0xFF10B981)
    val inactiveColor = Color(0xFF334155)

    val color = when {
        completed -> completedColor
        stepNumber == activeStep -> activeColor
        else -> inactiveColor
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .background(color = color.copy(alpha = 0.2f), shape = RoundedCornerShape(50))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = color,
            modifier = Modifier.size(14.dp)
        ) {}
    }
}

@Composable
private fun RoleSelectionCard(
    title: String,
    subtext: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardSurface = Color(0xFF1E293B)
    val selectedBorder = Color(0xFF38BDF8)
    val unselectedBorder = Color(0xFF334155)
    val selectedContainer = Color(0xFF1E3A8A).copy(alpha = 0.4f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) selectedContainer else cardSurface,
        border = BorderStroke(2.dp, if (isSelected) selectedBorder else unselectedBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) selectedBorder.copy(alpha = 0.2f) else Color(0xFF0F172A),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) selectedBorder else Color(0xFF94A3B8),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(Modifier.width(12.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) selectedBorder else Color.Transparent,
                border = BorderStroke(2.dp, if (isSelected) selectedBorder else Color(0xFF64748B)),
                modifier = Modifier.size(22.dp)
            ) {
                if (isSelected) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✓", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun RoleLoginScreen(
    role: AppRole,
    onBack: () -> Unit,
    onAuthCompleted: (PendingRegistrationData) -> Unit
) {
    SABOnboardingFlow(
        initialStep = OnboardingStep.LOGIN,
        onAuthCompleted = onAuthCompleted
    )
}
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RoleGatewayButton(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        contentColor = contentColor,
        shadowElevation = if (backgroundColor == RoleCustomerBg) 4.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun RoleLoginScreen(
    role: AppRole,
    onBack: () -> Unit,
    onAuthCompleted: (PendingRegistrationData) -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isCreateAccountMode by remember(role) { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAccountSettingsAction by remember { mutableStateOf(false) }

    val triggerGoogleAuth: () -> Unit = {
        errorMessage = null
        showAccountSettingsAction = false
        isLoading = true
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isLoading = false
                val currentFirebaseUser = Firebase.auth.currentUser
                onAuthCompleted(
                    PendingRegistrationData(
                        role = role,
                        fullName = fullName.trim().ifBlank { currentFirebaseUser?.displayName.orEmpty() },
                        email = currentFirebaseUser?.email.orEmpty(),
                        mobile = mobileNumber.trim()
                    )
                )
            },
            onAuthError = { msg, suggestAddAccount ->
                isLoading = false
                errorMessage = msg
                showAccountSettingsAction = suggestAddAccount
            },
            scope = scope,
            onAuthCancelled = {
                isLoading = false
                errorMessage = "Google Sign-In was cancelled. Select a Google account (or add one on this device) to continue."
                showAccountSettingsAction = true
            }
        )
    }

    val triggerTestLogin: () -> Unit = {
        errorMessage = null
        isLoading = false
        onAuthCompleted(
            PendingRegistrationData(
                role = role,
                fullName = fullName.trim().ifBlank { "Test ${role.label}" },
                email = "testuser@sabconnect.com",
                mobile = mobileNumber.trim()
            )
        )
    }

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color(0xFFFAFBFD),
        focusedBorderColor = RoyalBluePrimary,
        unfocusedBorderColor = Color(0xFFE2E8F0)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(10.dp))
        SabShowcaseLogoLockup()
        Spacer(Modifier.height(28.dp))

        Text(
            text = if (isCreateAccountMode) "Create Account" else "${role.label} Login",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MidnightNavy
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (isCreateAccountMode) "Join SAB Connect with your Google account." else role.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = RoyalBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Authentication uses your Google account via Firebase Auth. No separate password is required.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1E3A8A)
                    )
                }
            }

            if (isCreateAccountMode || role == AppRole.EMPLOYEE) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; errorMessage = null },
                    placeholder = {
                        Text(
                            if (role == AppRole.EMPLOYEE) "Your Display Name (Optional)"
                            else "Full Name (Optional — defaults to Google name)"
                        )
                    },
                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF64748B)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = inputColors,
                    modifier = Modifier.fillMaxWidth().testTag("signup_full_name_input")
                )
            }

            if (isCreateAccountMode) {
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    placeholder = { Text("Mobile Number (Optional)") },
                    leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null, tint = Color(0xFF64748B)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    colors = inputColors,
                    modifier = Modifier.fillMaxWidth().testTag("signup_mobile_input")
                )
            }

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        if (showAccountSettingsAction) {
                            OutlinedButton(
                                onClick = { openAddGoogleAccountSettings(context) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Add Google Account on Device")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Primary Royal Blue Action CTA powered by CredentialManager Google Sign-In
            Button(
                onClick = triggerGoogleAuth,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalBluePrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_sign_in_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Connecting with Google...")
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            GoogleGLogo(modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isCreateAccountMode) "Register with Google" else "Sign In with Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Quick Test Login (Bypass Google Sign-In for Development)
            OutlinedButton(
                onClick = triggerTestLogin,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF10B981)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF047857)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("test_login_bypass_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.LockOpen,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Quick Test Login (Bypass Google)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (role == AppRole.CUSTOMER && !isCreateAccountMode) {
                OutlinedButton(
                    onClick = { isCreateAccountMode = true; errorMessage = null },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, RoyalBluePrimary.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalBluePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("create_new_account_button")
                ) {
                    Text(
                        text = "Create New Account",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Surface(
                onClick = {
                    if (isCreateAccountMode) {
                        isCreateAccountMode = false
                        errorMessage = null
                    } else {
                        onBack()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F4F9),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("auth_back_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Secured by Google Sign-In & Firebase Cloud",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GoogleGLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.22f
        val inset = strokeWidth / 2f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(inset, inset)
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Butt)

        drawArc(GoogleRed, 212f, 112f, false, topLeft, arcSize, style = stroke)
        drawArc(GoogleYellow, 148f, 66f, false, topLeft, arcSize, style = stroke)
        drawArc(GoogleGreen, 38f, 112f, false, topLeft, arcSize, style = stroke)
        drawArc(GoogleBlue, -4f, 46f, false, topLeft, arcSize, style = stroke)
        drawLine(
            color = GoogleBlue,
            start = Offset(size.width * 0.50f, size.height * 0.50f),
            end = Offset(size.width - strokeWidth * 0.2f, size.height * 0.50f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Butt
        )
    }
}
