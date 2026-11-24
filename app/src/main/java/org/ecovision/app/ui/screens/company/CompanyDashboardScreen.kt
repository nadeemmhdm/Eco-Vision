package org.ecovision.app.ui.screens.company

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
fun CompanyDashboardScreen() {
    val profile by SessionManager.currentProfile.collectAsState()
    val companyDetails by SessionManager.companyDetails.collectAsState()
    val scope = rememberCoroutineScope()

    var feedItems by remember { mutableStateOf<List<GodownInventoryItem>>(emptyList()) }
    var pastOrders by remember { mutableStateOf<List<BulkOrder>>(emptyList()) }
    var selectedCategoryForBulk by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            feedItems = SupabaseClient.getVerifiedGodownFeed()
            if (profile != null) {
                pastOrders = SupabaseClient.getCompanyBulkOrders(profile!!.id)
            }
        }
    }

    LaunchedEffect(profile?.id) {
        refresh()
    }

    // Aggregate godown stock by category
    val pooledStock = feedItems.groupBy { it.categoryId }.mapValues { entry ->
        entry.value.sumOf { it.verifiedWeightKg }
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

            // Company Header
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = companyDetails?.companyName ?: "GreenLoop Recyclers",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "GST: ${companyDetails?.gstNumber ?: "32AABCG1234F1Z5"} · Verified B2B Partner",
                            style = MaterialTheme.typography.bodySmall,
                            color = TechBlue
                        )
                    }
                    StatusBadge(text = "KYC VERIFIED", color = EcoEmerald400)
                }
            }
        }

        if (statusMessage != null) {
            item {
                StatusBadge(text = statusMessage!!, color = EcoEmerald400)
            }
        }

        // Section 1: Verified Godown Stock Feed (Aggregated Pooling)
        item {
            Text(
                text = "Verified Godown Stock Feed (Pooled B2B Lots)",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Aggregated from verified household collections. 100% verified scale weight.",
                fontSize = 11.sp,
                color = Slate400
            )
        }

        if (pooledStock.isEmpty()) {
            item {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No godown-accepted lots available right now.",
                        color = Slate400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(pooledStock.entries.toList()) { entry ->
                val categoryName = entry.key.replace("_", " ").uppercase()
                val totalKg = entry.value
                val ratePerKg = when (entry.key) {
                    "pet_plastic" -> 16.5
                    "cardboard" -> 12.0
                    "aluminum" -> 110.0
                    "e_waste" -> 75.0
                    else -> 10.0
                }
                val lotTotal = totalKg * ratePerKg

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
                                text = categoryName,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(text = "LOT READY", color = EcoEmerald400)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Available Volume", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                Text(
                                    text = "${String.format("%.1f", totalKg)} kg",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Procurement Value", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                Text(
                                    text = "₹${String.format("%.2f", lotTotal)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EcoEmerald400,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        EcoButton(
                            text = "🛒 Single Bulk Purchase Order",
                            onClick = {
                                scope.launch {
                                    if (profile != null) {
                                        val order = SupabaseClient.createBulkPurchaseOrder(
                                            companyId = profile!!.id,
                                            categoryId = entry.key,
                                            weightKg = totalKg,
                                            totalAmount = lotTotal
                                        )
                                        if (order != null) {
                                            statusMessage = "Order confirmed! Manifest ${order.manifestNumber} generated with ESG impact."
                                            refresh()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Section 2: Procurement History & ESG Reports
        item {
            Text(
                text = "Procurement Orders & ESG Impact Reports",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        if (pastOrders.isEmpty()) {
            item {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No past bulk purchase orders.",
                        color = Slate400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(pastOrders) { order ->
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
                                text = order.manifestNumber,
                                style = MaterialTheme.typography.titleMedium,
                                color = LeafCoinGold,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(text = order.status.uppercase(), color = EcoEmerald400)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Lot: ${order.totalWeightKg} kg of ${order.categoryId.replace("_", " ").uppercase()} · ₹${order.totalAmount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🌱 ESG Carbon Offset: ${order.esgCarbonOffsetKg} kg CO2 equivalent saved",
                            fontSize = 12.sp,
                            color = EcoEmerald400,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}
