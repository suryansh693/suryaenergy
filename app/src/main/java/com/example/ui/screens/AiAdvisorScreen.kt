package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdvisorRecommendation
import com.example.model.ChatMessage
import com.example.model.PlantScenario
import com.example.model.PlantState
import com.example.model.RecommendationCategory
import com.example.ui.theme.HydroAlarmRed
import com.example.ui.theme.HydroAquaFlow
import com.example.ui.theme.HydroBlueSecondary
import com.example.ui.theme.HydroCyanPrimary
import com.example.ui.theme.HydroElectricYellow
import com.example.ui.theme.HydroNavyDark
import com.example.ui.theme.HydroNormalGreen
import com.example.ui.theme.HydroSurfaceCardDark
import com.example.ui.theme.HydroWarningAmber
import com.example.ui.theme.TextSecondaryDark

@Composable
fun AiAdvisorScreen(
    plantState: PlantState,
    recommendations: List<AdvisorRecommendation>,
    chatMessages: List<ChatMessage>,
    scenarios: List<PlantScenario>,
    isAiLoading: Boolean,
    onRefreshAi: () -> Unit,
    onLoadScenario: (PlantScenario) -> Unit,
    onAskAi: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var userQueryText by remember { mutableStateOf("") }

    val suggestedQuestions = listOf(
        "How does the MIV bypass equalize penstock pressure?",
        "What is Thoma cavitation criterion in Francis turbines?",
        "Why is generation stepped up to 230 kV for grid transmission?",
        "How does the governor droop maintain grid frequency?"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HydroNavyDark)
            .padding(horizontal = 14.dp)
            .testTag("ai_advisor_screen_list")
    ) {
        // AI Advisor Header Banner
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HydroSurfaceCardDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(HydroCyanPrimary.copy(alpha = 0.6f), HydroElectricYellow.copy(alpha = 0.6f))
                    )
                ),
                modifier = Modifier.fillMaxWidth().testTag("ai_advisor_header_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = HydroElectricYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SURYA ENERGY AI DISPATCH ADVISOR",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Real-time telemetry analysis for optimal dispatch revenue & asset longevity.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }

                    Button(
                        onClick = onRefreshAi,
                        enabled = !isAiLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = HydroCyanPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("refresh_ai_btn")
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = HydroNavyDark,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = HydroNavyDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Analyze", color = HydroNavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Operational Scenarios Carousel
        item {
            Text(
                text = "SIMULATION CHALLENGE SCENARIOS",
                style = MaterialTheme.typography.labelSmall,
                color = HydroCyanPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                scenarios.forEach { scenario ->
                    ScenarioCard(scenario = scenario, onSelect = { onLoadScenario(scenario) })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Live Recommendations
        item {
            Text(
                text = "ACTIVE AI RECOMMENDATIONS",
                style = MaterialTheme.typography.labelSmall,
                color = HydroCyanPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(recommendations.size) { index ->
            val rec = recommendations[index]
            RecommendationCard(recommendation = rec)
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Ask AI Hydro Engineer Section
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "ASK THE HYDROELECTRIC AI ENGINEER",
                style = MaterialTheme.typography.labelSmall,
                color = HydroCyanPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Suggested prompt pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedQuestions.forEach { question ->
                    Surface(
                        color = Color(0xFF07121E),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF243B53))),
                        modifier = Modifier.clickable { onAskAi(question) }
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.labelSmall,
                            color = HydroCyanPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Query Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userQueryText,
                    onValueChange = { userQueryText = it },
                    placeholder = { Text("Ask about plant physics, MIV, cavitation, frequency...", fontSize = 12.sp, color = TextSecondaryDark) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_query_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HydroCyanPrimary,
                        unfocusedBorderColor = Color(0xFF334E68),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = HydroSurfaceCardDark,
                        unfocusedContainerColor = HydroSurfaceCardDark
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (userQueryText.isNotBlank()) {
                            onAskAi(userQueryText.trim())
                            userQueryText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(HydroCyanPrimary)
                        .testTag("ai_send_query_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = HydroNavyDark
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Chat Message History
        items(chatMessages.size) { index ->
            val chat = chatMessages[index]
            ChatMessageItem(chat)
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ScenarioCard(scenario: PlantScenario, onSelect: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HydroSurfaceCardDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF243B53))),
        modifier = Modifier
            .width(220.dp)
            .clickable { onSelect() }
            .testTag("scenario_card_${scenario.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = HydroCyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = scenario.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = HydroElectricYellow
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = scenario.initialDescription,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF07121E),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Load Scenario",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = HydroCyanPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: AdvisorRecommendation) {
    val isUrgent = recommendation.urgencyLevel == "HIGH" || recommendation.urgencyLevel == "CRITICAL"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HydroSurfaceCardDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isUrgent) HydroWarningAmber.copy(alpha = 0.6f) else Color(0xFF1E3857)
            )
        ),
        modifier = Modifier.fillMaxWidth().testTag("recommendation_card_${recommendation.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (recommendation.category) {
                            RecommendationCategory.DISPATCH -> Icons.AutoMirrored.Filled.TrendingUp
                            RecommendationCategory.MAINTENANCE -> Icons.Default.Engineering
                            RecommendationCategory.GRID_STABILITY -> Icons.Default.ElectricBolt
                            RecommendationCategory.EFFICIENCY -> Icons.Default.Water
                        },
                        contentDescription = null,
                        tint = HydroCyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = recommendation.category.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HydroCyanPrimary
                    )
                }

                Surface(
                    color = if (isUrgent) HydroWarningAmber.copy(alpha = 0.2f) else Color(0xFF0E2238),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = recommendation.urgencyLevel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUrgent) HydroWarningAmber else Color(0xFF627D98),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = recommendation.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = recommendation.summary,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD9E2EC)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF07121E),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Technical Rationale & Engineering Basis:",
                        fontSize = 10.sp,
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = recommendation.technicalRationale,
                        fontSize = 11.sp,
                        color = Color(0xFFBCCCDC)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            recommendation.recommendedActions.forEach { action ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(HydroCyanPrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = action,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Estimated Impact: ${recommendation.estimatedFinancialImpact}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = HydroNormalGreen
            )
        }
    }
}

@Composable
private fun ChatMessageItem(chat: ChatMessage) {
    val isUser = chat.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 12.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) HydroBlueSecondary else HydroSurfaceCardDark
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = if (isUser) "Operator" else "AI Hydro Engineer",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) HydroElectricYellow else HydroCyanPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = chat.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }
    }
}
