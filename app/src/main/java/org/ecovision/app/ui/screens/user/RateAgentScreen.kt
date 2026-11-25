package org.ecovision.app.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ecovision.app.data.*
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.EcoButton
import org.ecovision.app.ui.components.GlassCard
import org.ecovision.app.ui.components.StatusBadge
import java.util.UUID

@Composable
fun RateAgentScreen(
    requestId: String,
    onSubmitted: () -> Unit,
    onBack: () -> Unit
) {
    val profile by SessionManager.currentProfile.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedRating by remember { mutableIntStateOf(5) }
    var reviewComments by remember { mutableStateOf("") }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("Weight tampering") }
    var reportDetails by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rate Pickup Agent",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 12-Hour Exclusive Rating Window Banner
            Surface(
                color = Slate800,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Timer, null, tint = LeafCoinGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "12-Hour Exclusive Rating Window",
                            style = MaterialTheme.typography.labelLarge,
                            color = LeafCoinGold
                        )
                        Text(
                            text = "Only households who completed a verified pickup with this agent can rate (prevents fake reviews). Window closes in 11h 42m.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            GlassCard {
                Text(
                    text = "Agent: Rahul Kumar (KL-07-CD-5678)",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Star Rating Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..5) {
                        val isFilled = i <= selectedRating
                        Icon(
                            imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "$i stars",
                            tint = if (isFilled) LeafCoinGold else Slate600,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { selectedRating = i }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = reviewComments,
                    onValueChange = { reviewComments = it },
                    label = { Text("Public Feedback Comments") },
                    placeholder = { Text("Punctual doorstep arrival, fair physical scale weighing...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoEmerald500,
                        unfocusedBorderColor = CardBorderBright
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                EcoButton(
                    text = "Submit Verified Review",
                    icon = Icons.Default.Send,
                    onClick = {
                        scope.launch {
                            if (profile != null) {
                                SupabaseClient.submitReview(
                                    AgentReview(
                                        id = UUID.randomUUID().toString(),
                                        pickupRequestId = requestId,
                                        userId = profile!!.id,
                                        agentId = "00000000-0000-0000-0000-000000000003",
                                        rating = selectedRating,
                                        reviewText = reviewComments
                                    )
                                )
                                statusMessage = "Review submitted and published on Agent's profile!"
                                onSubmitted()
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Report Agent Option
                TextButton(
                    onClick = { showReportDialog = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.Flag, null, tint = CoralRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Report Agent Misconduct / Scale Tampering", color = CoralRed, fontSize = 12.sp)
                }
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                StatusBadge(text = statusMessage!!, color = EcoEmerald400)
            }
        }

        // Report Agent Modal Dialog
        if (showReportDialog) {
            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                containerColor = Slate850,
                titleContentColor = Color.White,
                textContentColor = Slate300,
                title = { Text("🚩 Report Pickup Agent") },
                text = {
                    Column {
                        Text("This alert is dispatched immediately to the Admin Dispute Desk.")
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = reportReason,
                            onValueChange = { reportReason = it },
                            label = { Text("Reason for Report") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reportDetails,
                            onValueChange = { reportDetails = it },
                            label = { Text("Specific Details & Evidence") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                        onClick = {
                            showReportDialog = false
                            scope.launch {
                                if (profile != null) {
                                    SupabaseClient.submitReport(
                                        AgentReport(
                                            id = UUID.randomUUID().toString(),
                                            pickupRequestId = requestId,
                                            userId = profile!!.id,
                                            agentId = "00000000-0000-0000-0000-000000000003",
                                            reason = reportReason,
                                            details = reportDetails
                                        )
                                    )
                                    statusMessage = "Report filed with Admin Dispute Desk."
                                }
                            }
                        }
                    ) {
                        Text("Submit Report", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReportDialog = false }) {
                        Text("Cancel", color = Slate400)
                    }
                }
            )
        }
    }
}
