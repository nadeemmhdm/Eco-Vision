package org.ecovision.app.ui.screens.user

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun AiScanScreen(
    onAddedToBin: () -> Unit,
    onNavigateToMarketplace: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ecovision_ai_prefs", Context.MODE_PRIVATE) }
    val profile by SessionManager.currentProfile.collectAsState()
    val scope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(false) }
    var detectionResult by remember { mutableStateOf<AiDetectionResult?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var estimatedWeight by remember { mutableDoubleStateOf(2.5) }
    var categories by remember { mutableStateOf<List<WasteCategory>>(emptyList()) }
    var pricingSlabs by remember { mutableStateOf<List<WeightPricingSlab>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf("pet_plastic") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showInstructionsDialog by remember { mutableStateOf(false) }

    // AI Vision Gateway Key State
    var aiApiKey by remember {
        mutableStateOf(
            prefs.getString("ai_api_key", "app_NftMOURzTebhtIAhgz_gLeOZPGEOHqXB") ?: "app_NftMOURzTebhtIAhgz_gLeOZPGEOHqXB"
        )
    }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var inputApiKey by remember { mutableStateOf(aiApiKey) }

    // Camera Permission State
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            statusMessage = "Camera permission is required to capture photos directly."
        }
    }

    // Camera Photo Capture Launcher
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            scope.launch {
                isScanning = true
                try {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    val bytes = stream.toByteArray()

                    val scanDir = File(context.cacheDir, "waste_scans")
                    if (!scanDir.exists()) scanDir.mkdirs()
                    val savedFile = File(scanDir, "scan_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(savedFile).use { it.write(bytes) }

                    val result = AiVisionEngine.analyzeWasteImage(
                        imageUri = savedFile.absolutePath,
                        imageBytes = bytes,
                        apiKey = aiApiKey.ifBlank { null }
                    )
                    detectionResult = result
                    selectedCategoryId = result.categoryId
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                isScanning = false
            }
        }
    }

    // Gallery Picker Launcher
    val pickGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isScanning = true
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    val bytes = stream?.readBytes()
                    if (bytes != null) {
                        capturedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        val result = AiVisionEngine.analyzeWasteImage(
                            imageUri = uri.toString(),
                            imageBytes = bytes,
                            apiKey = aiApiKey.ifBlank { null }
                        )
                        detectionResult = result
                        selectedCategoryId = result.categoryId
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                isScanning = false
            }
        }
    }

    LaunchedEffect(Unit) {
        categories = SupabaseClient.getCategories()
        pricingSlabs = SupabaseClient.getPricingSlabs()
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
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
                        text = "Scan",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (aiApiKey.isNotBlank()) "AI Gateway Vision Engine" else "Smart Multimodal Material Recognition",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (aiApiKey.isNotBlank()) EcoEmerald400 else Slate400,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = {
                    inputApiKey = aiApiKey
                    showApiKeyDialog = true
                }) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "AI Gateway Key",
                        tint = if (aiApiKey.isNotBlank()) EcoEmerald400 else Slate400
                    )
                }
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
            // ==========================================
            // 1. CAMERA VIEWFINDER WITH RETICLE BRACKETS [ ] (Image 1)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Slate950)
                    .border(2.dp, if (detectionResult != null) EcoEmerald500 else CardBorderBright, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background image or placeholder
                if (capturedBitmap != null) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Captured Waste Item",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (detectionResult?.imageUrl != null) {
                    AsyncImage(
                        model = detectionResult?.imageUrl,
                        contentDescription = "Scanned Waste",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // 4 Corner Brackets [ ] Overlay (Image 1)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val bracketLen = 34.dp.toPx()
                    val strokeW = 4.dp.toPx()
                    val padding = 28.dp.toPx()
                    val color = Color.White

                    // Top-Left [
                    drawLine(color, Offset(padding, padding), Offset(padding + bracketLen, padding), strokeW)
                    drawLine(color, Offset(padding, padding), Offset(padding, padding + bracketLen), strokeW)

                    // Top-Right ]
                    drawLine(color, Offset(w - padding, padding), Offset(w - padding - bracketLen, padding), strokeW)
                    drawLine(color, Offset(w - padding, padding), Offset(w - padding, padding + bracketLen), strokeW)

                    // Bottom-Left [
                    drawLine(color, Offset(padding, h - padding), Offset(padding + bracketLen, h - padding), strokeW)
                    drawLine(color, Offset(padding, h - padding), Offset(padding, h - padding - bracketLen), strokeW)

                    // Bottom-Right ]
                    drawLine(color, Offset(w - padding, h - padding), Offset(w - padding - bracketLen, h - padding), strokeW)
                    drawLine(color, Offset(w - padding, h - padding), Offset(w - padding, h - padding - bracketLen), strokeW)
                }

                // Centered Reticle Prompt (Image 1)
                if (capturedBitmap == null && detectionResult == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.QrCodeScanner, null, tint = EcoEmerald400, modifier = Modifier.size(42.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Use the camera to take a picture of the object or scan a packaging barcode",
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // AI Detection Match Chip
                if (detectionResult != null) {
                    Surface(
                        color = EcoEmerald600,
                        shape = RoundedCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "${detectionResult?.categoryName} · ${detectionResult?.confidence?.toInt() ?: 96}% AI Match",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 2. GREEN START SCAN BUTTON (Image 1)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (hasCameraPermission) {
                            takePhotoLauncher.launch(null)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isScanning) "AI Analyzing..." else if (detectionResult != null) "Scan" else "Start Scan",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = { pickGalleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(0.9f),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gallery", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            // ==========================================
            // 3. SCAN RESULTS DRAWER (Matching Image 1)
            // ==========================================
            if (detectionResult != null) {
                val result = detectionResult!!
                val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
                val baseRate = selectedCategory?.baseRatePerKg ?: result.estimatedBasePrice
                val currentSlabRate = SupabaseClient.calculateSlabRate(pricingSlabs, selectedCategoryId, estimatedWeight, baseRate)
                val estimatedTotal = estimatedWeight * currentSlabRate

                Spacer(modifier = Modifier.height(18.dp))

                GlassCard {
                    // Header Title (e.g. Glass Bottle)
                    Text(
                        text = result.categoryName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Material Table with Light Tint Background (Image 1)
                    Surface(
                        color = Color(0xFFBAE6FD).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7DD3FC).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Column Headers
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Material", color = Slate400, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text("Number on bottom", color = Slate400, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                                Text("Recyclable?", color = Slate400, fontSize = 11.sp, modifier = Modifier.weight(0.8f))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            // Row Values
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = result.categoryName.split(" ").firstOrNull() ?: "Glass",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "70, 71, 72",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text(
                                    text = if (result.isRecyclable) "Yes" else "No",
                                    color = if (result.isRecyclable) EcoEmerald400 else CoralRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description Text (Image 1)
                    Text(
                        text = "Most glass and packaging materials are 100% recyclable and can be processed endlessly without loss in quality or purity. Clean the item before disposing in curbside collection.",
                        color = Slate300,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scan Again & Recycling Instructions Buttons (Image 1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                detectionResult = null
                                capturedBitmap = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Scan Again", color = Color.White, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showInstructionsDialog = true },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Recycling Instructions", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Curbside Pickup Card (Image 1)
                    Surface(
                        color = Color(0xFFDCFCE7).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EcoEmerald500.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Curbside Pickup",
                                color = EcoEmerald400,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "This item is accepted in your local curbside pickup. Please put materials into the light blue or grey bins. In Malappuram pilot zones, collection runs every Tuesday and Friday.",
                                color = Slate200,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Drop-Off Centers (Image 1)
                    Text(
                        text = "${result.categoryName} recycling drop offs",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DropOffDepotCard(
                        name = "Malappuram Central Recycling Depot",
                        address = "Civil Station Road, Malappuram, Kerala 676505"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    DropOffDepotCard(
                        name = "Town Centre Waste & Recovery Hub",
                        address = "995 Bypass Junction, Perinthalmanna Road"
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = CardBorderBright)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Quantity Slider & Selling Destinations
                    Text(
                        text = "Estimated Weight: ${String.format("%.1f", estimatedWeight)} kg (₹${String.format("%.2f", estimatedTotal)})",
                        style = MaterialTheme.typography.titleMedium,
                        color = LeafCoinGold,
                        fontWeight = FontWeight.Bold
                    )
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EcoButton(
                            text = "List in Upcycle Store",
                            icon = Icons.Default.Storefront,
                            isSecondary = true,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToMarketplace
                        )

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
                StatusBadge(text = statusMessage!!, color = EcoEmerald400)
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Recycling Instructions Dialog
    if (showInstructionsDialog) {
        AlertDialog(
            onDismissRequest = { showInstructionsDialog = false },
            title = { Text("Recycling Guidelines", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("1. Rinse thoroughly to remove residual food or liquid.", color = Slate300, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("2. Separate caps, corks, and labels where applicable.", color = Slate300, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("3. Keep broken glass separate from intact bottles.", color = Slate300, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("4. Place in designated blue curbside bin by 7:00 AM on collection days.", color = Slate300, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInstructionsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600)
                ) {
                    Text("Got It", color = Color.White)
                }
            },
            containerColor = Slate850
        )
    }

    // API Key Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("AI Gateway API Key", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Connects to https://ai-gateway-platform.mhdysn313.workers.dev/v1/chat/completions (OpenAI Compatible):",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inputApiKey,
                        onValueChange = { inputApiKey = it },
                        label = { Text("Bearer Key (app_...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcoEmerald400,
                            unfocusedBorderColor = CardBorderBright,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        aiApiKey = inputApiKey.trim()
                        prefs.edit().putString("ai_api_key", aiApiKey).apply()
                        showApiKeyDialog = false
                        statusMessage = "AI Gateway key saved!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600)
                ) {
                    Text("Save Key", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Slate850
        )
    }
}

@Composable
private fun DropOffDepotCard(name: String, address: String) {
    Surface(
        color = Slate850,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(EcoEmerald600.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Place, null, tint = EcoEmerald400, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(text = address, color = Slate400, fontSize = 10.sp)
            }
        }
    }
}
