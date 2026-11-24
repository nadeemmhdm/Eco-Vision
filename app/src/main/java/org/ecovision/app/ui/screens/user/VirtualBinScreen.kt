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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.*

@Composable
fun VirtualBinScreen(
    onPickupRequested: () -> Unit,
    onBack: () -> Unit
) {
    val profile by SessionManager.currentProfile.collectAsState()
    val settings by SessionManager.platformSettings.collectAsState()
    val scope = rememberCoroutineScope()

    var binItems by remember { mutableStateOf<List<VirtualBinItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var deliveryAddress by remember { mutableStateOf(profile?.defaultAddress ?: "Flat 4B, Kakkanad, Kochi") }
    var deliveryPincode by remember { mutableStateOf(profile?.pincode ?: "682030") }

    fun refreshBin() {
        if (profile != null) {
            scope.launch {
                isLoading = true
                binItems = SupabaseClient.getVirtualBin(profile!!.id)
                isLoading = false
            }
        }
    }

    LaunchedEffect(profile?.id) {
        refreshBin()
    }

    val totalWeight = binItems.sumOf { it.estimatedWeightKg }
    val totalAmount = binItems.sumOf { it.estimatedPrice }
    val thresholdAmount = settings.minPickupThresholdAmount
    val thresholdWeight = settings.minPickupThresholdWeight

    // Threshold rule: Met if either total value >= threshold OR total weight >= threshold
    val isThresholdMet = totalAmount >= thresholdAmount || totalWeight >= thresholdWeight

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
                    text = "Raw Waste Virtual Bin",
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
            // Bin Accumulator Summary Card
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Accumulated Bin Total",
                            style = MaterialTheme.typography.labelLarge,
                            color = Slate400
                        )
                        Text(
                            text = "₹${String.format("%.2f", totalAmount)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = EcoEmerald400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${String.format("%.1f", totalWeight)} kg",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${binItems.size} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Threshold Check Component
                ThresholdProgressBar(
                    currentValue = totalAmount,
                    thresholdValue = thresholdAmount,
                    unit = "₹",
                    isMet = isThresholdMet
                )

                if (!isThresholdMet) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🔒 Pickup Request Locked: Accumulate at least ₹$thresholdAmount or $thresholdWeight kg of recyclables to request free doorstep collection.",
                        fontSize = 11.sp,
                        color = LeafCoinGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bin Item List
            Text(
                text = "Items in Virtual Bin",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (binItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your virtual bin is currently empty.\nScan waste using AI Waste Scanner to add items.",
                        color = Slate500,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(binItems) { item ->
                        Surface(
                            color = Slate850,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (item.imageUrl != null) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.categoryId.replace("_", " ").uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Weight: ${item.estimatedWeightKg} kg · AI Match: ${item.aiConfidence}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate400
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₹${String.format("%.2f", item.estimatedPrice)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = EcoEmerald400,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                SupabaseClient.deleteVirtualBinItem(item.id)
                                                refreshBin()
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = CoralRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            DisclaimerBanner()
            Spacer(modifier = Modifier.height(12.dp))

            // Request Pickup Action Button (Locked until threshold satisfied)
            EcoButton(
                text = if (isThresholdMet) "Request Doorstep Pickup" else "Threshold Not Met (Need ₹$thresholdAmount)",
                icon = if (isThresholdMet) Icons.Default.LocalShipping else Icons.Default.Lock,
                enabled = isThresholdMet && binItems.isNotEmpty(),
                onClick = { showConfirmDialog = true }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Confirmation Sheet / Dialog
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                containerColor = Slate850,
                titleColor = Color.White,
                textContentColor = Slate300,
                title = { Text("Confirm Pickup Request") },
                text = {
                    Column {
                        Text("Doorstep pickup will be assigned to a verified agent in your zone ($deliveryPincode).")
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it },
                            label = { Text("Pickup Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = deliveryPincode,
                            onValueChange = { deliveryPincode = it },
                            label = { Text("Pincode / Zone") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Estimated Cash Payout: ₹${String.format("%.2f", totalAmount)} (${String.format("%.1f", totalWeight)} kg)",
                            color = EcoEmerald400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                        onClick = {
                            showConfirmDialog = false
                            scope.launch {
                                if (profile != null) {
                                    val req = SupabaseClient.createPickupRequest(
                                        userId = profile!!.id,
                                        pincode = deliveryPincode,
                                        address = deliveryAddress,
                                        totalWeight = totalWeight,
                                        totalAmount = totalAmount,
                                        binItemIds = binItems.map { it.id }
                                    )
                                    if (req != null) {
                                        onPickupRequested()
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Confirm Request", color = Slate950, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancel", color = Slate400)
                    }
                }
            )
        }
    }
}
