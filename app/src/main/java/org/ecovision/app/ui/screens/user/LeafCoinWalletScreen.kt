package org.ecovision.app.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.EcoButton
import org.ecovision.app.ui.components.GlassCard
import org.ecovision.app.ui.components.StatusBadge

@Composable
fun LeafCoinWalletScreen(onBack: () -> Unit) {
    val profile by SessionManager.currentProfile.collectAsState()
    val settings by SessionManager.platformSettings.collectAsState()
    val scope = rememberCoroutineScope()

    var transactions by remember { mutableStateOf<List<LeafCoinTransaction>>(emptyList()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val minRedemption = settings.minRedemptionCoins
    val coinRate = settings.coinToCashRate // e.g. 0.10 => 10 coins = 1 rupee
    val canRedeem = (profile?.leafCoins ?: 0) >= minRedemption

    fun refresh() {
        if (profile != null) {
            scope.launch {
                transactions = SupabaseClient.getLeafCoinTransactions(profile!!.id)
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
                    text = "Leaf Coin Rewards Wallet",
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
                .padding(horizontal = 20.dp)
        ) {
            // Leaf Coin Balance Card
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Leaf Coins Balance",
                            style = MaterialTheme.typography.labelLarge,
                            color = Slate400
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Spa, null, tint = LeafCoinGold, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${profile?.leafCoins ?: 0}",
                                style = MaterialTheme.typography.headlineLarge,
                                color = LeafCoinGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Cash Value",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                        Text(
                            text = "₹${String.format("%.2f", (profile?.leafCoins ?: 0) * coinRate)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = EcoEmerald400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Referral Bonus Box
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Your Referral Code (+100 Coins)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                            Text(
                                text = profile?.referralCode ?: "ADHIL10",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { statusMessage = "Referral code copied to clipboard!" },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                        ) {
                            Text("Share Code", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Redemption Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EcoButton(
                        text = if (canRedeem) "Redeem Cash (UPI)" else "Min $minRedemption Coins",
                        icon = Icons.Default.CurrencyRupee,
                        enabled = canRedeem,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch {
                                if (profile != null) {
                                    SupabaseClient.redeemLeafCoins(profile!!.id, 200, "Direct UPI Cash")
                                    statusMessage = "₹20 transferred to registered UPI ID!"
                                    refresh()
                                }
                            }
                        }
                    )
                    EcoButton(
                        text = "Eco Vouchers",
                        icon = Icons.Default.CardGiftcard,
                        isSecondary = true,
                        enabled = canRedeem,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch {
                                if (profile != null) {
                                    SupabaseClient.redeemLeafCoins(profile!!.id, 200, "EcoStore Voucher")
                                    statusMessage = "₹25 Shopping voucher generated: ECO-VOUCHER-789"
                                    refresh()
                                }
                            }
                        }
                    )
                }
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                StatusBadge(text = statusMessage!!, color = EcoEmerald400)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction History
            Text(
                text = "Leaf Coin Activity Ledger",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (transactions.isEmpty()) {
                    item {
                        Surface(
                            color = Slate850,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Initial Welcome Bonus: +50 Leaf Coins credited 🍃",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LeafCoinGold,
                                modifier = Modifier.padding(14.dp)
                            )
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
                                    text = if (tx.amount > 0) "+${tx.amount} 🍃" else "${tx.amount} 🍃",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (tx.amount > 0) EcoEmerald400 else CoralRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
