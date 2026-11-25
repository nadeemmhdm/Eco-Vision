package org.ecovision.app.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.*
import java.util.UUID

@Composable
fun AdminDashboardScreen() {
    val settings by SessionManager.platformSettings.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var profiles by remember { mutableStateOf<List<Profile>>(emptyList()) }
    var slabs by remember { mutableStateOf<List<WeightPricingSlab>>(emptyList()) }
    var vouchers by remember { mutableStateOf<List<EcoVoucher>>(emptyList()) }
    var cashRedemptions by remember { mutableStateOf<List<CashRedemption>>(emptyList()) }
    var voucherRedemptions by remember { mutableStateOf<List<VoucherRedemption>>(emptyList()) }
    var vehicleChangeRequests by remember { mutableStateOf<List<VehicleChangeRequest>>(emptyList()) }
    var adminNotifications by remember { mutableStateOf<List<AdminNotification>>(emptyList()) }

    var notifTitle by remember { mutableStateOf("") }
    var notifMessage by remember { mutableStateOf("") }
    var notifTargetRole by remember { mutableStateOf("all") }

    var isMaintenanceMode by remember { mutableStateOf(settings.maintenanceMode) }
    var downtimeNotice by remember { mutableStateOf(settings.maintenanceNotice) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Multi-page Tab Navigation State
    val externalAdminTab by SessionManager.adminTab.collectAsState()
    var currentTab by remember { mutableIntStateOf(externalAdminTab) }
    LaunchedEffect(externalAdminTab) {
        currentTab = externalAdminTab
    }
    val tabs = listOf(
        "KYC Verification" to Icons.Default.VerifiedUser,
        "User Governance" to Icons.Default.People,
        "Voucher Studio" to Icons.Default.CardGiftcard,
        "Cash Redeems" to Icons.Default.Payments,
        "Fleet & System" to Icons.Default.Tune
    )

    // User Governance & KYC Sub-Filters
    var userFilter by remember { mutableStateOf("all") }
    var kycFilter by remember { mutableStateOf("pending") }

    // Proof Document Viewer State
    var proofProfileToView by remember { mutableStateOf<Profile?>(null) }

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
            adminNotifications = SupabaseClient.getAdminNotifications("admin")
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    val pendingKycProfiles = profiles.filter { it.kycStatus != "verified" }
    val pendingCashCount = cashRedemptions.count { it.status == "waitlist" || it.status == "pending" }
    val pendingVoucherCount = voucherRedemptions.count { it.status == "waitlist" || it.status == "pending" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. TOP 3D MASCOT HERO & LIQUID GLASS HEADER
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedCharacterHero(
                title = "Super Admin Hub",
                subtitle = "Simplified Multi-Desk Governance • Real-Time Verifications"
            )
        }

        // ==========================================
        // 2. HORIZONTAL TAB NAVIGATION BAR (5 SUB-PAGES)
        // ==========================================
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tabs.size) { idx ->
                    val (label, icon) = tabs[idx]
                    val isSelected = currentTab == idx

                    Surface(
                        color = if (isSelected) EcoEmerald600 else Slate850,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) EcoEmerald400 else CardBorder
                        ),
                        modifier = Modifier.clickable { currentTab = idx }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) Color.White else Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else Slate300,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )

                            // Notification dot for pending tasks
                            if (idx == 0 && pendingKycProfiles.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(LeafCoinGold)
                                )
                            } else if (idx == 3 && pendingCashCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(LeafCoinGold)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (statusMessage != null) {
            item {
                StatusBadge(text = statusMessage!!, color = EcoEmerald400)
            }
        }

        // =========================================================================
        // TAB 0: KYC VERIFICATION DESK (SIMPLIFIED, FOCUSED)
        // =========================================================================
        if (currentTab == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Identity Verification Desk",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${pendingKycProfiles.size} accounts awaiting approval. Review proof and approve instantly.",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sub-filter: Pending vs All vs Verified
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val kycOptions = listOf("pending" to "Needs Approval", "verified" to "Approved", "all" to "All")
                    for ((k, label) in kycOptions) {
                        val isSel = kycFilter == k
                        Surface(
                            color = if (isSel) EcoEmerald700 else Slate800,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) EcoEmerald400 else CardBorder),
                            modifier = Modifier.clickable { kycFilter = k }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                color = if (isSel) Color.White else Slate400,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            val targetKycList = when (kycFilter) {
                "pending" -> profiles.filter { it.kycStatus != "verified" }
                "verified" -> profiles.filter { it.kycStatus == "verified" }
                else -> profiles
            }

            if (targetKycList.isEmpty()) {
                item {
                    LiquidGlassCard {
                        Text(
                            text = "🎉 All verification requests have been processed! No pending KYC.",
                            color = Slate300,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        )
                    }
                }
            } else {
                items(targetKycList) { p ->
                    LiquidGlassCard(
                        highlightColor = if (p.kycStatus == "verified") EcoEmerald400 else LeafCoinGold
                    ) {
                        // User Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(EcoEmerald600.copy(alpha = 0.25f))
                                        .border(1.dp, EcoEmerald400, CircleShape),
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
                                        text = "@${p.username ?: p.mobile.takeLast(4)} • ${p.mobile}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate400
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                StatusBadge(text = p.role.uppercase(), color = TechBlue)
                                Spacer(modifier = Modifier.height(4.dp))
                                StatusBadge(
                                    text = p.kycStatus.uppercase(),
                                    color = if (p.kycStatus == "verified") EcoEmerald400 else if (p.kycStatus == "rejected") CoralRed else LeafCoinGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Document Specs Box
                        Surface(
                            color = Slate800.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ID: ${p.kycDocumentType?.uppercase() ?: "GOVERNMENT ID"} • ${p.kycDocumentNumber ?: "DOC-2026-SUBMITTED"}",
                                        color = TechBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "District: ${p.district ?: "Malappuram"}, Kerala",
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                }

                                // Prominent "View Uploaded Proof" Button
                                Button(
                                    onClick = { proofProfileToView = p },
                                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Visibility, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("View Proof", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions Row: Accept Button (Always Available), Reject Button, Block Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Primary ACCEPT & VERIFY KYC Button
                            Button(
                                onClick = {
                                    scope.launch {
                                        SupabaseClient.updateUserStatus(p.id, "verified")
                                        statusMessage = "✅ Successfully Approved & Verified KYC for ${p.fullName}!"
                                        refresh()
                                    }
                                },
                                modifier = Modifier.weight(1.3f),
                                colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (p.kycStatus == "verified") "Re-Approve ✓" else "Accept KYC ✓",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            // REJECT with Reason Button
                            Button(
                                onClick = {
                                    userToReject = p
                                    selectedRejectionReason = "Proof image is blurry or low quality"
                                    customRejectionReason = ""
                                    showRejectKycDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Cancel, null, tint = CoralRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reject", color = CoralRed, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }

                            // BLOCK BoxIcon
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        SupabaseClient.updateUserStatus(p.id, "blocked")
                                        statusMessage = "Blocked account for ${p.fullName}."
                                        refresh()
                                    }
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                BoxIcon(
                                    icon = Icons.Default.Block,
                                    boxColor = CoralRed.copy(alpha = 0.15f),
                                    iconColor = CoralRed,
                                    size = 36.dp,
                                    iconSize = 18.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB 1: USER GOVERNANCE (MEMBERS & MODERATION)
        // =========================================================================
        if (currentTab == 1) {
            item {
                Text(
                    text = "Platform Users & Account Governance",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage citizens, pickup agents, and recycling companies.",
                    fontSize = 11.sp,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Role Selector Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val roles = listOf("all" to "All (${profiles.size})", "user" to "Citizens", "agent" to "Agents", "company" to "Companies", "admin" to "Admins")
                    items(roles) { (rKey, rLabel) ->
                        val isSel = userFilter == rKey
                        Surface(
                            color = if (isSel) TechBlue else Slate800,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color.White else CardBorder),
                            modifier = Modifier.clickable { userFilter = rKey }
                        ) {
                            Text(
                                text = rLabel,
                                fontSize = 11.sp,
                                color = if (isSel) Color.White else Slate400,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            val targetUsers = if (userFilter == "all") profiles else profiles.filter { it.role == userFilter }

            items(targetUsers) { p ->
                LiquidGlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = p.fullName, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(text = "${p.mobile} • ${p.role.uppercase()} • District: ${p.district ?: "Malappuram"}", color = Slate400, fontSize = 11.sp)
                        }
                        StatusBadge(
                            text = (p.accountStatus ?: "ACTIVE").uppercase(),
                            color = if (p.accountStatus == "blocked" || p.accountStatus == "banned") CoralRed else EcoEmerald400
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val nextStatus = if (p.accountStatus == "blocked") "active" else "blocked"
                                scope.launch {
                                    SupabaseClient.updateUserStatus(p.id, nextStatus)
                                    statusMessage = "Updated status for ${p.fullName} to $nextStatus"
                                    refresh()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (p.accountStatus == "blocked") "Unblock" else "Block",
                                color = if (p.accountStatus == "blocked") EcoEmerald400 else CoralRed,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    SupabaseClient.deleteProfile(p.id)
                                    statusMessage = "Deleted account for ${p.fullName}."
                                    refresh()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralRed.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Delete", color = CoralRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB 2: VOUCHER STUDIO & FLIPPABLE CARDS
        // =========================================================================
        if (currentTab == 2) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Voucher Studio & Card Vault",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap any card below to preview the 3D flippable barcode model.",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                    Button(
                        onClick = { showCreateVoucherDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Voucher", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (vouchers.isEmpty()) {
                item {
                    LiquidGlassCard {
                        Text(
                            text = "No active vouchers. Tap '+ New Voucher' to create your first partner voucher.",
                            color = Slate400,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            } else {
                items(vouchers) { v ->
                    FlippableVoucherCard(
                        voucher = v,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // User Voucher Redemptions Waiting List
            item {
                Spacer(modifier = Modifier.height(14.dp))
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
                    LiquidGlassCard(
                        highlightColor = if (isPending) LeafCoinGold else EcoEmerald400
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${red.voucherTitle} • ${red.brandName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "User: ${red.userName ?: "Eco User"} (${red.userEmail ?: "No email recorded"})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TechBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Paid: ${red.coinCost} Coins (Worth ₹${red.coinCost / 10})",
                                    color = LeafCoinGold,
                                    fontSize = 11.sp
                                )
                                if (red.voucherDetails != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Issued Code: ${red.voucherDetails}",
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
                                colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                onClick = {
                                    voucherToFulfill = red
                                    fulfillVoucherCodeInput = "ECO-${red.brandName.take(3).uppercase()}-${(1000..9999).random()}"
                                    fulfillVoucherNotesInput = "Valid on partner store checkout. Expires in 6 months."
                                    showFulfillVoucherDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Issue Code & Push to User Wallet ✉️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB 3: CASH REDEEMS & UPI SETTLEMENT DESK
        // =========================================================================
        if (currentTab == 3) {
            item {
                Text(
                    text = "💳 Coin-to-Cash UPI Settlement Desk",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Verify user UPI IDs and approve direct wallet cash payouts (10 Coins = ₹1).",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }

            if (cashRedemptions.isEmpty()) {
                item {
                    LiquidGlassCard {
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
                    val isPending = red.status != "completed"
                    LiquidGlassCard(
                        highlightColor = if (isPending) LeafCoinGold else EcoEmerald400
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "₹${red.cashAmount.toInt()} • ${red.holderName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "UPI ID: ${red.upiId} • ${red.coinsAmount} Coins",
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

                        if (isPending) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                onClick = {
                                    scope.launch {
                                        SupabaseClient.updateCashRedemptionStatus(red.id, "completed")
                                        statusMessage = "Settlement completed for ${red.holderName} (UPI: ${red.upiId})!"
                                        refresh()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verify UPI & Mark Settled ✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB 4: FLEET & SYSTEM SETTINGS
        // =========================================================================
        if (currentTab == 4) {
            // Admin Push Notifications Broadcast Studio
            item {
                Text(
                    text = "🔔 Push Notifications Broadcast",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Send instant push alerts & announcements to EcoVision users, agents, or companies.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LiquidGlassCard {
                    OutlinedTextField(
                        value = notifTitle,
                        onValueChange = { notifTitle = it },
                        label = { Text("Notification Title *") },
                        placeholder = { Text("e.g. Double Coins Weekend!") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notifMessage,
                        onValueChange = { notifMessage = it },
                        label = { Text("Message Body *") },
                        placeholder = { Text("Recycle dry plastic & cardboard today to earn 2x Coins!") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Target Audience", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("all" to "All", "user" to "Citizens", "agent" to "Agents", "company" to "Buyers").forEach { (roleCode, label) ->
                            val selected = notifTargetRole == roleCode
                            Surface(
                                color = if (selected) EcoEmerald600 else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { notifTargetRole = roleCode }
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 7.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    EcoButton(
                        text = "Send Push Notification",
                        icon = Icons.Default.NotificationsActive,
                        enabled = notifTitle.isNotBlank() && notifMessage.isNotBlank(),
                        onClick = {
                            scope.launch {
                                val newNotif = AdminNotification(
                                    id = UUID.randomUUID().toString(),
                                    title = notifTitle.trim(),
                                    message = notifMessage.trim(),
                                    targetRole = notifTargetRole,
                                    createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())
                                )
                                SupabaseClient.sendAdminNotification(newNotif)
                                NotificationHelper.showPushNotification(context, newNotif.title, newNotif.message)
                                statusMessage = "Push notification broadcast to ${notifTargetRole.uppercase()}!"
                                notifTitle = ""
                                notifMessage = ""
                                refresh()
                            }
                        }
                    )

                    if (adminNotifications.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Recent Broadcasts (${adminNotifications.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        adminNotifications.take(3).forEach { n ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("${n.title} • [${n.targetRole.uppercase()}]", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoEmerald400)
                                    Text(n.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Vehicle Requests
            item {
                Text(
                    text = "🚚 Agent Vehicle Change Requests",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Inspect new vehicle plate and photo; photo is purged upon approval.",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }

            if (vehicleChangeRequests.isEmpty()) {
                item {
                    LiquidGlassCard {
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
                    LiquidGlassCard {
                        Text(text = "New Vehicle: ${req.newVehicleNumber}", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = "Previous: ${req.oldVehicleNumber ?: "Not recorded"}", color = Slate400, fontSize = 11.sp)
                        if (req.vehiclePhotoUrl != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = req.vehiclePhotoUrl,
                                contentDescription = "Vehicle Photo",
                                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        if (req.status == "pending") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    onClick = {
                                        scope.launch {
                                            SupabaseClient.reviewVehicleChangeRequest(req.id, req.agentId, req.newVehicleNumber, "approved")
                                            statusMessage = "Approved vehicle change to ${req.newVehicleNumber}!"
                                            refresh()
                                        }
                                    }
                                ) {
                                    Text("Approve ✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    onClick = {
                                        scope.launch {
                                            SupabaseClient.reviewVehicleChangeRequest(req.id, req.agentId, req.newVehicleNumber, "rejected", "Details mismatch")
                                            statusMessage = "Rejected vehicle change."
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

            // Maintenance Switch
            item {
                Spacer(modifier = Modifier.height(14.dp))
                LiquidGlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Maintenance Mode Switch", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(text = "Redirect non-admin users to maintenance screen", color = Slate400, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isMaintenanceMode,
                            onCheckedChange = { chk ->
                                isMaintenanceMode = chk
                                scope.launch {
                                    val updated = settings.copy(maintenanceMode = chk, maintenanceNotice = downtimeNotice)
                                    SupabaseClient.updatePlatformSettings(updated)
                                    SessionManager.updateSettings(updated)
                                    statusMessage = if (chk) "Maintenance mode enabled!" else "Platform reopened to all users!"
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = CoralRed, checkedTrackColor = CoralRed.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            // Dynamic Slabs
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Dynamic Pricing Slabs Engine", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(slabs) { slab ->
                LiquidGlassCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "${slab.categoryId.replace("_", " ").uppercase()} • ${slab.tierName}", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(text = "Range: ${slab.minKg} kg to ${slab.maxKg ?: "Infinity"} kg", color = Slate400, fontSize = 11.sp)
                        }
                        Text(text = "₹${slab.ratePerKg}/kg", color = EcoEmerald400, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }

    // ==========================================
    // MODAL 1: PROOF DOCUMENT VIEWER DIALOG
    // ==========================================
    if (proofProfileToView != null) {
        val target = proofProfileToView!!
        ProofViewerDialog(
            profile = target,
            onDismiss = { proofProfileToView = null },
            onApprove = {
                scope.launch {
                    SupabaseClient.updateUserStatus(target.id, "verified")
                    statusMessage = "✅ Successfully Verified KYC for ${target.fullName}!"
                    proofProfileToView = null
                    refresh()
                }
            },
            onReject = {
                userToReject = target
                selectedRejectionReason = "Proof image is blurry or low quality"
                customRejectionReason = ""
                proofProfileToView = null
                showRejectKycDialog = true
            }
        )
    }

    // ==========================================
    // MODAL 2: CREATE VOUCHER DIALOG
    // ==========================================
    if (showCreateVoucherDialog) {
        val calculatedDiscount = (vCoinCost.toIntOrNull() ?: 100) / 10.0
        AlertDialog(
            onDismissRequest = { showCreateVoucherDialog = false },
            containerColor = Slate900,
            titleContentColor = Color.White,
            textContentColor = Slate300,
            title = { Text("Publish Partner Voucher for Wallet", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = vTitle,
                        onValueChange = { vTitle = it },
                        label = { Text("Voucher Title *") },
                        placeholder = { Text("e.g. ₹50 Off Zero-Waste Groceries") },
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
                        label = { Text("Voucher Image URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = vDesc,
                        onValueChange = { vDesc = it },
                        label = { Text("Description & Terms") },
                        placeholder = { Text("Valid on organic groceries...") },
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
                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
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
                    Text("Publish Voucher", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateVoucherDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            }
        )
    }

    // ==========================================
    // MODAL 3: FULFILL VOUCHER DIALOG
    // ==========================================
    if (showFulfillVoucherDialog && voucherToFulfill != null) {
        val req = voucherToFulfill!!
        AlertDialog(
            onDismissRequest = {
                showFulfillVoucherDialog = false
                voucherToFulfill = null
            },
            containerColor = Slate900,
            titleContentColor = Color.White,
            textContentColor = Slate300,
            title = { Text("Issue & Push Voucher Code", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "User: ${req.userName ?: "Eco User"} (${req.userEmail ?: "No email"})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "Requested: ${req.voucherTitle} (${req.brandName})", color = LeafCoinGold, fontSize = 12.sp)
                    Text(text = "Coins Paid: ${req.coinCost} Coins (Worth ₹${req.coinCost / 10})", color = EcoEmerald400, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = fulfillVoucherCodeInput,
                        onValueChange = { fulfillVoucherCodeInput = it },
                        label = { Text("Voucher Code / Claim PIN *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fulfillVoucherNotesInput,
                        onValueChange = { fulfillVoucherNotesInput = it },
                        label = { Text("Redemption Instructions / Expiry") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = fulfillVoucherCodeInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                    onClick = {
                        val fullDetails = if (fulfillVoucherNotesInput.isNotBlank()) {
                            "${fulfillVoucherCodeInput.trim()} | ${fulfillVoucherNotesInput.trim()}"
                        } else {
                            fulfillVoucherCodeInput.trim()
                        }
                        scope.launch {
                            SupabaseClient.fulfillVoucherRedemption(req.id, fullDetails)
                            statusMessage = "🎉 Voucher code issued! Pushed to user wallet and emailed."
                            refresh()
                            showFulfillVoucherDialog = false
                            voucherToFulfill = null
                        }
                    }
                ) {
                    Text("Submit & Push to User ✉️", color = Color.White, fontWeight = FontWeight.Bold)
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

    // ==========================================
    // MODAL 4: REJECTION REASON DIALOG
    // ==========================================
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
            containerColor = Slate900,
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
                    Text(text = "Specify rejection reason (user sees this in Profile to reapply):", fontSize = 11.sp, color = Slate400)
                    Spacer(modifier = Modifier.height(10.dp))

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
                        label = { Text("Or Type Custom Reason") },
                        modifier = Modifier.fillMaxWidth()
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
