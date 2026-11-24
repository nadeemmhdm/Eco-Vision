package org.ecovision.app.ui.screens.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
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
import java.util.UUID

@Composable
fun AiScanScreen(
    onAddedToBin: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    onBack: () -> Unit
) {
    val profile by SessionManager.currentProfile.collectAsState()
    val scope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(false) }
    var detectionResult by remember { mutableStateOf<AiDetectionResult?>(null) }
    var estimatedWeight by remember { mutableDoubleStateOf(2.5) }
    var categories by remember { mutableStateOf<List<WasteCategory>>(emptyList()) }
    var pricingSlabs by remember { mutableStateOf<List<WeightPricingSlab>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf("pet_plastic") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        categories = SupabaseClient.getCategories()
        pricingSlabs = SupabaseClient.getPricingSlabs()
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
                    text = "AI Waste Scanner",
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camera / Viewfinder Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Slate950)
                    .border(2.dp, if (detectionResult != null) EcoEmerald500 else CardBorderBright, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (detectionResult?.imageUrl != null) {
                    AsyncImage(
                        model = detectionResult?.imageUrl,
                        contentDescription = "Scanned Waste",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Slate800),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, null, tint = EcoEmerald400, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Point camera at waste item",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400
                        )
                        Text(
                            text = "YOLOv8 & Google Vision AI Ready",
                            style = MaterialTheme.typography.labelSmall,
                            color = EcoEmerald500
                        )
                    }
                }

                // AI Bounding Box HUD Overlay
                if (detectionResult != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .border(2.dp, EcoEmerald400, RoundedCornerShape(12.dp))
                    ) {
                        Surface(
                            color = EcoEmerald600,
                            shape = RoundedCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "${detectionResult?.categoryName} · ${detectionResult?.confidence}% Match",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action: Run Scan Simulation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EcoButton(
                    text = if (isScanning) "AI Analyzing..." else "Capture & Scan Waste",
                    icon = Icons.Default.CameraAlt,
                    enabled = !isScanning,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        scope.launch {
                            isScanning = true
                            val result = AiVisionEngine.analyzeWasteImage(null)
                            detectionResult = result
                            selectedCategoryId = result.categoryId
                            isScanning = false
                        }
                    }
                )
            }

            // Results Section
            if (detectionResult != null) {
                val result = detectionResult!!
                val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
                val baseRate = selectedCategory?.baseRatePerKg ?: result.estimatedBasePrice
                val currentSlabRate = SupabaseClient.calculateSlabRate(pricingSlabs, selectedCategoryId, estimatedWeight, baseRate)
                val estimatedTotal = estimatedWeight * currentSlabRate

                Spacer(modifier = Modifier.height(20.dp))

                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = result.categoryName,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "AI Confidence Score: ${result.confidence}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }
                        StatusBadge(
                            text = if (result.isRecyclable) "RECYCLABLE" else "NON-RECYCLABLE",
                            color = if (result.isRecyclable) EcoEmerald400 else CoralRed
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // AI Upcycle Recommendations
                    Text(
                        text = "💡 AI DIY / Upcycle Recommendations:",
                        style = MaterialTheme.typography.labelLarge,
                        color = LeafCoinGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    for (idea in result.diyRecommendations) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = EcoEmerald400, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = idea,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate300
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = CardBorderBright)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Weight Quantity Input
                    Text(
                        text = "Estimated Waste Quantity: ${String.format("%.1f", estimatedWeight)} kg",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = estimatedWeight.toFloat(),
                        onValueChange = { estimatedWeight = it.toDouble() },
                        valueRange = 0.5f..25f,
                        steps = 48,
                        colors = SliderDefaults.colors(
                            thumbColor = EcoEmerald500,
                            activeTrackColor = EcoEmerald500,
                            inactiveTrackColor = Slate800
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Weight-Tier Rate: ₹$currentSlabRate/kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                        Text(
                            text = "Estimated Value: ₹${String.format("%.2f", estimatedTotal)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = EcoEmerald400,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    DisclaimerBanner()

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dual Selling Options
                    Text(
                        text = "Choose Selling Destination:",
                        style = MaterialTheme.typography.labelLarge,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Option A: Upcycled P2P Store
                        EcoButton(
                            text = "List in Upcycle Store",
                            icon = Icons.Default.Storefront,
                            isSecondary = true,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToMarketplace
                        )

                        // Option B: Virtual Bin
                        EcoButton(
                            text = "Add to Virtual Bin",
                            icon = Icons.Default.AddShoppingCart,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                scope.launch {
                                    if (profile != null) {
                                        val item = VirtualBinItem(
                                            id = UUID.randomUUID().toString(),
                                            userId = profile!!.id,
                                            categoryId = selectedCategoryId,
                                            aiConfidence = result.confidence,
                                            imageUrl = result.imageUrl,
                                            estimatedWeightKg = estimatedWeight,
                                            estimatedPrice = estimatedTotal
                                        )
                                        val ok = SupabaseClient.addToVirtualBin(item)
                                        if (ok) {
                                            statusMessage = "Added to Virtual Bin!"
                                            onAddedToBin()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                StatusBadge(text = statusMessage!!, color = EcoEmerald500)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
