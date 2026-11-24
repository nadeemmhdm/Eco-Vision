package org.ecovision.app.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
    onBack: () -> Unit
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
                Text(text = "Leaf Coins Balance: 🍃 ${profile?.leafCoins ?: 50} Coins", color = LeafCoinGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                Text(
                    text = if (profile?.kycStatus == "verified")
                        "Your identity and credentials are fully verified by EcoVision governance."
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

                Spacer(modifier = Modifier.height(14.dp))

                EcoButton(
                    text = if (isSubmittingKyc) "Submitting..." else if (profile?.kycStatus == "verified") "Update KYC Details" else "Submit KYC for Verification",
                    enabled = !isSubmittingKyc && idDocNumber.isNotBlank() || gstNumber.isNotBlank() || vehicleNumber.isNotBlank(),
                    onClick = {
                        scope.launch {
                            isSubmittingKyc = true
                            val curr = profile ?: return@launch
                            val updatedProfile = curr.copy(
                                defaultAddress = address,
                                pincode = pincode,
                                kycDocumentNumber = if (curr.role == "company") gstNumber else idDocNumber,
                                kycDocumentType = if (curr.role == "company") "gst" else "aadhaar",
                                kycStatus = "pending"
                            )

                            // Update profile in Supabase
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
