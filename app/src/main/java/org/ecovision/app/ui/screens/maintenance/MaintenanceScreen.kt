package org.ecovision.app.ui.screens.maintenance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ecovision.app.data.SessionManager
import org.ecovision.app.data.SupabaseClient
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.EcoButton
import org.ecovision.app.ui.components.GlassCard

@Composable
fun MaintenanceScreen(onRecovery: () -> Unit) {
    val settings by SessionManager.platformSettings.collectAsState()
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(LeafCoinGold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Construction,
                        contentDescription = "Maintenance",
                        tint = LeafCoinGold,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Platform Maintenance",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = settings.maintenanceNotice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate300,
                    textAlign = TextAlign.Center
                )

                if (settings.estimatedRecovery != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Estimated Recovery: ${settings.estimatedRecovery}",
                            fontSize = 12.sp,
                            color = LeafCoinGold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                EcoButton(
                    text = if (isChecking) "Checking..." else "Refresh Status",
                    icon = Icons.Default.Refresh,
                    onClick = {
                        scope.launch {
                            isChecking = true
                            val fresh = SupabaseClient.getPlatformSettings()
                            if (fresh != null) {
                                SessionManager.updateSettings(fresh)
                                if (!fresh.maintenanceMode) {
                                    onRecovery()
                                }
                            }
                            isChecking = false
                        }
                    }
                )
            }
        }
    }
}
