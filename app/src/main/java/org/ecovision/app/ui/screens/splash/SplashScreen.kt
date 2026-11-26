package org.ecovision.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ecovision.app.data.Profile
import org.ecovision.app.data.SessionManager
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.EcoVisionAppLogo

@Composable
fun SplashScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToHome: (Profile) -> Unit
) {
    val isInitialized by SessionManager.isInitialized.collectAsState()
    val currentProfile by SessionManager.currentProfile.collectAsState()
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

    LaunchedEffect(isInitialized, currentProfile) {
        if (!hasNavigated && isInitialized) {
            hasNavigated = true
            val profile = currentProfile
            if (profile != null) {
                onNavigateToHome(profile)
            } else {
                onNavigateToAuth()
            }
        }
    }

    // Branded Splash Loading Screen
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
                // Inner Official Logo Container
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.5.dp, EcoEmerald400.copy(alpha = 0.6f), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    EcoVisionAppLogo(size = 80.dp)
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
            val context = androidx.compose.ui.platform.LocalContext.current
            val versionName = remember(context) {
                try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.15"
                } catch (_: Exception) {
                    "1.0.15"
                }
            }
            Text(
                text = "v$versionName • Circular Economy Platform",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
            )
        }
    }
}
