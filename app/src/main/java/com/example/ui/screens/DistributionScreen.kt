package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GridFeeder
import com.example.model.PlantState
import com.example.ui.theme.HydroAlarmRed
import com.example.ui.theme.HydroBlueSecondary
import com.example.ui.theme.HydroCyanPrimary
import com.example.ui.theme.HydroElectricYellow
import com.example.ui.theme.HydroNavyDark
import com.example.ui.theme.HydroNormalGreen
import com.example.ui.theme.HydroSurfaceCardDark
import com.example.ui.theme.HydroWarningAmber
import com.example.ui.theme.TextSecondaryDark
import kotlin.math.abs

@Composable
fun DistributionScreen(
    plantState: PlantState,
    onToggleFeeder: (String) -> Unit,
    onUpdateFeederAllocation: (String, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HydroNavyDark)
            .padding(horizontal = 14.dp)
            .testTag("distribution_screen_list")
    ) {
        // Revenue & Commercial Metrics Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            RevenueAnalyticsCard(plantState)
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Substation Busbar & Grid Stability HUD
        item {
            SubstationStatusCard(plantState)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Feeder Dispatch Management
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REGIONAL FEEDER DISPATCH (230 kV)",
                    style = MaterialTheme.typography.labelSmall,
                    color = HydroCyanPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Total Dispatched: ${String.format("%.1f", plantState.totalFeederDemandMw)} MW",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(plantState.feeders.size) { index ->
            val feeder = plantState.feeders[index]
            FeederCard(
                feeder = feeder,
                availablePlantMw = plantState.deliveredGridMw,
                onToggle = { onToggleFeeder(feeder.id) },
                onAllocationChange = { onUpdateFeederAllocation(feeder.id, it) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RevenueAnalyticsCard(state: PlantState) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HydroSurfaceCardDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(HydroBlueSecondary.copy(alpha = 0.5f), HydroCyanPrimary.copy(alpha = 0.5f))
            )
        ),
        modifier = Modifier.fillMaxWidth().testTag("revenue_analytics_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = HydroNormalGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COMMERCIAL POWER SALES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = HydroNormalGreen,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = HydroNormalGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "$${String.format("%.0f", state.hourlyRevenueRateUsd)} / hour",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = HydroNormalGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricMiniCard(
                    title = "TODAY'S REVENUE",
                    value = "$${String.format("%.0f", state.accumulatedRevenueUsd)}",
                    color = HydroNormalGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "ENERGY DELIVERED",
                    value = "${String.format("%.1f", state.totalEnergyGeneratedMwh)} MWh",
                    color = HydroCyanPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "CO₂ AVOIDED",
                    value = "${(state.totalEnergyGeneratedMwh * 0.85).toInt()} T",
                    color = HydroElectricYellow,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricMiniCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF07121E),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 8.5.sp, color = TextSecondaryDark, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun SubstationStatusCard(state: PlantState) {
    val isBalanced = abs(state.netGridBalanceMw) < 5.0f
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HydroSurfaceCardDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E3857))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SURYA ENERGY SUBSTATION & BUSBAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = HydroCyanPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Surface(
                    color = if (isBalanced) HydroNormalGreen.copy(alpha = 0.2f) else HydroWarningAmber.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (isBalanced) "GRID BALANCED" else "DISPATCH IMBALANCE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBalanced) HydroNormalGreen else HydroWarningAmber,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Busbar Voltage: ${String.format("%.1f", state.secondaryVoltageKv)} kV (230 kV Nominal)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    Text(text = "Grid Frequency: ${String.format("%.2f", state.frequencyHz)} Hz (Target 50.00 Hz)", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Stability: ${state.gridStabilityIndex.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (state.gridStabilityIndex > 90f) HydroNormalGreen else HydroWarningAmber
                    )
                    Text(
                        text = "Balance: ${String.format("%+.1f", state.netGridBalanceMw)} MW",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isBalanced) HydroNormalGreen else HydroWarningAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (state.gridStabilityIndex / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (state.gridStabilityIndex > 90f) HydroNormalGreen else HydroWarningAmber,
                trackColor = Color(0xFF0B192C)
            )
        }
    }
}

@Composable
private fun FeederCard(
    feeder: GridFeeder,
    availablePlantMw: Float,
    onToggle: () -> Unit,
    onAllocationChange: (Float) -> Unit
) {
    val feederRevenuePerHour = feeder.allocatedMw * feeder.tariffUsdPerMwh

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HydroSurfaceCardDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (feeder.isConnected) HydroCyanPrimary.copy(alpha = 0.5f) else Color(0xFF243B53)
            )
        ),
        modifier = Modifier.fillMaxWidth().testTag("feeder_card_${feeder.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = feeder.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${feeder.customerType} • Tariff: $${String.format("%.2f", feeder.tariffUsdPerMwh)} / MWh",
                        style = MaterialTheme.typography.bodySmall,
                        color = HydroElectricYellow
                    )
                }

                Switch(
                    checked = feeder.isConnected,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HydroCyanPrimary,
                        checkedTrackColor = HydroBlueSecondary,
                        uncheckedTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.testTag("feeder_switch_${feeder.id}")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = feeder.description,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Allocated: ${feeder.allocatedMw.toInt()} / ${feeder.maxCapacityMw.toInt()} MW",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (feeder.isConnected) Color.White else TextSecondaryDark
                )
                Text(
                    text = "$${String.format("%.0f", feederRevenuePerHour)} / hr",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (feeder.isConnected) HydroNormalGreen else TextSecondaryDark
                )
            }

            Slider(
                value = feeder.allocatedMw,
                onValueChange = onAllocationChange,
                valueRange = 0f..feeder.maxCapacityMw,
                enabled = feeder.isConnected,
                colors = SliderDefaults.colors(
                    thumbColor = HydroCyanPrimary,
                    activeTrackColor = HydroCyanPrimary,
                    inactiveTrackColor = Color(0xFF0F2033)
                ),
                modifier = Modifier.testTag("feeder_slider_${feeder.id}")
            )
        }
    }
}
