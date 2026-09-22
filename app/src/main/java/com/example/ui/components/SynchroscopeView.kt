package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlantState
import com.example.ui.theme.HydroAlarmRed
import com.example.ui.theme.HydroCyanPrimary
import com.example.ui.theme.HydroElectricYellow
import com.example.ui.theme.HydroNavyDark
import com.example.ui.theme.HydroNormalGreen
import com.example.ui.theme.HydroSurfaceCardDark
import com.example.ui.theme.HydroWarningAmber
import com.example.ui.theme.TextSecondaryDark
import kotlin.math.abs

@Composable
fun SynchroscopeView(
    plantState: PlantState,
    onToggleSynchronize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val freqSlip = plantState.frequencyHz - 50.00f
    val isNearSync = abs(freqSlip) < 0.15f && plantState.terminalVoltageKv > 12.0f

    val infiniteTransition = rememberInfiniteTransition(label = "synchroscope_slip")
    val slipRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (abs(freqSlip) > 0.02f) {
                    (2000 / abs(freqSlip).coerceAtLeast(0.05f)).toInt().coerceIn(400, 8000)
                } else 12000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "slip_pointer"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("synchroscope_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HydroNavyDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E3857)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = HydroCyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GRID SYNCHROSCOPE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = HydroCyanPrimary,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = if (plantState.isSynchronized) HydroNormalGreen.copy(alpha = 0.2f) else HydroAlarmRed.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (plantState.isSynchronized) HydroNormalGreen else HydroAlarmRed)
                    )
                ) {
                    Text(
                        text = if (plantState.isSynchronized) "LOCKED IN PARALLEL" else "OPEN CIRCUIT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (plantState.isSynchronized) HydroNormalGreen else HydroAlarmRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Synchroscope Dial Canvas
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF07121E))
                        .border(2.dp, Color(0xFF334E68), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f

                        // Outer ring
                        drawCircle(
                            color = Color(0xFF243B53),
                            radius = radius - 4f,
                            style = Stroke(width = 2f)
                        )

                        // Top 12 o'clock indicator (In-phase target)
                        drawLine(
                            color = HydroNormalGreen,
                            start = Offset(center.x, center.y - radius + 5f),
                            end = Offset(center.x, center.y - radius + 15f),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )

                        // Pointer needle
                        val pointerAngle = if (plantState.isSynchronized) 0f else slipRotation
                        rotate(pointerAngle, pivot = center) {
                            drawLine(
                                color = if (plantState.isSynchronized) HydroNormalGreen else HydroElectricYellow,
                                start = center,
                                end = Offset(center.x, center.y - radius + 12f),
                                strokeWidth = 3.5f,
                                cap = StrokeCap.Round
                            )
                        }

                        // Center cap
                        drawCircle(color = Color.White, radius = 5f, center = center)
                    }
                    Text(
                        text = if (plantState.isSynchronized) "IN PHASE" else if (freqSlip > 0) "FAST" else "SLOW",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp)
                    )
                }

                // Synchronization telemetry readouts
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "Generator: ${String.format("%.2f", plantState.frequencyHz)} Hz",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Grid Busbar: 50.00 Hz",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = "Slip: ${String.format("%+.2f", freqSlip)} Hz",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (abs(freqSlip) < 0.10f) HydroNormalGreen else HydroWarningAmber
                    )
                    Text(
                        text = "Terminal: ${String.format("%.2f", plantState.terminalVoltageKv)} kV (Grid: 13.80 kV)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onToggleSynchronize,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (plantState.isSynchronized) HydroAlarmRed else HydroNormalGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("toggle_synchronizer_btn")
                    ) {
                        Text(
                            text = if (plantState.isSynchronized) "Trip Grid Breaker" else "Close Sync Breaker",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HydroNavyDark
                        )
                    }
                }
            }
        }
    }
}
