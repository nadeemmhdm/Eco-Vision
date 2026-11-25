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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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

    // Gemini API Key State
    var geminiApiKey by remember { mutableStateOf(prefs.getString("gemini_api_key", "") ?: "") }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var inputApiKey by remember { mutableStateOf(geminiApiKey) }

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
                    // Save to device storage cache
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
                        geminiApiKey = geminiApiKey.ifBlank { null }
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
                            geminiApiKey = geminiApiKey.ifBlank { null }
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

    // Ask camera permission on first entry if not granted
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
                        text = "AI Waste Scanner",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (geminiApiKey.isNotBlank()) "Powered by Google Gemini Vision AI" else "Vision AI & Smart Material Grading",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (geminiApiKey.isNotBlank()) EcoEmerald400 else Slate400
                    )
                }
                IconButton(onClick = {
                    inputApiKey = geminiApiKey
                    showApiKeyDialog = true
                }) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Gemini API Key",
                        tint = if (geminiApiKey.isNotBlank()) EcoEmerald400 else Slate400
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
                            text = if (hasCameraPermission) "Tap 'Take Photo' to scan waste" else "Camera permission needed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400
                        )
                        Text(
                            text = if (geminiApiKey.isNotBlank()) "Gemini Vision AI Engine: ACTIVE" else "Smart Multimodal Material Recognition",
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
                            .padding(20.dp)
                            .border(2.dp, EcoEmerald400, RoundedCornerShape(12.dp))
                    ) {
                        Surface(
                            color = EcoEmerald600,
                            shape = RoundedCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "${detectionResult?.categoryName} · ${detectionResult?.confidence?.toInt() ?: 96}% Match",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action: Camera Capture & Gallery Pick Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EcoButton(
                    text = if (isScanning) "AI Analyzing..." else "Take Photo 📸",
                    icon = Icons.Default.CameraAlt,
                    enabled = !isScanning,
                    modifier = Modifier.weight(1.2f),
                    onClick = {
                        if (hasCameraPermission) {
                            takePhotoLauncher.launch(null)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )
                EcoButton(
                    text = "Gallery 🖼️",
                    icon = Icons.Default.PhotoLibrary,
                    isSecondary = true,
                    enabled = !isScanning,
                    modifier = Modifier.weight(1f),
                    onClick = { pickGalleryLauncher.launch("image/*") }
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
                                text = "AI Confidence Score: ${result.confidence.toInt()}%",
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

        // Gemini API Key Configuration Dialog
        if (showApiKeyDialog) {
            AlertDialog(
                onDismissRequest = { showApiKeyDialog = false },
                containerColor = Slate850,
                titleContentColor = Color.White,
                textContentColor = Slate300,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = EcoEmerald400)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google Gemini API Key")
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Enter your Google Gemini API key to activate real-time Gemini 1.5 Flash Vision AI analysis on waste photos:",
                            fontSize = 12.sp,
                            color = Slate300
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = inputApiKey,
                            onValueChange = { inputApiKey = it },
                            label = { Text("Gemini API Key (AIzaSy...)") },
                            placeholder = { Text("Paste your API key here") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EcoEmerald500,
                                unfocusedBorderColor = CardBorderBright
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Get a free Gemini API key at aistudio.google.com. If blank, high-accuracy built-in AI will analyze your item.",
                            fontSize = 10.sp,
                            color = Slate400
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            geminiApiKey = inputApiKey.trim()
                            prefs.edit().putString("gemini_api_key", geminiApiKey).apply()
                            showApiKeyDialog = false
                            statusMessage = if (geminiApiKey.isNotBlank()) "Gemini Vision AI Activated! ✓" else "Reset to Built-in AI Engine"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald500)
                    ) {
                        Text("Save & Apply", color = Slate950, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showApiKeyDialog = false }) {
                        Text("Cancel", color = Slate400)
                    }
                }
            )
        }
    }
}
