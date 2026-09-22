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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoreComponent
import com.example.model.PlantState
import com.example.model.PlantStatus
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
import kotlin.math.sin

@Composable
fun PlantDigitalTwinView(
    plantState: PlantState,
    selectedComponent: CoreComponent?,
    onSelectComponent: (CoreComponent) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "plant_dynamics")

    // Water flow animation phase
    val waterPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "water_flow"
    )

    // Turbine rotation angle
    val turbineRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (plantState.turbineRpm > 10f) {
                    (60_000 / plantState.turbineRpm.coerceAtLeast(60f)).toInt()
                } else 10000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "turbine_spin"
    )

    // Electric glow pulse
    val electricGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "electric_pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("plant_digital_twin_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HydroNavyDark
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(HydroBlueSecondary, HydroCyanPrimary.copy(alpha = 0.6f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header with status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = when (plantState.status) {
                                    PlantStatus.NORMAL -> HydroNormalGreen
                                    PlantStatus.WARNING -> HydroWarningAmber
                                    PlantStatus.TRIPPED -> HydroAlarmRed
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SURYA ENERGY DIGITAL TWIN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = HydroCyanPrimary,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Tap any component to inspect",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Visual Schematic Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF07121E))
                    .border(1.dp, Color(0xFF1E3857), RoundedCornerShape(12.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. DAM & RESERVOIR (Left Section: 0 to 0.26w)
                    val damTop = h * 0.15f
                    val damBase = h * 0.85f
                    val damWidth = w * 0.22f

                    // Water in reservoir
                    val waterLevelRatio = (plantState.reservoirWaterLevelM / 220f).coerceIn(0.4f, 0.95f)
                    val waterY = h * (1f - (waterLevelRatio * 0.7f))

                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(HydroAquaFlow.copy(alpha = 0.8f), HydroBlueSecondary.copy(alpha = 0.95f))
                        ),
                        topLeft = Offset(0f, waterY),
                        size = Size(damWidth, h - waterY)
                    )

                    // Reservoir surface ripples
                    val wavePath = Path().apply {
                        moveTo(0f, waterY)
                        for (i in 0..6) {
                            val waveX = (damWidth / 6f) * i
                            val waveY = waterY + (sin((i + waterPhase * 4f) * 1.5).toFloat() * 3f)
                            lineTo(waveX, waveY)
                        }
                    }
                    drawPath(wavePath, color = HydroCyanPrimary, style = Stroke(width = 2.5f))

                    // Dam concrete face
                    val damPath = Path().apply {
                        moveTo(damWidth * 0.7f, damTop)
                        lineTo(damWidth, damTop + 20f)
                        lineTo(damWidth + 25f, damBase)
                        lineTo(damWidth * 0.5f, damBase)
                        close()
                    }
                    drawPath(
                        damPath,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF334E68), Color(0xFF1E293B)),
                            start = Offset(damWidth * 0.7f, damTop),
                            end = Offset(damWidth + 25f, damBase)
                        )
                    )

                    // 2. PENSTOCK CONDUIT (From dam to powerhouse: ~0.24w, 0.35h to ~0.50w, 0.75h)
                    val penstockStartX = damWidth + 5f
                    val penstockStartY = damTop + 45f
                    val penstockEndX = w * 0.48f
                    val penstockEndY = h * 0.72f

                    // Steel outer pipe
                    drawLine(
                        color = Color(0xFF627D98),
                        start = Offset(penstockStartX, penstockStartY),
                        end = Offset(penstockEndX, penstockEndY),
                        strokeWidth = 24f,
                        cap = StrokeCap.Round
                    )
                    // Water flow core
                    val isFlowing = plantState.waterFlowRateQ > 5f
                    drawLine(
                        color = if (isFlowing) HydroCyanPrimary else Color(0xFF1B4965),
                        start = Offset(penstockStartX, penstockStartY),
                        end = Offset(penstockEndX, penstockEndY),
                        strokeWidth = 16f,
                        cap = StrokeCap.Round
                    )

                    // Animated water particles inside penstock
                    if (isFlowing) {
                        for (i in 0..4) {
                            val t = ((waterPhase + (i * 0.2f)) % 1.0f)
                            val px = penstockStartX + (penstockEndX - penstockStartX) * t
                            val py = penstockStartY + (penstockEndY - penstockStartY) * t
                            drawCircle(
                                color = Color.White,
                                radius = 3.5f,
                                center = Offset(px, py)
                            )
                        }
                    }

                    // 3. MAIN INLET VALVE (MIV) at (0.48w, 0.72h)
                    val mivX = penstockEndX
                    val mivY = penstockEndY
                    drawCircle(
                        color = Color(0xFF102A43),
                        radius = 16f,
                        center = Offset(mivX, mivY)
                    )
                    drawCircle(
                        color = if (plantState.mivOpeningPercent > 50f) HydroNormalGreen else HydroAlarmRed,
                        radius = 12f,
                        center = Offset(mivX, mivY),
                        style = Stroke(width = 3.5f)
                    )
                    // Valve disc angle
                    val valveAngle = (plantState.mivOpeningPercent / 100f) * 90f
                    rotate(valveAngle, pivot = Offset(mivX, mivY)) {
                        drawLine(
                            color = Color.White,
                            start = Offset(mivX - 9f, mivY),
                            end = Offset(mivX + 9f, mivY),
                            strokeWidth = 3f
                        )
                    }

                    // 4. POWER HOUSE CAVERN ENCLOSURE (0.50w to 0.82w)
                    val phLeft = w * 0.51f
                    val phRight = w * 0.80f
                    val phTop = h * 0.22f
                    val phBottom = h * 0.90f

                    drawRoundRect(
                        color = Color(0x33102A43),
                        topLeft = Offset(phLeft, phTop),
                        size = Size(phRight - phLeft, phBottom - phTop),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                        style = Stroke(width = 1.5f)
                    )

                    // 5. TURBINE RUNNER (Bottom of powerhouse: ~0.56w, 0.78h)
                    val turbineX = phLeft + 35f
                    val turbineY = phBottom - 25f
                    drawCircle(
                        color = Color(0xFF1F3A56),
                        radius = 22f,
                        center = Offset(turbineX, turbineY)
                    )
                    // Rotating turbine runner blades
                    rotate(turbineRotation, pivot = Offset(turbineX, turbineY)) {
                        for (b in 0..5) {
                            val angleRad = Math.toRadians((b * 60.0))
                            val bx = turbineX + (18f * kotlin.math.cos(angleRad)).toFloat()
                            val by = turbineY + (18f * kotlin.math.sin(angleRad)).toFloat()
                            drawLine(
                                color = HydroCyanPrimary,
                                start = Offset(turbineX, turbineY),
                                end = Offset(bx, by),
                                strokeWidth = 3f
                            )
                        }
                    }

                    // 6. VERTICAL SHAFT (0.56w, connecting turbine to generator)
                    val generatorY = phTop + 38f
                    drawLine(
                        color = if (plantState.shaftVibrationMmS > 3.8f) HydroAlarmRed else Color(0xFFBCCCDC),
                        start = Offset(turbineX, turbineY - 22f),
                        end = Offset(turbineX, generatorY + 22f),
                        strokeWidth = 7f,
                        cap = StrokeCap.Square
                    )

                    // 7. GENERATOR ROTOR & STATOR (~0.56w, phTop + 38f)
                    drawCircle(
                        color = Color(0xFF0B2136),
                        radius = 26f,
                        center = Offset(turbineX, generatorY)
                    )
                    drawCircle(
                        color = if (plantState.isSynchronized) HydroElectricYellow.copy(alpha = electricGlow) else Color.Gray,
                        radius = 23f,
                        center = Offset(turbineX, generatorY),
                        style = Stroke(width = 4f)
                    )
                    // Rotor poles
                    rotate(turbineRotation, pivot = Offset(turbineX, generatorY)) {
                        drawLine(
                            color = HydroElectricYellow,
                            start = Offset(turbineX - 16f, generatorY),
                            end = Offset(turbineX + 16f, generatorY),
                            strokeWidth = 4f
                        )
                        drawLine(
                            color = HydroElectricYellow,
                            start = Offset(turbineX, generatorY - 16f),
                            end = Offset(turbineX, generatorY + 16f),
                            strokeWidth = 4f
                        )
                    }

                    // 8. TRANSFORMER (Step-up unit at ~0.72w, 0.45h)
                    val transX = phRight - 28f
                    val transY = phTop + 55f
                    drawRoundRect(
                        color = Color(0xFF1E3A5F),
                        topLeft = Offset(transX - 16f, transY - 18f),
                        size = Size(32f, 36f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                    )
                    // Transformer coil rings
                    drawCircle(
                        color = Color(0xFFFF9E00),
                        radius = 8f,
                        center = Offset(transX - 5f, transY),
                        style = Stroke(width = 2.5f)
                    )
                    drawCircle(
                        color = HydroCyanPrimary,
                        radius = 8f,
                        center = Offset(transX + 5f, transY),
                        style = Stroke(width = 2.5f)
                    )

                    // Generator to transformer cable
                    drawLine(
                        color = HydroElectricYellow,
                        start = Offset(turbineX + 26f, generatorY),
                        end = Offset(transX - 16f, transY),
                        strokeWidth = 2.5f
                    )

                    // 9. SUBSTATION & TRANSMISSION PYLON (~0.88w to 0.98w)
                    val pylonX = w * 0.90f
                    val pylonBaseY = h * 0.85f
                    val pylonTopY = h * 0.20f

                    // Transformer to Substation line
                    drawLine(
                        color = HydroCyanPrimary,
                        start = Offset(transX + 16f, transY),
                        end = Offset(pylonX - 10f, pylonTopY + 25f),
                        strokeWidth = 2.5f
                    )

                    // High voltage lattice transmission pylon
                    val pylonPath = Path().apply {
                        moveTo(pylonX, pylonTopY)
                        lineTo(pylonX - 18f, pylonBaseY)
                        lineTo(pylonX + 18f, pylonBaseY)
                        close()
                    }
                    drawPath(pylonPath, color = Color(0xFF486581), style = Stroke(width = 2f))
                    // Crossarms
                    drawLine(
                        color = Color(0xFF486581),
                        start = Offset(pylonX - 25f, pylonTopY + 20f),
                        end = Offset(pylonX + 25f, pylonTopY + 20f),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color(0xFF486581),
                        start = Offset(pylonX - 20f, pylonTopY + 45f),
                        end = Offset(pylonX + 20f, pylonTopY + 45f),
                        strokeWidth = 2f
                    )

                    // Outgoing grid power lines
                    if (plantState.deliveredGridMw > 5f) {
                        drawLine(
                            color = HydroCyanPrimary.copy(alpha = electricGlow),
                            start = Offset(pylonX + 25f, pylonTopY + 20f),
                            end = Offset(w, pylonTopY + 15f),
                            strokeWidth = 2.5f
                        )
                        drawLine(
                            color = HydroCyanPrimary.copy(alpha = electricGlow),
                            start = Offset(pylonX + 20f, pylonTopY + 45f),
                            end = Offset(w, pylonTopY + 40f),
                            strokeWidth = 2.5f
                        )
                    }
                }

                // Component Overlay Badges (Clickable hot-spots)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ComponentMiniPill(
                        label = "Dam",
                        value = "${plantState.reservoirWaterLevelM.toInt()}m",
                        isSelected = selectedComponent == CoreComponent.DAM,
                        onClick = { onSelectComponent(CoreComponent.DAM) }
                    )
                    ComponentMiniPill(
                        label = "Penstock",
                        value = "${String.format("%.1f", plantState.penstockPressureBar)} Bar",
                        isSelected = selectedComponent == CoreComponent.PENSTOCK,
                        onClick = { onSelectComponent(CoreComponent.PENSTOCK) }
                    )
                    ComponentMiniPill(
                        label = "MIV",
                        value = "${plantState.mivOpeningPercent.toInt()}%",
                        isSelected = selectedComponent == CoreComponent.MAIN_INLET_VALVE,
                        onClick = { onSelectComponent(CoreComponent.MAIN_INLET_VALVE) }
                    )
                    ComponentMiniPill(
                        label = "Turbine",
                        value = "${plantState.turbineRpm.toInt()} RPM",
                        isSelected = selectedComponent == CoreComponent.TURBINE,
                        onClick = { onSelectComponent(CoreComponent.TURBINE) }
                    )
                    ComponentMiniPill(
                        label = "Generator",
                        value = "${plantState.activePowerMw.toInt()} MW",
                        isSelected = selectedComponent == CoreComponent.GENERATOR,
                        onClick = { onSelectComponent(CoreComponent.GENERATOR) }
                    )
                    ComponentMiniPill(
                        label = "Substation",
                        value = "230 kV",
                        isSelected = selectedComponent == CoreComponent.SUBSTATION,
                        onClick = { onSelectComponent(CoreComponent.SUBSTATION) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontally Scrollable Selector for All 9 Core Components
            Text(
                text = "CORE SYSTEM COMPONENTS",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryDark,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CoreComponent.values().forEach { comp ->
                    val isSelected = comp == selectedComponent
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) HydroCyanPrimary else HydroSurfaceCardDark,
                        modifier = Modifier
                            .clickable { onSelectComponent(comp) }
                            .testTag("comp_chip_${comp.name}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = when (comp) {
                                    CoreComponent.DAM, CoreComponent.PENSTOCK -> Icons.Default.Water
                                    CoreComponent.TURBINE, CoreComponent.SHAFT -> Icons.AutoMirrored.Filled.RotateRight
                                    CoreComponent.GENERATOR, CoreComponent.TRANSFORMER, CoreComponent.SUBSTATION -> Icons.Default.ElectricBolt
                                    else -> Icons.Default.Info
                                },
                                contentDescription = comp.displayName,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) HydroNavyDark else HydroCyanPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = comp.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) HydroNavyDark else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponentMiniPill(
    label: String,
    value: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) HydroCyanPrimary.copy(alpha = 0.9f) else Color(0xCC0E2135),
        modifier = Modifier
            .clickable { onClick() }
            .border(
                1.dp,
                if (isSelected) HydroCyanPrimary else Color(0x44486581),
                RoundedCornerShape(6.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = if (isSelected) HydroNavyDark else TextSecondaryDark,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) HydroNavyDark else Color.White
            )
        }
    }
}
