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
import kotlinx.coroutines.launch
import org.ecovision.app.data.SessionManager
import org.ecovision.app.data.UserRole
import org.ecovision.app.theme.*

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
                    // Leaf Coin Counter Pill
                    if (profile != null) {
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
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = "Leaf Coins",
                                    tint = LeafCoinGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${profile?.leafCoins ?: 0}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = LeafCoinGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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

            Spacer(modifier = Modifier.height(10.dp))
            RoleSwitcherPill()
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
