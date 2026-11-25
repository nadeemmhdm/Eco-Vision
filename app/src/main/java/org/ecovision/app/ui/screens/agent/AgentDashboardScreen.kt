package org.ecovision.app.ui.screens.agent

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.*

@Composable
fun AgentDashboardScreen() {
    val profile by SessionManager.currentProfile.collectAsState()
    val agentDetails by SessionManager.agentDetails.collectAsState()
    val scope = rememberCoroutineScope()

    var isOnDuty by remember { mutableStateOf(true) }
    var availablePickups by remember { mutableStateOf<List<PickupRequest>>(emptyList()) }
    var activePickups by remember { mutableStateOf<List<PickupRequest>>(emptyList()) }
    var godownItems by remember { mutableStateOf<List<GodownInventoryItem>>(emptyList()) }

    var selectedPickupForWeighing by remember { mutableStateOf<PickupRequest?>(null) }
    var actualScaleWeight by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            val zones = agentDetails?.assignedZones ?: listOf("682001", "682030", "676505")
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
            Spacer(modifier = Modifier.height(6.dp))

            // 1. Shift Duty Hero Banner (Matching Image 3 Theme)
            Surface(
                color = Slate850,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(EcoEmerald600),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalShipping, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = profile?.fullName ?: "Rafiq Khan",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Vehicle: ${agentDetails?.vehicleNumber ?: "KL-07-AB-1234"}",
                                    color = LeafCoinGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Shift On/Off Duty Switch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOnDuty) "ON DUTY" else "OFF DUTY",
                                color = if (isOnDuty) EcoEmerald400 else Slate500,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isOnDuty,
                                onCheckedChange = { isOnDuty = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EcoEmerald400,
                                    checkedTrackColor = EcoEmerald800
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = CardBorderBright)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Shift Summary Stats (Verified KG, Earnings, Pickups)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ShiftStatItem(label = "Verified Today", value = "142 kg", color = EcoEmerald400)
                        ShiftStatItem(label = "Shift Earnings", value = "₹1,850", color = LeafCoinGold)
                        ShiftStatItem(label = "Completed", value = "6 Pickups", color = TechBlue)
                        ShiftStatItem(label = "Rating", value = "★ 4.8", color = Color.White)
                    }
                }
            }
        }

        if (statusMessage != null) {
            item {
                StatusBadge(text = statusMessage!!, color = EcoEmerald400)
            }
        }

        // Section 2: Active Pickup Route (Image 3 Live Tracking Card)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Assigned Pickup (${activePickups.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (activePickups.isNotEmpty()) {
                    StatusBadge(text = "LIVE ON ROAD", color = EcoEmerald400)
                }
            }
        }

        if (activePickups.isEmpty()) {
            item {
                // Demo Live Route preview matching Image 3 when queue is clear
                Image3PickupLiveCard(
                    scheduledSlot = "Today, 4:00 PM - 5:00 PM",
                    isOnTime = true,
                    step = 3,
                    etaMinutes = 12,
                    agentName = profile?.fullName ?: "Rafiq Khan",
                    agentRole = "Pickup Agent (You)",
                    agentRating = "4.8 (124 ratings)",
                    vehiclePlate = agentDetails?.vehicleNumber ?: "KL-07-AB-1234",
                    vehicleType = "Green Van",
                    addressText = "Perinthalmanna Road, Malappuram (Waiting for next household dispatch)",
                    onCallClick = { statusMessage = "Initiating simulated phone call to household..." },
                    onChatClick = { statusMessage = "Opening live customer support chat..." },
                    onVerifyClick = {
                        statusMessage = "Vehicle verified! Proceed to household for scale weighing."
                    },
                    verifyButtonText = "Doorstep Weigh",
                    verifyButtonIcon = Icons.Default.Balance
                )
            }
        } else {
            items(activePickups) { req ->
                val currentStep = when (req.status) {
                    "requested" -> 1
                    "agent_assigned" -> 2
                    "at_doorstep" -> 3
                    "spot_weighed", "completed" -> 4
                    else -> 2
                }

                Image3PickupLiveCard(
                    scheduledSlot = "Scheduled: Today (Immediate Dispatch)",
                    isOnTime = true,
                    step = currentStep,
                    etaMinutes = if (req.status == "at_doorstep") 0 else 8,
                    agentName = profile?.fullName ?: "Rafiq Khan",
                    agentRole = "Pickup Agent (You)",
                    agentRating = "4.8 (124 ratings)",
                    vehiclePlate = agentDetails?.vehicleNumber ?: "KL-07-AB-1234",
                    vehicleType = "Green Van",
                    addressText = req.pickupAddress,
                    onCallClick = { statusMessage = "Connecting to ${req.user?.fullName ?: "Household"} at ${req.user?.mobile ?: "+91 98470 12345"}..." },
                    onChatClick = { statusMessage = "Opening chat with ${req.user?.fullName ?: "Household"}..." },
                    onVerifyClick = {
                        selectedPickupForWeighing = req
                        actualScaleWeight = "${req.totalEstimatedWeight}"
                    },
                    verifyButtonText = if (req.status == "at_doorstep") "Spot Weigh & Pay" else "Arrived Doorstep",
                    verifyButtonIcon = if (req.status == "at_doorstep") Icons.Default.Balance else Icons.Default.LocationOn
                )
            }
        }

        // Section 3: Available Pickups in Pilot District Queue
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
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Malappuram Zone",
                    color = Slate400,
                    fontSize = 11.sp
                )
            }
        }

        if (availablePickups.isEmpty()) {
            item {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "All household pickups in your zone are currently assigned. Stand by for new requests.",
                        color = Slate400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
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
                            Text(
                                text = "Est. ₹${req.totalEstimatedAmount}",
                                style = MaterialTheme.typography.titleMedium,
                                color = EcoEmerald400,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(text = "${req.totalEstimatedWeight} kg", color = TechBlue)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Pickup Location: ${req.pickupAddress}", color = Color.White, fontSize = 12.sp)
                        Text(text = "Pincode: ${req.pincode} · Distance: 1.8 km away", color = Slate400, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        EcoButton(
                            text = "Accept & Start Navigation",
                            icon = Icons.Default.CheckCircle,
                            onClick = {
                                scope.launch {
                                    if (profile != null) {
                                        SupabaseClient.acceptPickup(req.id, profile!!.id)
                                        statusMessage = "Pickup accepted! Route navigation loaded."
                                        refresh()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Section 4: Godown Entry & Stock Acceptance Gate
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Godown Inventory & Acceptance Gate",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Collected waste batches must be formally unloaded and accepted into Godown Stock.",
                fontSize = 11.sp,
                color = Slate400
            )
        }

        if (godownItems.isEmpty()) {
            item {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No pending batches in vehicle trunk. Complete doorstep pickups to deposit godown stock.",
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val catTitle = item.categoryId.replace("_", " ").uppercase()
                            Text(text = catTitle, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(text = "Weight: ${item.verifiedWeightKg} kg · Status: ${item.status.uppercase()}", color = Slate400, fontSize = 11.sp)
                        }

                        if (item.status == "pending_acceptance") {
                            EcoButton(
                                text = "Accept to Godown",
                                icon = Icons.Default.Inventory,
                                onClick = {
                                    scope.launch {
                                        SupabaseClient.acceptToGodownStock(item.id)
                                        statusMessage = "Accepted ${item.verifiedWeightKg} kg into Malappuram Central Godown!"
                                        refresh()
                                    }
                                }
                            )
                        } else {
                            StatusBadge(text = "IN GODOWN STOCK", color = EcoEmerald400)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }
    }

    // Spot Scale Weighing & Doorstep Payout Dialog
    if (selectedPickupForWeighing != null) {
        val req = selectedPickupForWeighing!!
        AlertDialog(
            onDismissRequest = { selectedPickupForWeighing = null },
            title = { Text("Doorstep Precision Scale Weighing", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Enter actual weight measured on your certified portable Bluetooth scale:",
                        color = Slate300,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = actualScaleWeight,
                        onValueChange = { actualScaleWeight = it },
                        label = { Text("Actual Scale Weight (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcoEmerald400,
                            unfocusedBorderColor = CardBorderBright,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
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
                            statusMessage = "Doorstep weighing complete! ₹${String.format("%.2f", finalAmount)} credited to household."
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
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Slate850
        )
    }
}

@Composable
private fun ShiftStatItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, color = Slate400, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
