package org.ecovision.app.ui.screens.user

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.*

@Composable
fun UserHomeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToBin: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToDoorstep: (String) -> Unit,
    onNavigateToRate: (String) -> Unit
) {
    val context = LocalContext.current
    val profile by SessionManager.currentProfile.collectAsState()
    var activePickups by remember { mutableStateOf<List<PickupRequest>>(emptyList()) }
    var virtualBinCount by remember { mutableIntStateOf(0) }
    var virtualBinValue by remember { mutableDoubleStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var isLocationEnabled by remember { mutableStateOf(DeviceLocationHelper.isLocationEnabled(context)) }
    var detectedAddress by remember { mutableStateOf<String?>(profile?.defaultAddress) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isLocationEnabled = DeviceLocationHelper.isLocationEnabled(context)
            if (isLocationEnabled) {
                try {
                    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    val loc: Location? = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (loc != null && profile != null) {
                        scope.launch {
                            val curr = profile!!
                            val updated = curr.copy(
                                latitude = loc.latitude,
                                longitude = loc.longitude,
                                defaultAddress = curr.defaultAddress ?: "${curr.district ?: "Malappuram"}, Kerala"
                            )
                            SupabaseClient.updateProfile(updated)
                            SessionManager.setProfile(updated, persist = true)
                            detectedAddress = updated.defaultAddress
                        }
                    }
                } catch (e: SecurityException) { }
            }
        }
    }

    LaunchedEffect(Unit) {
        val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (finePerm != PackageManager.PERMISSION_GRANTED && coarsePerm != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            isLocationEnabled = DeviceLocationHelper.isLocationEnabled(context)
        }
    }

    val isServiceAvailable = remember(profile?.district) {
        val dist = profile?.district?.trim()?.lowercase() ?: "malappuram"
        dist == "malappuram"
    }

    LaunchedEffect(profile?.id) {
        if (profile != null) {
            isLoading = true
            val pickups = SupabaseClient.getUserPickups(profile!!.id)
            activePickups = pickups
            val bin = SupabaseClient.getVirtualBin(profile!!.id)
            virtualBinCount = bin.size
            virtualBinValue = bin.sumOf { it.estimatedPrice }
            isLoading = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. TOP CURVED EMERALD HEADER (Matching Image 2)
        // ==========================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF15803D), Color(0xFF16A34A), Color(0xFF10B981))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    // Header Bar (Name, Coins Badge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hi ${profile?.fullName?.split(" ")?.firstOrNull() ?: "Eco Hero"}!",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "You've correctly recycled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        // Coins Badge Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                .clickable { onNavigateToWallet() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                EcoCoinIcon(size = 18.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${profile?.leafCoins ?: 0}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LeafCoinGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // "122 Items" Headline
                    Text(
                        text = "122 Items",
                        fontSize = 32.sp,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Circular Percentage Gauges (Image 2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CircularPercentGauge(percent = 65, label = "Recycled", ringColor = Color(0xFF22C55E))
                        CircularPercentGauge(percent = 5, label = "Compost", ringColor = Color(0xFF06B6D4))
                        CircularPercentGauge(percent = 2, label = "Rubbish", ringColor = Color(0xFFEF4444))
                    }
                }
            }
        }

        // ==========================================
        // 2. SEARCH & RECYCLE ACTIONS
        // ==========================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Search Bar (Matching Image 2)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search for an item to recycle...", color = Slate400, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = EcoEmerald400) },
                    trailingIcon = {
                        IconButton(onClick = onNavigateToScan) {
                            Icon(Icons.Default.QrCodeScanner, null, tint = EcoEmerald400)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate850,
                        unfocusedContainerColor = Slate850,
                        focusedBorderColor = EcoEmerald400,
                        unfocusedBorderColor = CardBorderBright,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                // Location Banners (Location Off / Kerala District Status)
                if (!isLocationEnabled) {
                    Surface(
                        color = Color(0xFF18181B),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOff, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Device Location is Turned Off · Turn ON for auto-collection",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    try { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) } catch (e: Exception) {}
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Turn ON", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (!isServiceAvailable) {
                    Surface(
                        color = Color(0xFF18181B),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Service expanding in ${profile?.district ?: "Kerala"}. Location saved — we will notify you when collection starts!",
                                color = Slate300,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // "Recycle Again" Quick Items (Image 2)
                Text(
                    text = "Recycle Again",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RecycleAgainCard(title = "Bread Bag", bin = "Green Caddy", binColor = EcoEmerald400, icon = Icons.Default.Fastfood)
                    RecycleAgainCard(title = "AA Battery", bin = "Metcalf Way", binColor = LeafCoinGold, icon = Icons.Default.BatteryChargingFull)
                    RecycleAgainCard(title = "Water Bottle", bin = "Red Bin", binColor = CoralRed, icon = Icons.Default.LocalDrink)
                    RecycleAgainCard(title = "Amazon Box", bin = "Blue Bin", binColor = TechBlue, icon = Icons.Default.Inventory2)
                }

                // "Top Categories" (6 Photo Cards from Image 2)
                Text(
                    text = "Top Categories",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CategoryGridCard(title = "Paper", icon = Icons.Default.Description, modifier = Modifier.weight(1f), onClick = onNavigateToScan)
                        CategoryGridCard(title = "Foil", icon = Icons.Default.Layers, modifier = Modifier.weight(1f), onClick = onNavigateToScan)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CategoryGridCard(title = "Cardboard", icon = Icons.Default.Inventory, modifier = Modifier.weight(1f), onClick = onNavigateToScan)
                        CategoryGridCard(title = "Drink Cans", icon = Icons.Default.LocalBar, modifier = Modifier.weight(1f), onClick = onNavigateToScan)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CategoryGridCard(title = "Plastic Bottles", icon = Icons.Default.LocalDrink, modifier = Modifier.weight(1f), onClick = onNavigateToScan)
                        CategoryGridCard(title = "Glass", icon = Icons.Default.WineBar, modifier = Modifier.weight(1f), onClick = onNavigateToScan)
                    }
                }

                // Recycling Bins Guide Row (Image 2)
                Text(
                    text = "Recycling Bins Guide",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BinGuidePill(title = "Green Bin", subtitle = "Organics", color = EcoEmerald400, modifier = Modifier.weight(1f))
                    BinGuidePill(title = "Red Bin", subtitle = "Haz / E-Waste", color = CoralRed, modifier = Modifier.weight(1f))
                    BinGuidePill(title = "Blue Bin", subtitle = "Dry Paper/Glass", color = TechBlue, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Virtual Bin Peek Bar
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToBin() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EcoEmerald500.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DeleteOutline, null, tint = EcoEmerald400, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Virtual Bin",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$virtualBinCount items ready to recycle",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${String.format("%.1f", virtualBinValue)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = EcoEmerald400,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Est. Value",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. LIVE PICKUP TRACKING (Image 3)
        // ==========================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Track Your Pickup",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                if (activePickups.isEmpty()) {
                    // Modern Live Tracking Card with Active Demonstration (Matching Image 3)
                    Image3PickupLiveCard(
                        scheduledSlot = "Today, 4:00 PM - 5:00 PM",
                        isOnTime = true,
                        step = 3,
                        etaMinutes = 12,
                        agentName = "Rafiq Khan",
                        agentRole = "Pickup Agent",
                        agentRating = "4.8 (124 ratings)",
                        vehiclePlate = "KL-07-AB-1234",
                        vehicleType = "Green Van",
                        addressText = detectedAddress ?: "Civil Station, Malappuram",
                        onCallClick = { /* Call action */ },
                        onChatClick = { /* Chat action */ },
                        onVerifyClick = { onNavigateToBin() },
                        verifyButtonText = "Doorstep Match"
                    )
                } else {
                    for (pickup in activePickups) {
                        val currentStep = when (pickup.status) {
                            "requested" -> 1
                            "agent_assigned" -> 2
                            "at_doorstep" -> 3
                            "spot_weighed", "completed" -> 4
                            else -> 2
                        }

                        Image3PickupLiveCard(
                            scheduledSlot = "Today, 4:00 PM - 5:00 PM",
                            isOnTime = true,
                            step = currentStep,
                            etaMinutes = if (pickup.status == "at_doorstep") 0 else 12,
                            agentName = "Rafiq Khan",
                            agentRole = "Pickup Agent",
                            agentRating = "4.8 (124 ratings)",
                            vehiclePlate = "KL-07-AB-1234",
                            vehicleType = "Green Van",
                            addressText = pickup.pickupAddress,
                            onCallClick = { /* Call */ },
                            onChatClick = { /* Chat */ },
                            onVerifyClick = {
                                if (pickup.status == "completed") {
                                    onNavigateToRate(pickup.id)
                                } else {
                                    onNavigateToDoorstep(pickup.id)
                                }
                            },
                            verifyButtonText = if (pickup.status == "completed") "Rate Agent" else "Verify Vehicle"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }
    }
}

@Composable
private fun CircularPercentGauge(percent: Int, label: String, ringColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(4.dp, ringColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$percent%",
                fontSize = 17.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RecycleAgainCard(title: String, bin: String, binColor: Color, icon: ImageVector) {
    Surface(
        color = Slate850,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
        modifier = Modifier.width(130.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Slate800),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = binColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = binColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "📍 $bin",
                    color = binColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryGridCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = Slate850,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(EcoEmerald600.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = EcoEmerald400, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BinGuidePill(title: String, subtitle: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Slate850,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = Slate400, fontSize = 10.sp)
        }
    }
}
