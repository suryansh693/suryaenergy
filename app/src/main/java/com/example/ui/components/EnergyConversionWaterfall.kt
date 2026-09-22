package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlantState
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
fun EnergyConversionWaterfall(
    plantState: PlantState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("energy_conversion_waterfall_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HydroNavyDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(HydroBlueSecondary.copy(alpha = 0.5f), HydroCyanPrimary.copy(alpha = 0.5f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = HydroCyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENERGY CONVERSION PATHWAY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = HydroCyanPrimary,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = HydroCyanPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HydroCyanPrimary))
                ) {
                    Text(
                        text = "Overall Efficiency: ${String.format("%.1f", plantState.overallPlantEfficiency)}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HydroCyanPrimary
                    )
                }
            }

            Text(
                text = "Real-time transformation from impounded water head to high-voltage grid dispatch.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Stage 1: Gravitational Potential Energy
            EnergyStageItem(
                stepNumber = "1",
                stageName = "Gravitational Potential Energy",
                subtext = "Dam Reservoir (Head: ${String.format("%.1f", plantState.grossHeadM)} m, Inflow: ${String.format("%.0f", plantState.waterFlowRateQ)} m³/s)",
                powerMw = plantState.potentialEnergyMw,
                maxPowerMw = 300f,
                efficiencyPercent = 100f,
                color = HydroAquaFlow,
                icon = Icons.Default.Water,
                lossDescription = "Baseline 100% potential impoundment"
            )

            ConversionArrow(lossLabel = "Friction & Head Losses (-${String.format("%.1f", plantState.potentialEnergyMw - plantState.kineticEnergyMw)} MW)")

            // Stage 2: Hydrodynamic Kinetic & Pressure Energy
            EnergyStageItem(
                stepNumber = "2",
                stageName = "Hydrodynamic Kinetic & Pressure",
                subtext = "Penstock conduit (v = ${String.format("%.1f", plantState.penstockVelocityMs)} m/s, P = ${String.format("%.1f", plantState.penstockPressureBar)} Bar)",
                powerMw = plantState.kineticEnergyMw,
                maxPowerMw = 300f,
                efficiencyPercent = if (plantState.potentialEnergyMw > 0f) (plantState.kineticEnergyMw / plantState.potentialEnergyMw) * 100f else 0f,
                color = Color(0xFF00B4D8),
                icon = Icons.Default.Waves,
                lossDescription = "Darcy-Weisbach pipe friction & entrance head loss"
            )

            ConversionArrow(lossLabel = "Turbine Hydraulic & Swirl Losses (-${String.format("%.1f", plantState.kineticEnergyMw - plantState.mechanicalPowerMw)} MW)")

            // Stage 3: Mechanical Rotational Energy
            EnergyStageItem(
                stepNumber = "3",
                stageName = "Mechanical Rotational Torque",
                subtext = "Francis Turbine & Shaft (N = ${plantState.turbineRpm.toInt()} RPM, τ = ${String.format("%.0f", plantState.turbineTorqueKNm)} kN·m)",
                powerMw = plantState.mechanicalPowerMw,
                maxPowerMw = 300f,
                efficiencyPercent = plantState.turbineEfficiency,
                color = HydroElectricYellow,
                icon = Icons.Default.Speed,
                lossDescription = "Runner disc friction, leakage & draft tube losses"
            )

            ConversionArrow(lossLabel = "Generator Copper & Iron Losses (-${String.format("%.1f", plantState.mechanicalPowerMw - plantState.electricalGeneratedMw)} MW)")

            // Stage 4: Electrical Alternator AC Generation
            EnergyStageItem(
                stepNumber = "4",
                stageName = "Electromagnetic 3-Phase AC",
                subtext = "Synchronous Generator (13.8 kV, 50.0 Hz, cos φ = ${plantState.powerFactor})",
                powerMw = plantState.electricalGeneratedMw,
                maxPowerMw = 300f,
                efficiencyPercent = 98.4f,
                color = HydroCyanPrimary,
                icon = Icons.Default.ElectricBolt,
                lossDescription = "Stator winding I²R heating & rotor core hysteresis"
            )

            ConversionArrow(lossLabel = "Step-up Transformer Core & Copper Loss (-${String.format("%.1f", plantState.electricalGeneratedMw - plantState.deliveredGridMw)} MW)")

            // Stage 5: High-Voltage Grid Dispatch
            EnergyStageItem(
                stepNumber = "5",
                stageName = "230 kV Substation Grid Delivery",
                subtext = "Switchyard Busbar (Delivering to 3 Regional Feeders)",
                powerMw = plantState.deliveredGridMw,
                maxPowerMw = 300f,
                efficiencyPercent = 99.1f,
                color = HydroNormalGreen,
                icon = Icons.Default.ElectricBolt,
                lossDescription = "Final saleable dispatch generating $$${String.format("%.0f", plantState.hourlyRevenueRateUsd)} / hour"
            )
        }
    }
}

@Composable
private fun EnergyStageItem(
    stepNumber: String,
    stageName: String,
    subtext: String,
    powerMw: Float,
    maxPowerMw: Float,
    efficiencyPercent: Float,
    color: Color,
    icon: ImageVector,
    lossDescription: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (powerMw / maxPowerMw).coerceIn(0f, 1f),
        label = "stage_progress"
    )

    Surface(
        color = HydroSurfaceCardDark,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(color.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stepNumber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stageName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = subtext,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryDark
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format("%.1f", powerMw)} MW",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = "η = ${String.format("%.1f", efficiencyPercent)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = Color(0xFF0A192F)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lossDescription,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF627D98),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ConversionArrow(lossLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = null,
            tint = Color(0xFF486581),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = lossLabel,
            fontSize = 11.sp,
            color = HydroWarningAmber.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
    }
}
