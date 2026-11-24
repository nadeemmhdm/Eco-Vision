package org.ecovision.app.ui.screens.admin

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
fun AdminDashboardScreen() {
    val settings by SessionManager.platformSettings.collectAsState()
    val scope = rememberCoroutineScope()

    var profiles by remember { mutableStateOf<List<Profile>>(emptyList()) }
    var slabs by remember { mutableStateOf<List<WeightPricingSlab>>(emptyList()) }
    var isMaintenanceMode by remember { mutableStateOf(settings.maintenanceMode) }
    var downtimeNotice by remember { mutableStateOf(settings.maintenanceNotice) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            profiles = SupabaseClient.getAllProfiles()
            slabs = SupabaseClient.getPricingSlabs()
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    val pendingKycProfiles = profiles.filter { it.kycStatus == "pending" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(horizontal = 20.dp),
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
                            text = "Admin Governance Desk",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Master Control, KYC Verification & Dynamic Rates",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                    StatusBadge(text = "SUPER ADMIN", color = CoralRed)
                }
            }
        }

        if (statusMessage != null) {
            item {
                StatusBadge(text = statusMessage!!, color = EcoEmerald400)
            }
        }

        // Module 1: Maintenance Mode Control
        item {
            GlassCard(
                borderColor = if (isMaintenanceMode) CoralRed else CardBorder
            ) {
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

        // Module 2: KYC & Vehicle Photo Verification Desk
        item {
            Text(
                text = "KYC & Vehicle Verification Desk (${pendingKycProfiles.size} Pending)",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Verify Aadhaar / GST & match Agent vehicle number against uploaded vehicle photo.",
                fontSize = 11.sp,
                color = Slate400
            )
        }

        if (pendingKycProfiles.isEmpty()) {
            item {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "All user, agent, and company documents are verified! No pending reviews.",
                        color = EcoEmerald400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(pendingKycProfiles) { p ->
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = p.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(text = p.role.uppercase(), color = LeafCoinGold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Mobile: ${p.mobile} · Address: ${p.defaultAddress ?: "N/A"}", color = Slate300, fontSize = 12.sp)
                        Text(text = "Document: ${p.kycDocumentType?.uppercase()} - ${p.kycDocumentNumber ?: "Submitted"}", color = TechBlue, fontSize = 12.sp)

                        if (p.role == "agent") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=300",
                                    contentDescription = "Vehicle",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = "Vehicle Photo Matching", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                    Text(text = "Plate: KL-07-CD-5678", style = MaterialTheme.typography.titleMedium, color = LeafCoinGold, fontWeight = FontWeight.Bold)
                                    Text(text = "Type: Commercial Pickup Van", fontSize = 11.sp, color = Slate300)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    scope.launch {
                                        SupabaseClient.updateKycStatus(p.id, "verified")
                                        statusMessage = "Verified ${p.fullName} (${p.role})!"
                                        refresh()
                                    }
                                }
                            ) {
                                Text("Approve KYC", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    scope.launch {
                                        SupabaseClient.updateKycStatus(p.id, "rejected")
                                        statusMessage = "Rejected ${p.fullName}."
                                        refresh()
                                    }
                                }
                            ) {
                                Text("Reject", color = CoralRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Module 3: Weight-Based Pricing Engine
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

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}
