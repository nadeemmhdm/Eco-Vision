package org.ecovision.app.ui.screens.profile

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
import org.ecovision.app.data.SessionManager
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
    var totp2FaEnabled by remember { mutableStateOf(profile?.totpEnabled ?: false) }
    var email2FaEnabled by remember { mutableStateOf(profile?.emailOtpEnabled ?: false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
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
                            text = "KYC: ${profile?.kycStatus?.uppercase() ?: "VERIFIED"}",
                            color = if (profile?.kycStatus == "verified") EcoEmerald400 else LeafCoinGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CardBorderBright)
                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Default Address: ${profile?.defaultAddress ?: "Kakkanad, Kochi"}", color = Slate300, fontSize = 12.sp)
                Text(text = "Active Role: ${profile?.role?.uppercase()}", color = TechBlue, fontSize = 12.sp)
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

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                StatusBadge(text = statusMessage!!, color = EcoEmerald400)
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
        }
    }
}
