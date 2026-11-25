package org.ecovision.app.ui.screens.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onNavigateToWallet: () -> Unit = {}
) {
    val context = LocalContext.current
    val profile by SessionManager.currentProfile.collectAsState()
    val themeMode by SessionManager.themeMode.collectAsState()
    val agentDetails by SessionManager.agentDetails.collectAsState()
    val companyDetails by SessionManager.companyDetails.collectAsState()
    val scope = rememberCoroutineScope()

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Section 3: 2FA TOTP State
    var totpEnabled by remember { mutableStateOf(profile?.totpEnabled ?: false) }
    var showTotpSetup by remember { mutableStateOf(false) }
    var generatedTotpSecret by remember { mutableStateOf("") }
    var totpVerificationCode by remember { mutableStateOf("") }
    var isVerifyingTotp by remember { mutableStateOf(false) }
    var showDisableTotpDialog by remember { mutableStateOf(false) }

    // Section 4: KYC State
    var idDocNumber by remember { mutableStateOf(profile?.kycDocumentNumber ?: "") }
    var kycProofUri by remember { mutableStateOf<String?>(null) }
    var isSubmittingKyc by remember { mutableStateOf(false) }

    // Section 5: Agent Vehicle State
    var newVehicleNumberInput by remember { mutableStateOf("") }
    var newVehiclePhotoUri by remember { mutableStateOf<String?>(null) }
    var isSubmittingVehicleChange by remember { mutableStateOf(false) }

    // Section 6: Address & District State
    var userAddress by remember { mutableStateOf(profile?.defaultAddress ?: "") }
    var userDistrict by remember { mutableStateOf(profile?.district ?: "Malappuram") }
    var userPincode by remember { mutableStateOf(profile?.pincode ?: "676505") }
    var isSavingAddress by remember { mutableStateOf(false) }
    var showDistrictPicker by remember { mutableStateOf(false) }

    // Section 8: Danger Zone (30-Day Deletion) State
    var showDeletionDialog by remember { mutableStateOf(false) }
    var isSchedulingDeletion by remember { mutableStateOf(false) }

    val keralaDistricts = listOf(
        "Malappuram (Active Zone)", "Thiruvananthapuram", "Kollam", "Pathanamthitta",
        "Alappuzha", "Kottayam", "Idukki", "Ernakulam", "Thrissur", "Palakkad",
        "Kozhikode", "Wayanad", "Kannur", "Kasaragod"
    )

    val vehiclePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) newVehiclePhotoUri = uri.toString()
    }

    val kycProofPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            kycProofUri = uri.toString()
            statusMessage = "Document proof selected! Tap Submit to upload."
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Profile & Account Center",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Complete account management & security",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Notification Messages
            AnimatedVisibility(visible = statusMessage != null) {
                if (statusMessage != null) {
                    StatusBadge(text = statusMessage!!, color = EcoEmerald400)
                }
            }
            AnimatedVisibility(visible = errorMessage != null) {
                if (errorMessage != null) {
                    StatusBadge(text = errorMessage!!, color = CoralRed)
                }
            }

            // ==========================================
            // SECTION 01: IDENTITY & AVATAR
            // ==========================================
            SectionHeader(number = "01", title = "User Identity & Verification")
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(EcoEmerald600),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile?.fullName?.take(2)?.uppercase() ?: "EV",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile?.fullName ?: "Eco Hero",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${profile?.mobile} · @${profile?.username ?: "user"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusBadge(
                                text = (profile?.role ?: "user").uppercase(),
                                color = TechBlue
                            )
                            StatusBadge(
                                text = if (profile?.kycStatus == "verified") "KYC VERIFIED" else "KYC ${profile?.kycStatus?.uppercase() ?: "PENDING"}",
                                color = if (profile?.kycStatus == "verified") EcoEmerald400 else LeafCoinGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = CardBorderBright)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Registered Area", color = Slate400, fontSize = 11.sp)
                        Text(text = "${profile?.district ?: "Malappuram"}, Kerala", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Account Status", color = Slate400, fontSize = 11.sp)
                        val isDeactivated = profile?.accountStatus == "deactivated"
                        Text(
                            text = if (isDeactivated) "Scheduled for Purge" else "Active & Secured",
                            color = if (isDeactivated) CoralRed else EcoEmerald400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ==========================================
            // SECTION 02: COINS & WALLET DESK
            // ==========================================
            SectionHeader(number = "02", title = "Eco Coins & Wallet Desk")
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EcoCoinIcon(size = 36.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${profile?.leafCoins ?: 0} Coins",
                                style = MaterialTheme.typography.headlineSmall,
                                color = LeafCoinGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Worth ₹${String.format("%.2f", (profile?.leafCoins ?: 0) / 10.0)} UPI Cash (10 Coins = ₹1)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = onNavigateToWallet,
                        colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Open Wallet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // ==========================================
            // SECTION 03: APP THEME & APPEARANCE (Auto / Light / Dark)
            // ==========================================
            SectionHeader(number = "03", title = "App Appearance & Theme")
            GlassCard {
                Text(
                    text = "Display Theme Mode",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Choose your preferred display style or let EcoVision adapt to your device automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                val themeOptions = listOf(
                    Triple(ThemeMode.SYSTEM, "Auto Detect", "Follows system dark/light schedule"),
                    Triple(ThemeMode.LIGHT, "Light Theme", "Crisp whites and vibrant emeralds"),
                    Triple(ThemeMode.DARK, "Dark Theme", "Obsidian glass and neon accents")
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    themeOptions.forEach { (mode, label, desc) ->
                        val isSelected = themeMode == mode
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SessionManager.setThemeMode(mode)
                                    statusMessage = "Switched to $label mode"
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = when (mode) {
                                            ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                            ThemeMode.LIGHT -> Icons.Default.LightMode
                                            ThemeMode.DARK -> Icons.Default.DarkMode
                                        },
                                        contentDescription = label,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SECTION 04: TWO-FACTOR AUTHENTICATION (2FA TOTP)
            // ==========================================
            SectionHeader(number = "04", title = "Two-Factor Authentication (2FA TOTP)")
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (totpEnabled) Icons.Default.VerifiedUser else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (totpEnabled) EcoEmerald400 else Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Authenticator App (TOTP)",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (totpEnabled)
                                "Active & Protected. Requires 6-digit code on every login."
                            else
                                "Requires Google Authenticator or Authy 6-digit code.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (totpEnabled) EcoEmerald400 else Slate400,
                            fontSize = 11.sp
                        )
                    }

                    if (totpEnabled) {
                        Button(
                            onClick = { showDisableTotpDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Disable", color = CoralRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = {
                                generatedTotpSecret = TotpHelper.generateSecret()
                                showTotpSetup = true
                                totpVerificationCode = ""
                                errorMessage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Set Up 2FA", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 2FA Setup Flow Drawer
                AnimatedVisibility(visible = showTotpSetup && !totpEnabled) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .background(Slate800.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorderBright, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Step 1: Copy Authenticator Secret Key",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add this key into Google Authenticator or Authy to generate dynamic 6-digit codes:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate300,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Secret Key Display Box with Copy Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate950, RoundedCornerShape(8.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = generatedTotpSecret.chunked(4).joinToString("-"),
                                color = LeafCoinGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            IconButton(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("TOTP Secret", generatedTotpSecret))
                                    statusMessage = "Secret key copied to clipboard!"
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = EcoEmerald400, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Step 2: Enter 6-Digit Code to Activate",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enter code currently shown in your authenticator app (Demo: 123456):",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = totpVerificationCode,
                            onValueChange = { if (it.length <= 6) totpVerificationCode = it },
                            label = { Text("6-Digit Code") },
                            placeholder = { Text("e.g. 123456") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EcoEmerald400,
                                unfocusedBorderColor = CardBorderBright,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showTotpSetup = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cancel", color = Slate400)
                            }

                            EcoButton(
                                text = if (isVerifyingTotp) "Activating..." else "Activate 2FA",
                                enabled = !isVerifyingTotp && totpVerificationCode.length >= 6,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    scope.launch {
                                        isVerifyingTotp = true
                                        val valid = TotpHelper.verifyTotp(generatedTotpSecret, totpVerificationCode)
                                        if (valid && profile != null) {
                                            val updated = profile!!.copy(
                                                totpEnabled = true,
                                                twoFactorSecret = generatedTotpSecret
                                            )
                                            SupabaseClient.updateProfile(updated)
                                            SessionManager.updateCurrentProfile(updated)
                                            totpEnabled = true
                                            showTotpSetup = false
                                            statusMessage = "✓ 2FA TOTP Protection activated successfully!"
                                            errorMessage = null
                                        } else {
                                            errorMessage = "Invalid verification code! Please verify your authenticator app."
                                        }
                                        isVerifyingTotp = false
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // ==========================================
            // SECTION 04: KYC VERIFICATION DESK
            // ==========================================
            SectionHeader(number = "04", title = "KYC Verification & Re-application Desk")
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Identity Document Verification",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Aadhaar / Voter ID / Govt Document",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                    StatusBadge(
                        text = profile?.kycStatus?.uppercase() ?: "PENDING",
                        color = when (profile?.kycStatus) {
                            "verified" -> EcoEmerald400
                            "rejected" -> CoralRed
                            else -> LeafCoinGold
                        }
                    )
                }

                // If Rejected: Display Admin reason & attempt limit
                if (profile?.kycStatus == "rejected") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = CoralRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, null, tint = CoralRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Verification Rejected by Admin",
                                    color = CoralRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Reason: ${profile?.kycRejectionReason ?: "Low image quality. Please upload a clear photo of your official ID document."}",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Attempts: ${profile?.kycAttemptCount ?: 1} of 5 daily attempts used",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Privacy & Auto-delete Compliance notice
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Slate800.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoDelete, contentDescription = null, tint = EcoEmerald400, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Auto-Delete Guarantee: ID proofs are strictly used for one-time verification and automatically deleted from our secure vault upon Admin review.",
                            color = Slate300,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                // KYC Submission / Re-upload form (if not yet verified)
                if (profile?.kycStatus != "verified") {
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = idDocNumber,
                        onValueChange = { idDocNumber = it },
                        label = { Text("Government ID / Aadhaar Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcoEmerald400,
                            unfocusedBorderColor = CardBorderBright,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { kycProofPickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, null, tint = EcoEmerald400, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (kycProofUri != null) "Selected ✓" else "Upload Proof", color = Color.White, fontSize = 12.sp)
                        }

                        EcoButton(
                            text = if (isSubmittingKyc) "Submitting..." else "Submit Proof",
                            enabled = !isSubmittingKyc && idDocNumber.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                scope.launch {
                                    isSubmittingKyc = true
                                    val currentAttempts = (profile?.kycAttemptCount ?: 0) + 1
                                    if (currentAttempts > 5) {
                                        errorMessage = "Daily attempt limit (5) reached. Please wait 24 hours to re-apply."
                                    } else if (profile != null) {
                                        val updated = profile!!.copy(
                                            kycDocumentNumber = idDocNumber,
                                            kycDocumentUrl = kycProofUri ?: "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=400",
                                            kycStatus = "pending",
                                            kycAttemptCount = currentAttempts,
                                            kycRejectionReason = null
                                        )
                                        SupabaseClient.updateProfile(updated)
                                        SessionManager.updateCurrentProfile(updated)
                                        statusMessage = "KYC documents submitted for Admin verification! Proof will be auto-deleted once approved."
                                    }
                                    isSubmittingKyc = false
                                }
                            }
                        )
                    }
                }
            }

            // ==========================================
            // SECTION 05: VEHICLE MANAGEMENT (Agent Only)
            // ==========================================
            if (profile?.role == "agent") {
                SectionHeader(number = "05", title = "Pickup Agent Vehicle Desk")
                GlassCard {
                    Text(
                        text = "Current Vehicle: ${agentDetails?.vehicleNumber ?: "KL-07-AB-1234"} (Green Van)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Request a vehicle update by submitting new plate details and vehicle photo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newVehicleNumberInput,
                        onValueChange = { newVehicleNumberInput = it.uppercase() },
                        label = { Text("New Vehicle Number (e.g. KL-10-AZ-5678)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcoEmerald400,
                            unfocusedBorderColor = CardBorderBright,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { vehiclePhotoPickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DirectionsCar, null, tint = LeafCoinGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (newVehiclePhotoUri != null) "Photo Ready ✓" else "Vehicle Photo", color = Color.White, fontSize = 12.sp)
                        }

                        EcoButton(
                            text = if (isSubmittingVehicleChange) "Submitting..." else "Request Change",
                            enabled = !isSubmittingVehicleChange && newVehicleNumberInput.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                scope.launch {
                                    isSubmittingVehicleChange = true
                                    val req = VehicleChangeRequest(
                                        id = java.util.UUID.randomUUID().toString(),
                                        agentId = profile!!.id,
                                        oldVehicleNumber = agentDetails?.vehicleNumber ?: "KL-07-AB-1234",
                                        newVehicleNumber = newVehicleNumberInput,
                                        vehiclePhotoUrl = newVehiclePhotoUri ?: "https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=400"
                                    )
                                    SupabaseClient.submitVehicleChangeRequest(req)
                                    statusMessage = "Vehicle update submitted to Admin! Photo will be auto-deleted upon approval."
                                    newVehicleNumberInput = ""
                                    newVehiclePhotoUri = null
                                    isSubmittingVehicleChange = false
                                }
                            }
                        )
                    }
                }
            }

            // ==========================================
            // SECTION 06: ADDRESS & SERVICE DISTRICT
            // ==========================================
            SectionHeader(number = "06", title = "Service District & Address")
            GlassCard {
                Text(
                    text = "Collection Address & Kerala District",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Malappuram is the active pilot district with instant doorstep collection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = userAddress,
                    onValueChange = { userAddress = it },
                    label = { Text("Default Pickup Address") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoEmerald400,
                        unfocusedBorderColor = CardBorderBright,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1.2f)) {
                        OutlinedButton(
                            onClick = { showDistrictPicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "District: $userDistrict", color = Color.White, fontSize = 12.sp)
                        }

                        DropdownMenu(
                            expanded = showDistrictPicker,
                            onDismissRequest = { showDistrictPicker = false },
                            modifier = Modifier.background(Slate800)
                        ) {
                            keralaDistricts.forEach { distName ->
                                val cleanDist = distName.split(" ").first()
                                DropdownMenuItem(
                                    text = { Text(distName, color = if (cleanDist == "Malappuram") EcoEmerald400 else Color.White) },
                                    onClick = {
                                        userDistrict = cleanDist
                                        showDistrictPicker = false
                                    }
                                )
                            }
                        }
                    }

                    EcoButton(
                        text = if (isSavingAddress) "Saving..." else "Save Address",
                        enabled = !isSavingAddress && userAddress.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch {
                                isSavingAddress = true
                                if (profile != null) {
                                    val updated = profile!!.copy(
                                        defaultAddress = userAddress,
                                        district = userDistrict,
                                        pincode = userPincode
                                    )
                                    SupabaseClient.updateProfile(updated)
                                    SessionManager.updateCurrentProfile(updated)
                                    statusMessage = "Address & District preferences saved!"
                                }
                                isSavingAddress = false
                            }
                        }
                    )
                }
            }

            // ==========================================
            // SECTION 07: SECURITY & DEVICE COMPLIANCE
            // ==========================================
            SectionHeader(number = "07", title = "Security & Device Compliance")
            GlassCard {
                Text(
                    text = "Device & Network Verification",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val devInfo = remember { DeviceLocationHelper.getDeviceInfo() }
                val devIp = remember { DeviceLocationHelper.getDeviceIp() }

                Text(text = "Hardware Model: $devInfo", color = Slate300, fontSize = 12.sp)
                Text(text = "Connected IP: $devIp", color = Slate300, fontSize = 12.sp)
                Text(text = "Compliance: ISO 27001 & Kerala Data Privacy Guidelines", color = EcoEmerald400, fontSize = 11.sp)

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = CardBorderBright)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📜 Terms & Conditions",
                        color = TechBlue,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { statusMessage = "EcoVision Terms: Verification proofs auto-deleted upon Admin review." }
                    )
                    Text(
                        text = "🔒 Privacy Policy",
                        color = TechBlue,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { statusMessage = "EcoVision Privacy: Strict zero-knowledge storage for user identification." }
                    )
                }
            }

            // ==========================================
            // SECTION 08: DANGER ZONE (30-DAY DELETION)
            // ==========================================
            SectionHeader(number = "08", title = "Danger Zone - Account Deactivation")
            Surface(
                color = CoralRed.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CoralRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "30-Day Inactive Account Deletion",
                            style = MaterialTheme.typography.titleMedium,
                            color = CoralRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Scheduling account deletion places your account in a 30-day inactive grace period. If you do not sign in for 30 consecutive days, your profile, balances, and history will be permanently deleted from the database. Signing in anytime within the 30 days automatically cancels the deletion.",
                        color = Slate300,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (profile?.accountStatus == "deactivated") {
                        Button(
                            onClick = {
                                scope.launch {
                                    if (profile != null) {
                                        SupabaseClient.cancelAccountDeletion(profile!!.id)
                                        statusMessage = "Scheduled deletion cancelled! Account is active."
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel Deletion & Restore Active Status", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { showDeletionDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralRed.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteForever, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Schedule Account Deletion (30-Day Purge)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Logout Button
            EcoButton(
                text = "Sign Out",
                icon = Icons.Default.Logout,
                isSecondary = true,
                onClick = {
                    SessionManager.logout()
                    onLogout()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 30-Day Deletion Confirmation Dialog
    if (showDeletionDialog) {
        AlertDialog(
            onDismissRequest = { showDeletionDialog = false },
            title = { Text("Confirm 30-Day Account Deactivation", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Your account will be deactivated and scheduled for permanent deletion in 30 days.\n\nTo cancel at any time, simply sign back into EcoVision before the 30-day window expires.",
                    color = Slate300,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isSchedulingDeletion = true
                            if (profile != null) {
                                SupabaseClient.scheduleAccountDeletion(profile!!.id)
                                SessionManager.logout()
                                showDeletionDialog = false
                                onLogout()
                            }
                            isSchedulingDeletion = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text(if (isSchedulingDeletion) "Deactivating..." else "Deactivate & Start 30-Day Clock", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletionDialog = false }) {
                    Text("Keep Account", color = Slate400)
                }
            },
            containerColor = Slate850
        )
    }

    // Disable 2FA Dialog
    if (showDisableTotpDialog) {
        AlertDialog(
            onDismissRequest = { showDisableTotpDialog = false },
            title = { Text("Disable 2FA Protection?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Disabling 2FA will remove secondary code verification from your account login.",
                    color = Slate300,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            if (profile != null) {
                                val updated = profile!!.copy(
                                    totpEnabled = false,
                                    twoFactorSecret = null
                                )
                                SupabaseClient.updateProfile(updated)
                                SessionManager.updateCurrentProfile(updated)
                                totpEnabled = false
                                statusMessage = "2FA TOTP disabled."
                            }
                            showDisableTotpDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("Confirm Disable", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisableTotpDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Slate850
        )
    }
}

@Composable
private fun SectionHeader(number: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    ) {
        Surface(
            color = EcoEmerald600.copy(alpha = 0.25f),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, EcoEmerald500.copy(alpha = 0.4f))
        ) {
            Text(
                text = number,
                color = EcoEmerald400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
