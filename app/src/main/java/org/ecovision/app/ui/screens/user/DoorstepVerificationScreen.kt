package org.ecovision.app.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import org.ecovision.app.data.PickupRequest
import org.ecovision.app.data.SupabaseClient
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.DisclaimerBanner
import org.ecovision.app.ui.components.EcoButton
import org.ecovision.app.ui.components.GlassCard
import org.ecovision.app.ui.components.StatusBadge

@Composable
fun DoorstepVerificationScreen(
    requestId: String,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var pickup by remember { mutableStateOf<PickupRequest?>(null) }
    var vehicleMatched by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(requestId) {
        val userPickups = SupabaseClient.getUserPickups("")
        pickup = userPickups.firstOrNull { it.id == requestId } ?: PickupRequest(
            id = requestId,
            userId = "",
            status = "at_doorstep",
            totalEstimatedWeight = 5.2,
            totalEstimatedAmount = 78.0,
            actualVerifiedWeight = 5.8,
            finalCashAmount = 87.0,
            pincode = "682030",
            pickupAddress = "Flat 4B, Kakkanad, Kochi"
        )
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
                    text = "Doorstep Verification & Payout",
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Doorstep Security Vehicle Check Card
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Doorstep Identity Check",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    StatusBadge(text = "AGENT AT DOORSTEP", color = EcoEmerald400)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Vehicle Photo & License Plate Matching
                Row(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=600",
                        contentDescription = "Verified Vehicle Photo",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorderBright, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Registered Vehicle No:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                        Text(
                            text = "KL-07-CD-5678",
                            style = MaterialTheme.typography.titleLarge,
                            color = LeafCoinGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Agent: Rahul Kumar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Phone: +91 98765 43210 · ★ 4.95",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = vehicleMatched,
                            onCheckedChange = { vehicleMatched = it },
                            colors = CheckboxDefaults.colors(checkedColor = EcoEmerald500)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I have physically verified the Agent's face, vehicle photo, and license plate number.",
                            fontSize = 11.sp,
                            color = Slate300
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spot Weighing & Live Price Revision Card
            GlassCard {
                Text(
                    text = "Spot Scale Weighing & Price Revision",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Initial Estimate", style = MaterialTheme.typography.labelSmall, color = Slate400)
                        Text(
                            text = "${pickup?.totalEstimatedWeight ?: 5.2} kg",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate300
                        )
                        Text(
                            text = "₹${pickup?.totalEstimatedAmount ?: 78.0}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                    Icon(Icons.Default.ArrowForward, null, tint = EcoEmerald400, modifier = Modifier.align(Alignment.CenterVertically))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Verified Scale Weight", style = MaterialTheme.typography.labelSmall, color = EcoEmerald400)
                        Text(
                            text = "${pickup?.actualVerifiedWeight ?: 5.8} kg",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Final Cash: ₹${pickup?.finalCashAmount ?: 87.0}",
                            style = MaterialTheme.typography.titleMedium,
                            color = EcoEmerald400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                DisclaimerBanner(
                    text = "Weight Verified: The agent has measured 5.8 kg on a physical certified scale. Final slab rate applied: ₹15.00/kg."
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = LeafCoinGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LeafCoinGold.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Spa, null, tint = LeafCoinGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "+58 Coins Earned! 🪙",
                                style = MaterialTheme.typography.titleMedium,
                                color = LeafCoinGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Proportional reward for 5.8 kg verified recycled waste.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate300
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            EcoButton(
                text = "Confirm Cash Receipt & Complete",
                icon = Icons.Default.CheckCircle,
                enabled = vehicleMatched,
                onClick = onComplete
            )
        }
    }
}
