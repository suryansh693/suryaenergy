package com.example.simulation

import com.example.model.CoreComponent
import com.example.model.GridFeeder
import com.example.model.MaintenanceLog
import com.example.model.MivState
import com.example.model.OperationalMode
import com.example.model.PlantState
import com.example.model.PlantStatus
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object HydroEngine {

    const val RATED_RPM = 375.0f // 16 poles at 50 Hz
    const val RATED_FREQ_HZ = 50.00f
    const val RATED_TERMINAL_KV = 13.8f
    const val RATED_GRID_KV = 230.0f
    const val MAX_FLOW_M3S = 185.0f

    /**
     * Executes one discrete simulation tick (e.g. 500ms real-time step).
     */
    fun tick(currentState: PlantState, dtSeconds: Float = 0.5f): PlantState {
        var state = currentState

        // 1. Check Emergency Trip Condition
        if (state.isEmergencyTrip) {
            return processEmergencyTripTick(state, dtSeconds)
        }

        // 2. Reservoir mass balance
        val tailraceElevation = 15.0f
        val grossHead = max(0f, state.reservoirWaterLevelM - tailraceElevation)

        // Auto-governor adjustments if enabled
        var guideVanes = state.guideVanePercent
        if (state.operationalMode == OperationalMode.AUTO_GOVERNOR && state.isSynchronized) {
            val targetDemand = state.totalFeederDemandMw
            val targetGate = (targetDemand / 2.7f).coerceIn(15f, 98f)
            guideVanes += (targetGate - guideVanes) * 0.15f
        } else if (state.operationalMode == OperationalMode.AI_OPTIMIZED && state.isSynchronized) {
            // AI Mode prioritizes highest tariff feeders
            val optimalGate = 85.0f
            guideVanes += (optimalGate - guideVanes) * 0.10f
        }

        // MIV opening dynamics
        val mivFactor = (state.mivOpeningPercent / 100f).coerceIn(0f, 1f)
        val intakeFactor = (state.intakeGatePercent / 100f).coerceIn(0f, 1f)
        val gateFactor = (guideVanes / 100f).coerceIn(0f, 1f)

        // Flow rate Q calculation: Q = A * Cd * sqrt(2 * g * H)
        val effectiveHeadRatio = sqrt(max(0.01f, grossHead / 190.0f))
        val flowRateQ = MAX_FLOW_M3S * intakeFactor * mivFactor * gateFactor * effectiveHeadRatio

        // Reservoir volume and water level variation
        val netVolumeFlow = (state.riverInflowM3s - flowRateQ) * dtSeconds
        val updatedLevel = (state.reservoirWaterLevelM + (netVolumeFlow / 2_000_000f)).coerceIn(135f, 218f)

        // Penstock friction losses (Darcy-Weisbach approximation: h_f = k * Q^2)
        val penstockDiameter = 4.2f
        val penstockArea = (Math.PI * (penstockDiameter / 2.0).let { it * it }).toFloat()
        val velocity = if (penstockArea > 0f) flowRateQ / penstockArea else 0f
        val headLoss = 0.00022f * (flowRateQ * flowRateQ)
        val netHead = max(0f, grossHead - headLoss)
        val penstockPressure = (netHead * 0.0981f) + state.waterHammerPressureBar

        // Water hammer decay
        val updatedHammerPressure = max(0f, state.waterHammerPressureBar * 0.85f)

        // Francis Turbine efficiency curve (peaks at ~82% wicket gate opening)
        val turbineEfficiency = if (gateFactor > 0.05f) {
            val peakGate = 0.82f
            val deviation = abs(gateFactor - peakGate)
            (93.8f - (deviation * deviation * 45f)).coerceIn(60.0f, 94.2f)
        } else {
            0.0f
        }

        // Potential Energy: Ep = rho * g * Q * H (MW)
        val potentialMw = (1000f * 9.81f * flowRateQ * grossHead) / 1_000_000f
        val kineticMw = (1000f * 9.81f * flowRateQ * netHead) / 1_000_000f
        val mechanicalMw = kineticMw * (turbineEfficiency / 100f)

        // Rotational Dynamics & Governor
        var rpm = state.turbineRpm
        var freq = state.frequencyHz

        if (!state.isSynchronized) {
            // Free spinning acceleration/deceleration
            val targetNoLoadRpm = (gateFactor * 420f * mivFactor)
            rpm += (targetNoLoadRpm - rpm) * 0.12f
            freq = (rpm / RATED_RPM) * 50.0f
        } else {
            // Synchronized to electrical grid: locked near 375 RPM
            // Frequency deviation driven by supply vs demand imbalance
            val powerImbalance = mechanicalMw - state.totalFeederDemandMw
            val freqDelta = (powerImbalance * 0.0025f).coerceIn(-0.45f, 0.45f)
            freq = 50.00f + freqDelta
            rpm = (freq / 50.00f) * RATED_RPM
        }

        // Shaft mechanical torque: Tau = P / omega
        val omega = (2.0f * Math.PI.toFloat() * rpm) / 60.0f
        val torqueKNm = if (omega > 0.5f) (mechanicalMw * 1000f) / omega else 0f

        // Shaft vibration (increases if off-resonance or cavitation)
        val cavitationSigma = if (netHead > 10f) {
            (10.3f - (velocity * velocity / (2f * 9.81f))) / netHead
        } else 0.5f
        val cavitationRisk = if (cavitationSigma < 0.09f && gateFactor > 0.2f) 1.8f else 1.0f

        val rpmDeviation = abs(rpm - RATED_RPM) / RATED_RPM
        val baseVibration = 1.2f + (rpmDeviation * 3.5f) + (if (gateFactor < 0.35f && gateFactor > 0.05f) 0.8f else 0.0f)
        val vibrationMmS = (baseVibration * cavitationRisk).coerceIn(0.5f, 8.5f)

        // Bearing temperature heating / cooling
        val targetBearingTemp = 42.0f + (vibrationMmS * 6.2f) + (mechanicalMw * 0.06f)
        val bearingTemp = state.bearingTempC + (targetBearingTemp - state.bearingTempC) * 0.05f

        // Generator Electrical Stage
        val exciterRatio = (state.exciterCurrentAmps / 380.0f).coerceIn(0.2f, 1.3f)
        val terminalKv = if (rpm > 50f) RATED_TERMINAL_KV * exciterRatio * (rpm / RATED_RPM) else 0f

        val genEfficiency = 98.4f
        val activeMw = if (state.isSynchronized) {
            (mechanicalMw * (genEfficiency / 100f) * exciterRatio).coerceIn(0f, 320f)
        } else {
            0.0f
        }
        val reactiveMvar = if (state.isSynchronized) {
            (state.exciterCurrentAmps - 380.0f) * 0.45f
        } else 0.0f

        // Step-up Transformer Stage
        val secondaryKv = (terminalKv / RATED_TERMINAL_KV) * RATED_GRID_KV
        val transformerEfficiency = 99.1f
        val deliveredMw = activeMw * (transformerEfficiency / 100f)
        val targetOilTemp = 48.0f + (deliveredMw * 0.07f)
        val transformerOilTemp = state.transformerOilTempC + (targetOilTemp - state.transformerOilTempC) * 0.03f

        // Substation & Feeders
        val updatedFeeders = updateFeederDispatch(state.feeders, deliveredMw)
        val totalDispatched = updatedFeeders.filter { it.isConnected }.sumOf { it.allocatedMw.toDouble() }.toFloat()

        // Hourly Revenue rate calculation: $/hr = Sum(MW_i * $/MWh)
        val hourlyRevenue = updatedFeeders.filter { it.isConnected }.sumOf {
            (it.allocatedMw * it.tariffUsdPerMwh).toDouble()
        }.toFloat()

        val revenueStep = (hourlyRevenue / 3600.0) * dtSeconds
        val totalRevenue = state.accumulatedRevenueUsd + revenueStep
        val totalMwh = state.totalEnergyGeneratedMwh + ((deliveredMw / 3600.0) * dtSeconds)

        // Grid stability index
        val freqStability = (1.0f - min(1.0f, abs(freq - 50.00f) / 1.5f)) * 50f
        val balanceStability = (1.0f - min(1.0f, abs(deliveredMw - totalDispatched) / max(10f, deliveredMw))) * 50f
        val stabilityIndex = (freqStability + balanceStability).coerceIn(40f, 100f)

        // Health Degradation Tracking
        val wearFactor = if (vibrationMmS > 3.0f) 0.004f else 0.0005f
        val cavitationWear = if (cavitationSigma < 0.09f) 0.008f else 0.0002f

        val updatedLubeHealth = max(10f, state.shaftBearingLubricationHealth - wearFactor)
        val updatedRunnerHealth = max(15f, state.turbineRunnerHealth - cavitationWear)
        val updatedMivSeal = max(20f, state.mivSealIntegrity - 0.0001f)
        val updatedTransOil = max(25f, state.transformerDielectricOilHealth - (if (transformerOilTemp > 75f) 0.005f else 0.0002f))

        // Auto Diagnostic Alerts
        val newAlerts = evaluateDiagnostics(
            vibrationMmS,
            bearingTemp,
            freq,
            cavitationSigma,
            transformerOilTemp,
            state.alerts
        )

        // Status determination
        val status = when {
            vibrationMmS >= 6.5f || bearingTemp >= 80f || abs(freq - 50.0f) > 2.0f -> PlantStatus.TRIPPED
            vibrationMmS >= 3.8f || bearingTemp >= 68f || stabilityIndex < 80f -> PlantStatus.WARNING
            else -> PlantStatus.NORMAL
        }

        val autoTripped = status == PlantStatus.TRIPPED
        val tripReason = if (autoTripped) {
            when {
                vibrationMmS >= 6.5f -> "Shaft High Vibration Interlock (Trip > 6.5 mm/s)"
                bearingTemp >= 80f -> "Thrust Bearing High Temperature Trip (> 80°C)"
                else -> "Under/Over Frequency Protection Trip"
            }
        } else null

        return state.copy(
            status = status,
            isEmergencyTrip = autoTripped,
            tripReason = tripReason,
            simulationSeconds = state.simulationSeconds + 1,
            reservoirWaterLevelM = updatedLevel,
            grossHeadM = grossHead,
            netHeadM = netHead,
            penstockVelocityMs = velocity,
            penstockPressureBar = penstockPressure,
            waterHammerPressureBar = updatedHammerPressure,
            guideVanePercent = guideVanes,
            waterFlowRateQ = flowRateQ,
            turbineRpm = rpm,
            turbineTorqueKNm = torqueKNm,
            turbineEfficiency = turbineEfficiency,
            cavitationIndex = cavitationSigma,
            shaftVibrationMmS = vibrationMmS,
            bearingTempC = bearingTemp,
            terminalVoltageKv = terminalKv,
            frequencyHz = freq,
            activePowerMw = activeMw,
            reactivePowerMvar = reactiveMvar,
            secondaryVoltageKv = secondaryKv,
            transformerLoadMva = sqrt((activeMw * activeMw) + (reactiveMvar * reactiveMvar)),
            transformerOilTempC = transformerOilTemp,
            feeders = updatedFeeders,
            gridStabilityIndex = stabilityIndex,
            hourlyRevenueRateUsd = hourlyRevenue,
            accumulatedRevenueUsd = totalRevenue,
            totalEnergyGeneratedMwh = totalMwh,
            shaftBearingLubricationHealth = updatedLubeHealth,
            turbineRunnerHealth = updatedRunnerHealth,
            mivSealIntegrity = updatedMivSeal,
            transformerDielectricOilHealth = updatedTransOil,
            alerts = newAlerts,
            potentialEnergyMw = potentialMw,
            kineticEnergyMw = kineticMw,
            mechanicalPowerMw = mechanicalMw,
            electricalGeneratedMw = activeMw,
            deliveredGridMw = deliveredMw
        )
    }

    private fun processEmergencyTripTick(state: PlantState, dtSeconds: Float): PlantState {
        // Fast emergency closure of MIV and wicket gates
        val closingMiv = max(0f, state.mivOpeningPercent - (20f * dtSeconds))
        val closingGates = max(0f, state.guideVanePercent - (35f * dtSeconds))
        val decayingRpm = max(0f, state.turbineRpm - (45f * dtSeconds))
        val decayingFlow = max(0f, state.waterFlowRateQ - (30f * dtSeconds))
        val decayingMw = max(0f, state.activePowerMw - (80f * dtSeconds))

        // Water hammer spike when shut off abruptly
        val surgePressure = if (state.waterFlowRateQ > 20f && closingGates > 0f) 5.5f else 0f

        return state.copy(
            status = PlantStatus.TRIPPED,
            mivOpeningPercent = closingMiv,
            mivState = if (closingMiv <= 0f) MivState.CLOSED else MivState.CLOSING,
            guideVanePercent = closingGates,
            waterFlowRateQ = decayingFlow,
            turbineRpm = decayingRpm,
            activePowerMw = decayingMw,
            deliveredGridMw = max(0f, state.deliveredGridMw - (80f * dtSeconds)),
            waterHammerPressureBar = surgePressure,
            isSynchronized = false,
            frequencyHz = if (decayingRpm > 0f) (decayingRpm / RATED_RPM) * 50f else 0f
        )
    }

    private fun updateFeederDispatch(currentFeeders: List<GridFeeder>, totalAvailableMw: Float): List<GridFeeder> {
        val connectedCount = currentFeeders.count { it.isConnected }
        if (connectedCount == 0 || totalAvailableMw <= 0f) return currentFeeders

        var remainingMw = totalAvailableMw
        // Priority allocation: Metro (firm residential/hospital), then Industrial, then Intertie spot market
        return currentFeeders.map { feeder ->
            if (!feeder.isConnected) {
                feeder.copy(allocatedMw = 0f)
            } else {
                val desired = min(feeder.maxCapacityMw, remainingMw)
                remainingMw -= desired
                feeder.copy(allocatedMw = desired)
            }
        }
    }

    private fun evaluateDiagnostics(
        vibration: Float,
        bearingTemp: Float,
        freq: Float,
        cavitation: Float,
        oilTemp: Float,
        existing: List<MaintenanceLog>
    ): List<MaintenanceLog> {
        val updated = existing.toMutableList()

        if (vibration > 3.8f && updated.none { it.component == CoreComponent.SHAFT && !it.resolved }) {
            updated.add(
                0,
                MaintenanceLog(
                    id = "alert_vib_${System.currentTimeMillis()}",
                    timestamp = "Real-time Telemetry",
                    component = CoreComponent.SHAFT,
                    severity = "WARNING",
                    message = "Shaft guide bearing vibration elevated (${String.format("%.2f", vibration)} mm/s RMS). Recommend inspecting bearing oil film and shaft alignment."
                )
            )
        }

        if (cavitation < 0.085f && updated.none { it.component == CoreComponent.TURBINE && !it.resolved }) {
            updated.add(
                0,
                MaintenanceLog(
                    id = "alert_cavit_${System.currentTimeMillis()}",
                    timestamp = "Real-time Telemetry",
                    component = CoreComponent.TURBINE,
                    severity = "CRITICAL",
                    message = "Critical Thoma cavitation factor breach (σ = ${String.format("%.3f", cavitation)}). Risk of runner pitting and blade erosion."
                )
            )
        }

        if (oilTemp > 75.0f && updated.none { it.component == CoreComponent.TRANSFORMER && !it.resolved }) {
            updated.add(
                0,
                MaintenanceLog(
                    id = "alert_oil_${System.currentTimeMillis()}",
                    timestamp = "Real-time Telemetry",
                    component = CoreComponent.TRANSFORMER,
                    severity = "WARNING",
                    message = "GSU Step-Up Transformer top oil temperature elevated (${String.format("%.1f", oilTemp)}°C). Initiate radiator forced cooling fans."
                )
            )
        }

        return updated.take(15) // keep most recent 15
    }
}
