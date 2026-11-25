package org.ecovision.app.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.ecovision.app.data.Profile
import org.ecovision.app.data.SessionManager
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.EcoCoinIcon
import org.ecovision.app.ui.components.StatusBadge

@Composable
fun SplashScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToHome: (Profile) -> Unit
) {
    val isInitialized by SessionManager.isInitialized.collectAsState()
    val currentProfile by SessionManager.currentProfile.collectAsState()

    var showOnboarding by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }

    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    LaunchedEffect(Unit) {
        if (!isInitialized) {
            SessionManager.init()
        }
    }

    LaunchedEffect(isInitialized) {
        if (isInitialized && !hasNavigated) {
            delay(500)
            val profile = currentProfile
            if (profile != null) {
                hasNavigated = true
                onNavigateToHome(profile)
            } else {
                showOnboarding = true
            }
        }
    }

    if (showOnboarding) {
        // =========================================================================
        // ONBOARDING SCREEN MATCHING IMAGE 3 (SCREEN 1)
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE8F5E9),
                            Color(0xFFF1F8E9),
                            Color(0xFFE0F2F1)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Brand Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF15803D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "EcoVision",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF14532D)
                    )
                }

                // Middle 3D Art & Headlines
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Headline & Subtitle (Image 3)
                    Text(
                        text = "Pickup On Time.",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF064E3B),
                        lineHeight = 44.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Smart Waste Collection for a Greener Tomorrow",
                        fontSize = 15.sp,
                        color = Color(0xFF166534),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Stylized Winding Road & 3D Green Truck Illustration
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Lush green hill curve
                            val hillPath = Path().apply {
                                moveTo(0f, h * 0.75f)
                                cubicTo(w * 0.3f, h * 0.55f, w * 0.7f, h * 0.85f, w, h * 0.65f)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(hillPath, color = Color(0xFFA7F3D0).copy(alpha = 0.6f))

                            // Winding Road
                            val roadPath = Path().apply {
                                moveTo(w * 0.15f, h * 0.88f)
                                cubicTo(w * 0.45f, h * 0.80f, w * 0.50f, h * 0.40f, w * 0.80f, h * 0.28f)
                            }
                            drawPath(
                                path = roadPath,
                                color = Color.White,
                                style = Stroke(width = 42f, cap = StrokeCap.Round)
                            )
                            drawPath(
                                path = roadPath,
                                color = Color(0xFF64748B),
                                style = Stroke(width = 32f, cap = StrokeCap.Round)
                            )
                            drawPath(
                                path = roadPath,
                                color = Color.White.copy(alpha = 0.8f),
                                style = Stroke(width = 4f, cap = StrokeCap.Round)
                            )
                        }

                        // Big Green Location Pin with Leaf (Matching Image 3)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 40.dp, top = 20.dp)
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF15803D))
                                .border(3.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Eco,
                                contentDescription = "Pin Leaf",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        // 3D Recycling Truck Emblem on the road
                        Surface(
                            color = Color(0xFF16A34A),
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 10.dp,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(end = 24.dp)
                                .size(90.dp, 64.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.LocalShipping,
                                    contentDescription = "Collection Truck",
                                    tint = Color.White,
                                    modifier = Modifier.size(38.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Eco,
                                    contentDescription = "Eco",
                                    tint = Color(0xFFDCFCE7),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom Carousel Indicators & "Get Started ->" Button (Image 3)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 4 Carousel Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 22.dp, height = 7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF15803D))
                        )
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF86EFAC)))
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF86EFAC)))
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF86EFAC)))
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Green Pill "Get Started ->" Button
                    Button(
                        onClick = onNavigateToAuth,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14532D)),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Get Started",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Forward",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Initial Splash Loading Screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Slate950,
                            Slate900,
                            Color(0xFF041E15)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Outer Glow
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(EcoEmerald500.copy(alpha = glowAlpha)),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner Icon Container
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(EcoEmerald400, EcoEmerald600)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "EcoVision Logo",
                            tint = Slate950,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // App Title
                Text(
                    text = "EcoVision",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 36.sp,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Smart Waste • Dynamic Pricing • Verified Loop",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EcoEmerald400,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(36.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Slate800.copy(alpha = 0.8f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EcoEmerald400)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentProfile != null) "Restoring session..." else "Connecting platform...",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate300
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "v1.0.6 • Circular Economy Platform",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }
        }
    }
}
