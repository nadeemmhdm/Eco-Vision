package org.ecovision.app.ui.screens.company

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.*

@Composable
fun CompanyDashboardScreen() {
    val profile by SessionManager.currentProfile.collectAsState()
    val companyDetails by SessionManager.companyDetails.collectAsState()
    val scope = rememberCoroutineScope()

    var feedItems by remember { mutableStateOf<List<GodownInventoryItem>>(emptyList()) }
    var pastOrders by remember { mutableStateOf<List<BulkOrder>>(emptyList()) }
    var selectedCategoryFilter by remember { mutableStateOf("all") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Multi-page Tab Navigation State
    var companyTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Godown Marketplace" to Icons.Default.Storefront,
        "Orders & Manifests" to Icons.Default.LocalShipping,
        "ESG & Credentials" to Icons.Default.VerifiedUser
    )

    // Purchase Dialog State
    var lotToPurchase by remember { mutableStateOf<Pair<String, Double>?>(null) } // categoryId to totalKg
    var isPurchasing by remember { mutableStateOf(false) }
    var latestConfirmedOrder by remember { mutableStateOf<BulkOrder?>(null) }

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

    val filteredLots = pooledStock.entries.filter {
        selectedCategoryFilter == "all" || it.key == selectedCategoryFilter
    }.toList()

    val totalProcuredKg = pastOrders.sumOf { it.totalWeightKg }
    val totalCo2Saved = pastOrders.sumOf { it.esgCarbonOffsetKg }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. TOP 3D MASCOT HERO & LIQUID GLASS HEADER
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedCharacterHero(
                title = companyDetails?.companyName ?: "B2B Procurement Desk",
                subtitle = "Verified Godown Stock • Digital Dispatch Manifests • ESG Offsets"
            )
        }

        // ==========================================
        // 2. HORIZONTAL TAB NAVIGATION (3 PAGES)
        // ==========================================
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tabs.size) { idx ->
                    val (label, icon) = tabs[idx]
                    val isSelected = companyTab == idx

                    Surface(
                        color = if (isSelected) TechBlue else Slate850,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color.White else CardBorder
                        ),
                        modifier = Modifier.clickable { companyTab = idx }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) Color.White else Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else Slate300,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )

                            if (idx == 0 && filteredLots.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(EcoEmerald400)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (statusMessage != null) {
            item {
                StatusBadge(text = statusMessage!!, color = EcoEmerald400)
            }
        }

        // =========================================================================
        // TAB 0: GODOWN STOCK MARKETPLACE
        // =========================================================================
        if (companyTab == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Verified Godown Stock Feed",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Certified household collection lots ready for dispatch",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                    BoxIcon(
                        icon = Icons.Default.Warehouse,
                        boxColor = EcoEmerald600.copy(alpha = 0.2f),
                        iconColor = EcoEmerald400,
                        size = 36.dp,
                        iconSize = 18.dp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Material Filter Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryFilterPill(label = "All Lots", isSelected = selectedCategoryFilter == "all", onClick = { selectedCategoryFilter = "all" })
                    CategoryFilterPill(label = "Plastics", isSelected = selectedCategoryFilter == "pet_plastic", onClick = { selectedCategoryFilter = "pet_plastic" })
                    CategoryFilterPill(label = "Glass", isSelected = selectedCategoryFilter == "glass", onClick = { selectedCategoryFilter = "glass" })
                    CategoryFilterPill(label = "Cardboard", isSelected = selectedCategoryFilter == "cardboard", onClick = { selectedCategoryFilter = "cardboard" })
                    CategoryFilterPill(label = "Aluminum", isSelected = selectedCategoryFilter == "aluminum", onClick = { selectedCategoryFilter = "aluminum" })
                    CategoryFilterPill(label = "E-Waste", isSelected = selectedCategoryFilter == "e_waste", onClick = { selectedCategoryFilter = "e_waste" })
                }
            }

            if (filteredLots.isEmpty()) {
                item {
                    LiquidGlassCard {
                        Text(
                            text = "No godown-accepted lots matching '$selectedCategoryFilter' available right now.",
                            color = Slate400,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(filteredLots) { entry ->
                    val categoryName = entry.key.replace("_", " ").uppercase()
                    val totalKg = entry.value
                    val ratePerKg = when (entry.key) {
                        "pet_plastic" -> 16.5
                        "glass" -> 14.0
                        "cardboard" -> 12.0
                        "aluminum" -> 110.0
                        "e_waste" -> 75.0
                        else -> 10.0
                    }
                    val lotTotal = totalKg * ratePerKg
                    val estCarbonOffset = Math.round(totalKg * 1.85 * 10.0) / 10.0

                    LiquidGlassCard(highlightColor = EcoEmerald400) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BoxIcon(
                                    icon = when (entry.key) {
                                        "glass" -> Icons.Default.WineBar
                                        "pet_plastic" -> Icons.Default.LocalDrink
                                        "aluminum" -> Icons.Default.LocalBar
                                        "e_waste" -> Icons.Default.Memory
                                        else -> Icons.Default.Inventory
                                    },
                                    boxColor = Slate800,
                                    iconColor = EcoEmerald400,
                                    size = 40.dp,
                                    iconSize = 20.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = categoryName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Hub: Central Godown, Malappuram",
                                        color = Slate400,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            StatusBadge(text = "VERIFIED LOT", color = EcoEmerald400)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Available Weight", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                Text(
                                    text = "${String.format("%.1f", totalKg)} kg",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(text = "Wholesale Price", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                Text(
                                    text = "₹$ratePerKg/kg",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LeafCoinGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Lot Procurement Total", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                Text(
                                    text = "₹${String.format("%.2f", lotTotal)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EcoEmerald400,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = Color(0xFF064E3B).copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Eco, null, tint = EcoEmerald400, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Estimated ESG Impact: Avoids ~$estCarbonOffset kg CO2 emissions",
                                    color = EcoEmerald400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { lotToPurchase = Pair(entry.key, totalKg) },
                            colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Procure Lot (Generate Manifest & Waybill)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB 1: ORDERS & MANIFEST TRACKING
        // =========================================================================
        if (companyTab == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Purchase Orders & Digital Manifests",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${pastOrders.size} bulk shipments confirmed with waybills",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                    BoxIcon(
                        icon = Icons.Default.Receipt,
                        boxColor = Slate800,
                        iconColor = LeafCoinGold,
                        size = 36.dp,
                        iconSize = 18.dp
                    )
                }
            }

            if (pastOrders.isEmpty()) {
                item {
                    LiquidGlassCard {
                        Text(
                            text = "No past bulk purchase orders. Tap 'Godown Marketplace' to procure lots.",
                            color = Slate400,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(pastOrders) { order ->
                    LiquidGlassCard(highlightColor = TechBlue) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Manifest: ${order.manifestNumber}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Material: ${order.categoryId.replace("_", " ").uppercase()} • ${String.format("%.1f", order.totalWeightKg)} kg",
                                    color = TechBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            StatusBadge(text = order.status.uppercase(), color = EcoEmerald400)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Invoice: ₹${String.format("%.2f", order.totalAmount)}", color = LeafCoinGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("CO2 Saved: 🌱 ${String.format("%.1f", order.esgCarbonOffsetKg)} kg", color = EcoEmerald400, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { latestConfirmedOrder = order },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Digital Dispatch Waybill", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB 2: ESG & CREDENTIALS
        // =========================================================================
        if (companyTab == 2) {
            item {
                LiquidGlassCard(highlightColor = EcoEmerald400) {
                    Text(
                        text = "Green Business Accreditation",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Company: ${companyDetails?.companyName ?: "GreenLoop Recyclers"}",
                        color = Slate200,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "GSTIN: ${companyDetails?.gstNumber ?: "32AABCG1234F1Z5"} • Verified Industrial Recycler",
                        color = Slate400,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = CardBorderBright)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Procured Volume", color = Slate400, fontSize = 11.sp)
                            Text("${String.format("%.1f", totalProcuredKg)} kg", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Cumulative ESG Offset", color = Slate400, fontSize = 11.sp)
                            Text("🌱 ${String.format("%.1f", totalCo2Saved)} kg CO2", color = EcoEmerald400, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = Color(0xFF064E3B).copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, null, tint = EcoEmerald400, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ESG Carbon Credit Compliant", color = EcoEmerald400, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Every kilogram of post-consumer material procured through EcoVision generates auditable ESG carbon offsets adhering to national circular economy guidelines.",
                                color = Slate300,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }
    }

    // Purchase Order Confirmation Modal Dialog
    if (lotToPurchase != null) {
        val (catId, kg) = lotToPurchase!!
        val catName = catId.replace("_", " ").uppercase()
        val rate = when (catId) {
            "pet_plastic" -> 16.5
            "glass" -> 14.0
            "cardboard" -> 12.0
            "aluminum" -> 110.0
            "e_waste" -> 75.0
            else -> 10.0
        }
        val total = kg * rate
        val carbonOffset = Math.round(kg * 1.85 * 10.0) / 10.0

        AlertDialog(
            onDismissRequest = { if (!isPurchasing) lotToPurchase = null },
            containerColor = Slate900,
            titleContentColor = Color.White,
            textContentColor = Slate300,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BoxIcon(icon = Icons.Default.LocalShipping, boxColor = EcoEmerald600.copy(alpha = 0.2f), iconColor = EcoEmerald400)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Confirm Bulk Purchase Order", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column {
                    Text(text = "Material: $catName", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Volume: ${String.format("%.1f", kg)} kg @ ₹$rate/kg", color = TechBlue, fontSize = 12.sp)
                    Text(text = "Procurement Amount: ₹${String.format("%.2f", total)}", color = LeafCoinGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "ESG Carbon Offset: 🌱 $carbonOffset kg CO2", color = EcoEmerald400, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Upon confirmation, a digital dispatch manifest will be generated for godown collection.",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isPurchasing = true
                            val order = SupabaseClient.createBulkPurchaseOrder(
                                companyId = profile?.id ?: "company_guest",
                                categoryId = catId,
                                weightKg = kg,
                                totalAmount = total
                            )
                            isPurchasing = false
                            lotToPurchase = null
                            if (order != null) {
                                latestConfirmedOrder = order
                                statusMessage = "🎉 Order confirmed! Manifest #${order.manifestNumber} generated."
                                refresh()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                    enabled = !isPurchasing
                ) {
                    Text(if (isPurchasing) "Generating Manifest..." else "Confirm Purchase", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { lotToPurchase = null }, enabled = !isPurchasing) {
                    Text("Cancel", color = Slate400)
                }
            }
        )
    }

    // Waybill Preview Dialog
    if (latestConfirmedOrder != null) {
        val ord = latestConfirmedOrder!!
        AlertDialog(
            onDismissRequest = { latestConfirmedOrder = null },
            containerColor = Slate900,
            titleContentColor = Color.White,
            textContentColor = Slate300,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BoxIcon(icon = Icons.Default.Receipt, boxColor = TechBlue.copy(alpha = 0.2f), iconColor = TechBlue)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Digital Waybill & Manifest", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Manifest Number: ${ord.manifestNumber}", color = EcoEmerald400, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Material: ${ord.categoryId.replace("_", " ").uppercase()}", color = Color.White, fontSize = 12.sp)
                            Text(text = "Net Weight: ${ord.totalWeightKg} kg", color = Slate300, fontSize = 12.sp)
                            Text(text = "Total Value: ₹${ord.totalAmount}", color = LeafCoinGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = "ESG Credit: ${ord.esgCarbonOffsetKg} kg CO2", color = EcoEmerald400, fontSize = 12.sp)
                            Text(text = "Dispatch Hub: Godown #4, Malappuram Hub", color = Slate400, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { latestConfirmedOrder = null },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600)
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun CategoryFilterPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) EcoEmerald600 else Slate850,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) EcoEmerald400 else CardBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Slate400,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}
