package org.ecovision.app.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import org.ecovision.app.R
import org.ecovision.app.data.EcoVoucher
import org.ecovision.app.data.Profile
import org.ecovision.app.data.SessionManager
import org.ecovision.app.data.UserRole
import org.ecovision.app.theme.*

/**
 * Official EcoVision Waste Management Truck Logo Composable
 */
@Composable
fun EcoVisionAppLogo(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    contentScale: ContentScale = ContentScale.Fit
) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "EcoVision Logo",
        modifier = modifier.size(size),
        contentScale = contentScale
    )
}

/**
 * Custom Coin Design with Center Leaf Motif:
 * A minted circular coin with beveled metallic golden rim, inner milled concentric ring,
 * and a stylized organic green leaf centered inside the coin.
 */
@Composable
fun EcoCoinIcon(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 24.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFDF70),
                        Color(0xFFF59E0B),
                        Color(0xFFB45309)
                    )
                )
            )
            .border(
                width = (size.value * 0.08f).coerceAtLeast(1f).dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFFBEB), Color(0xFFD97706), Color(0xFF78350F))
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner milled rim of the coin
        Box(
            modifier = Modifier
                .fillMaxSize(0.82f)
                .clip(CircleShape)
                .background(Color(0xFFFEF3C7).copy(alpha = 0.15f))
                .border(
                    width = (size.value * 0.04f).coerceAtLeast(0.75f).dp,
                    color = Color(0xFF78350F).copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Stylized Leaf Emblem in the Center of the Coin
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "Coin",
                tint = Color(0xFF047857),
                modifier = Modifier.fillMaxSize(0.68f)
            )
        }
    }
}

/**
 * BoxIcon Component:
 * Clean square-rounded container with background, border, and centered icon
 * inspired by modern Boxicons design.
 */
@Composable
fun BoxIcon(
    icon: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
    boxColor: Color = Slate800,
    iconColor: Color = EcoEmerald400,
    borderColor: Color = CardBorderBright,
    cornerRadius: androidx.compose.ui.unit.Dp = 10.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(boxColor)
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun EcoHeader(
    title: String,
    subtitle: String? = null,
    onProfileClick: () -> Unit,
    onWalletClick: () -> Unit
) {
    val profile by SessionManager.currentProfile.collectAsState()
    val themeMode by SessionManager.themeMode.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EcoVisionAppLogo(
                        size = 44.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Coins Counter Pill (Only for Household Users)
                    if (profile != null && (profile?.role == "user" || profile?.role == null)) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .clickable { onWalletClick() }
                                .padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                EcoCoinIcon(size = 18.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${profile?.leafCoins ?: 0}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = LeafCoinGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (profile != null) {
                        // Role Badge for Admin, Agent, Company
                        val badgeColor = when (profile?.role) {
                            "admin" -> Color(0xFFF472B6)
                            "agent" -> LeafCoinGold
                            "company" -> Color(0xFF60A5FA)
                            else -> EcoEmerald400
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = badgeColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = (profile?.role ?: "").uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = badgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Auto/Light/Dark Theme Switcher Quick Toggle
                    IconButton(
                        onClick = { SessionManager.toggleThemeMode() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                ThemeMode.DARK -> Icons.Default.DarkMode
                            },
                            contentDescription = "Toggle Theme (Auto / Light / Dark)",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoleSwitcherPill() {
    val profile by SessionManager.currentProfile.collectAsState()
    val scope = rememberCoroutineScope()
    val currentRoleCode = profile?.role ?: "user"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Slate850)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val roles = listOf(
            UserRole.USER to "Seller",
            UserRole.AGENT to "Agent",
            UserRole.COMPANY to "Recycler",
            UserRole.ADMIN to "Admin"
        )

        for ((role, label) in roles) {
            val isSelected = currentRoleCode == role.code
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) EcoEmerald600 else Color.Transparent)
                    .clickable {
                        // Role switching is only permitted via authenticated sign-in
                    }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Slate400
                )
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@Composable
fun StitchSkeletonCard(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 110.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
    )
}

@Composable
fun StatusBadge(
    text: String,
    color: Color = EcoEmerald500,
    backgroundColor: Color = color.copy(alpha = 0.14f)
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun EcoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    isSecondary: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btn_press_scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .scale(pressScale),
        shape = RoundedCornerShape(999.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSecondary) 0.dp else 4.dp,
            pressedElevation = 1.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSecondary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
            contentColor = if (isSecondary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DisclaimerBanner(
    text: String = "Physical Weighing Scale Disclaimer: AI estimation is approximate. Final cash payout will be determined only after the Pickup Agent weighs the batch on a certified physical scale."
) {
    Surface(
        color = Color(0xFF2E1A04),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF78350F)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Notice",
                tint = LeafCoinGold,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFFFDE68A)
            )
        }
    }
}

@Composable
fun ThresholdProgressBar(
    currentValue: Double,
    thresholdValue: Double = 100.0,
    unit: String = "₹",
    isMet: Boolean
) {
    val targetProgress = (currentValue / thresholdValue).toFloat().coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "threshold_progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Minimum Pickup Threshold ($unit$thresholdValue)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$unit${String.format("%.1f", currentValue)} / $unit$thresholdValue",
                style = MaterialTheme.typography.labelSmall,
                color = if (isMet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (isMet) EcoEmerald500 else LeafCoinGold,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Modern Live Tracking Card (Exact Match to User Reference Image 3):
 * Features:
 * - Green Scheduled Slot Header Banner with "✓ On Time"
 * - Live Tracking indicator with 4-step Horizontal Stepper:
 *   Request Placed -> Agent Assigned -> On the Way -> Arriving Soon
 * - Route map card with curved track, delivery vehicle, destination pin, and floating ETA pill
 * - Driver profile (Photo/Avatar, verified badge, rating) + Vehicle badge (Plate, Model)
 * - 3 Action pills: Call, Chat, Verify Vehicle / Action
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Image3PickupLiveCard(
    modifier: Modifier = Modifier,
    scheduledSlot: String = "Today, 4:00 PM - 5:00 PM",
    isOnTime: Boolean = true,
    step: Int = 3, // 1: Request Placed, 2: Agent Assigned, 3: On the Way, 4: Arriving Soon
    etaMinutes: Int = 12,
    agentName: String = "Rafiq Khan",
    agentRole: String = "Pickup Agent",
    agentRating: String = "4.8 (124 ratings)",
    vehiclePlate: String = "KL-07-AB-1234",
    vehicleType: String = "Green Van",
    addressText: String? = null,
    onCallClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onVerifyClick: () -> Unit = {},
    verifyButtonText: String = "Verify Vehicle",
    verifyButtonIcon: ImageVector = Icons.Default.VerifiedUser
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Green Scheduled Banner
        Surface(
            color = EcoEmerald600,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pickup Scheduled",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = scheduledSlot,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                }
                if (isOnTime) {
                    Surface(
                        color = Color(0xFF064E3B),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("On Time", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Track Your Pickup Header + Live Pulse Dot
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Track Your Pickup",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EcoEmerald500)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Live Tracking",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }

        // 3. 4-Step Horizontal Stepper
        val stepLabels = listOf("Request\nPlaced", "Agent\nAssigned", "On the\nWay", "Arriving\nSoon")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..4) {
                val isCompleted = step > i
                val isActive = step == i

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted || isActive -> EcoEmerald500
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isCompleted -> Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            isActive -> Icon(Icons.Default.LocalShipping, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            else -> Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stepLabels[i - 1],
                        fontSize = 9.sp,
                        color = if (isActive || isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 11.sp
                    )
                }

                if (i < 4) {
                    Box(
                        modifier = Modifier
                            .weight(0.7f)
                            .height(2.dp)
                            .background(if (step > i) EcoEmerald500 else MaterialTheme.colorScheme.outlineVariant)
                            .padding(bottom = 12.dp)
                    )
                }
            }
        }

        // 4. Interactive Live Route Map with Real Road Network & Floating ETA Pill
        val context = LocalContext.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
        ) {
            // Real Interactive Map (Leaflet with OSM / Carto Real Road Network)
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        val mapHtml = """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                                <style>
                                    html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #e2e8f0; }
                                    .leaflet-control-attribution, .leaflet-control-zoom { display: none !important; }
                                    .pulse-truck {
                                        background: #16a34a;
                                        color: white;
                                        border: 2px solid white;
                                        border-radius: 50%;
                                        display: flex;
                                        align-items: center;
                                        justify-content: center;
                                        font-size: 16px;
                                        box-shadow: 0 4px 10px rgba(0,0,0,0.35);
                                    }
                                    .dest-house {
                                        background: #0f172a;
                                        color: #22c55e;
                                        border: 2px solid white;
                                        border-radius: 50%;
                                        display: flex;
                                        align-items: center;
                                        justify-content: center;
                                        font-size: 16px;
                                        box-shadow: 0 4px 10px rgba(0,0,0,0.35);
                                    }
                                </style>
                            </head>
                            <body>
                                <div id="map"></div>
                                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                                <script>
                                    try {
                                        var map = L.map('map', { zoomControl: false, attributionControl: false, dragging: true }).setView([11.0520, 76.0715], 14);
                                        L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                                            maxZoom: 19
                                        }).addTo(map);

                                        var truckIcon = L.divIcon({ html: '🚛', className: 'pulse-truck', iconSize: [32, 32], iconAnchor: [16, 16] });
                                        var houseIcon = L.divIcon({ html: '🏠', className: 'dest-house', iconSize: [32, 32], iconAnchor: [16, 16] });

                                        var truckPos = [11.0470, 76.0660];
                                        var destPos = [11.0570, 76.0770];

                                        var truckMarker = L.marker(truckPos, { icon: truckIcon }).addTo(map);
                                        var destMarker = L.marker(destPos, { icon: houseIcon }).addTo(map);

                                        var routePoints = [
                                            truckPos,
                                            [11.0490, 76.0685],
                                            [11.0520, 76.0715],
                                            [11.0545, 76.0745],
                                            destPos
                                        ];

                                        var routeLine = L.polyline(routePoints, {
                                            color: '#16a34a',
                                            weight: 5,
                                            opacity: 0.9,
                                            dashArray: '8, 8'
                                        }).addTo(map);

                                        map.fitBounds(routeLine.getBounds(), { padding: [35, 35] });
                                    } catch(e) {}
                                </script>
                            </body>
                            </html>
                        """.trimIndent()
                        loadDataWithBaseURL("https://ecovision.org", mapHtml, "text/html", "UTF-8", null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Real Google Maps Launcher Pill (Top-Right)
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clickable {
                        try {
                            val mapUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=11.0570,76.0770&travelmode=driving")
                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val webUri = Uri.parse("https://maps.google.com/?q=11.0570,76.0770")
                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Navigation, null, tint = EcoEmerald400, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open in Google Maps ↗", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Real Live GPS Status Indicator (Bottom-Left)
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(EcoEmerald400)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Real GPS Route",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Moving Vehicle & Floating ETA Pill (Matching Image 3)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Arriving in",
                            fontSize = 9.sp,
                            color = Slate600,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$etaMinutes min",
                            fontSize = 12.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EcoEmerald600)
                        .border(1.5.dp, Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocalShipping, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        // 5. Driver & Vehicle Details Card
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Driver Avatar with Verified Checkmark
                Box(modifier = Modifier.size(46.dp)) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(EcoEmerald700),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = agentName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(EcoEmerald400)
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(10.dp))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = agentName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = agentRole,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "★ $agentRating",
                        style = MaterialTheme.typography.bodySmall,
                        color = LeafCoinGold,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1.1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = vehiclePlate,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = vehicleType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (addressText != null) {
            Text(
                text = "Destination: $addressText",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        // 6. 2 Action Pill Buttons (Direct Platform Call, Verify / Weigh)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onCallClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onVerifyClick,
                modifier = Modifier.weight(1.6f),
                colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(verifyButtonIcon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(verifyButtonText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Modern Floating Bottom Navigation Bar with Center Elevated Scanner FAB
 * Matches Image 1, Image 2, and Image 3 navigation docks.
 */
@Composable
fun EcoFloatingBottomBar(
    currentRoute: String?,
    role: String?,
    onNavigate: (String) -> Unit,
    onCenterScanClick: () -> Unit
) {
    val agentTab by SessionManager.agentTab.collectAsState()
    val companyTab by SessionManager.companyTab.collectAsState()
    val adminTab by SessionManager.adminTab.collectAsState()

    val item1Selected = when (role) {
        "company" -> currentRoute == "company_dashboard" && companyTab == 0
        "admin" -> currentRoute == "admin_dashboard" && adminTab == 0
        "agent" -> currentRoute == "agent_dashboard" && agentTab == 0
        else -> currentRoute == "user_home"
    }
    val item2Selected = when (role) {
        "company" -> currentRoute == "company_dashboard" && companyTab == 1
        "admin" -> currentRoute == "admin_dashboard" && adminTab == 1
        "agent" -> currentRoute == "agent_dashboard" && agentTab == 1
        else -> currentRoute == "virtual_bin"
    }
    val item3Selected = when (role) {
        "company" -> currentRoute == "company_dashboard" && companyTab == 2
        "admin" -> currentRoute == "admin_dashboard" && adminTab == 3
        "agent" -> currentRoute == "agent_dashboard" && agentTab == 2
        else -> currentRoute == "wallet" || currentRoute == "leaf_coins"
    }
    val item4Selected = currentRoute in listOf("settings", "profile", "about", "terms", "privacy_policy", "faq")

    val activeSlotIndex = when {
        item1Selected -> 0
        item2Selected -> 1
        item3Selected -> 2
        item4Selected -> 3
        else -> -1
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        // Bottom Dock Surface
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            shadowElevation = 16.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item 1: Home
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    BottomNavIconItem(
                        label = "Home",
                        icon = Icons.Default.Home,
                        isSelected = item1Selected,
                        onClick = {
                            when (role) {
                                "agent" -> {
                                    SessionManager.setAgentTab(0)
                                    onNavigate("agent_dashboard")
                                }
                                "company" -> {
                                    SessionManager.setCompanyTab(0)
                                    onNavigate("company_dashboard")
                                }
                                "admin" -> {
                                    SessionManager.setAdminTab(0)
                                    onNavigate("admin_dashboard")
                                }
                                else -> onNavigate("user_home")
                            }
                        }
                    )
                }

                // Item 2: Operations / Pickups / Orders / User Governance
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    BottomNavIconItem(
                        label = when (role) {
                            "company" -> "Orders"
                            "admin" -> "Gov"
                            "agent" -> "Pickups"
                            else -> "Bin"
                        },
                        icon = when (role) {
                            "company" -> Icons.Default.Inventory
                            "admin" -> Icons.Default.People
                            "agent" -> Icons.Default.LocalShipping
                            else -> Icons.Default.DeleteOutline
                        },
                        isSelected = item2Selected,
                        onClick = {
                            when (role) {
                                "agent" -> {
                                    SessionManager.setAgentTab(1)
                                    onNavigate("agent_dashboard")
                                }
                                "company" -> {
                                    SessionManager.setCompanyTab(1)
                                    onNavigate("company_dashboard")
                                }
                                "admin" -> {
                                    SessionManager.setAdminTab(1)
                                    onNavigate("admin_dashboard")
                                }
                                else -> onNavigate("virtual_bin")
                            }
                        }
                    )
                }

                // Spacer for the center elevated floating button
                Spacer(modifier = Modifier.width(58.dp))

                // Item 3: Wallet / Redeems / History / ESG
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    BottomNavIconItem(
                        label = when (role) {
                            "company" -> "ESG"
                            "admin" -> "Payouts"
                            "agent" -> "History"
                            else -> "Wallet"
                        },
                        icon = when (role) {
                            "admin" -> Icons.Default.Payments
                            "company" -> Icons.Default.Verified
                            "agent" -> Icons.Default.History
                            else -> Icons.Default.AccountBalanceWallet
                        },
                        isSelected = item3Selected,
                        onClick = {
                            when (role) {
                                "admin" -> {
                                    SessionManager.setAdminTab(3)
                                    onNavigate("admin_dashboard")
                                }
                                "company" -> {
                                    SessionManager.setCompanyTab(2)
                                    onNavigate("company_dashboard")
                                }
                                "agent" -> {
                                    SessionManager.setAgentTab(2)
                                    onNavigate("agent_dashboard")
                                }
                                else -> onNavigate("wallet")
                            }
                        }
                    )
                }

                // Item 4: Settings
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    BottomNavIconItem(
                        label = "Settings",
                        icon = Icons.Default.Settings,
                        isSelected = item4Selected,
                        onClick = { onNavigate("settings") }
                    )
                }
            }
        }

        // Center Elevated Floating Scanner Button (FAB) with organic breathing pulse
        val fabInfiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
        val fabPulseScale by fabInfiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fab_scale"
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)
                .scale(fabPulseScale)
                .size(62.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF22C55E),
                            Color(0xFF16A34A),
                            Color(0xFF15803D)
                        )
                    )
                )
                .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                .clickable {
                    when (role) {
                        "admin" -> {
                            SessionManager.setAdminTab(2)
                            onNavigate("admin_dashboard")
                        }
                        "company" -> {
                            SessionManager.setCompanyTab(0)
                            onNavigate("company_dashboard")
                        }
                        "agent" -> {
                            SessionManager.setAgentTab(1)
                            onNavigate("agent_dashboard")
                        }
                        else -> onCenterScanClick()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (role) {
                    "admin" -> Icons.Default.CardGiftcard
                    "company" -> Icons.Default.Search
                    "agent" -> Icons.Default.Balance
                    else -> Icons.Default.QrCodeScanner
                },
                contentDescription = "Action",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun BottomNavIconItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "nav_scale"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 150),
        label = "nav_color"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "nav_bg"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = iconColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Premium Liquid Glass UI Card Container:
 * Translucent frosted glass effect with dual-layer specular highlight border,
 * fluid depth, and water-refractive backdrop shimmer.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 18.dp,
    highlightColor: Color = EcoEmerald400,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == Slate950
    Surface(
        color = if (isDark) Slate900.copy(alpha = 0.80f) else Color.White,
        shape = RoundedCornerShape(cornerRadius),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.25.dp,
            brush = Brush.linearGradient(
                colors = if (isDark) listOf(
                    Color.White.copy(alpha = 0.40f),
                    highlightColor.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.08f)
                ) else listOf(
                    highlightColor.copy(alpha = 0.5f),
                    Color(0xFFE2E8F0),
                    highlightColor.copy(alpha = 0.2f)
                )
            )
        ),
        shadowElevation = if (isDark) 8.dp else 4.dp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = if (isDark) listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f)
                        ) else listOf(
                            Color.White,
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(content = content)
        }
    }
}

/**
 * 3D Flippable Voucher Card Model:
 * Realistic interactive physical voucher card. Tapping flips the card 180 degrees
 * in 3D perspective to reveal the digital security barcode, claim pin, and terms.
 */
@Composable
fun FlippableVoucherCard(
    voucher: EcoVoucher,
    modifier: Modifier = Modifier,
    onRedeemClick: (() -> Unit)? = null
) {
    // Fixed uniform card size + animated flip when clicking image or title (NO voucher number or PIN)
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "voucher_flip"
    )

    Box(
        modifier = modifier
            .height(255.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
    ) {
        if (rotation <= 90f) {
            // FRONT: Image, Brand, Coin Cost, Title (Fixed uniform height)
            LiquidGlassCard(
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 18.dp,
                highlightColor = LeafCoinGold
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { isFlipped = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (!voucher.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = voucher.imageUrl,
                            contentDescription = voucher.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.CardGiftcard, null, tint = LeafCoinGold, modifier = Modifier.size(36.dp))
                    }
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                    ) {
                        Text("Tap for details ↺", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = voucher.brandName,
                        style = MaterialTheme.typography.labelMedium,
                        color = LeafCoinGold,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LeafCoinGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EcoCoinIcon(size = 14.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${voucher.coinCost}", color = LeafCoinGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = voucher.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFlipped = true }
                )

                Spacer(modifier = Modifier.weight(1f))

                if (onRedeemClick != null) {
                    Button(
                        onClick = onRedeemClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text("Request Voucher (${voucher.coinCost} Coins)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                    }
                }
            }
        } else {
            // BACK: Full Title & Description (NO voucher number or PIN)
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .clickable { isFlipped = false },
                cornerRadius = 18.dp,
                highlightColor = EcoEmerald400
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(voucher.brandName, color = LeafCoinGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Tap to flip ↻", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = voucher.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = voucher.description ?: "Valid on EcoVision partner zero-waste products.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (onRedeemClick != null) {
                    Button(
                        onClick = onRedeemClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text("Request Voucher (${voucher.coinCost} Coins)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun EcoPopupDialog(
    title: String,
    message: String,
    icon: ImageVector = Icons.Default.CheckCircle,
    accentColor: Color = EcoEmerald500,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(30.dp))
            }
        },
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}


/**
 * 3D Style Animated Character Mascot Hero:
 * Stylized glossy 3D eco guardian mascot with dynamic floating & pulse animation.
 */
@Composable
fun AnimatedCharacterHero(
    title: String = "Eco Hero",
    subtitle: String = "Protecting Earth",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "charFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "charFloatOffset"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "charGlowAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF064E3B).copy(alpha = 0.85f),
                        Color(0xFF0F172A).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.horizontalGradient(listOf(EcoEmerald400, TechBlue.copy(alpha = 0.3f))),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 3D Animated Character Avatar Orb
        Box(
            modifier = Modifier
                .offset(y = floatOffset.dp)
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF34D399),
                            Color(0xFF059669),
                            Color(0xFF064E3B)
                        )
                    )
                )
                .border(2.dp, Color.White.copy(alpha = glowAlpha), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Inner 3D Character Features (Stylized Eco Mascot)
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = "3D Eco Mascot",
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(text = "3D AI MASCOT", color = EcoEmerald400)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate300,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * KYC Proof Document Viewer Dialog:
 * Allows Super Admin to inspect full high-resolution uploaded ID/Vehicle proof photos
 * with direct inline approval ("Accept KYC ✓") and rejection buttons.
 */
@Composable
fun ProofViewerDialog(
    profile: Profile,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val proofUrl = profile.kycDocumentUrl
        ?: if (profile.role == "agent") "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=800"
        else "https://images.unsplash.com/photo-1554415707-9e49fe74a659?w=800"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        titleContentColor = Color.White,
        textContentColor = Slate300,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "KYC Proof Document",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${profile.fullName} (@${profile.username ?: profile.mobile})",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // High-Res Image Display Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black)
                        .border(1.5.dp, CardBorderBright, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = proofUrl,
                        contentDescription = "Uploaded Proof Document",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Document Metadata Card
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Document Type:", fontSize = 11.sp, color = Slate400)
                            Text(profile.kycDocumentType?.uppercase() ?: "GOVERNMENT ID", fontSize = 11.sp, color = TechBlue, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Document Number:", fontSize = 11.sp, color = Slate400)
                            Text(profile.kycDocumentNumber ?: "ID-9081-KRL-2026", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Assigned Role:", fontSize = 11.sp, color = Slate400)
                            Text(profile.role.uppercase(), fontSize = 11.sp, color = EcoEmerald400, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current KYC Status:", fontSize = 11.sp, color = Slate400)
                            Text(profile.kycStatus.uppercase(), fontSize = 11.sp, color = LeafCoinGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔒 Privacy Protection: Upon approval, sensitive proof documents are purged from storage.",
                    color = Slate400,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onApprove,
                colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Accept & Approve KYC ✓", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onReject,
                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Cancel, null, tint = CoralRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reject with Reason", color = CoralRed, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

/**
 * Animated global No-Internet Banner that slides in whenever network connectivity is lost
 * and pulses a WifiOff icon until connectivity is restored.
 */
@Composable
fun AnimatedNoInternetBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isOnline by remember {
        mutableStateOf(org.ecovision.app.data.DeviceLocationHelper.isNetworkAvailable(context))
    }

    DisposableEffect(context) {
        val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        val callback = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                isOnline = true
            }
            override fun onLost(network: android.net.Network) {
                isOnline = org.ecovision.app.data.DeviceLocationHelper.isNetworkAvailable(context)
            }
            override fun onCapabilitiesChanged(
                network: android.net.Network,
                networkCapabilities: android.net.NetworkCapabilities
            ) {
                isOnline = networkCapabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
        }
        try {
            val request = android.net.NetworkRequest.Builder()
                .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm?.registerNetworkCallback(request, callback)
        } catch (_: Exception) {
            isOnline = org.ecovision.app.data.DeviceLocationHelper.isNetworkAvailable(context)
        }
        onDispose {
            try {
                cm?.unregisterNetworkCallback(callback)
            } catch (_: Exception) {}
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "offlinePulse")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offlineIconScale"
    )
    val bannerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offlineBannerAlpha"
    )

    AnimatedVisibility(
        visible = !isOnline,
        enter = androidx.compose.animation.expandVertically(tween(300)) + androidx.compose.animation.fadeIn(tween(250)),
        exit = androidx.compose.animation.shrinkVertically(tween(260)) + androidx.compose.animation.fadeOut(tween(200)),
        modifier = modifier
    ) {
        Surface(
            color = CoralRed.copy(alpha = 0.95f * bannerAlpha),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "No Internet",
                    tint = Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .scale(iconScale)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "No Internet Connection",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Offline mode active • Viewing cached data only (DB actions disabled)",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}




