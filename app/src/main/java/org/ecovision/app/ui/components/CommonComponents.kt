package org.ecovision.app.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import org.ecovision.app.data.SessionManager
import org.ecovision.app.data.UserRole
import org.ecovision.app.theme.*

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

@Composable
fun EcoHeader(
    title: String,
    subtitle: String? = null,
    onProfileClick: () -> Unit,
    onWalletClick: () -> Unit
) {
    val profile by SessionManager.currentProfile.collectAsState()

    Surface(
        color = Slate900,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(EcoEmerald500, EcoEmerald600))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "EcoVision",
                            tint = Slate950,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Coins Counter Pill (Only for Household Users)
                    if (profile != null && (profile?.role == "user" || profile?.role == null)) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Slate800,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
                            modifier = Modifier
                                .clickable { onWalletClick() }
                                .padding(end = 10.dp)
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
                            modifier = Modifier.padding(end = 10.dp)
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

                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Slate800)
                            .border(1.dp, CardBorderBright, CircleShape)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Slate200,
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
    verifyButtonIcon: ImageVector = Icons.Default.Verified
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

