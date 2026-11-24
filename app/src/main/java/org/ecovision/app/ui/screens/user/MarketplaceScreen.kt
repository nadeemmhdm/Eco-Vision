package org.ecovision.app.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import java.util.UUID

@Composable
fun MarketplaceScreen(onBack: () -> Unit) {
    val profile by SessionManager.currentProfile.collectAsState()
    val scope = rememberCoroutineScope()

    var products by remember { mutableStateOf<List<MarketplaceProduct>>(emptyList()) }
    var selectedProductForBargain by remember { mutableStateOf<MarketplaceProduct?>(null) }
    var bargainOfferPrice by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    // New Product state
    var newTitle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var newMaterial by remember { mutableStateOf("PET Plastic Bottles") }
    var newPrice by remember { mutableStateOf("250") }
    var newImageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1544816155-12df9643f363?w=600") }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            products = SupabaseClient.getMarketplaceProducts()
        }
    }

    LaunchedEffect(Unit) {
        refresh()
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Upcycled P2P Store",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Buy & Bargain DIY Products Crafted from Waste",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.AddCircle, contentDescription = "List Product", tint = EcoEmerald400)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (notificationMessage != null) {
                StatusBadge(text = notificationMessage!!, color = EcoEmerald400)
                Spacer(modifier = Modifier.height(10.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(products) { item ->
                    Surface(
                        color = Slate850,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            if (item.imageUrl != null) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Crafted from: ${item.sourceWasteMaterial}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LeafCoinGold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "₹${item.askingPrice.toInt()}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = EcoEmerald400,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Button(
                                        onClick = {
                                            selectedProductForBargain = item
                                            bargainOfferPrice = "${(item.askingPrice * 0.85).toInt()}"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Bargain 💬", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bargain Negotiation Modal
        if (selectedProductForBargain != null) {
            val prod = selectedProductForBargain!!
            AlertDialog(
                onDismissRequest = { selectedProductForBargain = null },
                containerColor = Slate850,
                titleContentColor = Color.White,
                textContentColor = Slate300,
                title = { Text("💬 Propose Bargain Price") },
                text = {
                    Column {
                        Text(
                            text = prod.title,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Original Asking Price: ₹${prod.askingPrice.toInt()}",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = bargainOfferPrice,
                            onValueChange = { bargainOfferPrice = it },
                            label = { Text("Your Counter Offer (₹)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Once seller accepts your bargained price, the order is locked.",
                            color = LeafCoinGold,
                            fontSize = 11.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                        onClick = {
                            val offerVal = bargainOfferPrice.toDoubleOrNull() ?: prod.askingPrice
                            selectedProductForBargain = null
                            scope.launch {
                                if (profile != null) {
                                    SupabaseClient.submitBargainOffer(
                                        BargainOffer(
                                            id = UUID.randomUUID().toString(),
                                            productId = prod.id,
                                            buyerId = profile!!.id,
                                            offerPrice = offerVal
                                        )
                                    )
                                    notificationMessage = "Bargain offer of ₹$offerVal sent to seller!"
                                }
                            }
                        }
                    ) {
                        Text("Send Offer", color = Slate950, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedProductForBargain = null }) {
                        Text("Cancel", color = Slate400)
                    }
                }
            )
        }

        // Create / List Product Modal
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = Slate850,
                titleContentColor = Color.White,
                textContentColor = Slate300,
                title = { Text("List Upcycled Craft") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Product Title") },
                            placeholder = { Text("e.g. Self-Watering Planters") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newDesc,
                            onValueChange = { newDesc = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newMaterial,
                            onValueChange = { newMaterial = it },
                            label = { Text("Source Waste Material") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPrice,
                            onValueChange = { newPrice = it },
                            label = { Text("Asking Price (₹)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                        onClick = {
                            showCreateDialog = false
                            scope.launch {
                                if (profile != null) {
                                    SupabaseClient.createMarketplaceProduct(
                                        MarketplaceProduct(
                                            id = UUID.randomUUID().toString(),
                                            sellerId = profile!!.id,
                                            title = newTitle,
                                            description = newDesc,
                                            sourceWasteMaterial = newMaterial,
                                            askingPrice = newPrice.toDoubleOrNull() ?: 200.0,
                                            imageUrl = newImageUrl
                                        )
                                    )
                                    refresh()
                                    notificationMessage = "Craft listed on Upcycled Store!"
                                }
                            }
                        }
                    ) {
                        Text("List Product", color = Slate950, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel", color = Slate400)
                    }
                }
            )
        }
    }
}
