package org.ecovision.app.ui.screens.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
    var selectedProductForDetails by remember { mutableStateOf<MarketplaceProduct?>(null) }
    var selectedProductForBargain by remember { mutableStateOf<MarketplaceProduct?>(null) }
    var bargainOfferPrice by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showCartDialog by remember { mutableStateOf(false) }

    // Shopping Cart State
    val cartItems = remember { mutableStateListOf<CartItem>() }
    var deliveryAddress by remember { mutableStateOf(profile?.defaultAddress ?: "Palarivattom, Kochi, Kerala") }
    var paymentMethod by remember { mutableStateOf("UPI Online") }

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

    val totalCartCount = cartItems.sumOf { it.quantity }
    val cartSubtotal = cartItems.sumOf { it.product.askingPrice * it.quantity }
    val isFreeDelivery = cartSubtotal >= 200.0
    val deliveryFee = if (cartSubtotal == 0.0 || isFreeDelivery) 0.0 else 25.0
    val cartTotal = cartSubtotal + deliveryFee

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
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
                // Cart Icon with Badge
                BadgedBox(
                    badge = {
                        if (totalCartCount > 0) {
                            Badge(
                                containerColor = EcoEmerald500,
                                contentColor = Slate950
                            ) {
                                Text("$totalCartCount", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    IconButton(onClick = { showCartDialog = true }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = if (totalCartCount > 0) EcoEmerald400 else Slate400)
                    }
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
            // Free Delivery Promotion Banner
            Surface(
                color = Slate850,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EcoEmerald500.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = EcoEmerald400, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🚀 FREE Doorstep Delivery by Eco Agents on orders above ₹200!",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedProductForDetails = item }
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
                                    text = "♻️ ${item.sourceWasteMaterial}",
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
                                    IconButton(
                                        onClick = {
                                            val existing = cartItems.find { it.product.id == item.id }
                                            if (existing != null) {
                                                existing.quantity += 1
                                            } else {
                                                cartItems.add(CartItem(item, 1))
                                            }
                                            notificationMessage = "Added ${item.title} to Cart! 🛒"
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(EcoEmerald600)
                                    ) {
                                        Icon(Icons.Default.AddShoppingCart, contentDescription = "Add to Cart", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 1. PRODUCT DETAILS MODAL
        // ==========================================
        if (selectedProductForDetails != null) {
            val prod = selectedProductForDetails!!
            AlertDialog(
                onDismissRequest = { selectedProductForDetails = null },
                containerColor = Slate850,
                titleContentColor = Color.White,
                textContentColor = Slate300,
                title = {
                    Text(text = prod.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                },
                text = {
                    Column {
                        if (prod.imageUrl != null) {
                            AsyncImage(
                                model = prod.imageUrl,
                                contentDescription = prod.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Price: ₹${prod.askingPrice.toInt()}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = EcoEmerald400,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LeafCoinGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "♻️ ${prod.sourceWasteMaterial}",
                                    color = LeafCoinGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = prod.description.ifBlank { "Handcrafted upcycled piece built from sanitized circular waste materials. Durable and sustainable." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate300
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (prod.askingPrice >= 200.0) "🎉 Qualifies for FREE Eco Agent Doorstep Delivery!" else "🚚 Eco Agent Delivery: ₹25 (Free above ₹200)",
                            fontSize = 11.sp,
                            color = if (prod.askingPrice >= 200.0) EcoEmerald400 else Slate400,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                        onClick = {
                            val existing = cartItems.find { it.product.id == prod.id }
                            if (existing != null) {
                                existing.quantity += 1
                            } else {
                                cartItems.add(CartItem(prod, 1))
                            }
                            selectedProductForDetails = null
                            notificationMessage = "Added ${prod.title} to Cart!"
                        }
                    ) {
                        Text("Add to Cart 🛒", color = Slate950, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(
                            onClick = {
                                selectedProductForBargain = prod
                                bargainOfferPrice = "${(prod.askingPrice * 0.85).toInt()}"
                                selectedProductForDetails = null
                            }
                        ) {
                            Text("Bargain 💬", color = LeafCoinGold)
                        }
                        TextButton(onClick = { selectedProductForDetails = null }) {
                            Text("Close", color = Slate400)
                        }
                    }
                }
            )
        }

        // ==========================================
        // 2. SHOPPING CART & ONLINE CHECKOUT MODAL
        // ==========================================
        if (showCartDialog) {
            AlertDialog(
                onDismissRequest = { showCartDialog = false },
                containerColor = Slate850,
                titleContentColor = Color.White,
                textContentColor = Slate300,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = EcoEmerald400)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Your Shopping Cart (${cartItems.size})", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                    ) {
                        if (cartItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.RemoveShoppingCart, null, tint = Slate500, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Your cart is empty", color = Slate400)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(cartItems) { item ->
                                    Surface(
                                        color = Slate800,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.product.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                                Text("₹${item.product.askingPrice.toInt()} each", color = EcoEmerald400, fontSize = 11.sp)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        if (item.quantity > 1) {
                                                            item.quantity -= 1
                                                        } else {
                                                            cartItems.remove(item)
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                }
                                                Text("${item.quantity}", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                                                IconButton(
                                                    onClick = { item.quantity += 1 },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = { cartItems.remove(item) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, null, tint = CoralRed, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = CardBorderBright)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Subtotal & Free Delivery Progress
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Items Subtotal:", color = Slate300, fontSize = 12.sp)
                                Text("₹${cartSubtotal.toInt()}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Eco Agent Delivery:", color = Slate300, fontSize = 12.sp)
                                Text(if (isFreeDelivery) "FREE 🎉" else "₹25", color = if (isFreeDelivery) EcoEmerald400 else Slate400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            if (!isFreeDelivery) {
                                Text(
                                    text = "💡 Add ₹${(200.0 - cartSubtotal).toInt()} more items for FREE Delivery!",
                                    fontSize = 11.sp,
                                    color = LeafCoinGold,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Amount Payable:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("₹${cartTotal.toInt()}", color = EcoEmerald400, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = deliveryAddress,
                                onValueChange = { deliveryAddress = it },
                                label = { Text("Delivery Address *") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Payment Mode: $paymentMethod", fontSize = 11.sp, color = TechBlue)
                        }
                    }
                },
                confirmButton = {
                    if (cartItems.isNotEmpty()) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500),
                            onClick = {
                                scope.launch {
                                    val order = MarketplaceOrder(
                                        id = UUID.randomUUID().toString(),
                                        userId = profile?.id ?: "guest",
                                        subtotal = cartSubtotal,
                                        deliveryFee = deliveryFee,
                                        totalAmount = cartTotal,
                                        deliveryAddress = deliveryAddress,
                                        status = "assigned",
                                        paymentMethod = paymentMethod
                                    )
                                    SupabaseClient.createMarketplaceOrder(order)
                                    cartItems.clear()
                                    showCartDialog = false
                                    notificationMessage = "🎉 Order #${order.id.take(8)} placed! Pickup Agent assigned for doorstep delivery."
                                }
                            }
                        ) {
                            Text("Buy Online & Dispatch Agent (₹${cartTotal.toInt()})", color = Slate950, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCartDialog = false }) {
                        Text("Continue Shopping", color = Slate400)
                    }
                }
            )
        }

        // ==========================================
        // 3. BARGAIN NEGOTIATION MODAL
        // ==========================================
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

        // ==========================================
        // 4. CREATE / LIST PRODUCT MODAL
        // ==========================================
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
