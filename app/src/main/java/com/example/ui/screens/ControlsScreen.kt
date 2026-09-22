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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OperationalMode
import com.example.model.PlantState
import com.example.ui.components.SynchroscopeView
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
fun ControlsScreen(
    plantState: PlantState,
    onSetMode: (OperationalMode) -> Unit,
    onUpdateIntakeGate: (Float) -> Unit,
    onUpdateMivOpening: (Float) -> Unit,
    onToggleBypassValve: () -> Unit,
    onUpdateGuideVanes: (Float) -> Unit,
    onUpdateExciterCurrent: (Float) -> Unit,
    onToggleSynchronize: () -> Unit,
    onEmergencyTrip: () -> Unit,
    onResetTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HydroNavyDark)
            .padding(horizontal = 14.dp)
            .testTag("controls_screen_list")
    ) {
        // Operational Mode Selector
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "SCADA GOVERNOR OPERATING MODE",
                style = MaterialTheme.typography.labelSmall,
                color = HydroCyanPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OperationalMode.values().forEach { mode ->
                    val isSelected = plantState.operationalMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetMode(mode) },
                        label = {
                            Text(
                                text = mode.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HydroCyanPrimary,
                            selectedLabelColor = HydroNavyDark,
                            containerColor = HydroSurfaceCardDark,
                            labelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f).testTag("mode_chip_${mode.name}")
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Emergency Trip & Reset Master Controls
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (plantState.isEmergencyTrip) Color(0xFF350F12) else HydroSurfaceCardDark
                ),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (plantState.isEmergencyTrip) HydroAlarmRed else Color(0xFF1E3857))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (plantState.isEmergencyTrip) "PLANT TRIPPED" else "PLANT RUNNING SAFELY",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (plantState.isEmergencyTrip) HydroAlarmRed else HydroNormalGreen
                        )
                        Text(
                            text = if (plantState.isEmergencyTrip) "Fast closure executed on MIV and wicket gates." else "Safety interlocks armed. Ready for full dispatch.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }

                    if (plantState.isEmergencyTrip) {
                        Button(
                            onClick = onResetTrip,
                            colors = ButtonDefaults.buttonColors(containerColor = HydroNormalGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("reset_trip_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = HydroNavyDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Trip", color = HydroNavyDark, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onEmergencyTrip,
                            colors = ButtonDefaults.buttonColors(containerColor = HydroAlarmRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("emergency_trip_btn")
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("EMERGENCY TRIP", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 1. Water Intake & Reservoir Control
        item {
            ControlCard(
                title = "1. DAM RESERVOIR & INTAKE GATE",
                subtitle = "Regulates inflow into the penstock high-pressure conduit.",
                icon = Icons.Default.Water,
                currentValue = "${plantState.intakeGatePercent.toInt()}% Open",
                telemetryValue = "Flow: ${String.format("%.1f", plantState.waterFlowRateQ)} m³/s | Head: ${String.format("%.1f", plantState.grossHeadM)} m"
            ) {
                Slider(
                    value = plantState.intakeGatePercent,
                    onValueChange = onUpdateIntakeGate,
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = HydroCyanPrimary,
                        activeTrackColor = HydroCyanPrimary,
                        inactiveTrackColor = Color(0xFF0F2033)
                    ),
                    modifier = Modifier.testTag("intake_gate_slider")
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. Main Inlet Valve (MIV) Controller
        item {
            ControlCard(
                title = "2. MAIN INLET VALVE (MIV)",
                subtitle = "Spherical isolating plug between penstock and turbine runner.",
                icon = Icons.Default.Security,
                currentValue = "${plantState.mivOpeningPercent.toInt()}% (${plantState.mivState.name})",
                telemetryValue = "Seal Integrity: ${plantState.mivSealIntegrity.toInt()}% | Pressure: ${String.format("%.1f", plantState.penstockPressureBar)} Bar"
            ) {
                Slider(
                    value = plantState.mivOpeningPercent,
                    onValueChange = onUpdateMivOpening,
                    valueRange = 0f..100f,
                    enabled = !plantState.isEmergencyTrip,
                    colors = SliderDefaults.colors(
                        thumbColor = HydroNormalGreen,
                        activeTrackColor = HydroNormalGreen,
                        inactiveTrackColor = Color(0xFF0F2033)
                    ),
                    modifier = Modifier.testTag("miv_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Equalizing Bypass Valve: ${if (plantState.bypassValveOpen) "OPEN" else "CLOSED"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                    OutlinedButton(
                        onClick = onToggleBypassValve,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("bypass_valve_btn")
                    ) {
                        Text(
                            text = if (plantState.bypassValveOpen) "Close Bypass" else "Open Bypass",
                            fontSize = 11.sp,
                            color = HydroCyanPrimary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. Turbine Governor & Wicket Gates
        item {
            ControlCard(
                title = "3. TURBINE SPEED & GOVERNOR GATES",
                subtitle = "Distributor wicket vanes regulate water jet velocity and runner torque.",
                icon = Icons.Default.Speed,
                currentValue = "${plantState.guideVanePercent.toInt()}% Wicket Gates",
                telemetryValue = "Speed: ${plantState.turbineRpm.toInt()} RPM | Efficiency: ${String.format("%.1f", plantState.turbineEfficiency)}%"
            ) {
                Slider(
                    value = plantState.guideVanePercent,
                    onValueChange = onUpdateGuideVanes,
                    valueRange = 0f..100f,
                    enabled = plantState.operationalMode == OperationalMode.MANUAL && !plantState.isEmergencyTrip,
                    colors = SliderDefaults.colors(
                        thumbColor = HydroElectricYellow,
                        activeTrackColor = HydroElectricYellow,
                        inactiveTrackColor = Color(0xFF0F2033)
                    ),
                    modifier = Modifier.testTag("guide_vanes_slider")
                )
                if (plantState.operationalMode != OperationalMode.MANUAL) {
                    Text(
                        text = "Governor automatic feedback is active (Governor closed-loop mode). Switch to Manual SCADA to override.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryDark
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 4. Synchronous Generator Exciter Current
        item {
            ControlCard(
                title = "4. GENERATOR EXCITATION FIELD",
                subtitle = "Regulates magnetic flux, terminal voltage, and reactive power MVAr.",
                icon = Icons.Default.ElectricBolt,
                currentValue = "${plantState.exciterCurrentAmps.toInt()} Amps DC",
                telemetryValue = "Voltage: ${String.format("%.2f", plantState.terminalVoltageKv)} kV | Reactive: ${String.format("%.1f", plantState.reactivePowerMvar)} MVAr"
            ) {
                Slider(
                    value = plantState.exciterCurrentAmps,
                    onValueChange = onUpdateExciterCurrent,
                    valueRange = 200f..480f,
                    enabled = !plantState.isEmergencyTrip,
                    colors = SliderDefaults.colors(
                        thumbColor = HydroCyanPrimary,
                        activeTrackColor = HydroCyanPrimary,
                        inactiveTrackColor = Color(0xFF0F2033)
                    ),
                    modifier = Modifier.testTag("exciter_slider")
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 5. Grid Synchronizer Panel
        item {
            SynchroscopeView(
                plantState = plantState,
                onToggleSynchronize = onToggleSynchronize
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ControlCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    currentValue: String,
    telemetryValue: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = HydroSurfaceCardDark),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E3857))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(HydroCyanPrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = HydroCyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    color = Color(0xFF07121E),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = currentValue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HydroCyanPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(8.dp))
            content()

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = telemetryValue,
                style = MaterialTheme.typography.labelSmall,
                color = HydroElectricYellow,
                fontSize = 10.5.sp
            )
        }
    }
}
