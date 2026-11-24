package org.ecovision.app.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
fun UserHomeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToBin: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToDoorstep: (String) -> Unit,
    onNavigateToRate: (String) -> Unit
) {
    val profile by SessionManager.currentProfile.collectAsState()
    var activePickups by remember { mutableStateOf<List<PickupRequest>>(emptyList()) }
    var virtualBinCount by remember { mutableIntStateOf(0) }
    var virtualBinValue by remember { mutableDoubleStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(profile?.id) {
        if (profile != null) {
            isLoading = true
            val pickups = SupabaseClient.getUserPickups(profile!!.id)
            activePickups = pickups
            val bin = SupabaseClient.getVirtualBin(profile!!.id)
            virtualBinCount = bin.size
            virtualBinValue = bin.sumOf { it.estimatedPrice }
            isLoading = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // KYC Status Notice
            if (profile?.kycStatus == "pending") {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.HourglassTop, null, tint = LeafCoinGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "KYC Verification Pending",
                                style = MaterialTheme.typography.labelLarge,
                                color = LeafCoinGold
                            )
                            Text(
                                text = "Admin is reviewing your Aadhaar / ID details.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }
                    }
                }
            }
        }

        // Quick Stats Hero Card
        item {
            GlassCard(
                backgroundColor = Slate850,
                borderColor = CardBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${profile?.fullName?.split(" ")?.firstOrNull() ?: "Eco Hero"} 👋",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Area: ${profile?.pincode ?: "682001"} · Verified Household",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(LeafCoinGold.copy(alpha = 0.15f))
                            .border(1.dp, LeafCoinGold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable { onNavigateToWallet() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Spa, null, tint = LeafCoinGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${profile?.leafCoins ?: 0} 🍃",
                                style = MaterialTheme.typography.titleMedium,
                                color = LeafCoinGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Virtual Bin Quick Peek
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToBin() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EcoEmerald500.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DeleteOutline, null, tint = EcoEmerald400, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Virtual Bin",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$virtualBinCount items ready to recycle",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${String.format("%.1f", virtualBinValue)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = EcoEmerald400,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Est. Value",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                    }
                }
            }
        }

        // 4 Core Action Hub
        item {
            Text(
                text = "Eco Action Hub",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                HubCard(
                    title = "AI Waste Scanner",
                    subtitle = "Scan & Upcycle",
                    icon = Icons.Default.CameraAlt,
                    color = EcoEmerald500,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToScan
                )
                Spacer(modifier = Modifier.width(12.dp))
                HubCard(
                    title = "Virtual Bin",
                    subtitle = "Request Pickup",
                    icon = Icons.Default.Inventory2,
                    color = TechBlue,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToBin
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                HubCard(
                    title = "Upcycled Store",
                    subtitle = "P2P Bargain",
                    icon = Icons.Default.Storefront,
                    color = AiViolet,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMarketplace
                )
                Spacer(modifier = Modifier.width(12.dp))
                HubCard(
                    title = "Leaf Wallet",
                    subtitle = "Redeem Cash",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = LeafCoinGold,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToWallet
                )
            }
        }

        // Active Pickup Tracker & Doorstep Security
        item {
            Text(
                text = "Pickup Tracking & Doorstep Security",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        if (activePickups.isEmpty()) {
            item {
                GlassCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.LocalShipping, null, tint = Slate500, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Active Pickup Requests",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate300
                        )
                        Text(
                            text = "Scan recyclables into your Virtual Bin to request local doorstep pickup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
            }
        } else {
            items(activePickups) { pickup ->
                PickupTrackingCard(
                    pickup = pickup,
                    onDoorstepClick = { onNavigateToDoorstep(pickup.id) },
                    onRateClick = { onNavigateToRate(pickup.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
fun HubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        color = Slate850
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )
        }
    }
}

@Composable
fun PickupTrackingCard(
    pickup: PickupRequest,
    onDoorstepClick: () -> Unit,
    onRateClick: () -> Unit
) {
    val statusColor = when (pickup.status) {
        "requested" -> LeafCoinGold
        "agent_assigned" -> TechBlue
        "at_doorstep" -> AiViolet
        "spot_weighed" -> EcoEmerald400
        "completed" -> EcoEmerald500
        else -> Slate400
    }

    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(text = pickup.status.uppercase().replace("_", " "), color = statusColor)
            Text(
                text = "₹${pickup.finalCashAmount ?: pickup.totalEstimatedAmount}",
                style = MaterialTheme.typography.titleMedium,
                color = EcoEmerald400,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Address: ${pickup.pickupAddress}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        Text(
            text = "Est. Weight: ${pickup.totalEstimatedWeight} kg · Verified Weight: ${pickup.actualVerifiedWeight ?: "Pending scale"} kg",
            style = MaterialTheme.typography.bodySmall,
            color = Slate400
        )

        // Doorstep Vehicle Verification Panel
        if (pickup.status in listOf("agent_assigned", "at_doorstep", "spot_weighed")) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = Slate800,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=300",
                        contentDescription = "Agent Vehicle",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Doorstep Match Required",
                            style = MaterialTheme.typography.labelSmall,
                            color = LeafCoinGold
                        )
                        Text(
                            text = "Vehicle: KL-07-CD-5678",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Agent: Rahul Kumar (★ 4.95)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            EcoButton(
                text = "Open Doorstep Verification",
                icon = Icons.Default.VerifiedUser,
                onClick = onDoorstepClick
            )
        } else if (pickup.status == "completed") {
            Spacer(modifier = Modifier.height(12.dp))
            EcoButton(
                text = "Rate Agent (12-Hr Window)",
                icon = Icons.Default.Star,
                onClick = onRateClick
            )
        }
    }
}
