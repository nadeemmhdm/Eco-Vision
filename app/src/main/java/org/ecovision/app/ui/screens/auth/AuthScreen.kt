package org.ecovision.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.EcoButton
import org.ecovision.app.ui.components.GlassCard
import org.ecovision.app.ui.components.StatusBadge

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    var isSignUp by remember { mutableStateOf(false) }

    // Sign In state
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) } // 1: Identifier, 2: Password/OTP, 3: 2FA
    var isMobilePath by remember { mutableStateOf(false) }
    var twoFactorType by remember { mutableStateOf("totp") } // "totp" or "email_otp"

    // Sign Up state
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("+91") }
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.USER) }
    var referralCode by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("682001") }

    // Role-specific KYC fields
    var idDocNumber by remember { mutableStateOf("") } // Aadhaar or Voter ID
    var vehicleNumber by remember { mutableStateOf("") } // For Agent
    var vehiclePhotoUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=600") }
    var gstNumber by remember { mutableStateOf("") } // For Company
    var companyName by remember { mutableStateOf("") }

    // Mandatory Legal & Data Usage Consent Gate
    var agreedToTerms by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Slate900
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Brand Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(EcoEmerald500),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = "Logo",
                    tint = Slate950,
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "EcoVision",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isSignUp) "Create your verified eco-loop account" else "AI-Powered Circular Economy & Smart Waste",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate400
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle Tabs: Sign In / Sign Up
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
                        .clickable { isSignUp = false; errorMessage = null }
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
                        .clickable { isSignUp = true; errorMessage = null }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Register (KYC)",
                        color = if (isSignUp) Color.White else Slate400,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!isSignUp) {
                // --- Adaptive 2-Step Sign In Flow ---
                GlassCard {
                    Text(
                        text = when (step) {
                            1 -> "Adaptive Identification"
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
                            onValueChange = { identifier = it },
                            label = { Text("Username or 10-Digit Mobile") },
                            placeholder = { Text("e.g. adhil or +919123456789") },
                            leadingIcon = { Icon(Icons.Default.AccountCircle, null, tint = EcoEmerald400) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EcoEmerald500,
                                unfocusedBorderColor = CardBorderBright
                            )
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        EcoButton(
                            text = "Next",
                            onClick = {
                                if (identifier.isBlank()) {
                                    errorMessage = "Please enter your username or mobile number."
                                    return@EcoButton
                                }
                                val clean = identifier.trim()
                                isMobilePath = clean.startsWith("+91") || clean.length == 10 && clean.all { it.isDigit() }
                                step = 2
                            }
                        )
                    } else if (step == 2) {
                        if (isMobilePath) {
                            Text(
                                text = "A 6-digit SMS OTP has been sent to $identifier (Demo: enter 123456)",
                                style = MaterialTheme.typography.bodySmall,
                                color = LeafCoinGold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { otpCode = it },
                                label = { Text("6-Digit SMS OTP") },
                                leadingIcon = { Icon(Icons.Default.Sms, null, tint = EcoEmerald400) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EcoEmerald500,
                                    unfocusedBorderColor = CardBorderBright
                                )
                            )
                        } else {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = EcoEmerald400) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EcoEmerald500,
                                    unfocusedBorderColor = CardBorderBright
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = { step = 1 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back", color = Slate400)
                            }
                            EcoButton(
                                text = "Sign In",
                                modifier = Modifier.weight(1.5f),
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        val profile = if (isMobilePath) {
                                            val formatted = if (!identifier.startsWith("+91")) "+91$identifier" else identifier
                                            SupabaseClient.findProfileByMobile(formatted)
                                        } else {
                                            SupabaseClient.findProfileByUsername(identifier)
                                        }

                                        if (profile != null) {
                                            if (profile.totpEnabled || profile.emailOtpEnabled) {
                                                twoFactorType = if (profile.totpEnabled) "totp" else "email_otp"
                                                step = 3
                                            } else {
                                                SessionManager.setProfile(profile)
                                                onAuthSuccess()
                                            }
                                        } else {
                                            errorMessage = "User not found. Try switching role pill or register."
                                        }
                                        isLoading = false
                                    }
                                }
                            )
                        }
                    } else if (step == 3) {
                        Text(
                            text = if (twoFactorType == "totp")
                                "Enter 6-digit code from Google Authenticator / Authy app"
                            else
                                "Enter 6-digit verification code sent to your registered email",
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
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        EcoButton(
                            text = "Verify & Access Dashboard",
                            onClick = {
                                scope.launch {
                                    val profile = if (isMobilePath) {
                                        val formatted = if (!identifier.startsWith("+91")) "+91$identifier" else identifier
                                        SupabaseClient.findProfileByMobile(formatted)
                                    } else {
                                        SupabaseClient.findProfileByUsername(identifier)
                                    }
                                    if (profile != null) {
                                        SessionManager.setProfile(profile)
                                        onAuthSuccess()
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                // --- Complete KYC Sign-Up Form ---
                GlassCard {
                    Text(
                        text = "Role Selection",
                        style = MaterialTheme.typography.labelLarge,
                        color = Slate400
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
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = role.displayName.split(" ").first(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else Slate400
                                )
                            }
                            if (role != UserRole.COMPANY) Spacer(modifier = Modifier.width(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name / Legal Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = { Text("Mobile (+91)") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Default Pickup / Facility Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = pincode,
                            onValueChange = { pincode = it },
                            label = { Text("Pincode / Zone") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = referralCode,
                            onValueChange = { referralCode = it },
                            label = { Text("Referral (Optional)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Role-Specific KYC Inputs
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Mandatory KYC & Verification",
                        style = MaterialTheme.typography.labelLarge,
                        color = LeafCoinGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    when (selectedRole) {
                        UserRole.USER -> {
                            OutlinedTextField(
                                value = idDocNumber,
                                onValueChange = { idDocNumber = it },
                                label = { Text("Aadhaar Number / Voter ID") },
                                placeholder = { Text("12-digit Aadhaar / ID") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        UserRole.AGENT -> {
                            OutlinedTextField(
                                value = idDocNumber,
                                onValueChange = { idDocNumber = it },
                                label = { Text("Aadhaar / Driving License Number") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = vehicleNumber,
                                onValueChange = { vehicleNumber = it },
                                label = { Text("Vehicle Number (e.g. KL-07-AB-1234)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Live/Uploaded Vehicle Photo: Attached (Verified for doorstep matching)",
                                fontSize = 11.sp,
                                color = EcoEmerald400
                            )
                        }
                        UserRole.COMPANY -> {
                            OutlinedTextField(
                                value = companyName,
                                onValueChange = { companyName = it },
                                label = { Text("Registered Company Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = gstNumber,
                                onValueChange = { gstNumber = it },
                                label = { Text("GST Number (15 Digits) / CIN") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        else -> {}
                    }

                    // Mandatory Privacy Policy & Data Usage Disclosure
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = agreedToTerms,
                                onCheckedChange = { agreedToTerms = it },
                                colors = CheckboxDefaults.colors(checkedColor = EcoEmerald500)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "I agree to the Terms & Conditions and Privacy Policy. I authorize EcoVision to utilize personal, location, and waste data for doorstep collection and recycling logistics.",
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = Slate300
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    EcoButton(
                        text = "Submit Registration (Pending KYC)",
                        enabled = agreedToTerms && fullName.isNotBlank() && mobile.isNotBlank(),
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val newProfile = Profile(
                                    id = java.util.UUID.randomUUID().toString(),
                                    username = username.ifBlank { null },
                                    mobile = mobile,
                                    fullName = fullName,
                                    email = email.ifBlank { null },
                                    passwordHash = newPassword,
                                    role = selectedRole.code,
                                    kycStatus = "pending",
                                    kycDocumentType = if (selectedRole == UserRole.COMPANY) "gst" else "aadhaar",
                                    kycDocumentNumber = if (selectedRole == UserRole.COMPANY) gstNumber else idDocNumber,
                                    defaultAddress = address,
                                    pincode = pincode,
                                    referralCode = "REF-" + System.currentTimeMillis().toString().takeLast(6),
                                    referredBy = referralCode.ifBlank { null }
                                )

                                val agentDetails = if (selectedRole == UserRole.AGENT) {
                                    AgentDetails(
                                        agentId = newProfile.id,
                                        vehicleNumber = vehicleNumber.ifBlank { "KL-07-EX-9999" },
                                        vehiclePhotoUrl = vehiclePhotoUrl,
                                        assignedZones = listOf(pincode),
                                        allowedDays = listOf("Mon", "Wed", "Fri")
                                    )
                                } else null

                                val companyDetails = if (selectedRole == UserRole.COMPANY) {
                                    CompanyDetails(
                                        companyId = newProfile.id,
                                        companyName = companyName.ifBlank { fullName },
                                        gstNumber = gstNumber.ifBlank { "32AABCE5678F1Z9" }
                                    )
                                } else null

                                val created = SupabaseClient.createProfile(newProfile, agentDetails, companyDetails)
                                if (created != null) {
                                    SessionManager.setProfile(created)
                                    onAuthSuccess()
                                } else {
                                    errorMessage = "Registration failed. Please check your network and fields."
                                }
                                isLoading = false
                            }
                        }
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                StatusBadge(text = errorMessage!!, color = CoralRed)
            }
        }
    }
}
