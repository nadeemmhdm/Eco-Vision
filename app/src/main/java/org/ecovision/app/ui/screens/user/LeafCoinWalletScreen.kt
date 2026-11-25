package org.ecovision.app.ui.screens.user

import android.content.ClipData
import android.content.ClipboardManager as AndroidClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.EcoButton
import org.ecovision.app.ui.components.EcoCoinIcon
import org.ecovision.app.ui.components.GlassCard
import org.ecovision.app.ui.components.StatusBadge
import java.util.Locale
import java.util.UUID

@Composable
fun LeafCoinWalletScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val profile by SessionManager.currentProfile.collectAsState()
    val scope = rememberCoroutineScope()

    var transactions by remember { mutableStateOf<List<LeafCoinTransaction>>(emptyList()) }
    var availableVouchers by remember { mutableStateOf<List<EcoVoucher>>(emptyList()) }
    var myVoucherRedemptions by remember { mutableStateOf<List<VoucherRedemption>>(emptyList()) }
    var cashRedemptions by remember { mutableStateOf<List<CashRedemption>>(emptyList()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var selectedVoucherForRedeem by remember { mutableStateOf<EcoVoucher?>(null) }

    // Cash Redeem Dialog State (10 Coins = 1 Rupee)
    var showCashRedeemDialog by remember { mutableStateOf(false) }
    var redeemUpiId by remember { mutableStateOf(profile?.upiId ?: "") }
    var redeemHolderName by remember { mutableStateOf(profile?.upiHolderName ?: profile?.fullName ?: "") }
    var redeemCoinsInput by remember { mutableStateOf("100") }
    var isSubmittingCashRedeem by remember { mutableStateOf(false) }

    val userCoins = profile?.leafCoins ?: 0
    val cashEquivalent = userCoins / 10.0

    fun refresh() {
        if (profile != null) {
            scope.launch {
                transactions = SupabaseClient.getLeafCoinTransactions(profile!!.id)
                availableVouchers = SupabaseClient.getVouchers()
                val allCashRedemptions = SupabaseClient.getCashRedemptions()
                cashRedemptions = allCashRedemptions.filter { it.userId == profile!!.id }

                val allVoucherRedemptions = SupabaseClient.getVoucherRedemptions()
                myVoucherRedemptions = allVoucherRedemptions.filter { it.userId == profile!!.id }

                SessionManager.refreshProfile()
            }
        }
    }

    LaunchedEffect(profile?.id) {
        refresh()
    }

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
                    text = "Wallet",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Coins & Wallet Value Card
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Coins Balance",
                                style = MaterialTheme.typography.labelLarge,
                                color = Slate400
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                EcoCoinIcon(size = 34.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "$userCoins",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = LeafCoinGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Redeemable Cash",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%.2f", cashEquivalent)}",
                                style = MaterialTheme.typography.titleLarge,
                                color = EcoEmerald400,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = LeafCoinGold.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, LeafCoinGold.copy(alpha = 0.3f)),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "10 Coins = ₹1.00",
                                    color = LeafCoinGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Direct UPI Cash Payout Trigger
                    EcoButton(
                        text = if (userCoins >= 50) "Redeem Coins for Cash via UPI" else "Min 50 Coins to Redeem Cash",
                        icon = Icons.Default.CurrencyRupee,
                        enabled = userCoins >= 50,
                        onClick = { showCashRedeemDialog = true }
                    )
                }
            }

            if (statusMessage != null) {
                item {
                    StatusBadge(text = statusMessage!!, color = EcoEmerald400)
                }
            }

            // ==========================================
            // ADMIN-LISTED PARTNER VOUCHERS SECTION
            // ==========================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎁 Partner Eco Vouchers",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Redeem Coins for shopping and service discount vouchers",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(availableVouchers) { voucher ->
                        val canAfford = userCoins >= voucher.coinCost
                        val rupeeWorth = voucher.coinCost / 10.0
                        Surface(
                            color = Slate850,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            modifier = Modifier
                                .width(240.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            Column {
                                if (voucher.imageUrl != null) {
                                    AsyncImage(
                                        model = voucher.imageUrl,
                                        contentDescription = voucher.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(115.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = EcoEmerald500.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = voucher.brandName,
                                            color = EcoEmerald400,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = voucher.title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = voucher.description ?: "Valid on zero-waste partner items.",
                                        color = Slate400,
                                        fontSize = 11.sp,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            EcoCoinIcon(size = 16.dp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Column {
                                                Text(
                                                    text = "${voucher.coinCost} Coins",
                                                    color = LeafCoinGold,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = "(₹${String.format(Locale.US, "%.0f", rupeeWorth)} value)",
                                                    color = Slate400,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                        Button(
                                            enabled = canAfford,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = EcoEmerald600,
                                                disabledContainerColor = Slate800
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            onClick = { selectedVoucherForRedeem = voucher }
                                        ) {
                                            Text(
                                                text = if (canAfford) "Redeem" else "Need ${voucher.coinCost - userCoins}",
                                                fontSize = 11.sp,
                                                color = if (canAfford) Color.White else Slate500
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // MY VOUCHER REDEMPTIONS & WAITING LIST
            // ==========================================
            if (myVoucherRedemptions.isNotEmpty()) {
                item {
                    Text(
                        text = "🎟️ My Voucher Redemptions & Claims",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(myVoucherRedemptions) { red ->
                    val isPending = red.status == "waitlist" || red.status == "pending"
                    Surface(
                        color = Slate850,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isPending) LeafCoinGold.copy(alpha = 0.5f) else EcoEmerald500.copy(alpha = 0.6f)
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
                                        text = red.voucherTitle,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${red.brandName} · Paid ${red.coinCost} Coins",
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                }
                                StatusBadge(
                                    text = if (isPending) "WAITING LIST ⏳" else "ACTIVE ✓",
                                    color = if (isPending) LeafCoinGold else EcoEmerald400
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isPending) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Slate900,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = LeafCoinGold, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Waiting for Admin to issue digital voucher code. Details will be delivered here and to ${profile?.email ?: "your email"}.",
                                            color = Slate300,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            } else {
                                // Completed / Fulfilled by Admin
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EcoEmerald900.copy(alpha = 0.25f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EcoEmerald500.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "VOUCHER DETAILS & CODE",
                                                    color = EcoEmerald400,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = red.voucherDetails ?: "ECO-CLAIMED",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            IconButton(onClick = {
                                                red.voucherDetails?.let { code ->
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? AndroidClipboardManager
                                                    clipboard?.setPrimaryClip(ClipData.newPlainText("Voucher Code", code))
                                                    statusMessage = "Copied voucher code to clipboard!"
                                                }
                                            }) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = EcoEmerald400)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "✉️ Voucher code and usage terms delivered to ${red.userEmail ?: profile?.email} ✓",
                                            color = Slate300,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // CASH UPI REDEMPTIONS (WAITLIST QUEUE)
            // ==========================================
            if (cashRedemptions.isNotEmpty()) {
                item {
                    Text(
                        text = "💸 Cash UPI Redemptions",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(cashRedemptions) { red ->
                    val isPending = red.status == "waitlist" || red.status == "processing"
                    Surface(
                        color = Slate850,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isPending) LeafCoinGold.copy(alpha = 0.4f) else EcoEmerald500.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "₹${String.format(Locale.US, "%.2f", red.cashAmount)} Cash Payout",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "UPI: ${red.upiId} · ${red.coinsAmount} Coins Redeemed",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = if (isPending) "Status: Waiting List Queue (Processing)" else "Status: Settled · Credited to UPI ✓",
                                    color = if (isPending) LeafCoinGold else EcoEmerald400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            StatusBadge(
                                text = if (isPending) "WAITLIST" else "SETTLED ✓",
                                color = if (isPending) LeafCoinGold else EcoEmerald400
                            )
                        }
                    }
                }
            }

            // ==========================================
            // COINS ACTIVITY LEDGER
            // ==========================================
            item {
                Text(
                    text = "Coins Activity Ledger",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Surface(
                        color = Slate850,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EcoCoinIcon(size = 18.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Initial Welcome Bonus: +50 Coins credited 🪙",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LeafCoinGold
                            )
                        }
                    }
                }
            } else {
                items(transactions) { tx ->
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
                                    text = tx.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = tx.type.replace("_", " ").uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate400
                                )
                            }
                            Text(
                                text = if (tx.amount > 0) "+${tx.amount}" else "${tx.amount}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (tx.amount > 0) EcoEmerald400 else CoralRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // ==========================================
        // VOUCHER REDEMPTION CONFIRMATION DIALOG
        // ==========================================
        if (selectedVoucherForRedeem != null) {
            val v = selectedVoucherForRedeem!!
            val rupeeValue = v.coinCost / 10.0
            AlertDialog(
                onDismissRequest = { selectedVoucherForRedeem = null },
                containerColor = Slate850,
                titleContentColor = Color.White,
                textContentColor = Slate300,
                title = { Text("Redeem ${v.brandName} Voucher?") },
                text = {
                    Column {
                        Text("You are redeeming ${v.coinCost} Coins for:", color = Slate300)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(v.title, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(v.description ?: "", fontSize = 12.sp, color = Slate400)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Value: ₹${String.format(Locale.US, "%.0f", rupeeValue)} (10 Coins = ₹1.00)", color = EcoEmerald400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate900,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Text(
                                text = "Notice: This request is sent to the Waiting List. Once approved, Admin will issue your official digital voucher code, which will appear in your Wallet and be delivered to your registered email (${profile?.email ?: "account"}).",
                                color = Slate400,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Remaining Balance after redemption: ${userCoins - v.coinCost} Coins", color = LeafCoinGold, fontSize = 11.sp)
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                        onClick = {
                            scope.launch {
                                if (profile != null) {
                                    // Deduct coins immediately
                                    SupabaseClient.redeemLeafCoins(profile!!.id, v.coinCost, "Redeemed ${v.title}")

                                    // Add to voucher waiting list
                                    val newRedemption = VoucherRedemption(
                                        id = UUID.randomUUID().toString(),
                                        userId = profile!!.id,
                                        userName = profile!!.fullName,
                                        userEmail = profile!!.email,
                                        voucherId = v.id,
                                        voucherTitle = v.title,
                                        brandName = v.brandName,
                                        coinCost = v.coinCost,
                                        status = "waitlist",
                                        createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(java.util.Date())
                                    )
                                    SupabaseClient.submitVoucherRedemption(newRedemption)

                                    statusMessage = "🎉 Redemption submitted! Added to waiting list. Admin will issue your code shortly."
                                    refresh()
                                }
                            }
                            selectedVoucherForRedeem = null
                        }
                    ) {
                        Text("Confirm & Submit to Waiting List", color = Slate950, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedVoucherForRedeem = null }) {
                        Text("Cancel", color = Slate400)
                    }
                }
            )
        }

        // ==========================================
        // REAL CASH REDEEM DIALOG (10 COINS = 1 RUPEE)
        // ==========================================
        if (showCashRedeemDialog) {
            val coinsToRedeem = redeemCoinsInput.toIntOrNull() ?: 0
            val payoutRupees = coinsToRedeem / 10.0
            val isUpiValid = redeemUpiId.contains("@") && redeemUpiId.length >= 5
            val canSubmit = coinsToRedeem in 50..userCoins && isUpiValid && redeemHolderName.isNotBlank()

            AlertDialog(
                onDismissRequest = { showCashRedeemDialog = false },
                containerColor = Slate850,
                titleContentColor = Color.White,
                textContentColor = Slate300,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EcoCoinIcon(size = 22.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Redeem Coins for Cash", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Exchange your verified coins directly for cash sent to your UPI ID.\nRate: 10 Coins = ₹1.00",
                            fontSize = 12.sp,
                            color = Slate400
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = redeemHolderName,
                            onValueChange = { redeemHolderName = it },
                            label = { Text("Account Holder Name *") },
                            placeholder = { Text("Full name as per bank") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = redeemUpiId,
                            onValueChange = { redeemUpiId = it.trim() },
                            label = { Text("UPI Virtual Payment Address (VPA) *") },
                            placeholder = { Text("e.g. adhil@oksbi") },
                            isError = redeemUpiId.isNotBlank() && !isUpiValid,
                            supportingText = {
                                if (redeemUpiId.isNotBlank() && !isUpiValid) {
                                    Text("Enter valid UPI ID (e.g. name@bank)", color = CoralRed, fontSize = 10.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = redeemCoinsInput,
                            onValueChange = { redeemCoinsInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Coins to Redeem (Min 50) *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Available: $userCoins Coins", fontSize = 11.sp, color = Slate400)
                            Text("Payout Amount: ₹${String.format(Locale.US, "%.2f", payoutRupees)}", fontSize = 12.sp, color = EcoEmerald400, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = canSubmit && !isSubmittingCashRedeem,
                        colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                        onClick = {
                            scope.launch {
                                isSubmittingCashRedeem = true
                                if (profile != null) {
                                    SupabaseClient.redeemLeafCoins(profile!!.id, coinsToRedeem, "Redeemed $coinsToRedeem Coins for Cash via UPI")
                                    val newRed = CashRedemption(
                                        id = UUID.randomUUID().toString(),
                                        userId = profile!!.id,
                                        coinsAmount = coinsToRedeem,
                                        cashAmount = payoutRupees,
                                        upiId = redeemUpiId,
                                        holderName = redeemHolderName,
                                        status = "waitlist",
                                        createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(java.util.Date())
                                    )
                                    SupabaseClient.submitCashRedemption(newRed)

                                    val updatedProfile = profile!!.copy(upiId = redeemUpiId, upiHolderName = redeemHolderName)
                                    SupabaseClient.updateProfile(updatedProfile)

                                    statusMessage = "🎉 Redemption of ₹${String.format(Locale.US, "%.2f", payoutRupees)} requested! Added to UPI payment waitlist."
                                    refresh()
                                }
                                isSubmittingCashRedeem = false
                                showCashRedeemDialog = false
                            }
                        }
                    ) {
                        Text(
                            text = if (isSubmittingCashRedeem) "Submitting..." else "Confirm & Redeem ₹${String.format(Locale.US, "%.2f", payoutRupees)}",
                            color = Slate950,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCashRedeemDialog = false }) {
                        Text("Cancel", color = Slate400)
                    }
                }
            )
        }
    }
}
