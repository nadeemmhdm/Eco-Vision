package org.ecovision.app.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.EcoButton
import org.ecovision.app.ui.components.EcoPopupDialog
import org.ecovision.app.ui.components.EcoVisionAppLogo
import org.ecovision.app.ui.components.StatusBadge

private data class SettingsMenuItem(
    val title: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit
)

/**
 * Clean, flat list-based Settings Screen:
 * Profile, My Orders, Security, About, Terms and Conditions, Privacy Policy, FAQ, Delete Account, and Log Out.
 */
@Composable
fun SettingsScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToMyOrders: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToFaq: () -> Unit,
    onLogout: () -> Unit
) {
    val profile by SessionManager.currentProfile.collectAsState()
    val themeMode by SessionManager.themeMode.collectAsState()
    val scope = rememberCoroutineScope()
    var showDeleteCooldownDialog by remember { mutableStateOf(false) }
    var popupNotice by remember { mutableStateOf<Pair<String, String>?>(null) }

    val menuItems = buildList {
        add(SettingsMenuItem("Profile", Icons.Default.Person, EcoEmerald500, onNavigateToProfile))
        if ((profile?.role ?: "user") == "user") {
            add(SettingsMenuItem("My Orders", Icons.Default.ShoppingBag, TechBlue, onNavigateToMyOrders))
        }
        add(SettingsMenuItem("Security", Icons.Default.Security, LeafCoinGold, onNavigateToSecurity))
        add(SettingsMenuItem("About", Icons.Default.Info, EcoEmerald400, onNavigateToAbout))
        add(SettingsMenuItem("Terms and Conditions", Icons.Default.Description, Color(0xFFF59E0B), onNavigateToTerms))
        add(SettingsMenuItem("Privacy Policy", Icons.Default.PrivacyTip, Color(0xFF8B5CF6), onNavigateToPrivacy))
        add(SettingsMenuItem("FAQ", Icons.Default.HelpOutline, EcoEmerald500, onNavigateToFaq))
        add(SettingsMenuItem("Delete Account", Icons.Default.DeleteForever, CoralRed, { showDeleteCooldownDialog = true }))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                EcoVisionAppLogo(size = 38.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EcoEmerald500.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (themeMode) {
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                },
                                contentDescription = "Theme Mode",
                                tint = EcoEmerald500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Appearance Mode",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Switch between System, Light, and Dark themes",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val options = listOf(
                            Triple(ThemeMode.SYSTEM, "System", Icons.Default.BrightnessAuto),
                            Triple(ThemeMode.LIGHT, "Light", Icons.Default.LightMode),
                            Triple(ThemeMode.DARK, "Dark", Icons.Default.DarkMode)
                        )
                        options.forEach { (mode, label, icon) ->
                            val selected = themeMode == mode
                            Surface(
                                color = if (selected) EcoEmerald600 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selected) EcoEmerald500 else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { SessionManager.setThemeMode(mode) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        items(menuItems) { item ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { item.onClick() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(item.accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = item.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (item.title == "Delete Account") CoralRed else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = {
                    scope.launch {
                        SessionManager.logout()
                        onLogout()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralRed.copy(alpha = 0.14f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = CoralRed, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", color = CoralRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    // Delete Account Dialog — shows ONLY 30-day inactivity cooldown details
    if (showDeleteCooldownDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCooldownDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = CoralRed, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("30-Day Deletion Cooldown", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Account deletion follows a mandatory 30-day inactivity cooldown:\n\n• Your account is deactivated immediately.\n• Data is permanently deleted only after 30 continuous days of inactivity.\n• Signing back in within 30 days automatically cancels deletion and restores your account.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                    onClick = {
                        showDeleteCooldownDialog = false
                        scope.launch {
                            if (profile != null) {
                                SupabaseClient.scheduleAccountDeletion(profile!!.id)
                            }
                            popupNotice = "Account Deletion Scheduled" to "Your 30-day inactivity cooldown has started. Sign in within 30 days to cancel."
                        }
                    }
                ) {
                    Text("Confirm 30-Day Cooldown", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCooldownDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (popupNotice != null) {
        EcoPopupDialog(
            title = popupNotice!!.first,
            message = popupNotice!!.second,
            accentColor = CoralRed,
            onDismiss = {
                popupNotice = null
                scope.launch {
                    SessionManager.logout()
                    onLogout()
                }
            }
        )
    }
}

@Composable
fun SecurityScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val profile by SessionManager.currentProfile.collectAsState()
    val themeMode by SessionManager.themeMode.collectAsState()
    val scope = rememberCoroutineScope()

    var totpEnabled by remember(profile?.totpEnabled) { mutableStateOf(profile?.totpEnabled ?: false) }
    var showTotpSetup by remember { mutableStateOf(false) }
    var generatedSecret by remember { mutableStateOf(TotpHelper.generateSecret()) }
    var totpCodeInput by remember { mutableStateOf("") }
    var popupMessage by remember { mutableStateOf<Pair<String, String>?>(null) }

    val deviceInfo = remember { profile?.deviceInfo?.takeIf { it.isNotBlank() } ?: DeviceLocationHelper.getDeviceInfo() }
    val deviceIp = remember { profile?.deviceIp?.takeIf { it.isNotBlank() } ?: DeviceLocationHelper.getDeviceIp() }

    InfoPageScaffold(title = "Security & Devices", onBack = onBack) {
        // 1. 2FA TOTP On/Off Card
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (totpEnabled) Icons.Default.VerifiedUser else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (totpEnabled) EcoEmerald500 else LeafCoinGold,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Two-Factor Auth (2FA TOTP)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (totpEnabled) "ON · Authenticator code required on sign-in" else "OFF · Tap switch to enable 2FA",
                                color = if (totpEnabled) EcoEmerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = totpEnabled,
                        onCheckedChange = { enable ->
                            if (enable) {
                                generatedSecret = TotpHelper.generateSecret()
                                totpCodeInput = ""
                                showTotpSetup = true
                            } else {
                                scope.launch {
                                    if (profile != null) {
                                        val updated = profile!!.copy(totpEnabled = false, twoFactorSecret = null)
                                        SupabaseClient.updateProfile(updated)
                                        SessionManager.setProfile(updated, persist = true)
                                    }
                                    totpEnabled = false
                                    showTotpSetup = false
                                    popupMessage = "2FA Disabled" to "Two-Factor Authentication has been turned OFF for your account."
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EcoEmerald600
                        )
                    )
                }

                AnimatedVisibility(visible = showTotpSetup && !totpEnabled) {
                    Column(modifier = Modifier.padding(top = 14.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        val qrCodeUri = remember(generatedSecret, profile?.username) {
                            val uriString = TotpHelper.getOtpAuthUri(profile?.username ?: "user", generatedSecret)
                            "https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=" + Uri.encode(uriString)
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(8.dp)
                        ) {
                            AsyncImage(
                                model = qrCodeUri,
                                contentDescription = "2FA QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(generatedSecret.chunked(4).joinToString("-"), color = LeafCoinGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            IconButton(onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("TOTP Secret", generatedSecret))
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = totpCodeInput,
                            onValueChange = { if (it.length <= 6) totpCodeInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Enter 6-Digit Authenticator Code") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        EcoButton(
                            text = "Verify & Turn ON 2FA",
                            enabled = totpCodeInput.length == 6,
                            onClick = {
                                scope.launch {
                                    if (TotpHelper.verifyTotp(generatedSecret, totpCodeInput) && profile != null) {
                                        val updated = profile!!.copy(totpEnabled = true, twoFactorSecret = generatedSecret)
                                        SupabaseClient.updateProfile(updated)
                                        SessionManager.setProfile(updated, persist = true)
                                        totpEnabled = true
                                        showTotpSetup = false
                                        popupMessage = "2FA Activated!" to "Two-Factor Authentication is now ON. You will be asked for a 6-digit code on future sign-ins."
                                    } else {
                                        popupMessage = "Invalid Code" to "Please enter a valid 6-digit code from your authenticator app."
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // 2. Security & Devices Compliance Card
        InfoBodyCard(
            title = "Device & Session Compliance",
            body = "Registered Device: $deviceInfo\nActive IP Address: $deviceIp\nEncryption: TLS 1.3 + Encrypted Cloud Vault\nBrute-Force Lockout: 5-Attempt 24h Protection Active"
        )
        InfoBodyCard(
            title = "Account Security Status",
            body = "KYC Status: ${(profile?.kycStatus ?: "pending").uppercase()}\n2FA Protection: ${if (totpEnabled) "ENABLED (RFC 6238 TOTP)" else "DISABLED"}\nData Privacy: Raw KYC proofs purged after approval"
        )
    }

    if (popupMessage != null) {
        EcoPopupDialog(
            title = popupMessage!!.first,
            message = popupMessage!!.second,
            onDismiss = { popupMessage = null }
        )
    }
}

@Composable
fun MyOrdersScreen(onBack: () -> Unit) {
    val profile by SessionManager.currentProfile.collectAsState()
    var orders by remember { mutableStateOf<List<MarketplaceOrder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(profile?.id) {
        val uid = profile?.id ?: "user-adhil"
        orders = SupabaseClient.getUserMarketplaceOrders(uid)
        isLoading = false
    }

    InfoPageScaffold(title = "My Orders", onBack = onBack) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (orders.isEmpty()) {
            InfoBodyCard(
                title = "No Orders Yet",
                body = "Orders placed in the Upcycled P2P Store will appear here with real-time Pickup Agent tracking (Placed → Picked Up → Processing → Shipped → Completed)."
            )
        } else {
            orders.forEach { order ->
                val stepIndex = when (order.status.lowercase()) {
                    "placed" -> 0
                    "picked_up" -> 1
                    "processing" -> 2
                    "shipped" -> 3
                    "completed", "delivered" -> 4
                    else -> 0
                }
                val steps = listOf("Placed", "Picked Up", "Processing", "Shipped", "Completed")

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Order #${order.id.take(8).uppercase()}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(
                                text = order.status.replace("_", " ").uppercase(),
                                color = if (stepIndex == 4) EcoEmerald500 else LeafCoinGold
                            )
                        }
                        if (order.itemsSummary.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(order.itemsSummary, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Total Paid: ₹${order.totalAmount.toInt()} (${order.paymentMethod})", color = EcoEmerald500, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Delivery Address: ${order.deliveryAddress}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        Text("Assigned Pickup Agent: Rafiq Khan (Seller Pickup & Delivery)", color = TechBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Current Order Status Stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            steps.forEachIndexed { idx, label ->
                                val reached = idx <= stepIndex
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(if (reached) EcoEmerald500 else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (reached) {
                                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        color = if (reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (idx == stepIndex) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoPageScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    InfoPageScaffold(title = "About EcoVision", onBack = onBack) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EcoVisionAppLogo(size = 56.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                val ctx = LocalContext.current
                val versionName = remember(ctx) {
                    try {
                        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "1.0.15"
                    } catch (_: Exception) {
                        "1.0.15"
                    }
                }
                Text("EcoVision", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                Text("Version $versionName • Circular Economy Platform", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        InfoBodyCard(
            title = "Our Mission",
            body = "EcoVision connects households, verified doorstep pickup agents, and certified B2B recycling companies across Kerala into a transparent circular economy loop. Using AI waste classification, weight-based dynamic pricing, and instant coin rewards, we make responsible recycling effortless and rewarding."
        )
        InfoBodyCard(
            title = "How It Works",
            body = "1. Scan waste with AI Vision to check recyclability, estimated weight, and live price.\n2. Add recyclable items to your Virtual Bin and schedule doorstep collection.\n3. Verify your assigned agent's vehicle & weigh batch on a certified scale.\n4. Earn Coins & cash payouts via UPI or claim partner discount vouchers."
        )
        InfoBodyCard(
            title = "Service Coverage",
            body = "Supporting all 14 districts of Kerala with real-time GPS pickup routing, digital godown manifests, and ESG carbon offset tracking."
        )
    }
}

@Composable
fun TermsScreen(onBack: () -> Unit) {
    InfoPageScaffold(title = "Terms and Conditions", onBack = onBack) {
        InfoBodyCard(
            title = "1. Platform Eligibility & KYC",
            body = "Users, Pickup Agents, and Buyer Companies must provide accurate registration details and valid KYC documents (Aadhaar / Vehicle RC / GSTIN) before performing verified payouts or bulk orders."
        )
        InfoBodyCard(
            title = "2. AI Estimation vs Certified Scale",
            body = "AI Waste Scanner weight and price estimates are indicative only. Final cash payouts and Coins are calculated strictly from the certified physical weighing scale reading recorded by the Pickup Agent at your doorstep."
        )
        InfoBodyCard(
            title = "3. Recyclable Segregation",
            body = "Non-recyclable, wet, or hazardous waste must not be placed into the Recyclable Bin. Agents reserve the right to reject contaminated batches."
        )
        InfoBodyCard(
            title = "4. Coin & Cash Redemptions",
            body = "Cash redemptions via UPI require a minimum balance of 1,000 Coins (₹100.00) and an active internet connection. Voucher redemptions are processed via the fulfillment queue."
        )
        InfoBodyCard(
            title = "5. Account Deletion Policy",
            body = "Accounts scheduled for deletion remain in a 30-day inactivity grace period and are permanently removed only after 30 days of inactivity."
        )
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    InfoPageScaffold(title = "Privacy Policy", onBack = onBack) {
        InfoBodyCard(
            title = "1. Information We Collect",
            body = "We collect your name, mobile number, email, Kerala district, pickup address, device diagnostics, and optional location coordinates solely to dispatch doorstep collection agents and prevent fraud."
        )
        InfoBodyCard(
            title = "2. KYC Document Protection",
            body = "Uploaded KYC documents are used strictly for identity verification. Once KYC is approved, raw identity proof images are purged from active storage for your privacy."
        )
        InfoBodyCard(
            title = "3. Camera & Media Access",
            body = "Camera and gallery permissions are used only when you scan waste items, capture marketplace craft photos, or upload KYC/profile images."
        )
        InfoBodyCard(
            title = "4. Two-Factor Security (2FA)",
            body = "EcoVision supports RFC 6238 TOTP authenticator protection to keep your wallet, Coins, and payout details secure."
        )
        InfoBodyCard(
            title = "5. Offline Caching",
            body = "Vouchers, waste categories, and profile details are cached locally on your device for offline viewing. Financial redemptions require a live online connection."
        )
    }
}

@Composable
fun FaqScreen(onBack: () -> Unit) {
    val faqs = listOf(
        "How do I earn Coins and Cash?" to "Scan recyclable waste, add it to your Virtual Bin, and request a doorstep pickup. Once the Pickup Agent weighs and verifies your waste on a certified scale, you earn cash and bonus Coins.",
        "Why is 'Add to Recyclable Bin' hidden for some items?" to "If the AI Scanner identifies an item as non-recyclable (such as Styrofoam or soiled waste), the Recyclable Bin button is hidden to prevent contamination. You can still tap 'Upscale This Product' for DIY reuse ideas.",
        "What is the minimum cash withdrawal?" to "The minimum UPI cash redemption is ₹100.00 (1,000 Coins at 10 Coins = ₹1). You can also redeem Coins for partner shopping vouchers.",
        "Can I redeem vouchers offline?" to "You can browse cached vouchers offline, but redeeming vouchers or cash requires an active internet connection to verify balances.",
        "How does 2FA Authenticator work?" to "Open Settings → Security, turn ON 2FA TOTP, scan the generated QR code in Google Authenticator or Authy, and verify with the 6-digit code.",
        "How do I track my Upcycled Store orders?" to "Open Settings → My Orders to view your full order history and live Pickup Agent delivery progress."
    )

    InfoPageScaffold(title = "Frequently Asked Questions", onBack = onBack) {
        faqs.forEach { (question, answer) ->
            var expanded by remember { mutableStateOf(false) }
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { expanded = !expanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        Text(
                            text = answer,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBodyCard(title: String, body: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp
            )
        }
    }
}
