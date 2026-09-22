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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoreComponent
import com.example.model.MaintenanceLog
import com.example.model.PlantState
import com.example.ui.theme.HydroAlarmRed
import com.example.ui.theme.HydroCyanPrimary
import com.example.ui.theme.HydroElectricYellow
import com.example.ui.theme.HydroNavyDark
import com.example.ui.theme.HydroNormalGreen
import com.example.ui.theme.HydroSurfaceCardDark
import com.example.ui.theme.HydroWarningAmber
import com.example.ui.theme.TextSecondaryDark

@Composable
fun AnalyticsMaintenanceScreen(
    plantState: PlantState,
    isMonitoringServiceRunning: Boolean = false,
    onToggleMonitoringService: () -> Unit = {},
    onTriggerSimulatedWear: (CoreComponent) -> Unit = {},
    onPerformMaintenanceAction: (String) -> Unit,
    onResolveAlert: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HydroNavyDark)
            .padding(horizontal = 14.dp)
            .testTag("analytics_maintenance_screen_list")
    ) {
        // Predictive Wear Monitoring Background Service Card
        item {
            Spacer(modifier = Modifier.height(10.dp))
            BackgroundMonitoringServiceCard(
                isRunning = isMonitoringServiceRunning,
                onToggle = onToggleMonitoringService,
                onTriggerSimulatedWear = onTriggerSimulatedWear
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Asset Health Overview
        item {
            AssetHealthOverviewCard(plantState)
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Subsystems Health Progress Bars
        item {
            Text(
                text = "CORE COMPONENT HEALTH TELEMETRY",
                style = MaterialTheme.typography.labelSmall,
                color = HydroCyanPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            HealthBarItem(
                componentName = "MIV Spherical Seal Integrity",
                healthPercent = plantState.mivSealIntegrity,
                subtext = "Hydraulic pressure seal with low leakage rate (<0.8 L/min)"
            )
            Spacer(modifier = Modifier.height(8.dp))
            HealthBarItem(
                componentName = "Francis Runner Cavitation Health",
                healthPercent = plantState.turbineRunnerHealth,
                subtext = "13Cr4Ni stainless steel runner with pitting detection"
            )
            Spacer(modifier = Modifier.height(8.dp))
            HealthBarItem(
                componentName = "Shaft Guide Bearing Lubrication",
                healthPercent = plantState.shaftBearingLubricationHealth,
                subtext = "Forced oil hydrodynamic film at 4.8 Bar pressure"
            )
            Spacer(modifier = Modifier.height(8.dp))
            HealthBarItem(
                componentName = "Generator Stator Mica Insulation",
                healthPercent = plantState.generatorInsulationHealth,
                subtext = "Class F epoxy-mica insulation with thermal class B limits"
            )
            Spacer(modifier = Modifier.height(8.dp))
            HealthBarItem(
                componentName = "Transformer Dielectric Oil Quality",
                healthPercent = plantState.transformerDielectricOilHealth,
                subtext = "Mineral insulating oil breakdown voltage > 60 kV"
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Maintenance Actions
        item {
            Text(
                text = "INTERACTIVE PREVENTIVE MAINTENANCE",
                style = MaterialTheme.typography.labelSmall,
                color = HydroCyanPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            MaintenanceActionsRow(onPerformAction = onPerformMaintenanceAction)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Diagnostic Alerts Center
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = HydroElectricYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "REAL-TIME DIAGNOSTIC ALARMS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HydroElectricYellow,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "${plantState.alerts.count { !it.resolved }} Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryDark
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (plantState.alerts.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = HydroSurfaceCardDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = HydroNormalGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "All 9 plant components operating within normal safety limits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            items(plantState.alerts.size) { index ->
                val alert = plantState.alerts[index]
                DiagnosticAlertItem(alert = alert, onResolve = { onResolveAlert(alert.id) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AssetHealthOverviewCard(state: PlantState) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HydroSurfaceCardDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E3857))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = HydroCyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLANT HEALTH INDEX",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                val avgHealth = (
                    state.mivSealIntegrity +
                    state.turbineRunnerHealth +
                    state.shaftBearingLubricationHealth +
                    state.generatorInsulationHealth +
                    state.transformerDielectricOilHealth
                ) / 5f

                Surface(
                    color = if (avgHealth > 85f) HydroNormalGreen.copy(alpha = 0.2f) else HydroWarningAmber.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Composite: ${String.format("%.1f", avgHealth)}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (avgHealth > 85f) HydroNormalGreen else HydroWarningAmber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Vibration: ${String.format("%.2f", state.shaftVibrationMmS)} mm/s RMS", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    Text(text = "Bearing Temp: ${String.format("%.1f", state.bearingTempC)} °C", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Cavitation σ: ${String.format("%.3f", state.cavitationIndex)}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    Text(text = "Trans Oil Temp: ${String.format("%.1f", state.transformerOilTempC)} °C", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                }
            }
        }
    }
}

@Composable
private fun HealthBarItem(
    componentName: String,
    healthPercent: Float,
    subtext: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = HydroSurfaceCardDark),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = componentName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${String.format("%.1f", healthPercent)}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (healthPercent > 80f) HydroNormalGreen else if (healthPercent > 50f) HydroWarningAmber else HydroAlarmRed
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (healthPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (healthPercent > 80f) HydroNormalGreen else if (healthPercent > 50f) HydroWarningAmber else HydroAlarmRed,
                trackColor = Color(0xFF0A192F)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryDark,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun MaintenanceActionsRow(onPerformAction: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MaintenanceButton(
                title = "Flush Bearing Lube",
                onClick = { onPerformAction("LUBRICATION") },
                modifier = Modifier.weight(1f)
            )
            MaintenanceButton(
                title = "Hone Runner Blades",
                onClick = { onPerformAction("CAVITATION") },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MaintenanceButton(
                title = "Purify Trans Oil",
                onClick = { onPerformAction("TRANSFORMER") },
                modifier = Modifier.weight(1f)
            )
            MaintenanceButton(
                title = "Seal MIV Hydraulic",
                onClick = { onPerformAction("MIV") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MaintenanceButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = HydroCyanPrimary),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334E68)),
        modifier = modifier
    ) {
        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DiagnosticAlertItem(alert: MaintenanceLog, onResolve: () -> Unit) {
    val isCritical = alert.severity == "CRITICAL"
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCritical) Color(0xFF2E1214) else HydroSurfaceCardDark
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isCritical) HydroAlarmRed else HydroWarningAmber.copy(alpha = 0.5f))
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = if (isCritical) HydroAlarmRed else HydroWarningAmber,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = alert.component.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = alert.severity,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCritical) HydroAlarmRed else HydroWarningAmber
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD9E2EC)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onResolve,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Resolve", fontSize = 10.sp, color = HydroCyanPrimary)
            }
        }
    }
}

@Composable
private fun BackgroundMonitoringServiceCard(
    isRunning: Boolean,
    onToggle: () -> Unit,
    onTriggerSimulatedWear: (CoreComponent) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = HydroSurfaceCardDark
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isRunning) HydroCyanPrimary.copy(alpha = 0.5f) else Color(0x33486581)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("background_monitor_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRunning) HydroCyanPrimary.copy(alpha = 0.15f)
                                else Color(0x22486581)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = if (isRunning) HydroCyanPrimary else Color(0xFF9FB3C8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PREDICTIVE WEAR MONITOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = HydroCyanPrimary,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(
                                        color = if (isRunning) HydroNormalGreen else Color(0xFF627D98),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isRunning) "Telemetry Service Active" else "Service Paused",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isRunning) HydroNormalGreen else Color(0xFF9FB3C8)
                            )
                        }
                    }
                }

                Switch(
                    checked = isRunning,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HydroCyanPrimary,
                        checkedTrackColor = HydroCyanPrimary.copy(alpha = 0.3f),
                        uncheckedThumbColor = Color(0xFF627D98),
                        uncheckedTrackColor = Color(0x33486581)
                    ),
                    modifier = Modifier.testTag("toggle_monitoring_service_switch")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Background service computes continuous vibration harmonics, cavitation acoustic emissions (σ), and bearing oil shear. Automatically triggers system push notifications and SCADA alerts when maintenance is needed.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "TEST SIMULATED WEAR TRIGGER:",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9FB3C8),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Quick trigger chips row
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SimulatedWearChip(
                    label = "Cavitation Surge",
                    component = CoreComponent.TURBINE,
                    onClick = { onTriggerSimulatedWear(CoreComponent.TURBINE) }
                )
                SimulatedWearChip(
                    label = "Bearing Overheat",
                    component = CoreComponent.SHAFT,
                    onClick = { onTriggerSimulatedWear(CoreComponent.SHAFT) }
                )
                SimulatedWearChip(
                    label = "MIV Seal Leak",
                    component = CoreComponent.MAIN_INLET_VALVE,
                    onClick = { onTriggerSimulatedWear(CoreComponent.MAIN_INLET_VALVE) }
                )
                SimulatedWearChip(
                    label = "Transformer Oil Gas",
                    component = CoreComponent.TRANSFORMER,
                    onClick = { onTriggerSimulatedWear(CoreComponent.TRANSFORMER) }
                )
            }
        }
    }
}

@Composable
private fun SimulatedWearChip(
    label: String,
    component: CoreComponent,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0x22000000),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, HydroAlarmRed.copy(alpha = 0.5f)),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("test_wear_chip_${component.name}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = HydroAlarmRed,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
