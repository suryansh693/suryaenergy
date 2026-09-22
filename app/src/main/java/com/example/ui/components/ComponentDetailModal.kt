package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoreComponent
import com.example.model.PlantState
import com.example.ui.theme.HydroCyanPrimary
import com.example.ui.theme.HydroElectricYellow
import com.example.ui.theme.HydroNavyDark
import com.example.ui.theme.HydroNormalGreen
import com.example.ui.theme.HydroSurfaceCardDark
import com.example.ui.theme.HydroSurfaceDark
import com.example.ui.theme.HydroWarningAmber
import com.example.ui.theme.TextSecondaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentDetailModal(
    component: CoreComponent?,
    plantState: PlantState,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onPerformMaintenance: (CoreComponent) -> Unit
) {
    if (component == null) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HydroSurfaceDark,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("component_detail_modal")
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(HydroCyanPrimary.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, HydroCyanPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (component) {
                                CoreComponent.DAM, CoreComponent.PENSTOCK -> Icons.Default.Water
                                CoreComponent.GENERATOR, CoreComponent.TRANSFORMER, CoreComponent.SUBSTATION -> Icons.Default.ElectricBolt
                                else -> Icons.Default.Engineering
                            },
                            contentDescription = component.displayName,
                            tint = HydroCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = component.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = component.technicalName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_component_detail_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Telemetry KPI Section for this specific component
            Text(
                text = "LIVE SCADA TELEMETRY",
                style = MaterialTheme.typography.labelSmall,
                color = HydroCyanPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            ComponentTelemetryCard(component, plantState)

            Spacer(modifier = Modifier.height(16.dp))

            // Energy Conversion & Governing Equation
            Card(
                colors = CardDefaults.cardColors(containerColor = HydroSurfaceCardDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = HydroElectricYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Energy Conversion Role",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = HydroElectricYellow
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = component.energyConversionRole,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0x33486581))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Governing Engineering Equation:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFF0A192F),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = component.primaryEquation,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = HydroCyanPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cutaway Engineering Specs
            Card(
                colors = CardDefaults.cardColors(containerColor = HydroSurfaceCardDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = HydroCyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Structural Design & Cutaway Details",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = HydroCyanPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = component.cutawayDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD9E2EC),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Safety Thresholds & Protection Interlocks
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF241517)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(HydroWarningAmber.copy(alpha = 0.5f))),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = HydroWarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Protection Interlocks & Safety Thresholds",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = HydroWarningAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = component.safetyThresholds,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFD8A8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Maintenance / Calibration Action Button
            Button(
                onClick = {
                    onPerformMaintenance(component)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("component_maintenance_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = HydroCyanPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Engineering,
                    contentDescription = null,
                    tint = HydroNavyDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Perform Maintenance on ${component.displayName}",
                    fontWeight = FontWeight.Bold,
                    color = HydroNavyDark
                )
            }
        }
    }
}

@Composable
private fun ComponentTelemetryCard(component: CoreComponent, state: PlantState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = HydroSurfaceCardDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            when (component) {
                CoreComponent.DAM -> {
                    TelemetryRow("Reservoir Water Level", "${String.format("%.1f", state.reservoirWaterLevelM)} m", "Gross Head: ${String.format("%.1f", state.grossHeadM)} m")
                    TelemetryRow("River Inflow Rate", "${String.format("%.1f", state.riverInflowM3s)} m³/s", "Intake Gate: ${state.intakeGatePercent.toInt()}%")
                    TelemetryRow("Impounded Storage", "${state.reservoirStorageMcm.toInt()} MCM", "Headpond Status: Optimal")
                }
                CoreComponent.PENSTOCK -> {
                    TelemetryRow("Water Velocity", "${String.format("%.2f", state.penstockVelocityMs)} m/s", "Diameter: 4.20 m")
                    TelemetryRow("Hydrostatic Pressure", "${String.format("%.2f", state.penstockPressureBar)} Bar", "Net Head: ${String.format("%.1f", state.netHeadM)} m")
                    TelemetryRow("Water Hammer Surge", "${String.format("%.2f", state.waterHammerPressureBar)} Bar", "Safe Margin: < 26.5 Bar")
                }
                CoreComponent.MAIN_INLET_VALVE -> {
                    TelemetryRow("Valve Disc Position", "${state.mivOpeningPercent.toInt()}% Open", "State: ${state.mivState.name}")
                    TelemetryRow("Hydraulic Seal Health", "${String.format("%.1f", state.mivSealIntegrity)}%", "Hydraulic Actuator: 160 Bar")
                    TelemetryRow("Bypass Equalizer", if (state.bypassValveOpen) "OPEN (Balanced)" else "CLOSED", "Interlock: Ready")
                }
                CoreComponent.TURBINE -> {
                    TelemetryRow("Rotational Speed", "${String.format("%.1f", state.turbineRpm)} RPM", "Rated: 375.0 RPM (50 Hz)")
                    TelemetryRow("Water Flow Rate (Q)", "${String.format("%.1f", state.waterFlowRateQ)} m³/s", "Guide Vane: ${state.guideVanePercent.toInt()}%")
                    TelemetryRow("Runner Efficiency", "${String.format("%.1f", state.turbineEfficiency)}%", "Cavitation σ: ${String.format("%.3f", state.cavitationIndex)}")
                }
                CoreComponent.SHAFT -> {
                    TelemetryRow("Shaft Vibration (RMS)", "${String.format("%.2f", state.shaftVibrationMmS)} mm/s", "Alert Threshold: 3.8 mm/s")
                    TelemetryRow("Thrust Bearing Temp", "${String.format("%.1f", state.bearingTempC)} °C", "Trip Limit: 80.0 °C")
                    TelemetryRow("Lubrication Oil Health", "${String.format("%.1f", state.shaftBearingLubricationHealth)}%", "Pressure: 4.8 Bar")
                }
                CoreComponent.GENERATOR -> {
                    TelemetryRow("Active Electrical Power", "${String.format("%.1f", state.activePowerMw)} MW", "Rated: 300.0 MW")
                    TelemetryRow("Terminal Voltage", "${String.format("%.2f", state.terminalVoltageKv)} kV", "Excitation: ${state.exciterCurrentAmps.toInt()} A")
                    TelemetryRow("System Frequency", "${String.format("%.2f", state.frequencyHz)} Hz", "Synchronized: ${if (state.isSynchronized) "YES" else "NO"}")
                }
                CoreComponent.TRANSFORMER -> {
                    TelemetryRow("Secondary Transmission Voltage", "${String.format("%.1f", state.secondaryVoltageKv)} kV", "Ratio: 13.8 / 230 kV")
                    TelemetryRow("Apparent Load", "${String.format("%.1f", state.transformerLoadMva)} MVA", "Efficiency: 99.1%")
                    TelemetryRow("Top Oil Temperature", "${String.format("%.1f", state.transformerOilTempC)} °C", "Oil Health: ${state.transformerDielectricOilHealth.toInt()}%")
                }
                CoreComponent.POWER_HOUSE -> {
                    TelemetryRow("Operational Mode", state.operationalMode.title, "SCADA Master")
                    TelemetryRow("Machine Hall Temp", "${String.format("%.1f", state.machineHallTempC)} °C", "Drainage Sump: ${state.drainageSumpLevelPercent.toInt()}%")
                    TelemetryRow("Emergency Trip Interlock", if (state.isEmergencyTrip) "TRIPPED" else "ARMED & SAFE", "Status: ${state.status.label}")
                }
                CoreComponent.SUBSTATION -> {
                    TelemetryRow("Active Grid Dispatch", "${String.format("%.1f", state.deliveredGridMw)} MW", "Stability Index: ${state.gridStabilityIndex.toInt()}%")
                    TelemetryRow("Revenue Run Rate", "$${String.format("%.0f", state.hourlyRevenueRateUsd)} / hr", "Day Total: $${String.format("%.0f", state.accumulatedRevenueUsd)}")
                    TelemetryRow("Connected Feeders", "${state.feeders.count { it.isConnected }} of ${state.feeders.size}", "Busbar: 230 kV In-Phase")
                }
            }
        }
    }
}

@Composable
private fun TelemetryRow(label: String, value: String, subtext: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
            Text(text = subtext, style = MaterialTheme.typography.labelSmall, color = Color(0xFF627D98))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
