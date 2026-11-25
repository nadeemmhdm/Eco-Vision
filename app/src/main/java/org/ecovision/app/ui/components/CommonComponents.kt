package org.ecovision.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
                        scope.launch { SessionManager.switchDemoRole(role) }
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
    backgroundColor: Color = Slate850,
    borderColor: Color = CardBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

@Composable
fun StatusBadge(
    text: String,
    color: Color = EcoEmerald500,
    backgroundColor: Color = color.copy(alpha = 0.15f)
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
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
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSecondary) Slate800 else EcoEmerald500,
            contentColor = if (isSecondary) Slate200 else Slate950,
            disabledContainerColor = Slate800,
            disabledContentColor = Slate600
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
    val progress = (currentValue / thresholdValue).toFloat().coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Minimum Pickup Threshold ($unit$thresholdValue)",
                style = MaterialTheme.typography.labelSmall,
                color = Slate400
            )
            Text(
                text = "$unit${String.format("%.1f", currentValue)} / $unit$thresholdValue",
                style = MaterialTheme.typography.labelSmall,
                color = if (isMet) EcoEmerald400 else Slate400,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (isMet) EcoEmerald500 else LeafCoinGold,
            trackColor = Slate800
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
            .background(Slate850)
            .border(1.dp, CardBorderBright, RoundedCornerShape(20.dp))
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
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EcoEmerald400)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Live Tracking",
                    color = EcoEmerald400,
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
                val isPending = step < i

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
                                    else -> Slate700
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isCompleted -> Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            isActive -> Icon(Icons.Default.LocalShipping, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            else -> Icon(Icons.Default.Home, null, tint = Slate400, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stepLabels[i - 1],
                        fontSize = 9.sp,
                        color = if (isActive || isCompleted) Color.White else Slate500,
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
                            .background(if (step > i) EcoEmerald500 else Slate700)
                            .padding(bottom = 12.dp)
                    )
                }
            }
        }

        // 4. Map Canvas with Route & Floating ETA Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
        ) {
            // Subtle Grid & Road Curve Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw dotted road path
                val path = Path().apply {
                    moveTo(w * 0.15f, h * 0.75f)
                    cubicTo(w * 0.35f, h * 0.75f, w * 0.45f, h * 0.45f, w * 0.70f, h * 0.48f)
                    lineTo(w * 0.85f, h * 0.30f)
                }

                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.3f),
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )

                // Start dot
                drawCircle(color = Color(0xFF10B981), radius = 8f, center = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.75f))
                // End dot (Destination)
                drawCircle(color = Color(0xFF059669), radius = 10f, center = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.30f))
            }

            // Destination Pin Icon
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 26.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(EcoEmerald600),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }

            // Moving Vehicle & Floating ETA Pill
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(end = 20.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
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
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EcoEmerald600),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocalShipping, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        // 5. Driver & Vehicle Details Card
        Surface(
            color = Slate800,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
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
                            .border(1.dp, Slate800, CircleShape)
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
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = agentRole,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
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
                        .background(CardBorderBright)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1.1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, null, tint = EcoEmerald400, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = vehiclePlate,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = vehicleType,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (addressText != null) {
            Text(
                text = "Destination: $addressText",
                color = Slate300,
                fontSize = 11.sp
            )
        }

        // 6. 3 Action Pill Buttons (Call, Chat, Action)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCallClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(Icons.Default.Phone, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onChatClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(Icons.Default.Chat, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Chat", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onVerifyClick,
                modifier = Modifier.weight(1.4f),
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
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item 1: Home / Primary Desk
                BottomNavIconItem(
                    label = when (role) {
                        "company" -> "Godown"
                        "admin" -> "KYC"
                        "agent" -> "Desk"
                        else -> "Home"
                    },
                    icon = when (role) {
                        "company" -> Icons.Default.Storefront
                        "admin" -> Icons.Default.VerifiedUser
                        "agent" -> Icons.Default.Home
                        else -> Icons.Default.Home
                    },
                    isSelected = when (role) {
                        "company" -> currentRoute == "company_dashboard" && SessionManager.companyTab.value == 0
                        "admin" -> currentRoute == "admin_dashboard" && SessionManager.adminTab.value == 0
                        "agent" -> currentRoute == "agent_dashboard" && SessionManager.agentTab.value == 0
                        else -> currentRoute == "user_home"
                    },
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

                // Item 2: Operations / Pickups / Orders / User Governance
                BottomNavIconItem(
                    label = when (role) {
                        "company" -> "Orders"
                        "admin" -> "Gov"
                        "agent" -> "Pickups"
                        else -> "Virtual Bin"
                    },
                    icon = when (role) {
                        "company" -> Icons.Default.Inventory
                        "admin" -> Icons.Default.People
                        "agent" -> Icons.Default.LocalShipping
                        else -> Icons.Default.DeleteOutline
                    },
                    isSelected = when (role) {
                        "company" -> currentRoute == "company_dashboard" && SessionManager.companyTab.value == 1
                        "admin" -> currentRoute == "admin_dashboard" && SessionManager.adminTab.value == 1
                        "agent" -> currentRoute == "agent_dashboard" && SessionManager.agentTab.value == 0
                        else -> currentRoute == "virtual_bin"
                    },
                    onClick = {
                        when (role) {
                            "agent" -> {
                                SessionManager.setAgentTab(0)
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

                // Spacer for the center elevated floating button
                Spacer(modifier = Modifier.width(56.dp))

                // Item 4: Wallet / Redeems / Vouchers / ESG
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
                    isSelected = when (role) {
                        "company" -> currentRoute == "company_dashboard" && SessionManager.companyTab.value == 2
                        "admin" -> currentRoute == "admin_dashboard" && SessionManager.adminTab.value == 3
                        "agent" -> currentRoute == "agent_dashboard" && SessionManager.agentTab.value == 1
                        else -> currentRoute == "wallet" || currentRoute == "leaf_coins"
                    },
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
                                SessionManager.setAgentTab(1)
                                onNavigate("agent_dashboard")
                            }
                            else -> onNavigate("wallet")
                        }
                    }
                )

                // Item 5: Profile
                BottomNavIconItem(
                    label = "Profile",
                    icon = Icons.Default.Person,
                    isSelected = currentRoute == "profile",
                    onClick = { onNavigate("profile") }
                )
            }
        }

        // Center Elevated Floating Scanner Button (FAB)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)
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
                    if (role == "admin") {
                        SessionManager.setAdminTab(2)
                        onNavigate("admin_dashboard")
                    } else if (role == "company") {
                        SessionManager.setCompanyTab(0)
                        onNavigate("company_dashboard")
                    } else {
                        onCenterScanClick()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (role) {
                    "admin" -> Icons.Default.CardGiftcard
                    "company" -> Icons.Default.Search
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
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
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
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "voucherCardFlip"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .clickable { isFlipped = !isFlipped }
    ) {
        if (rotation <= 90f) {
            // FRONT OF THE VOUCHER CARD
            LiquidGlassCard(
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 18.dp,
                highlightColor = LeafCoinGold
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BoxIcon(
                            icon = Icons.Default.CardGiftcard,
                            boxColor = LeafCoinGold.copy(alpha = 0.15f),
                            iconColor = LeafCoinGold,
                            size = 36.dp,
                            iconSize = 20.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = voucher.brandName,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "OFFICIAL PARTNER VOUCHER",
                                fontSize = 9.sp,
                                color = LeafCoinGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Coin Cost Badge with Center Leaf Motif
                    Surface(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LeafCoinGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EcoCoinIcon(size = 18.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${voucher.coinCost} Coins",
                                color = LeafCoinGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Voucher Title & Value
                Text(
                    text = voucher.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Worth ₹${voucher.discountValue.toInt()} Cash Discount on Zero-Waste Essentials",
                    color = EcoEmerald400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tap to flip prompt chip
                    Surface(
                        color = Slate800.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FlipCameraAndroid, null, tint = Slate300, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tap to Flip Barcode ↺", fontSize = 10.sp, color = Slate300, fontWeight = FontWeight.Medium)
                        }
                    }

                    if (onRedeemClick != null) {
                        Button(
                            onClick = onRedeemClick,
                            colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald600),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Claim Voucher", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        } else {
            // BACK OF THE VOUCHER CARD (Mirrored 180 degrees so text reads properly)
            Surface(
                color = Slate950,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.25.dp, TechBlue.copy(alpha = 0.5f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SECURITY AUTHENTICATED",
                            color = TechBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        StatusBadge(text = "VALID 6 MONTHS", color = EcoEmerald400)
                    }

                    // Barcode Canvas Simulation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            var x = 0f
                            val strokeWidths = listOf(2f, 4f, 1f, 3f, 5f, 2f, 1f, 4f, 2f, 3f, 6f, 2f, 1f, 3f, 5f)
                            var idx = 0
                            while (x < w - 8f) {
                                val barW = strokeWidths[idx % strokeWidths.size]
                                drawLine(
                                    color = Color.Black,
                                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                                    end = androidx.compose.ui.geometry.Offset(x, h),
                                    strokeWidth = barW
                                )
                                x += barW + 3f
                                idx++
                            }
                        }
                    }

                    // Security Code
                    Text(
                        text = "VOUCHER PIN: ECO-${voucher.id.take(8).uppercase()}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = voucher.description ?: "Valid on zero-waste partner items.",
                        color = Slate400,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("10 Coins = ₹1 Value", fontSize = 10.sp, color = LeafCoinGold)
                        Text("Tap to Flip Back ↻", fontSize = 10.sp, color = Slate400)
                    }
                }
            }
        }
    }
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



