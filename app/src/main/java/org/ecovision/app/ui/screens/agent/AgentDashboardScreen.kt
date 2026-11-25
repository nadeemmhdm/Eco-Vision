package org.ecovision.app.ui.screens.agent

import android.content.Intent
import android.net.Uri
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

    LaunchedEffect(profile?.id) {
        refresh()
    }

    val tabs = listOf(
        "Home" to Icons.Default.Home,
        "Pickups (${availablePickups.size + activePickups.size})" to Icons.Default.LocalShipping,
        "History & Godown" to Icons.Default.History
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // TOP AGENT HEADER & HORIZONTAL TAB BAR
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(6.dp))
            AnimatedCharacterHero(
                title = profile?.fullName ?: "Agent Collection Desk",
                subtitle = "Vehicle ${agentDetails?.vehicleNumber ?: "KL-07-AB-1234"} • Doorstep Weighing & Dispatch"
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tabs.size) { idx ->
                    val (label, icon) = tabs[idx]
                    val isSelected = agentTab == idx
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { SessionManager.setAgentTab(idx) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        if (statusMessage != null) {
            item {
                StatusBadge(text = statusMessage!!, color = EcoEmerald500)
            }
        }

        // ==========================================
        // TAB 0: DESK (HOME OVERVIEW & LIVE ROUTE)
        // ==========================================
        if (agentTab == 0) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Vehicle: ${agentDetails?.vehicleNumber ?: "KL-07-AB-1234"}",
                                        color = LeafCoinGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isOnDuty) "ON DUTY" else "OFF DUTY",
                                    color = if (isOnDuty) EcoEmerald500 else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = isOnDuty,
                                    onCheckedChange = { newDuty ->
                                        isOnDuty = newDuty
                                        scope.launch {
                                            SupabaseClient.setAgentDutyStatus(profile?.id ?: "agent-rahul", newDuty)
                                            statusMessage = if (newDuty) "You are now ON DUTY and visible to households." else "You are now OFF DUTY."
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = EcoEmerald600
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ShiftStatItem(label = "Verified Today", value = "142 kg", color = EcoEmerald500)
                            ShiftStatItem(label = "Shift Earnings", value = "₹1,850", color = LeafCoinGold)
                            ShiftStatItem(label = "Completed", value = "${godownItems.size + 6}", color = TechBlue)
                            ShiftStatItem(label = "Rating", value = "★ 4.8", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
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
                        Icon(Icons.Default.LocalShipping, null, tint = EcoEmerald500, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${availablePickups.size + activePickups.size} Pickups",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View nearby & active household requests",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { SessionManager.setAgentTab(2) }
                    ) {
                        Icon(Icons.Default.Inventory, null, tint = LeafCoinGold, modifier = Modifier.size(26.dp))
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
        // TAB 1: PICKUPS (ACTIVE & NEARBY QUEUE)
        // ==========================================
        if (agentTab == 1) {
            item {
                Text(
                    text = "Active Doorstep Pickups (${activePickups.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            if (activePickups.isEmpty()) {
                item {
                    GlassCard {
                        Text(
                            text = "No active pickups assigned yet. Accept a nearby request below to start navigation and doorstep weighing.",
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
                            text = "All household pickups in your zone are currently assigned. Stand by for new requests.",
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

        // ==========================================
        // TAB 2: HISTORY & GODOWN ENTRY GATE
        // ==========================================
        if (agentTab == 2) {
            item {
                Text(
                    text = "Godown Inventory & Acceptance Gate",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Collected waste batches must be formally unloaded and accepted into Godown Stock.",
                    fontSize = 11.sp,
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
                                            statusMessage = "Accepted ${item.verifiedWeightKg} kg into Central Godown!"
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
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun ShiftStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, maxLines = 1)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
