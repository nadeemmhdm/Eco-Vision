package org.ecovision.app.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.EcoVisionAppLogo

private data class SettingsMenuItem(
    val title: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit
)

/**
 * Clean, flat list-based Settings Screen (no section models):
 * Profile, About, Terms and Conditions, Privacy Policy, FAQ — all clickable.
 */
@Composable
fun SettingsScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToFaq: () -> Unit
) {
    val menuItems = listOf(
        SettingsMenuItem("Profile", Icons.Default.Person, EcoEmerald500, onNavigateToProfile),
        SettingsMenuItem("About", Icons.Default.Info, TechBlue, onNavigateToAbout),
        SettingsMenuItem("Terms and Conditions", Icons.Default.Description, LeafCoinGold, onNavigateToTerms),
        SettingsMenuItem("Privacy Policy", Icons.Default.PrivacyTip, Color(0xFF8B5CF6), onNavigateToPrivacy),
        SettingsMenuItem("FAQ", Icons.Default.HelpOutline, EcoEmerald400, onNavigateToFaq)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                EcoVisionAppLogo(size = 38.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(menuItems) { item ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { item.onClick() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(item.accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = item.accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoPageScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    InfoPageScaffold(title = "About EcoVision", onBack = onBack) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EcoVisionAppLogo(size = 56.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text("EcoVision", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                Text("Version 1.0.11 • Circular Economy Platform", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        InfoBodyCard(
            title = "Our Mission",
            body = "EcoVision connects households, verified doorstep pickup agents, and certified B2B recycling companies across Kerala into a transparent circular economy loop. Using AI waste classification, weight-based dynamic pricing, and instant coin rewards, we make responsible recycling effortless and rewarding."
        )
        InfoBodyCard(
            title = "How It Works",
            body = "1. Scan waste with AI Vision to check recyclability, estimated weight, and live price.\n2. Add recyclable items to your Virtual Bin and schedule doorstep collection.\n3. Verify your assigned agent's vehicle & weigh batch on a certified scale.\n4. Earn Coins & cash payouts via UPI or claim partner discount vouchers."
        )
        InfoBodyCard(
            title = "Service Coverage",
            body = "Supporting all 14 districts of Kerala with real-time GPS pickup routing, digital godown manifests, and ESG carbon offset tracking."
        )
    }
}

@Composable
fun TermsScreen(onBack: () -> Unit) {
    InfoPageScaffold(title = "Terms and Conditions", onBack = onBack) {
        InfoBodyCard(
            title = "1. Platform Eligibility & KYC",
            body = "Users, Pickup Agents, and Buyer Companies must provide accurate registration details and valid KYC documents (Aadhaar / Vehicle RC / GSTIN) before performing verified payouts or bulk orders."
        )
        InfoBodyCard(
            title = "2. AI Estimation vs Certified Scale",
            body = "AI Waste Scanner weight and price estimates are indicative only. Final cash payouts and Coins are calculated strictly from the certified physical weighing scale reading recorded by the Pickup Agent at your doorstep."
        )
        InfoBodyCard(
            title = "3. Recyclable Segregation",
            body = "Non-recyclable, wet, or hazardous waste must not be placed into the Recyclable Bin. Agents reserve the right to reject contaminated batches."
        )
        InfoBodyCard(
            title = "4. Coin & Cash Redemptions",
            body = "Cash redemptions via UPI require a minimum balance of 1,000 Coins (₹100.00) and an active internet connection. Voucher redemptions are processed via the Admin fulfillment queue."
        )
        InfoBodyCard(
            title = "5. Account Deletion Policy",
            body = "Accounts scheduled for deletion remain in a 30-day inactivity grace period and are permanently removed only after 30 days of inactivity."
        )
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    InfoPageScaffold(title = "Privacy Policy", onBack = onBack) {
        InfoBodyCard(
            title = "1. Information We Collect",
            body = "We collect your name, mobile number, email, Kerala district, pickup address, device diagnostics, and optional location coordinates solely to dispatch doorstep collection agents and prevent fraud."
        )
        InfoBodyCard(
            title = "2. KYC Document Protection",
            body = "Uploaded KYC documents are used strictly for identity verification by Super Admins. Once KYC is approved, raw identity proof images are purged from active storage for your privacy."
        )
        InfoBodyCard(
            title = "3. Camera & Media Access",
            body = "Camera and gallery permissions are used only when you scan waste items, capture marketplace craft photos, or upload KYC/profile images."
        )
        InfoBodyCard(
            title = "4. Two-Factor Security (2FA)",
            body = "EcoVision supports RFC 6238 TOTP authenticator protection to keep your wallet, Coins, and payout details secure."
        )
        InfoBodyCard(
            title = "5. Offline Caching",
            body = "Vouchers, waste categories, and profile details are cached locally on your device for offline viewing. Financial redemptions require a live online connection."
        )
    }
}

@Composable
fun FaqScreen(onBack: () -> Unit) {
    val faqs = listOf(
        "How do I earn Coins and Cash?" to "Scan recyclable waste, add it to your Virtual Bin, and request a doorstep pickup. Once the Pickup Agent weighs and verifies your waste on a certified scale, you earn cash and bonus Coins.",
        "Why is 'Add to Recyclable Bin' hidden for some items?" to "If the AI Scanner identifies an item as non-recyclable (such as Styrofoam or soiled waste), the Recyclable Bin button is hidden to prevent contamination. You can still tap 'Upscale This Product' for DIY reuse ideas.",
        "What is the minimum cash withdrawal?" to "The minimum UPI cash redemption is ₹100.00 (1,000 Coins at 10 Coins = ₹1). You can also redeem Coins for partner shopping vouchers.",
        "Can I redeem vouchers offline?" to "You can browse cached vouchers offline, but redeeming vouchers or cash requires an active internet connection to verify balances.",
        "How does 2FA Authenticator work?" to "Open Settings → Profile → Security & 2FA, scan the generated QR code in Google Authenticator or Authy, and verify with the 6-digit code. Future logins will require your 6-digit code.",
        "How do I share my referral code?" to "Go to Settings → Profile, scroll to the Referral section, and tap Share Referral. It sends the EcoVision app logo image along with your invite message and referral code."
    )

    InfoPageScaffold(title = "Frequently Asked Questions", onBack = onBack) {
        faqs.forEach { (question, answer) ->
            var expanded by remember { mutableStateOf(false) }
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { expanded = !expanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        Text(
                            text = answer,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBodyCard(title: String, body: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp
            )
        }
    }
}
