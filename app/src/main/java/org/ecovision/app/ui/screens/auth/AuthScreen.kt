package org.ecovision.app.ui.screens.auth

import android.util.Patterns
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.EcoButton
import org.ecovision.app.ui.components.GlassCard
import org.ecovision.app.ui.components.StatusBadge

@Composable
fun AuthScreen(onAuthSuccess: (Profile) -> Unit) {
    var isSignUp by remember { mutableStateOf(false) }

    // Sign In state
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) } // 1: Identifier, 2: Password/OTP, 3: 2FA
    var isMobilePath by remember { mutableStateOf(false) }
    var twoFactorType by remember { mutableStateOf("totp") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Sign Up state - ONLY basic account info (KYC details collected post-registration)
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.USER) }
    var referralCode by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }

    // Live validation states
    var usernameCheckStatus by remember { mutableStateOf<String?>(null) } // null, "checking", "available", "taken"
    var mobileCheckStatus by remember { mutableStateOf<String?>(null) } // null, "valid", "invalid", "taken"
    var emailCheckStatus by remember { mutableStateOf<String?>(null) } // null, "valid", "invalid"
    var referralStatus by remember { mutableStateOf<String?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var usernameDebounceJob by remember { mutableStateOf<Job?>(null) }
    var mobileDebounceJob by remember { mutableStateOf<Job?>(null) }

    // Calculate password strength
    val passwordStrength = remember(newPassword) {
        calculatePasswordStrength(newPassword)
    }

    Scaffold(
        containerColor = Slate900
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding() // Ensures form lifts smoothly above soft keyboard
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Brand Header with Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(EcoEmerald500),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = "Logo",
                    tint = Slate950,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "EcoVision",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isSignUp) "Fast Account Registration" else "Smart Waste • Dynamic Pricing • Verified Loop",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate400
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Toggle Tabs: Sign In / Register
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate850)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isSignUp) EcoEmerald600 else Color.Transparent)
                        .clickable {
                            isSignUp = false
                            errorMessage = null
                            successMessage = null
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign In",
                        color = if (!isSignUp) Color.White else Slate400,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSignUp) EcoEmerald600 else Color.Transparent)
                        .clickable {
                            isSignUp = true
                            errorMessage = null
                            successMessage = null
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Register",
                        color = if (isSignUp) Color.White else Slate400,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Switch between Sign In and Register
            AnimatedContent(
                targetState = isSignUp,
                label = "AuthTabsAnimation"
            ) { targetIsSignUp ->
                if (!targetIsSignUp) {
                    // ==========================================
                    // SIGN IN VIEW
                    // ==========================================
                    Column(modifier = Modifier.fillMaxWidth()) {
                        GlassCard {
                            Text(
                                text = when (step) {
                                    1 -> "Account Identification"
                                    2 -> if (isMobilePath) "Enter Mobile OTP" else "Enter Password"
                                    else -> "Two-Factor Authentication (2FA)"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            if (step == 1) {
                                OutlinedTextField(
                                    value = identifier,
                                    onValueChange = {
                                        identifier = it
                                        errorMessage = null
                                    },
                                    label = { Text("Username or 10-Digit Mobile") },
                                    placeholder = { Text("e.g. adhil or 9123456789") },
                                    leadingIcon = { Icon(Icons.Default.AccountCircle, null, tint = EcoEmerald400) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = {
                                            if (identifier.isNotBlank()) {
                                                val clean = identifier.trim()
                                                val digits = clean.filter { it.isDigit() }
                                                isMobilePath = digits.length >= 10 || clean.startsWith("+91")
                                                step = 2
                                            }
                                        }
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EcoEmerald500,
                                        unfocusedBorderColor = CardBorderBright
                                    )
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                EcoButton(
                                    text = "Continue",
                                    enabled = identifier.isNotBlank(),
                                    onClick = {
                                        val clean = identifier.trim()
                                        if (clean.isBlank()) {
                                            errorMessage = "Please enter your username or mobile number."
                                            return@EcoButton
                                        }
                                        val digits = clean.filter { it.isDigit() }
                                        isMobilePath = digits.length >= 10 || clean.startsWith("+91")
                                        step = 2
                                        errorMessage = null
                                    }
                                )
                            } else if (step == 2) {
                                if (isMobilePath) {
                                    Text(
                                        text = "A 6-digit SMS OTP has been sent to $identifier (Demo: 123456)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LeafCoinGold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = otpCode,
                                        onValueChange = {
                                            otpCode = it
                                            errorMessage = null
                                        },
                                        label = { Text("6-Digit SMS OTP") },
                                        leadingIcon = { Icon(Icons.Default.Sms, null, tint = EcoEmerald400) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = EcoEmerald500,
                                            unfocusedBorderColor = CardBorderBright
                                        )
                                    )
                                } else {
                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = {
                                            password = it
                                            errorMessage = null
                                        },
                                        label = { Text("Password") },
                                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = EcoEmerald400) },
                                        trailingIcon = {
                                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                Icon(
                                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    tint = Slate400
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = EcoEmerald500,
                                            unfocusedBorderColor = CardBorderBright
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { step = 1 },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                                    ) {
                                        Text("Back")
                                    }
                                    EcoButton(
                                        text = if (isLoading) "Verifying..." else "Sign In",
                                        modifier = Modifier.weight(1.6f),
                                        enabled = !isLoading && (if (isMobilePath) otpCode.isNotBlank() else password.isNotBlank()),
                                        onClick = {
                                            scope.launch {
                                                isLoading = true
                                                errorMessage = null
                                                val clean = identifier.trim()
                                                val profile = if (isMobilePath) {
                                                    val formatted = if (clean.startsWith("+91")) clean else "+91${clean.takeLast(10)}"
                                                    SupabaseClient.findProfileByMobile(formatted)
                                                } else {
                                                    SupabaseClient.findProfileByUsername(clean)
                                                }

                                                if (profile != null) {
                                                    if (profile.totpEnabled || profile.emailOtpEnabled) {
                                                        twoFactorType = if (profile.totpEnabled) "totp" else "email_otp"
                                                        step = 3
                                                    } else {
                                                        SessionManager.setProfile(profile, persist = true)
                                                        onAuthSuccess(profile)
                                                    }
                                                } else {
                                                    errorMessage = "User account not found. Please check details or use 1-Tap test accounts below."
                                                }
                                                isLoading = false
                                            }
                                        }
                                    )
                                }
                            } else if (step == 3) {
                                Text(
                                    text = if (twoFactorType == "totp")
                                        "Enter 6-digit code from Google Authenticator / Authy app (Demo: 123456)"
                                    else
                                        "Enter 6-digit verification code sent to your registered email (Demo: 123456)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate300
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = otpCode,
                                    onValueChange = { otpCode = it },
                                    label = { Text("2FA Verification Code") },
                                    leadingIcon = { Icon(Icons.Default.Security, null, tint = LeafCoinGold) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                EcoButton(
                                    text = "Verify & Access Dashboard",
                                    enabled = otpCode.isNotBlank(),
                                    onClick = {
                                        scope.launch {
                                            val clean = identifier.trim()
                                            val profile = if (isMobilePath) {
                                                val formatted = if (clean.startsWith("+91")) clean else "+91${clean.takeLast(10)}"
                                                SupabaseClient.findProfileByMobile(formatted)
                                            } else {
                                                SupabaseClient.findProfileByUsername(clean)
                                            }
                                            if (profile != null) {
                                                SessionManager.setProfile(profile, persist = true)
                                                onAuthSuccess(profile)
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ==========================================
                        // 1-TAP QUICK TEST ACCOUNTS
                        // ==========================================
                        GlassCard {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = LeafCoinGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "1-Tap Quick Test Accounts",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Instantly log in with pre-configured roles to test platform workflows:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                TestAccountRow(
                                    roleIcon = "🏠",
                                    roleTitle = "Household User",
                                    username = "adhil",
                                    description = "Scan waste, DIY upcycle, P2P bargain, Leaf Coins",
                                    badgeColor = EcoEmerald400,
                                    onClick = {
                                        scope.launch {
                                            isLoading = true
                                            SessionManager.switchDemoRole(UserRole.USER)
                                            val p = SessionManager.currentProfile.value
                                            if (p != null) onAuthSuccess(p)
                                            isLoading = false
                                        }
                                    }
                                )

                                TestAccountRow(
                                    roleIcon = "🚚",
                                    roleTitle = "Pickup Agent",
                                    username = "rahul_agent",
                                    description = "Zone pickups, spot weighing scale, godown entry",
                                    badgeColor = LeafCoinGold,
                                    onClick = {
                                        scope.launch {
                                            isLoading = true
                                            SessionManager.switchDemoRole(UserRole.AGENT)
                                            val p = SessionManager.currentProfile.value
                                            if (p != null) onAuthSuccess(p)
                                            isLoading = false
                                        }
                                    }
                                )

                                TestAccountRow(
                                    roleIcon = "🏭",
                                    roleTitle = "Buyer Company",
                                    username = "greenloop_co",
                                    description = "B2B godown stock feed, bulk lots, ESG offset",
                                    badgeColor = Color(0xFF60A5FA),
                                    onClick = {
                                        scope.launch {
                                            isLoading = true
                                            SessionManager.switchDemoRole(UserRole.COMPANY)
                                            val p = SessionManager.currentProfile.value
                                            if (p != null) onAuthSuccess(p)
                                            isLoading = false
                                        }
                                    }
                                )

                                TestAccountRow(
                                    roleIcon = "🛡️",
                                    roleTitle = "Super Admin",
                                    username = "admin",
                                    description = "Maintenance switchboard, dynamic slabs, KYC desk",
                                    badgeColor = Color(0xFFF472B6),
                                    onClick = {
                                        scope.launch {
                                            isLoading = true
                                            SessionManager.switchDemoRole(UserRole.ADMIN)
                                            val p = SessionManager.currentProfile.value
                                            if (p != null) onAuthSuccess(p)
                                            isLoading = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // REGISTER VIEW (FAST - NO KYC REQUIRED)
                    // ==========================================
                    Column(modifier = Modifier.fillMaxWidth()) {
                        GlassCard {
                            Text(
                                text = "Select Account Role",
                                style = MaterialTheme.typography.labelLarge,
                                color = Slate300,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (role in listOf(UserRole.USER, UserRole.AGENT, UserRole.COMPANY)) {
                                    val isSel = selectedRole == role
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) EcoEmerald600 else Slate800)
                                            .clickable { selectedRole = role }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = when (role) {
                                                    UserRole.USER -> "🏠 Household"
                                                    UserRole.AGENT -> "🚚 Agent"
                                                    UserRole.COMPANY -> "🏭 Company"
                                                    else -> ""
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else Slate400
                                            )
                                        }
                                    }
                                    if (role != UserRole.COMPANY) Spacer(modifier = Modifier.width(6.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Full Name
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = {
                                    fullName = it
                                    errorMessage = null
                                },
                                label = { Text("Full Name / Business Name *") },
                                leadingIcon = { Icon(Icons.Default.Badge, null, tint = EcoEmerald400) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Username with Live Unique Check
                            OutlinedTextField(
                                value = username,
                                onValueChange = {
                                    username = it.trim().lowercase()
                                    errorMessage = null
                                    usernameDebounceJob?.cancel()
                                    if (username.length >= 3) {
                                        usernameCheckStatus = "checking"
                                        usernameDebounceJob = scope.launch {
                                            delay(500)
                                            val existing = SupabaseClient.findProfileByUsername(username)
                                            usernameCheckStatus = if (existing != null) "taken" else "available"
                                        }
                                    } else {
                                        usernameCheckStatus = null
                                    }
                                },
                                label = { Text("Username *") },
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = EcoEmerald400) },
                                trailingIcon = {
                                    when (usernameCheckStatus) {
                                        "checking" -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = EcoEmerald400)
                                        "available" -> Icon(Icons.Default.CheckCircle, null, tint = EcoEmerald400)
                                        "taken" -> Icon(Icons.Default.Cancel, null, tint = CoralRed)
                                        else -> null
                                    }
                                },
                                isError = usernameCheckStatus == "taken",
                                supportingText = {
                                    when (usernameCheckStatus) {
                                        "available" -> Text("✓ Username is available", color = EcoEmerald400, fontSize = 11.sp)
                                        "taken" -> Text("❌ Username is already taken", color = CoralRed, fontSize = 11.sp)
                                        "checking" -> Text("Checking availability...", color = Slate400, fontSize = 11.sp)
                                        else -> if (username.isNotBlank() && username.length < 3) {
                                            Text("Minimum 3 characters required", color = Slate400, fontSize = 11.sp)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Mobile with Live Format Check
                            OutlinedTextField(
                                value = mobile,
                                onValueChange = {
                                    mobile = it.filter { ch -> ch.isDigit() || ch == '+' }
                                    errorMessage = null
                                    val digits = mobile.filter { ch -> ch.isDigit() }
                                    val clean10 = if (digits.length == 12 && digits.startsWith("91")) digits.drop(2) else digits
                                    if (clean10.length == 10 && clean10.first() in '6'..'9') {
                                        mobileCheckStatus = "checking"
                                        mobileDebounceJob?.cancel()
                                        mobileDebounceJob = scope.launch {
                                            delay(500)
                                            val formatted = "+91$clean10"
                                            val existing = SupabaseClient.findProfileByMobile(formatted)
                                            mobileCheckStatus = if (existing != null) "taken" else "valid"
                                        }
                                    } else if (clean10.isNotEmpty()) {
                                        mobileCheckStatus = "invalid"
                                    } else {
                                        mobileCheckStatus = null
                                    }
                                },
                                label = { Text("Mobile Number (10 Digits) *") },
                                placeholder = { Text("e.g. 9876543210") },
                                leadingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, null, tint = EcoEmerald400, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+91", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    }
                                },
                                trailingIcon = {
                                    when (mobileCheckStatus) {
                                        "checking" -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = EcoEmerald400)
                                        "valid" -> Icon(Icons.Default.CheckCircle, null, tint = EcoEmerald400)
                                        "invalid", "taken" -> Icon(Icons.Default.Cancel, null, tint = CoralRed)
                                        else -> null
                                    }
                                },
                                isError = mobileCheckStatus == "invalid" || mobileCheckStatus == "taken",
                                supportingText = {
                                    when (mobileCheckStatus) {
                                        "valid" -> Text("✓ Valid Indian mobile (+91)", color = EcoEmerald400, fontSize = 11.sp)
                                        "taken" -> Text("❌ Mobile is already registered. Please sign in.", color = CoralRed, fontSize = 11.sp)
                                        "invalid" -> Text("Enter valid 10 digits starting with 6-9", color = CoralRed, fontSize = 11.sp)
                                        else -> null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Email Address with Live Format Check
                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it.trim()
                                    errorMessage = null
                                    emailCheckStatus = if (email.isBlank()) {
                                        null
                                    } else if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                        "valid"
                                    } else {
                                        "invalid"
                                    }
                                },
                                label = { Text("Email Address *") },
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = EcoEmerald400) },
                                trailingIcon = {
                                    when (emailCheckStatus) {
                                        "valid" -> Icon(Icons.Default.CheckCircle, null, tint = EcoEmerald400)
                                        "invalid" -> Icon(Icons.Default.Cancel, null, tint = CoralRed)
                                        else -> null
                                    }
                                },
                                isError = emailCheckStatus == "invalid",
                                supportingText = {
                                    if (emailCheckStatus == "invalid") {
                                        Text("Please enter a valid email format (name@domain.com)", color = CoralRed, fontSize = 11.sp)
                                    } else if (emailCheckStatus == "valid") {
                                        Text("✓ Valid email address", color = EcoEmerald400, fontSize = 11.sp)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Password with Animated Strength Bar
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = {
                                    newPassword = it
                                    errorMessage = null
                                },
                                label = { Text("Password *") },
                                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = EcoEmerald400) },
                                trailingIcon = {
                                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                        Icon(
                                            imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = Slate400
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                            )

                            // Password Strength Visual Indicator
                            if (newPassword.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                val animatedProgress by animateFloatAsState(
                                    targetValue = passwordStrength.score / 4f,
                                    label = "passwordStrengthProgress"
                                )
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Strength: ${passwordStrength.label}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = passwordStrength.color
                                        )
                                        Text(
                                            text = passwordStrength.suggestion,
                                            fontSize = 10.sp,
                                            color = Slate400
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { animatedProgress },
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                        color = passwordStrength.color,
                                        trackColor = Slate800
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Referral Code (Optional)
                            OutlinedTextField(
                                value = referralCode,
                                onValueChange = {
                                    referralCode = it.uppercase()
                                    referralStatus = if (referralCode.isNotBlank()) {
                                        "valid"
                                    } else null
                                },
                                label = { Text("Referral Code (Optional)") },
                                placeholder = { Text("e.g. ECO100 or friend's code") },
                                leadingIcon = { Icon(Icons.Default.CardGiftcard, null, tint = LeafCoinGold) },
                                supportingText = {
                                    if (referralCode.isNotBlank()) {
                                        Text("🍃 Referral bonus: +50 Leaf Coins upon first collection!", color = LeafCoinGold, fontSize = 11.sp)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Notice: KYC is collected post-registration
                            Surface(
                                color = Slate800.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = EcoEmerald400,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "KYC documents (Aadhaar / Vehicle / GST) can be submitted directly in your Profile after registering.",
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = Slate300
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Mandatory Terms & Privacy Consent
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = agreedToTerms,
                                    onCheckedChange = { agreedToTerms = it },
                                    colors = CheckboxDefaults.colors(checkedColor = EcoEmerald500)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "I agree to Terms & Conditions and Privacy Policy. I authorize EcoVision to manage recycling logistics data.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = Slate300
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            val canSubmit = agreedToTerms &&
                                    fullName.isNotBlank() &&
                                    username.length >= 3 &&
                                    usernameCheckStatus != "taken" &&
                                    mobileCheckStatus == "valid" &&
                                    emailCheckStatus == "valid" &&
                                    newPassword.length >= 6

                            EcoButton(
                                text = if (isLoading) "Creating Account..." else "Create Account",
                                enabled = canSubmit && !isLoading,
                                onClick = {
                                    focusManager.clearFocus()
                                    scope.launch {
                                        isLoading = true
                                        errorMessage = null

                                        val digits = mobile.filter { it.isDigit() }
                                        val clean10 = if (digits.length == 12 && digits.startsWith("91")) digits.drop(2) else digits
                                        val formattedMobile = "+91$clean10"

                                        val newProfile = Profile(
                                            id = java.util.UUID.randomUUID().toString(),
                                            username = username.ifBlank { null },
                                            mobile = formattedMobile,
                                            fullName = fullName.trim(),
                                            email = email.ifBlank { null },
                                            passwordHash = newPassword,
                                            role = selectedRole.code,
                                            kycStatus = "pending",
                                            kycDocumentType = if (selectedRole == UserRole.COMPANY) "gst" else "aadhaar",
                                            kycDocumentNumber = null, // Collected post-registration
                                            defaultAddress = "Kochi, Kerala",
                                            pincode = "682001",
                                            referralCode = "ECO-" + (1000..9999).random().toString(),
                                            referredBy = referralCode.ifBlank { null },
                                            leafCoins = if (referralCode.isNotBlank()) 100 else 50
                                        )

                                        val agentDetails = if (selectedRole == UserRole.AGENT) {
                                            AgentDetails(
                                                agentId = newProfile.id,
                                                vehicleNumber = "PENDING_KYC",
                                                assignedZones = listOf("682001", "682002"),
                                                allowedDays = listOf("Mon", "Wed", "Fri")
                                            )
                                        } else null

                                        val companyDetails = if (selectedRole == UserRole.COMPANY) {
                                            CompanyDetails(
                                                companyId = newProfile.id,
                                                companyName = fullName.trim(),
                                                gstNumber = "PENDING_KYC"
                                            )
                                        } else null

                                        val created = SupabaseClient.createProfile(newProfile, agentDetails, companyDetails)
                                        if (created != null) {
                                            SessionManager.setProfile(created, persist = true)
                                            successMessage = "Account created successfully! Welcome to EcoVision."
                                            delay(500)
                                            onAuthSuccess(created)
                                        } else {
                                            errorMessage = "Registration could not be completed. Please check your network connection."
                                        }
                                        isLoading = false
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Animated Error or Success Badges
            AnimatedVisibility(visible = errorMessage != null) {
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    StatusBadge(text = errorMessage!!, color = CoralRed)
                }
            }

            AnimatedVisibility(visible = successMessage != null) {
                if (successMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    StatusBadge(text = successMessage!!, color = EcoEmerald400)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TestAccountRow(
    roleIcon: String,
    roleTitle: String,
    username: String,
    description: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = Slate800,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = roleIcon, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = roleTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(@$username)",
                        fontSize = 11.sp,
                        color = badgeColor
                    )
                }
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = Slate400,
                    lineHeight = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(EcoEmerald600)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Login",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Password strength calculator helper
data class PasswordStrength(
    val score: Int, // 1 to 4
    val label: String,
    val color: Color,
    val suggestion: String
)

private fun calculatePasswordStrength(pass: String): PasswordStrength {
    if (pass.isEmpty()) return PasswordStrength(0, "", Slate500, "")
    var score = 0
    if (pass.length >= 8) score++
    if (pass.any { it.isUpperCase() } && pass.any { it.isLowerCase() }) score++
    if (pass.any { it.isDigit() }) score++
    if (pass.any { !it.isLetterOrDigit() }) score++

    return when (score) {
        0, 1 -> PasswordStrength(1, "Weak", CoralRed, "Min 8 chars, add numbers & symbols")
        2 -> PasswordStrength(2, "Fair", LeafCoinGold, "Add uppercase letters & symbols")
        3 -> PasswordStrength(3, "Good", Color(0xFF60A5FA), "Almost strong! Add a special symbol")
        else -> PasswordStrength(4, "Strong", EcoEmerald400, "Excellent password ✓")
    }
}
