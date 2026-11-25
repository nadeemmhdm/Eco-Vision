package org.ecovision.app.ui.screens.user

import android.Manifest
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
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
    val profile by SessionManager.currentProfile.collectAsState()
    val scope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(false) }
    var detectionResult by remember { mutableStateOf<AiDetectionResult?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var estimatedWeight by remember { mutableDoubleStateOf(1.2) }
    var categories by remember { mutableStateOf<List<WasteCategory>>(emptyList()) }
    var pricingSlabs by remember { mutableStateOf<List<WeightPricingSlab>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf("pet_plastic") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var viewingInstructionsScreen by remember { mutableStateOf(false) }
    var showUpcycleDialog by remember { mutableStateOf(false) }

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
                        imageBytes = bytes
                    )
                    detectionResult = result
                    selectedCategoryId = result.categoryId
                    estimatedWeight = result.estimatedWeightKg
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
                            imageBytes = bytes
                        )
                        detectionResult = result
                        selectedCategoryId = result.categoryId
                        estimatedWeight = result.estimatedWeightKg
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (viewingInstructionsScreen) {
                        viewingInstructionsScreen = false
                    } else {
                        onBack()
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (viewingInstructionsScreen) {
                            "${detectionResult?.wasteName ?: "Material"} Instructions"
                        } else {
                            "AI Waste Scanner"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (viewingInstructionsScreen) {
                            "Curbside pickup rules & drop-off centers"
                        } else {
                            "Instant AI Segregation & Recycling Value"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = EcoEmerald400,
                        fontSize = 11.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        if (viewingInstructionsScreen) {
            // =========================================================================
            // RECYCLING INSTRUCTIONS SCREEN
            // =========================================================================
            val result = detectionResult ?: AiDetectionResult(
                categoryId = "pet_plastic",
                categoryName = "PET Plastic Bottle",
                wasteName = "Plastic Bottle",
                wasteType = "Plastic",
                material = "PET Plastic",
                confidence = 98.0,
                isRecyclable = true,
                estimatedBasePrice = 16.50
            )
            val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
            val baseRate = selectedCategory?.baseRatePerKg ?: result.estimatedBasePrice
            val currentSlabRate = SupabaseClient.calculateSlabRate(pricingSlabs, selectedCategoryId, estimatedWeight, baseRate)
            val estimatedTotal = estimatedWeight * currentSlabRate

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Video Banner Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate800)
                        .border(1.dp, CardBorderBright, RoundedCornerShape(16.dp))
                        .clickable {
                            val v = result.upcycleVideos.firstOrNull()
                            if (v != null) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${v.videoId}"))
                                context.startActivity(intent)
                            } else {
                                statusMessage = "Opening sorting tutorial..."
                            }
                        }
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?w=800",
                        contentDescription = "Sorting Tutorial Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Play Video",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "Learn about sorting ${result.material}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Curbside Pickup Card
                Surface(
                    color = Color(0xFFD1FAE5).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EcoEmerald500.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, null, tint = EcoEmerald400, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (result.isRecyclable) "Doorstep Agent Pickup" else "Non-Recyclable Notice",
                                color = if (result.isRecyclable) EcoEmerald400 else CoralRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (result.isRecyclable) {
                                "This item is accepted in your doorstep recycling pickup! Please sanitize and place into your virtual bin. Our neighborhood collection van visits your area regularly."
                            } else {
                                "This material is non-recyclable in standard municipal streams. Please do not mix it with clean dry recyclables. Check upcycling ideas below."
                            },
                            color = Slate300,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Recycling Drop-Off Depots Nearby",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                DropOffDepotCard("Central Materials Recovery Hub", "Civil Station Road, Kannur, Kerala · Open 8 AM - 6 PM")
                Spacer(modifier = Modifier.height(8.dp))
                DropOffDepotCard("Zero-Waste Eco Drop Center", "Beach Road, Calicut, Kerala · Open 9 AM - 5 PM")

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showUpcycleDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Upscale Ideas 🎨", color = LeafCoinGold)
                    }

                    if (result.isRecyclable) {
                        EcoButton(
                            text = "Add to Bin ♻️",
                            icon = Icons.Default.AddShoppingCart,
                            modifier = Modifier.weight(1.2f),
                            onClick = {
                                scope.launch {
                                    val currentUserId = profile?.id ?: "user-adhil"
                                    val item = VirtualBinItem(
                                        id = UUID.randomUUID().toString(),
                                        userId = currentUserId,
                                        categoryId = selectedCategoryId,
                                        aiConfidence = result.confidence,
                                        imageUrl = result.imageUrl,
                                        estimatedWeightKg = estimatedWeight,
                                        estimatedPrice = estimatedTotal
                                    )
                                    SupabaseClient.addToVirtualBin(item)
                                    statusMessage = "Added to Virtual Bin!"
                                    onAddedToBin()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        } else {
            // =========================================================================
            // CAMERA VIEWFINDER & SCANNED ITEM CARD
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. CAMERA VIEWFINDER WITH RETICLE BRACKETS [ ]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Slate950)
                        .border(2.dp, if (detectionResult != null) EcoEmerald500 else CardBorderBright, RoundedCornerShape(22.dp)),
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
                    }

                    // 4 Corner Brackets [ ] Overlay
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
                                text = "Use camera to scan waste or packaging for instant AI classification & value estimation",
                                color = Color.White,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (detectionResult != null) {
                        Surface(
                            color = if (detectionResult!!.isRecyclable) EcoEmerald600 else CoralRed,
                            shape = RoundedCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = if (detectionResult!!.isRecyclable) "✓ RECYCLABLE · ${detectionResult!!.confidence.toInt()}% AI MATCH" else "✗ NON-RECYCLABLE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. SCAN BUTTONS ROW
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
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isScanning) "AI Analyzing..." else if (detectionResult != null) "Scan Another" else "Start Camera Scan",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { pickGalleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(0.9f),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                // 3. SCAN RESULTS & COMPREHENSIVE WASTE DETAILS
                if (detectionResult != null) {
                    val result = detectionResult!!
                    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
                    val baseRate = selectedCategory?.baseRatePerKg ?: result.estimatedBasePrice
                    val currentSlabRate = SupabaseClient.calculateSlabRate(pricingSlabs, selectedCategoryId, estimatedWeight, baseRate)
                    val estimatedTotal = estimatedWeight * currentSlabRate

                    Spacer(modifier = Modifier.height(18.dp))

                    GlassCard {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Slate600)
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Header: Waste Name + Recyclable Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = result.wasteName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${result.wasteType} · ${result.material}",
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

                        // Complete Waste Details Grid (Table Format)
                        Surface(
                            color = Slate900.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Waste Name", color = Slate400, fontSize = 11.sp)
                                        Text(result.wasteName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Waste Type", color = Slate400, fontSize = 11.sp)
                                        Text(result.wasteType, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Slate800)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Material", color = Slate400, fontSize = 11.sp)
                                        Text(result.material, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Recyclable?", color = Slate400, fontSize = 11.sp)
                                        Text(
                                            text = if (result.isRecyclable) "Yes (100% Recyclable)" else "No (Non-Recyclable)",
                                            color = if (result.isRecyclable) EcoEmerald400 else CoralRed,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Slate800)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Applicable Rate", color = Slate400, fontSize = 11.sp)
                                        Text("₹${String.format("%.2f", currentSlabRate)} / kg", color = LeafCoinGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Estimated Value", color = Slate400, fontSize = 11.sp)
                                        Text("₹${String.format("%.2f", estimatedTotal)}", color = EcoEmerald400, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Collection & Weight Estimation Stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Adjust Weight: ${String.format("%.1f", estimatedWeight)} kg",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (estimatedWeight > 0.2) estimatedWeight -= 0.2 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = Slate400)
                                }
                                Text(
                                    text = "${String.format("%.1f", estimatedWeight)} kg",
                                    color = LeafCoinGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(
                                    onClick = { estimatedWeight += 0.5 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = EcoEmerald400)
                                }
                            }
                        }

                        Slider(
                            value = estimatedWeight.toFloat(),
                            onValueChange = { estimatedWeight = it.toDouble() },
                            valueRange = 0.2f..20f,
                            steps = 39,
                            colors = SliderDefaults.colors(
                                thumbColor = EcoEmerald500,
                                activeTrackColor = EcoEmerald500,
                                inactiveTrackColor = Slate800
                            )
                        )

                        // Total Collection Summary Box
                        Surface(
                            color = EcoEmerald950.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EcoEmerald500.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Total Collected Weight", color = Slate400, fontSize = 11.sp)
                                    Text("${String.format("%.1f", estimatedWeight)} kg", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Estimated Collection Price", color = Slate400, fontSize = 11.sp)
                                    Text("₹${String.format("%.2f", estimatedTotal)}", color = EcoEmerald400, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Button 1: UPSCALE THIS PRODUCT (YouTube DIY Ideas)
                        Button(
                            onClick = { showUpcycleDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = LeafCoinGold),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Slate950, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Upscale This Product 🎨 (YouTube DIY)",
                                color = Slate950,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Button 2: ADD TO RECYCLABLE BIN (STRICT RULE: ONLY SHOWN IF RECYCLABLE)
                        if (result.isRecyclable) {
                            EcoButton(
                                text = "Add to Recyclable Bin ♻️",
                                icon = Icons.Default.AddShoppingCart,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    scope.launch {
                                        val currentUserId = profile?.id ?: "user-adhil"
                                        val item = VirtualBinItem(
                                            id = UUID.randomUUID().toString(),
                                            userId = currentUserId,
                                            categoryId = selectedCategoryId,
                                            aiConfidence = result.confidence,
                                            imageUrl = result.imageUrl,
                                            estimatedWeightKg = estimatedWeight,
                                            estimatedPrice = estimatedTotal
                                        )
                                        SupabaseClient.addToVirtualBin(item)
                                        statusMessage = "🎉 Added ${String.format("%.1f", estimatedWeight)} kg of ${result.wasteName} to Recyclable Bin!"
                                        onAddedToBin()
                                    }
                                }
                            )
                        } else {
                            // Non-recyclable warning card replacing the button
                            Surface(
                                color = CoralRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = CoralRed, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "⚠️ Non-Recyclable Item: This item cannot be added to the Recyclable Bin. Check 'Upscale This Product' above for creative reuse ideas!",
                                        color = CoralRed,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewingInstructionsScreen = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Instructions 📖", color = Color.White, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = onNavigateToMarketplace,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Craft Store 🛍️", color = LeafCoinGold, fontSize = 12.sp)
                            }
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
    }

    // ==========================================
    // UPSCALE THIS PRODUCT - YOUTUBE VIDEO DIALOG
    // ==========================================
    if (showUpcycleDialog && detectionResult != null) {
        val videos = detectionResult!!.upcycleVideos.ifEmpty {
            AiVisionEngine.getUpcycleVideosForCategory(selectedCategoryId, detectionResult!!.wasteName)
        }.take(5)

        AlertDialog(
            onDismissRequest = { showUpcycleDialog = false },
            containerColor = Slate850,
            titleContentColor = Color.White,
            textContentColor = Slate300,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = LeafCoinGold, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Upscale ${detectionResult!!.wasteName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Creative DIY & reuse ideas on YouTube", color = Slate400, fontSize = 11.sp)
                    }
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Discover how to transform your ${detectionResult!!.material} waste into valuable, functional products instead of throwing it away:",
                            color = Slate300,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    items(videos) { vid ->
                        Surface(
                            color = Slate900,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${vid.videoId}"))
                                    context.startActivity(intent)
                                }
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                ) {
                                    AsyncImage(
                                        model = vid.thumbnailUrl,
                                        contentDescription = vid.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Play icon overlay
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.65f))
                                            .align(Alignment.Center),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                    // Duration badge
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = vid.duration,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = vid.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "📺 ${vid.channel}",
                                        color = LeafCoinGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (vid.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = vid.description,
                                            color = Slate400,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp,
                                            maxLines = 2
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${vid.videoId}"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Watch on YouTube 📺", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUpcycleDialog = false }) {
                    Text("Close", color = EcoEmerald400, fontWeight = FontWeight.Bold)
                }
            }
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
