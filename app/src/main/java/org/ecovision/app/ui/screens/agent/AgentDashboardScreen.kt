package org.ecovision.app.ui.screens.agent

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.*

@Composable
fun AgentDashboardScreen() {
    val context = LocalContext.current
    val profile by SessionManager.currentProfile.collectAsState()
    val agentDetails by SessionManager.agentDetails.collectAsState()
    val agentTab by SessionManager.agentTab.collectAsState()
    val scope = rememberCoroutineScope()

    var isOnDuty by remember { mutableStateOf(true) }
    var availablePickups by remember { mutableStateOf<List<PickupRequest>>(emptyList()) }
    var activePickups by remember { mutableStateOf<List<PickupRequest>>(emptyList()) }
    var godownItems by remember { mutableStateOf<List<GodownInventoryItem>>(emptyList()) }
    var storeOrders by remember { mutableStateOf<List<MarketplaceOrder>>(emptyList()) }

    var selectedPickupForWeighing by remember { mutableStateOf<PickupRequest?>(null) }
    var actualScaleWeight by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var popupNotice by remember { mutableStateOf<Pair<String, String>?>(null) }

    fun refresh() {
        scope.launch {
            val zones = agentDetails?.assignedZones ?: listOf("682001", "682030", "676505")
            availablePickups = SupabaseClient.getAgentAvailablePickups(zones)
            storeOrders = SupabaseClient.getAgentMarketplaceOrders()
            if (profile != null) {
                activePickups = SupabaseClient.getAgentActivePickups(profile!!.id)
                godownItems = SupabaseClient.getGodownStockForAgent(profile!!.id)
                isOnDuty = SupabaseClient.getAgentDutyStatus(profile!!.id)
            }
        }
    }

    fun dialPhone(phone: String?) {
        val cleanPhone = phone?.takeIf { it.isNotBlank() } ?: "+919847012345"
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
            context.startActivity(intent)
        } catch (_: Exception) {
            statusMessage = "Dialer ready for $cleanPhone"
        }
    }

    LaunchedEffect(profile?.id, agentTab) {
        refresh()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (statusMessage != null) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                StatusBadge(text = statusMessage!!, color = EcoEmerald500)
            }
        }

        // ==========================================
        // PAGE 0: AGENT HOME PAGE (Dedicated Home View)
        // ==========================================
        if (agentTab == 0) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Redesigned First-View Agent Name & Vehicle Details Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF10B981))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LocalShipping, null, tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = profile?.fullName ?: "Rafiq Khan",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Verified Pickup Agent • ${profile?.district ?: "Malappuram"}",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Switch(
                                checked = isOnDuty,
                                onCheckedChange = { newDuty ->
                                    isOnDuty = newDuty
                                    scope.launch {
                                        SupabaseClient.setAgentDutyStatus(profile?.id ?: "agent-rahul", newDuty)
                                        popupNotice = (if (newDuty) "Shift Active (ON DUTY)" else "Shift Paused (OFF DUTY)") to
                                            if (newDuty) "You are now visible to households for pickup dispatch." else "You are now hidden from household live tracking."
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EcoEmerald700,
                                    checkedTrackColor = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Prominent Vehicle Details Strip
                        Surface(
                            color = Color.Black.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("ASSIGNED VEHICLE", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${agentDetails?.vehicleNumber ?: "KL-07-AB-1234"} · Eco Green Van",
                                        color = LeafCoinGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                StatusBadge(
                                    text = if (isOnDuty) "ON DUTY" else "OFF DUTY",
                                    color = if (isOnDuty) EcoEmerald300 else CoralRed,
                                    backgroundColor = Color.Black.copy(alpha = 0.35f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ShiftStatItem(label = "Verified Today", value = "142 kg", color = Color.White)
                            ShiftStatItem(label = "Shift Earnings", value = "₹1,850", color = LeafCoinGold)
                            ShiftStatItem(label = "Pending Queue", value = "${availablePickups.size + storeOrders.size}", color = Color.White)
                            ShiftStatItem(label = "Rating", value = "★ 4.8", color = Color.White)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Quick Operations",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { SessionManager.setAgentTab(1) }
                    ) {
                        Icon(Icons.Default.LocalShipping, null, tint = EcoEmerald500, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${availablePickups.size + activePickups.size + storeOrders.size} Pickups",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Household requests & store orders",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { SessionManager.setAgentTab(2) }
                    ) {
                        Icon(Icons.Default.Inventory, null, tint = LeafCoinGold, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${godownItems.size} Godown",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "History & godown stock gate",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // ==========================================
        // PAGE 1: PICKUPS & P2P STORE ORDERS PAGE
        // ==========================================
        if (agentTab == 1) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Doorstep Pickups & Store Orders",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // 1A. Upcycled P2P Store Seller Pickups & Delivery Orders
            item {
                Text(
                    text = "🛍️ P2P Store Seller Pickups (${storeOrders.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            if (storeOrders.isEmpty()) {
                item {
                    GlassCard {
                        Text(
                            text = "No P2P craft store orders awaiting pickup from sellers.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                items(storeOrders) { order ->
                    val nextStatusPair = when (order.status.lowercase()) {
                        "placed" -> "picked_up" to "Mark Picked Up"
                        "picked_up" -> "processing" to "Mark Processing"
                        "processing" -> "shipped" to "Mark Shipped"
                        "shipped" -> "completed" to "Mark Completed ✓"
                        else -> null
                    }
                    GlassCard {
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
                                color = if (order.status == "completed") EcoEmerald500 else LeafCoinGold
                            )
                        }
                        if (order.itemsSummary.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(order.itemsSummary, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Deliver To: ${order.deliveryAddress}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        Text("Amount: ₹${order.totalAmount.toInt()} (${order.paymentMethod})", color = EcoEmerald500, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        if (nextStatusPair != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            EcoButton(
                                text = nextStatusPair.second,
                                icon = Icons.Default.LocalShipping,
                                onClick = {
                                    scope.launch {
                                        SupabaseClient.updateMarketplaceOrderStatus(order.id, nextStatusPair.first)
                                        popupNotice = "Order Status Updated!" to "Order #${order.id.take(8).uppercase()} is now ${nextStatusPair.first.replace("_", " ").uppercase()}."
                                        refresh()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 1B. Active Doorstep Waste Pickups
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Active Waste Pickups (${activePickups.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            if (activePickups.isEmpty()) {
                item {
                    GlassCard {
                        Text(
                            text = "No active waste pickups assigned yet. Accept a nearby household request below.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                items(activePickups) { req ->
                    GlassCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = req.user?.fullName ?: "Household Pickup",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(text = req.status.replace("_", " ").uppercase(), color = EcoEmerald500)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Address: ${req.pickupAddress}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text(text = "Est. Weight: ${req.totalEstimatedWeight} kg • Est. ₹${req.totalEstimatedAmount}", color = LeafCoinGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { dialPhone(req.user?.mobile) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call")
                            }
                            Button(
                                onClick = {
                                    selectedPickupForWeighing = req
                                    actualScaleWeight = "${req.totalEstimatedWeight}"
                                },
                                modifier = Modifier.weight(1.4f),
                                colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Balance, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Weigh & Pay", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 1C. Nearby Household Requests
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nearby Pickup Requests (${availablePickups.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${profile?.district ?: "Malappuram"} Zone",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            if (availablePickups.isEmpty()) {
                item {
                    GlassCard {
                        Text(
                            text = "All household pickups in your zone are currently assigned.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                items(availablePickups) { req ->
                    GlassCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Est. ₹${req.totalEstimatedAmount}",
                                style = MaterialTheme.typography.titleMedium,
                                color = EcoEmerald500,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(text = "${req.totalEstimatedWeight} kg", color = TechBlue)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Pickup Location: ${req.pickupAddress}", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                        Text(text = "Pincode: ${req.pincode} · Nearby Dispatch", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        EcoButton(
                            text = "Accept & Start Navigation",
                            icon = Icons.Default.CheckCircle,
                            onClick = {
                                scope.launch {
                                    if (profile != null) {
                                        SupabaseClient.acceptPickup(req.id, profile!!.id)
                                        popupNotice = "Pickup Accepted!" to "Added to your active pickups list."
                                        refresh()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // ==========================================
        // PAGE 2: HISTORY & GODOWN ENTRY GATE
        // ==========================================
        if (agentTab == 2) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "History & Godown Stock Gate",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Collected waste batches must be formally unloaded and accepted into Godown Stock.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (godownItems.isEmpty()) {
                item {
                    GlassCard {
                        Text(
                            text = "No pending batches in vehicle trunk. Complete doorstep pickups to deposit godown stock.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                items(godownItems) { item ->
                    GlassCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val catTitle = item.categoryId.replace("_", " ").uppercase()
                                Text(text = catTitle, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                Text(text = "Weight: ${item.verifiedWeightKg} kg · Status: ${item.status.uppercase()}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            }

                            if (item.status == "pending_acceptance") {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            SupabaseClient.acceptToGodownStock(item.id)
                                            popupNotice = "Deposited to Godown!" to "Accepted ${item.verifiedWeightKg} kg into Central Godown!"
                                            refresh()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Accept Stock", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                StatusBadge(text = "IN GODOWN STOCK", color = EcoEmerald500)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(90.dp)) }
    }

    // Spot Scale Weighing & Doorstep Payout Dialog
    if (selectedPickupForWeighing != null) {
        val req = selectedPickupForWeighing!!
        AlertDialog(
            onDismissRequest = { selectedPickupForWeighing = null },
            title = { Text("Doorstep Precision Scale Weighing", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Enter actual weight measured on your certified portable Bluetooth scale:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = actualScaleWeight,
                        onValueChange = { actualScaleWeight = it },
                        label = { Text("Actual Scale Weight (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val weightDouble = actualScaleWeight.toDoubleOrNull() ?: req.totalEstimatedWeight
                    val calculatedAmount = weightDouble * 18.0
                    Text(
                        text = "Calculated Payout: ₹${String.format("%.2f", calculatedAmount)} (Rate: ₹18.00/kg)",
                        color = LeafCoinGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val weight = actualScaleWeight.toDoubleOrNull() ?: req.totalEstimatedWeight
                            val finalAmount = weight * 18.0
                            SupabaseClient.completePickupAndPayCash(req, profile?.id ?: "agent", weight, finalAmount)
                            popupNotice = "Weighing Complete!" to "₹${String.format("%.2f", finalAmount)} credited to household."
                            selectedPickupForWeighing = null
                            refresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600)
                ) {
                    Text("Confirm Scale & Payout", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPickupForWeighing = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (popupNotice != null) {
        EcoPopupDialog(
            title = popupNotice!!.first,
            message = popupNotice!!.second,
            onDismiss = { popupNotice = null }
        )
    }
}

@Composable
private fun ShiftStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp, maxLines = 1)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
