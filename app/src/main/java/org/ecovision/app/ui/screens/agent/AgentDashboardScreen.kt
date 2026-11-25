package org.ecovision.app.ui.screens.agent

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
fun AgentDashboardScreen() {
    val profile by SessionManager.currentProfile.collectAsState()
    val agentDetails by SessionManager.agentDetails.collectAsState()
    val scope = rememberCoroutineScope()

    var availablePickups by remember { mutableStateOf<List<PickupRequest>>(emptyList()) }
    var activePickups by remember { mutableStateOf<List<PickupRequest>>(emptyList()) }
    var godownItems by remember { mutableStateOf<List<GodownInventoryItem>>(emptyList()) }

    var selectedPickupForWeighing by remember { mutableStateOf<PickupRequest?>(null) }
    var actualScaleWeight by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            val zones = agentDetails?.assignedZones ?: listOf("682001", "682030", "682031")
            availablePickups = SupabaseClient.getAgentAvailablePickups(zones)
            if (profile != null) {
                activePickups = SupabaseClient.getAgentActivePickups(profile!!.id)
                godownItems = SupabaseClient.getGodownStockForAgent(profile!!.id)
            }
        }
    }

    LaunchedEffect(profile?.id) {
        refresh()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Agent Profile & Vehicle Hero Card
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = profile?.fullName ?: "Rahul Kumar (Agent)",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vehicle: ${agentDetails?.vehicleNumber ?: "KL-07-CD-5678"} · Verified ID",
                            style = MaterialTheme.typography.bodySmall,
                            color = LeafCoinGold
                        )
                    }
                    StatusBadge(text = "★ 4.95 RATING", color = LeafCoinGold)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Assigned Zones: ${agentDetails?.assignedZones?.joinToString(", ") ?: "682001, 682030"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                    Text(
                        text = "Allowed: Mon-Sat",
                        style = MaterialTheme.typography.bodySmall,
                        color = EcoEmerald400
                    )
                }
            }
        }

        if (statusMessage != null) {
            item {
                StatusBadge(text = statusMessage!!, color = EcoEmerald400)
            }
        }

        // Section 1: Active Pickups & Spot Weighing
        item {
            Text(
                text = "Active Pickups (${activePickups.size})",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        if (activePickups.isEmpty()) {
            item {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No active in-progress pickups. Accept from queue below.",
                        color = Slate400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(activePickups) { req ->
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusBadge(text = req.status.uppercase(), color = TechBlue)
                            Text(
                                text = "Est. ₹${req.totalEstimatedAmount}",
                                style = MaterialTheme.typography.titleMedium,
                                color = EcoEmerald400,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Address: ${req.pickupAddress}", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Est. Weight: ${req.totalEstimatedWeight} kg", color = Slate400, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (req.status == "agent_assigned") {
                                EcoButton(
                                    text = "Mark at Doorstep",
                                    icon = Icons.Default.LocationOn,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        scope.launch {
                                            SupabaseClient.markAtDoorstep(req.id)
                                            statusMessage = "Marked at doorstep! Household notified."
                                            refresh()
                                        }
                                    }
                                )
                            } else {
                                EcoButton(
                                    text = "Spot Scale Weigh & Pay",
                                    icon = Icons.Default.Balance,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        selectedPickupForWeighing = req
                                        actualScaleWeight = "${req.totalEstimatedWeight}"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Godown Entry & Stock Acceptance (Critical Gate)
        item {
            Text(
                text = "Godown Inventory Management (Stock Acceptance Gate)",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Collected waste is invisible to Buyer Companies until formally accepted into Godown Stock.",
                fontSize = 11.sp,
                color = Slate400
            )
        }

        if (godownItems.isEmpty()) {
            item {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No pending godown batches. Complete doorstep pickups to receive stock.",
                        color = Slate400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(godownItems) { item ->
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.verifiedWeightKg} kg · ${item.categoryId.replace("_", " ").uppercase()}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(
                                text = if (item.status == "godown_accepted") "GODOWN ACCEPTED (MARKETPLACE LIVE)" else "PENDING ACCEPTANCE",
                                color = if (item.status == "godown_accepted") EcoEmerald400 else LeafCoinGold
                            )
                        }

                        if (item.status == "pending_acceptance") {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                                onClick = {
                                    scope.launch {
                                        SupabaseClient.acceptToGodownStock(item.id)
                                        statusMessage = "Batch verified and accepted into Godown Stock! Pushed to Recycler Feed."
                                        refresh()
                                    }
                                }
                            ) {
                                Text("Accept to Godown", fontSize = 11.sp, color = Slate950, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Available Pickups in Assigned Zone
        item {
            Text(
                text = "Available Pickup Queue (Matching Zone & Schedule)",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        items(availablePickups) { req ->
            Surface(
                color = Slate850,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusBadge(text = "AVAILABLE", color = LeafCoinGold)
                        Text(
                            text = "Est. ₹${req.totalEstimatedAmount}",
                            style = MaterialTheme.typography.titleMedium,
                            color = EcoEmerald400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Address: ${req.pickupAddress}", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Zone: ${req.pincode} · Est. Weight: ${req.totalEstimatedWeight} kg", color = Slate400, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    EcoButton(
                        text = "Accept Pickup (Atomic Lock)",
                        icon = Icons.Default.Check,
                        onClick = {
                            scope.launch {
                                if (profile != null) {
                                    val ok = SupabaseClient.acceptPickup(req.id, profile!!.id)
                                    if (ok) {
                                        statusMessage = "Pickup accepted! Added to your active route."
                                        refresh()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }

    // Spot Weighing & Cash Payout Modal
    if (selectedPickupForWeighing != null) {
        val req = selectedPickupForWeighing!!
        AlertDialog(
            onDismissRequest = { selectedPickupForWeighing = null },
            containerColor = Slate850,
            titleContentColor = Color.White,
            textContentColor = Slate300,
            title = { Text("Scale Weighing & Cash Settlement") },
            text = {
                Column {
                    Text("User Address: ${req.pickupAddress}")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = actualScaleWeight,
                        onValueChange = { actualScaleWeight = it },
                        label = { Text("Actual Physical Scale Weight (kg)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val weightDouble = actualScaleWeight.toDoubleOrNull() ?: req.totalEstimatedWeight
                    val finalAmount = weightDouble * 15.0 // Slab tier rate
                    Text(
                        text = "Recalculated Final Payout: ₹${String.format("%.2f", finalAmount)}",
                        color = EcoEmerald400,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Coins to be awarded to user: ${(weightDouble * 10).toInt()} 🪙",
                        color = LeafCoinGold,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                    onClick = {
                        val weightDouble = actualScaleWeight.toDoubleOrNull() ?: req.totalEstimatedWeight
                        val finalAmount = weightDouble * 15.0
                        selectedPickupForWeighing = null
                        scope.launch {
                            if (profile != null) {
                                SupabaseClient.completePickupAndPayCash(req, profile!!.id, weightDouble, finalAmount)
                                statusMessage = "Pickup completed! Cash paid, batch moved to Godown."
                                refresh()
                            }
                        }
                    }
                ) {
                    Text("Pay Cash & Complete", color = Slate950, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPickupForWeighing = null }) {
                    Text("Cancel", color = Slate400)
                }
            }
        )
    }
}
