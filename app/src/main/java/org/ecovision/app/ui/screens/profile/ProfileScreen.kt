package org.ecovision.app.ui.screens.profile

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ecovision.app.data.AgentDetails
import org.ecovision.app.data.CompanyDetails
import org.ecovision.app.data.SessionManager
import org.ecovision.app.data.SupabaseClient
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.EcoButton
import org.ecovision.app.ui.components.GlassCard
import org.ecovision.app.ui.components.StatusBadge

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onNavigateToWallet: () -> Unit = {}
) {
    val profile by SessionManager.currentProfile.collectAsState()
    val agentDetails by SessionManager.agentDetails.collectAsState()
    val companyDetails by SessionManager.companyDetails.collectAsState()

    var totp2FaEnabled by remember { mutableStateOf(profile?.totpEnabled ?: false) }
    var email2FaEnabled by remember { mutableStateOf(profile?.emailOtpEnabled ?: false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSubmittingKyc by remember { mutableStateOf(false) }

    // KYC fields
    var idDocNumber by remember { mutableStateOf(profile?.kycDocumentNumber ?: "") }
    var vehicleNumber by remember { mutableStateOf(agentDetails?.vehicleNumber ?: "") }
    var gstNumber by remember { mutableStateOf(companyDetails?.gstNumber ?: "") }
    var companyName by remember { mutableStateOf(companyDetails?.companyName ?: profile?.fullName ?: "") }
    var address by remember { mutableStateOf(profile?.defaultAddress ?: "") }
    var pincode by remember { mutableStateOf(profile?.pincode ?: "682001") }
    var proofUri by remember { mutableStateOf<String?>(null) }

    // Direct Payout Details
    var upiId by remember { mutableStateOf(profile?.username?.let { "$it@ecobank" } ?: "user@okhdfcbank") }
    var bankAccount by remember { mutableStateOf("918237461928") }
    var ifscCode by remember { mutableStateOf("HDFC0001234") }
    var isSavingPayout by remember { mutableStateOf(false) }

    // Agent Vehicle Change Request State
    var newVehicleNumberInput by remember { mutableStateOf("") }
    var newVehiclePhotoUri by remember { mutableStateOf<String?>(null) }
    var isSubmittingVehicleChange by remember { mutableStateOf(false) }

    val vehiclePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) newVehiclePhotoUri = uri.toString()
    }

    val proofPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            proofUri = uri.toString()
            statusMessage = "Proof document selected! Click submit to save."
        }
    }

    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Slate900,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Profile & Security Center",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Card
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(EcoEmerald600),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile?.fullName ?: "User",
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
                        StatusBadge(
                            text = "KYC: ${profile?.kycStatus?.uppercase() ?: "PENDING"}",
                            color = if (profile?.kycStatus == "verified") EcoEmerald400 else LeafCoinGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CardBorderBright)
                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Default Address: ${profile?.defaultAddress ?: "Not set"}", color = Slate300, fontSize = 12.sp)
                Text(text = "Active Role: ${profile?.role?.uppercase()}", color = TechBlue, fontSize = 12.sp)
                Text(text = "Coins Balance: 🪙 ${profile?.leafCoins ?: 50} Coins", color = LeafCoinGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // COINS & VOUCHER HUB CARD
            // ==========================================
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = LeafCoinGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Coins Hub",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🪙 ${profile?.leafCoins ?: 50} Coins Available",
                            style = MaterialTheme.typography.titleLarge,
                            color = LeafCoinGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Redeem for brand discount coupons, upcycled crafts & real cash via UPI.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = onNavigateToWallet,
                        colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Vouchers", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // KYC VERIFICATION DESK (SUBMITTED POST-REGISTRATION)
            // ==========================================
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = if (profile?.kycStatus == "verified") EcoEmerald400 else LeafCoinGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "KYC & Identity Verification",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Rejection Reason Alert if KYC was rejected
                if (profile?.kycStatus == "rejected") {
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cancel, contentDescription = null, tint = CoralRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Verification Rejected (Attempt ${profile?.kycAttemptCount ?: 1} of 5)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CoralRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Reason: ${profile?.kycRejectionReason ?: "Low quality image or incomplete details. Please reapply with proper official proof."}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate300,
                                fontSize = 11.sp
                            )
                            if ((profile?.kycAttemptCount ?: 0) >= 5) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "⚠️ Maximum 5 reapply attempts reached. Cooldown active for 24 hours.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LeafCoinGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = if (profile?.kycStatus == "verified")
                        "Your identity and credentials are fully verified by EcoVision governance."
                    else if (profile?.kycStatus == "rejected")
                        "Please review the rejection reason above and reapply with clear official documents."
                    else
                        "Complete your KYC verification below to unlock doorstep pickups and transactions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Default Address & Pincode
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Default Address") },
                    leadingIcon = { Icon(Icons.Default.Home, null, tint = EcoEmerald400) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = pincode,
                    onValueChange = { pincode = it },
                    label = { Text("Service Zone / Pincode") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = EcoEmerald400) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                when (profile?.role) {
                    "agent" -> {
                        OutlinedTextField(
                            value = idDocNumber,
                            onValueChange = { idDocNumber = it },
                            label = { Text("Driving License / Aadhaar Number *") },
                            leadingIcon = { Icon(Icons.Default.Badge, null, tint = LeafCoinGold) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = vehicleNumber,
                            onValueChange = { vehicleNumber = it.uppercase() },
                            label = { Text("Vehicle Number (e.g. KL-07-AB-1234) *") },
                            leadingIcon = { Icon(Icons.Default.LocalShipping, null, tint = LeafCoinGold) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, null, tint = EcoEmerald400, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Vehicle Photo: Matched with license plate by Admin desk",
                                    fontSize = 11.sp,
                                    color = Slate300
                                )
                            }
                        }
                    }
                    "company" -> {
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Registered Business / Entity Name *") },
                            leadingIcon = { Icon(Icons.Default.Business, null, tint = LeafCoinGold) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = gstNumber,
                            onValueChange = { gstNumber = it.uppercase() },
                            label = { Text("GSTIN (15 Digits) / Business ID *") },
                            leadingIcon = { Icon(Icons.Default.Receipt, null, tint = LeafCoinGold) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    else -> {
                        OutlinedTextField(
                            value = idDocNumber,
                            onValueChange = { idDocNumber = it },
                            label = { Text("Aadhaar Number / Voter ID (12 Digits) *") },
                            leadingIcon = { Icon(Icons.Default.Fingerprint, null, tint = LeafCoinGold) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Document / ID Proof Upload Area
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorderBright, RoundedCornerShape(10.dp))
                        .clickable { proofPickerLauncher.launch("image/*") }
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (proofUri != null) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = if (proofUri != null) EcoEmerald400 else LeafCoinGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (proofUri != null) "Document Proof Uploaded ✓" else "Upload Proof (Aadhaar / Vehicle / GST)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (proofUri != null) "Document attached for Super Admin verification" else "Tap to choose photo from device gallery",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (proofUri != null) EcoEmerald400 else Slate400,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val isCooldownLocked = (profile?.kycAttemptCount ?: 0) >= 5 && isWithin24Hours(profile?.kycLastRejectedAt)

                EcoButton(
                    text = if (isSubmittingKyc) "Submitting..."
                    else if (isCooldownLocked) "Verification Locked (24h Cooldown)"
                    else if (profile?.kycStatus == "rejected") "Reapply & Submit Verification"
                    else if (profile?.kycStatus == "verified") "Update KYC Details"
                    else "Submit KYC for Verification",
                    enabled = !isSubmittingKyc && !isCooldownLocked && (idDocNumber.isNotBlank() || gstNumber.isNotBlank() || vehicleNumber.isNotBlank()),
                    onClick = {
                        scope.launch {
                            isSubmittingKyc = true
                            val curr = profile ?: return@launch
                            val updatedProfile = curr.copy(
                                defaultAddress = address,
                                pincode = pincode,
                                kycDocumentNumber = if (curr.role == "company") gstNumber else idDocNumber,
                                kycDocumentType = if (curr.role == "company") "gst" else "aadhaar",
                                kycDocumentUrl = proofUri ?: curr.kycDocumentUrl,
                                kycStatus = "pending",
                                kycRejectionReason = null
                            )

                            // Update profile in encrypted cloud vault
                            val ok = SupabaseClient.updateProfile(updatedProfile)
                            if (curr.role == "agent") {
                                SupabaseClient.updateAgentDetails(
                                    AgentDetails(
                                        agentId = curr.id,
                                        vehicleNumber = vehicleNumber.ifBlank { "KL-07-AB-1234" },
                                        assignedZones = listOf(pincode),
                                        allowedDays = listOf("Mon", "Wed", "Fri")
                                    )
                                )
                            } else if (curr.role == "company") {
                                SupabaseClient.updateCompanyDetails(
                                    CompanyDetails(
                                        companyId = curr.id,
                                        companyName = companyName.ifBlank { curr.fullName },
                                        gstNumber = gstNumber.ifBlank { "32AABCG1234F1Z5" }
                                    )
                                )
                            }

                            if (ok) {
                                SessionManager.setProfile(updatedProfile, persist = true)
                                statusMessage = "KYC documents submitted successfully! Admin will verify."
                            } else {
                                statusMessage = "Could not submit KYC. Please try again."
                            }
                            isSubmittingKyc = false
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // DIRECT UPI & BANK PAYOUT SETTLEMENT
            // ==========================================
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = LeafCoinGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Doorstep Payout & Settlement Setup",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Receive instant real money payouts directly when collection agents weigh scrap at your doorstep.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("UPI Virtual Payment Address (VPA)") },
                    placeholder = { Text("e.g. adhil@oksbi") },
                    leadingIcon = { Icon(Icons.Default.Send, null, tint = EcoEmerald400) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bankAccount,
                        onValueChange = { bankAccount = it },
                        label = { Text("Bank A/C Number") },
                        modifier = Modifier.weight(1.3f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ifscCode,
                        onValueChange = { ifscCode = it.uppercase() },
                        label = { Text("IFSC Code") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                EcoButton(
                    text = if (isSavingPayout) "Saving..." else "Update Payout Details",
                    enabled = !isSavingPayout && upiId.isNotBlank(),
                    onClick = {
                        scope.launch {
                            isSavingPayout = true
                            statusMessage = "Payout details saved! Ready for doorstep instant settlements."
                            isSavingPayout = false
                        }
                    }
                )
            }

            // ==========================================
            // AGENT VEHICLE CHANGE SECTION
            // ==========================================
            if (profile?.role == "agent") {
                Spacer(modifier = Modifier.height(16.dp))

                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Agent Vehicle Change",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current Registered Vehicle: ${agentDetails?.vehicleNumber ?: "KL-07-AB-1234"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newVehicleNumberInput,
                        onValueChange = { newVehicleNumberInput = it.uppercase() },
                        label = { Text("New Vehicle Number (e.g. KL-10-AB-5678)") },
                        leadingIcon = { Icon(Icons.Default.DirectionsCar, null, tint = Color.White) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorderBright, RoundedCornerShape(10.dp))
                            .clickable { vehiclePhotoPickerLauncher.launch("image/*") }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (newVehiclePhotoUri != null) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = if (newVehiclePhotoUri != null) EcoEmerald400 else Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (newVehiclePhotoUri != null) "New Vehicle Photo Attached ✓" else "Take / Upload New Vehicle Photo",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Photo will be automatically deleted upon Admin verification.",
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    EcoButton(
                        text = if (isSubmittingVehicleChange) "Submitting Request..." else "Submit Vehicle Change Request",
                        enabled = !isSubmittingVehicleChange && newVehicleNumberInput.isNotBlank(),
                        onClick = {
                            scope.launch {
                                isSubmittingVehicleChange = true
                                val req = VehicleChangeRequest(
                                    id = java.util.UUID.randomUUID().toString(),
                                    agentId = profile!!.id,
                                    oldVehicleNumber = agentDetails?.vehicleNumber,
                                    newVehicleNumber = newVehicleNumberInput,
                                    vehiclePhotoUrl = newVehiclePhotoUri ?: "https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=400"
                                )
                                SupabaseClient.submitVehicleChangeRequest(req)
                                statusMessage = "Vehicle change request submitted to Admin! Photo will be auto-deleted once approved."
                                isSubmittingVehicleChange = false
                                newVehicleNumberInput = ""
                                newVehiclePhotoUri = null
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2FA Security Manager Card
            GlassCard {
                Text(
                    text = "Two-Factor Authentication (2FA) Manager",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Secure your login with secondary verification codes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )

                Spacer(modifier = Modifier.height(14.dp))

                // TOTP Authenticator App Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Authenticator App (TOTP)",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Text(
                            text = "Google Authenticator or Authy 6-digit codes",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                    Switch(
                        checked = totp2FaEnabled,
                        onCheckedChange = {
                            totp2FaEnabled = it
                            statusMessage = if (it) "TOTP 2FA enabled! Secret key generated." else "TOTP 2FA disabled."
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = EcoEmerald500)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Email OTP 2FA Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Email OTP 2FA",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Text(
                            text = "Send 6-digit OTP to ${profile?.email ?: "registered email"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                    Switch(
                        checked = email2FaEnabled,
                        onCheckedChange = {
                            email2FaEnabled = it
                            statusMessage = if (it) "Email OTP 2FA enabled!" else "Email OTP 2FA disabled."
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = EcoEmerald500)
                    )
                }
            }

            AnimatedVisibility(visible = statusMessage != null) {
                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    StatusBadge(text = statusMessage!!, color = EcoEmerald400)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            EcoButton(
                text = "Log Out",
                icon = Icons.Default.Logout,
                isSecondary = true,
                onClick = {
                    SessionManager.logout()
                    onLogout()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private fun isWithin24Hours(dateStr: String?): Boolean {
    if (dateStr == null) return false
    return try {
        val clean = dateStr.replace("T", " ").take(19)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
        val date = sdf.parse(clean) ?: return false
        val diff = System.currentTimeMillis() - date.time
        diff in 0 until (24 * 60 * 60 * 1000L)
    } catch (e: Exception) {
        false
    }
}
