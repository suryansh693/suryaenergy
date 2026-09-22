package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoreComponent
import com.example.model.PlantState
import com.example.model.PlantStatus
import com.example.ui.components.EnergyConversionWaterfall
import com.example.ui.components.PlantDigitalTwinView
import com.example.ui.theme.HydroAlarmRed
import com.example.ui.theme.HydroCyanPrimary
import com.example.ui.theme.HydroElectricYellow
import com.example.ui.theme.HydroNavyDark
import com.example.ui.theme.HydroNormalGreen
import com.example.ui.theme.HydroSurfaceCardDark
import com.example.ui.theme.HydroSurfaceDark
import com.example.ui.theme.HydroWarningAmber
import com.example.ui.theme.TextSecondaryDark

@Composable
fun SchematicScreen(
    plantState: PlantState,
    selectedComponent: CoreComponent?,
    onSelectComponent: (CoreComponent) -> Unit,
    onEmergencyTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HydroNavyDark)
            .padding(horizontal = 14.dp)
            .testTag("schematic_screen_list")
    ) {
        // Quick Telemetry HUD
        item {
            Spacer(modifier = Modifier.height(8.dp))
            QuickTelemetryHud(plantState)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Digital Twin Interactive Schematic
        item {
            PlantDigitalTwinView(
                plantState = plantState,
                selectedComponent = selectedComponent,
                onSelectComponent = onSelectComponent
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Active Emergency Banner if Tripped
        if (plantState.status == PlantStatus.TRIPPED) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1215)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HydroAlarmRed)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("emergency_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = HydroAlarmRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "EMERGENCY TRIP SHUTDOWN ENGAGED",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = HydroAlarmRed
                            )
                            Text(
                                text = plantState.tripReason ?: "Main Inlet Valve & Wicket Gates Closed for Asset Protection.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Educational Energy Conversion Waterfall
        item {
            EnergyConversionWaterfall(plantState = plantState)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Core Components Directory
        item {
            Text(
                text = "PLANT SUBSYSTEM DIRECTORY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = HydroCyanPrimary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Tap any of the 9 core assets to review physical principles & operational specs",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark,
                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
            )
        }

        items(CoreComponent.values().size) { index ->
            val comp = CoreComponent.values()[index]
            ComponentDirectoryCard(
                component = comp,
                plantState = plantState,
                onClick = { onSelectComponent(comp) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickTelemetryHud(state: PlantState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HudTile(
            title = "ACTIVE POWER",
            value = "${String.format("%.1f", state.activePowerMw)} MW",
            statusColor = if (state.activePowerMw > 50f) HydroNormalGreen else HydroWarningAmber,
            modifier = Modifier.weight(1f)
        )
        HudTile(
            title = "FREQUENCY",
            value = "${String.format("%.2f", state.frequencyHz)} Hz",
            statusColor = if (kotlin.math.abs(state.frequencyHz - 50f) < 0.1f) HydroNormalGreen else HydroWarningAmber,
            modifier = Modifier.weight(1f)
        )
        HudTile(
            title = "WATER FLOW",
            value = "${String.format("%.0f", state.waterFlowRateQ)} m³/s",
            statusColor = HydroCyanPrimary,
            modifier = Modifier.weight(1f)
        )
        HudTile(
            title = "EFFICIENCY",
            value = "${String.format("%.1f", state.overallPlantEfficiency)}%",
            statusColor = HydroElectricYellow,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HudTile(
    title: String,
    value: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = HydroSurfaceCardDark,
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E3857))),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondaryDark,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

@Composable
private fun ComponentDirectoryCard(
    component: CoreComponent,
    plantState: PlantState,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = HydroSurfaceCardDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("component_card_${component.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(HydroCyanPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (component) {
                        CoreComponent.DAM, CoreComponent.PENSTOCK -> Icons.Default.Water
                        CoreComponent.TURBINE, CoreComponent.SHAFT -> Icons.AutoMirrored.Filled.RotateRight
                        CoreComponent.GENERATOR, CoreComponent.TRANSFORMER, CoreComponent.SUBSTATION -> Icons.Default.ElectricBolt
                        else -> Icons.Default.Engineering
                    },
                    contentDescription = component.displayName,
                    tint = HydroCyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = component.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = component.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Details",
                tint = Color(0xFF627D98),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
