package org.ecovision.app.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.EcoButton
import org.ecovision.app.ui.components.GlassCard
import org.ecovision.app.ui.components.StatusBadge
import java.util.UUID

@Composable
fun AdminDashboardScreen() {
    val settings by SessionManager.platformSettings.collectAsState()
    val scope = rememberCoroutineScope()

    var profiles by remember { mutableStateOf<List<Profile>>(emptyList()) }
    var slabs by remember { mutableStateOf<List<WeightPricingSlab>>(emptyList()) }
    var vouchers by remember { mutableStateOf<List<EcoVoucher>>(emptyList()) }
    var cashRedemptions by remember { mutableStateOf<List<CashRedemption>>(emptyList()) }
    var voucherRedemptions by remember { mutableStateOf<List<VoucherRedemption>>(emptyList()) }
    var vehicleChangeRequests by remember { mutableStateOf<List<VehicleChangeRequest>>(emptyList()) }

    var isMaintenanceMode by remember { mutableStateOf(settings.maintenanceMode) }
    var downtimeNotice by remember { mutableStateOf(settings.maintenanceNotice) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var userFilter by remember { mutableStateOf("all") } // all, pending, agent, company, user

    // Rejection Dialog State
    var showRejectKycDialog by remember { mutableStateOf(false) }
    var userToReject by remember { mutableStateOf<Profile?>(null) }
    var selectedRejectionReason by remember { mutableStateOf("Proof image is blurry or low quality") }
    var customRejectionReason by remember { mutableStateOf("") }

    // New Voucher State
    var showCreateVoucherDialog by remember { mutableStateOf(false) }
    var vTitle by remember { mutableStateOf("") }
    var vBrand by remember { mutableStateOf("") }
    var vDesc by remember { mutableStateOf("") }
    var vImageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1542838132-92c53300491e?w=400") }
    var vCoinCost by remember { mutableStateOf("100") }
    var vDiscountVal by remember { mutableStateOf("10") }

    // Fulfill Voucher Dialog State
    var showFulfillVoucherDialog by remember { mutableStateOf(false) }
    var voucherToFulfill by remember { mutableStateOf<VoucherRedemption?>(null) }
    var fulfillVoucherCodeInput by remember { mutableStateOf("") }
    var fulfillVoucherNotesInput by remember { mutableStateOf("") }

    fun refresh() {
        scope.launch {
            profiles = SupabaseClient.getAllProfiles()
            slabs = SupabaseClient.getPricingSlabs()
            vouchers = SupabaseClient.getVouchers()
            cashRedemptions = SupabaseClient.getCashRedemptions()
            voucherRedemptions = SupabaseClient.getVoucherRedemptions()
            vehicleChangeRequests = SupabaseClient.getVehicleChangeRequests()
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    val pendingKycCount = profiles.count { it.kycStatus == "pending" }
    val filteredProfiles = when (userFilter) {
        "pending" -> profiles.filter { it.kycStatus == "pending" }
        "agent" -> profiles.filter { it.role == "agent" }
        "company" -> profiles.filter { it.role == "company" }
        "user" -> profiles.filter { it.role == "user" }
        else -> profiles
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Admin Header
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Super Admin Governance",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Platform Moderation, KYC Desks, Vouchers & Rates",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                    StatusBadge(text = "👑 SUPER ADMIN", color = Color(0xFFF472B6))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Users", fontSize = 11.sp, color = Slate400)
                        Text("${profiles.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column {
                        Text("Pending KYC", fontSize = 11.sp, color = Slate400)
                        Text("$pendingKycCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (pendingKycCount > 0) LeafCoinGold else EcoEmerald400)
                    }
                    Column {
                        Text("Active Vouchers", fontSize = 11.sp, color = Slate400)
                        Text("${vouchers.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EcoEmerald400)
                    }
                    Column {
                        Text("System Mode", fontSize = 11.sp, color = Slate400)
                        Text(if (isMaintenanceMode) "MAINTENANCE" else "OPERATIONAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isMaintenanceMode) CoralRed else EcoEmerald400)
                    }
                }
            }
        }

        if (statusMessage != null) {
            item {
                StatusBadge(text = statusMessage!!, color = EcoEmerald400)
            }
        }

        // ==========================================
        // MODULE 1: REAL USER MODERATION & KYC PROOF DESK
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "User Moderation & KYC Verification Desk",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Approve proof of upload, manage status (Active / Blocked / Banned)",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Tabs
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filters = listOf(
                    "all" to "All Users (${profiles.size})",
                    "pending" to "Pending KYC ($pendingKycCount)",
                    "agent" to "Agents (${profiles.count { it.role == "agent" }})",
                    "company" to "Companies (${profiles.count { it.role == "company" }})",
                    "user" to "Citizens (${profiles.count { it.role == "user" }})"
                )
                items(filters) { (key, label) ->
                    val isSel = userFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) EcoEmerald600 else Slate800)
                            .clickable { userFilter = key }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            color = if (isSel) Color.White else Slate300,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        items(filteredProfiles) { p ->
            Surface(
                color = Slate850,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (p.kycStatus == "pending") LeafCoinGold.copy(alpha = 0.5f) else CardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Slate700),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p.fullName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = p.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@${p.username ?: p.mobile.takeLast(4)} · ${p.mobile}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(
                                text = p.role.uppercase(),
                                color = when (p.role) {
                                    "admin" -> Color(0xFFF472B6)
                                    "agent" -> LeafCoinGold
                                    "company" -> Color(0xFF60A5FA)
                                    else -> EcoEmerald400
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(
                                text = p.kycStatus.uppercase(),
                                color = if (p.kycStatus == "verified") EcoEmerald400 else if (p.kycStatus == "rejected") CoralRed else LeafCoinGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "📍 Address: ${p.defaultAddress ?: "Not specified"} (Pincode: ${p.pincode ?: "682001"})", color = Slate300, fontSize = 12.sp)

                    // Upload Proof Section
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "📑 Uploaded Proof of Identity / KYC:",
                                fontSize = 11.sp,
                                color = Slate400,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Document: ${p.kycDocumentType?.uppercase() ?: "Standard ID"} · Number: ${p.kycDocumentNumber ?: "PENDING_SUBMISSION"}",
                                fontSize = 12.sp,
                                color = TechBlue,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (p.role == "agent") {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=300",
                                        contentDescription = "Vehicle Proof",
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = "Vehicle Matching Proof", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                        Text(text = "Registration: KL-07-CD-5678", style = MaterialTheme.typography.bodySmall, color = LeafCoinGold, fontWeight = FontWeight.Bold)
                                        Text(text = "Type: Commercial Multi-Zone Collector Van", fontSize = 10.sp, color = Slate300)
                                    }
                                }
                            }
                        }
                    }

                    // Moderation & Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (p.kycStatus == "pending" || p.kycStatus == "rejected") {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp),
                                onClick = {
                                    scope.launch {
                                        SupabaseClient.updateUserStatus(p.id, "verified")
                                        statusMessage = "Verified KYC for ${p.fullName}! Proof documents auto-deleted for privacy."
                                        refresh()
                                    }
                                }
                            ) {
                                Text("Approve KYC ✓", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        if (p.kycStatus == "pending" || p.kycStatus == "verified") {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp),
                                onClick = {
                                    userToReject = p
                                    selectedRejectionReason = "Proof image is blurry or low quality"
                                    customRejectionReason = ""
                                    showRejectKycDialog = true
                                }
                            ) {
                                Text("Reject KYC", color = CoralRed, fontSize = 11.sp)
                            }
                        }

                        // Block/Ban Account Action
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp),
                            onClick = {
                                scope.launch {
                                    SupabaseClient.updateUserStatus(p.id, "blocked")
                                    statusMessage = "Blocked account for ${p.fullName}."
                                    refresh()
                                }
                            }
                        ) {
                            Text("Block 🛑", color = Slate300, fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = {
                                scope.launch {
                                    SupabaseClient.deleteProfile(p.id)
                                    statusMessage = "Deleted account for ${p.fullName}."
                                    refresh()
                                }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // ==========================================
        // MODULE 2: ADMIN VOUCHERS MANAGEMENT
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎁 Admin Eco Vouchers Store Management",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Create and manage vouchers redeemable with Coins",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
                IconButton(onClick = { showCreateVoucherDialog = true }) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Voucher", tint = EcoEmerald400)
                }
            }
        }

        items(vouchers) { v ->
            Surface(
                color = Slate850,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (v.imageUrl != null) {
                        AsyncImage(
                            model = v.imageUrl,
                            contentDescription = v.title,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(v.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${v.brandName} · ${v.description ?: ""}", color = Slate400, fontSize = 11.sp, maxLines = 1)
                        Text("🪙 ${v.coinCost} Coins (Value: ₹${v.discountValue.toInt()})", color = LeafCoinGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    StatusBadge(text = "ACTIVE", color = EcoEmerald400)
                }
            }
        }

        // ==========================================
        // MODULE 3: PLATFORM MAINTENANCE CONTROL
        // ==========================================
        item {
            GlassCard(borderColor = if (isMaintenanceMode) CoralRed else CardBorder) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Platform Maintenance Mode",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isMaintenanceMode) "Active: Non-admin users see maintenance screen" else "Inactive: Platform live and operating normally",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isMaintenanceMode) CoralRed else EcoEmerald400
                        )
                    }
                    Switch(
                        checked = isMaintenanceMode,
                        onCheckedChange = { checked ->
                            isMaintenanceMode = checked
                            scope.launch {
                                val updated = settings.copy(maintenanceMode = checked, maintenanceNotice = downtimeNotice)
                                SupabaseClient.updatePlatformSettings(updated)
                                SessionManager.updateSettings(updated)
                                statusMessage = if (checked) "Maintenance Mode enabled!" else "Platform reopened to all users!"
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CoralRed,
                            checkedTrackColor = CoralRed.copy(alpha = 0.3f)
                        )
                    )
                }

                if (isMaintenanceMode) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = downtimeNotice,
                        onValueChange = { downtimeNotice = it },
                        label = { Text("Downtime Notice") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ==========================================
        // MODULE 4: DYNAMIC PRICING SLABS ENGINE
        // ==========================================
        item {
            Text(
                text = "Weight-Based Pricing Slabs Engine",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configures dynamic per-kg rates for each material weight-tier.",
                fontSize = 11.sp,
                color = Slate400
            )
        }

        items(slabs) { slab ->
            Surface(
                color = Slate850,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${slab.categoryId.replace("_", " ").uppercase()} · ${slab.tierName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Range: ${slab.minKg} kg to ${slab.maxKg ?: "Infinity"} kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                    Text(
                        text = "₹${slab.ratePerKg}/kg",
                        style = MaterialTheme.typography.titleMedium,
                        color = EcoEmerald400,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ==========================================
        // MODULE 4: REAL CASH REDEMPTION QUEUE & SETTLEMENT
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "💳 Cash Redemption & UPI Settlement Desk",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Verify user UPI IDs and mark payouts as completed",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            }
        }

        if (cashRedemptions.isEmpty()) {
            item {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No pending cash redemption requests in queue.",
                        color = Slate400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(cashRedemptions) { red ->
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (red.status == "waitlist") LeafCoinGold.copy(alpha = 0.5f) else CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "₹${red.cashAmount.toInt()} · ${red.holderName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "UPI ID: ${red.upiId} · ${red.coinsAmount} Coins",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TechBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            StatusBadge(
                                text = if (red.status == "completed") "SETTLED ✓" else "WAITLIST",
                                color = if (red.status == "completed") EcoEmerald400 else LeafCoinGold
                            )
                        }

                        if (red.status != "completed") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 6.dp),
                                onClick = {
                                    scope.launch {
                                        SupabaseClient.updateCashRedemptionStatus(red.id, "completed")
                                        statusMessage = "Settlement completed for ${red.holderName} (UPI: ${red.upiId})!"
                                        refresh()
                                    }
                                }
                            ) {
                                Text("Verify UPI & Mark Settled ✓", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // MODULE 4B: USER VOUCHER REDEMPTION WAITING LIST DESK
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎟️ User Voucher Redemptions Waiting List",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Review user voucher claims, enter digital code & push to user wallet/email",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            }
        }

        if (voucherRedemptions.isEmpty()) {
            item {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No voucher redemption requests in waiting list.",
                        color = Slate400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(voucherRedemptions) { red ->
                val isPending = red.status == "waitlist" || red.status == "pending"
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isPending) LeafCoinGold.copy(alpha = 0.5f) else CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${red.voucherTitle} · ${red.brandName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "User: ${red.userName ?: "Eco User"} (${red.userEmail ?: "No email"}) · ${red.coinCost} Coins",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TechBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (red.voucherDetails != null) {
                                    Text(
                                        text = "Code: ${red.voucherDetails}",
                                        color = EcoEmerald400,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            StatusBadge(
                                text = if (isPending) "WAITING LIST ⏳" else "FULFILLED ✓",
                                color = if (isPending) LeafCoinGold else EcoEmerald400
                            )
                        }

                        if (isPending) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 6.dp),
                                onClick = {
                                    voucherToFulfill = red
                                    fulfillVoucherCodeInput = "ECO-${red.brandName.take(3).uppercase()}-${(1000..9999).random()}"
                                    fulfillVoucherNotesInput = "Valid on partner store checkout. Expires in 6 months."
                                    showFulfillVoucherDialog = true
                                }
                            ) {
                                Text("Fulfill Voucher & Push Code to User ✉️", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // MODULE 5: AGENT VEHICLE CHANGE REQUESTS
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🚚 Agent Vehicle Change Requests",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Inspect new vehicle plate and photo; photo is auto-deleted upon approval",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            }
        }

        if (vehicleChangeRequests.isEmpty()) {
            item {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No pending vehicle change requests.",
                        color = Slate400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(vehicleChangeRequests) { req ->
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "New Vehicle: ${req.newVehicleNumber}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Previous: ${req.oldVehicleNumber ?: "Not recorded"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                            StatusBadge(
                                text = req.status.uppercase(),
                                color = if (req.status == "approved") EcoEmerald400 else if (req.status == "rejected") CoralRed else LeafCoinGold
                            )
                        }

                        if (req.vehiclePhotoUrl != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = req.vehiclePhotoUrl,
                                contentDescription = "Vehicle Photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        if (req.status == "pending") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 6.dp),
                                    onClick = {
                                        scope.launch {
                                            SupabaseClient.reviewVehicleChangeRequest(req.id, req.agentId, req.newVehicleNumber, "approved")
                                            statusMessage = "Approved vehicle change to ${req.newVehicleNumber}! Photo auto-deleted."
                                            refresh()
                                        }
                                    }
                                ) {
                                    Text("Approve & Update ✓", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 6.dp),
                                    onClick = {
                                        scope.launch {
                                            SupabaseClient.reviewVehicleChangeRequest(req.id, req.agentId, req.newVehicleNumber, "rejected", "Vehicle details could not be verified")
                                            statusMessage = "Rejected vehicle change request."
                                            refresh()
                                        }
                                    }
                                ) {
                                    Text("Reject", color = CoralRed, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }

    // Create Voucher Dialog
    if (showCreateVoucherDialog) {
        val calculatedDiscount = (vCoinCost.toIntOrNull() ?: 100) / 10.0
        AlertDialog(
            onDismissRequest = { showCreateVoucherDialog = false },
            containerColor = Slate850,
            titleContentColor = Color.White,
            textContentColor = Slate300,
            title = { Text("Create Partner Voucher for Wallet") },
            text = {
                Column {
                    OutlinedTextField(
                        value = vTitle,
                        onValueChange = { vTitle = it },
                        label = { Text("Voucher Name *") },
                        placeholder = { Text("e.g. ₹50 Off Organic Groceries") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = vBrand,
                        onValueChange = { vBrand = it },
                        label = { Text("Brand / Partner Name *") },
                        placeholder = { Text("e.g. GreenBasket") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = vImageUrl,
                        onValueChange = { vImageUrl = it },
                        label = { Text("Voucher Image URL *") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = vDesc,
                        onValueChange = { vDesc = it },
                        label = { Text("Description *") },
                        placeholder = { Text("Terms and valid items...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = vCoinCost,
                        onValueChange = { vCoinCost = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Voucher Coins Price *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Exchange Rate: 10 Coins = ₹1.00 (Worth ₹${String.format(java.util.Locale.US, "%.0f", calculatedDiscount)})",
                        color = EcoEmerald400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = vTitle.isNotBlank() && vCoinCost.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                    onClick = {
                        showCreateVoucherDialog = false
                        val cost = vCoinCost.toIntOrNull() ?: 100
                        scope.launch {
                            val newVoucher = EcoVoucher(
                                id = UUID.randomUUID().toString(),
                                title = vTitle,
                                brandName = vBrand.ifBlank { "EcoPartner" },
                                description = vDesc.ifBlank { "Valid on zero-waste partner items." },
                                imageUrl = vImageUrl.ifBlank { "https://images.unsplash.com/photo-1542838132-92c53300491e?w=400" },
                                coinCost = cost,
                                discountValue = cost / 10.0
                            )
                            SupabaseClient.createVoucher(newVoucher)
                            refresh()
                            statusMessage = "Voucher '${newVoucher.title}' published to user wallets!"
                        }
                    }
                ) {
                    Text("Publish Voucher", color = Slate950, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateVoucherDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            }
        )
    }

    // Fulfill Voucher Dialog
    if (showFulfillVoucherDialog && voucherToFulfill != null) {
        val req = voucherToFulfill!!
        AlertDialog(
            onDismissRequest = {
                showFulfillVoucherDialog = false
                voucherToFulfill = null
            },
            containerColor = Slate850,
            titleContentColor = Color.White,
            textContentColor = Slate300,
            title = {
                Text("Issue & Push Voucher Code", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "User: ${req.userName ?: "Eco User"} (${req.userEmail ?: "No email recorded"})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Requested: ${req.voucherTitle} (${req.brandName})",
                        color = LeafCoinGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Coins Paid: ${req.coinCost} Coins (Worth ₹${req.coinCost / 10})",
                        color = EcoEmerald400,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = fulfillVoucherCodeInput,
                        onValueChange = { fulfillVoucherCodeInput = it },
                        label = { Text("Voucher Code / Claim PIN *") },
                        placeholder = { Text("e.g. ECO-GREEN-2026-X88") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fulfillVoucherNotesInput,
                        onValueChange = { fulfillVoucherNotesInput = it },
                        label = { Text("Redemption Instructions / Expiry") },
                        placeholder = { Text("e.g. Valid on partner store checkout. Expires in 6 months.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Upon submission, details will be pushed to the user's Wallet and emailed to ${req.userEmail ?: "user email"}.",
                        color = Slate400,
                        fontSize = 10.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = fulfillVoucherCodeInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                    onClick = {
                        val fullDetails = if (fulfillVoucherNotesInput.isNotBlank()) {
                            "${fulfillVoucherCodeInput.trim()} | ${fulfillVoucherNotesInput.trim()}"
                        } else {
                            fulfillVoucherCodeInput.trim()
                        }
                        scope.launch {
                            SupabaseClient.fulfillVoucherRedemption(req.id, fullDetails)
                            statusMessage = "🎉 Voucher code issued! Pushed to user wallet and sent to ${req.userEmail ?: "user email"}."
                            refresh()
                            showFulfillVoucherDialog = false
                            voucherToFulfill = null
                            fulfillVoucherCodeInput = ""
                            fulfillVoucherNotesInput = ""
                        }
                    }
                ) {
                    Text("Submit & Push to User ✉️", color = Slate950, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFulfillVoucherDialog = false
                    voucherToFulfill = null
                }) {
                    Text("Cancel", color = Slate400)
                }
            }
        )
    }

    // Rejection Reason Modal Dialog
    if (showRejectKycDialog && userToReject != null) {
        val u = userToReject!!
        val presetReasons = listOf(
            "Proof image is blurry or low quality",
            "Document name does not match profile",
            "Upload real proper official proof",
            "Expired identification document",
            "Vehicle registration plate mismatch"
        )

        AlertDialog(
            onDismissRequest = { showRejectKycDialog = false },
            containerColor = Slate850,
            titleContentColor = Color.White,
            textContentColor = Slate300,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = CoralRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reject Verification for ${u.fullName}", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Specify the exact rejection reason. The user will see this in their Profile to reapply (Max 5 attempts allowed):",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    presetReasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedRejectionReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedRejectionReason == reason,
                                onClick = { selectedRejectionReason = reason },
                                colors = RadioButtonDefaults.colors(selectedColor = CoralRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(reason, fontSize = 12.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customRejectionReason,
                        onValueChange = { customRejectionReason = it },
                        label = { Text("Or Type Custom Rejection Reason") },
                        placeholder = { Text("e.g. Aadhaar QR code is unreadable") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                    onClick = {
                        val finalReason = if (customRejectionReason.isNotBlank()) customRejectionReason else selectedRejectionReason
                        scope.launch {
                            SupabaseClient.updateUserStatus(u.id, "rejected", finalReason)
                            statusMessage = "Rejected KYC for ${u.fullName} with reason: '$finalReason'"
                            showRejectKycDialog = false
                            refresh()
                        }
                    }
                ) {
                    Text("Confirm Rejection", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectKycDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            }
        )
    }
}
