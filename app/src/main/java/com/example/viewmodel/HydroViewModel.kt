package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.HydroAiService
import com.example.model.AdvisorRecommendation
import com.example.model.ChatMessage
import com.example.model.CoreComponent
import com.example.model.MaintenanceBannerAlert
import com.example.model.MaintenanceLog
import com.example.model.MivState
import com.example.model.OperationalMode
import com.example.model.PlantScenario
import com.example.model.PlantState
import com.example.model.PlantStatus
import com.example.model.RecommendationCategory
import com.example.model.WearSeverity
import com.example.notification.TurbineWearNotificationManager
import com.example.service.TurbineWearMonitorBridge
import com.example.service.TurbineWearMonitorService
import com.example.simulation.HydroEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class HydroTab(val label: String) {
    SCHEMATIC("Digital Twin"),
    CONTROLS("SCADA Control"),
    DISTRIBUTION("Substation"),
    MAINTENANCE("Asset Health"),
    AI_ADVISOR("AI Advisor")
}

class HydroViewModel(
    private val aiService: HydroAiService = HydroAiService()
) : ViewModel() {

    private val _plantState = MutableStateFlow(PlantState())
    val plantState: StateFlow<PlantState> = _plantState.asStateFlow()

    private val _selectedComponent = MutableStateFlow<CoreComponent?>(null)
    val selectedComponent: StateFlow<CoreComponent?> = _selectedComponent.asStateFlow()

    private val _activeTab = MutableStateFlow(HydroTab.SCHEMATIC)
    val activeTab: StateFlow<HydroTab> = _activeTab.asStateFlow()

    private val _recommendations = MutableStateFlow<List<AdvisorRecommendation>>(emptyList())
    val recommendations: StateFlow<List<AdvisorRecommendation>> = _recommendations.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _scenarios = MutableStateFlow<List<PlantScenario>>(emptyList())
    val scenarios: StateFlow<List<PlantScenario>> = _scenarios.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    val activeBanners: StateFlow<List<MaintenanceBannerAlert>> = TurbineWearMonitorBridge.activeBanners
    val isWearMonitorRunning: StateFlow<Boolean> = TurbineWearMonitorBridge.isServiceRunning

    init {
        initScenarios()
        initInitialChat()
        refreshAiAnalysis()
        startSimulationEngine()
        listenToBridgeEvents()
    }

    private fun listenToBridgeEvents() {
        viewModelScope.launch {
            TurbineWearMonitorBridge.quickMaintenanceEvents.collect { actionType ->
                performMaintenanceAction(actionType)
            }
        }
    }

    private fun startSimulationEngine() {
        viewModelScope.launch {
            while (isActive) {
                delay(500)
                val updatedState = HydroEngine.tick(_plantState.value, dtSeconds = 0.5f)
                _plantState.value = updatedState
                evaluateTelemetryWearThresholds(updatedState)
            }
        }
    }

    private fun evaluateTelemetryWearThresholds(state: PlantState) {
        val currentAlertIds = TurbineWearMonitorBridge.activeBanners.value.map { it.id }.toSet()

        // 1. Francis Turbine Runner Cavitation Wear
        if (state.turbineRunnerHealth < 60f && !currentAlertIds.contains("alert_cavitation_runner")) {
            TurbineWearMonitorBridge.postAlert(
                MaintenanceBannerAlert(
                    id = "alert_cavitation_runner",
                    component = CoreComponent.TURBINE,
                    severity = if (state.turbineRunnerHealth < 45f) WearSeverity.CRITICAL else WearSeverity.WARNING,
                    title = "Runner Cavitation Wear Critical",
                    description = "Francis runner blade pitting detected (Cavitation σ: ${String.format("%.3f", state.cavitationIndex)}). Grinding and cladding required.",
                    currentHealthPercent = state.turbineRunnerHealth,
                    actionType = "CAVITATION",
                    actionLabel = "Hone Runner Blades"
                )
            )
        }

        // 2. Shaft Guide Bearing Lubrication & High Vibration
        if ((state.shaftBearingLubricationHealth < 60f || state.bearingTempC > 70f || state.shaftVibrationMmS > 3.8f) &&
            !currentAlertIds.contains("alert_shaft_bearing")) {
            TurbineWearMonitorBridge.postAlert(
                MaintenanceBannerAlert(
                    id = "alert_shaft_bearing",
                    component = CoreComponent.SHAFT,
                    severity = if (state.bearingTempC > 75f || state.shaftVibrationMmS > 4.5f) WearSeverity.CRITICAL else WearSeverity.WARNING,
                    title = "Thrust Bearing Lubrication Warning",
                    description = "Bearing temp at ${String.format("%.1f", state.bearingTempC)}°C with vibration at ${String.format("%.2f", state.shaftVibrationMmS)} mm/s. Oil film breakdown imminent.",
                    currentHealthPercent = state.shaftBearingLubricationHealth,
                    actionType = "LUBRICATION",
                    actionLabel = "Flush Bearing Lube"
                )
            )
        }

        // 3. Main Inlet Valve Seal Wear
        if (state.mivSealIntegrity < 65f && !currentAlertIds.contains("alert_miv_seal")) {
            TurbineWearMonitorBridge.postAlert(
                MaintenanceBannerAlert(
                    id = "alert_miv_seal",
                    component = CoreComponent.MAIN_INLET_VALVE,
                    severity = WearSeverity.WARNING,
                    title = "MIV Spherical Seal Degraded",
                    description = "Differential penstock pressure leakage across spherical valve disc. Replace upstream & downstream seals.",
                    currentHealthPercent = state.mivSealIntegrity,
                    actionType = "MIV",
                    actionLabel = "Refurbish MIV Seals"
                )
            )
        }

        // 4. GSU Transformer Dielectric Oil Breakdown
        if ((state.transformerDielectricOilHealth < 65f || state.transformerOilTempC > 75f) &&
            !currentAlertIds.contains("alert_transformer_dielectric")) {
            TurbineWearMonitorBridge.postAlert(
                MaintenanceBannerAlert(
                    id = "alert_transformer_dielectric",
                    component = CoreComponent.TRANSFORMER,
                    severity = WearSeverity.WARNING,
                    title = "Transformer Dielectric Oil Alert",
                    description = "Dielectric oil breakdown voltage degraded (Oil Temp: ${String.format("%.1f", state.transformerOilTempC)}°C). Vacuum degassing required.",
                    currentHealthPercent = state.transformerDielectricOilHealth,
                    actionType = "TRANSFORMER",
                    actionLabel = "Degas Transformer Oil"
                )
            )
        }
    }

    private fun initScenarios() {
        _scenarios.value = listOf(
            PlantScenario(
                id = "flood_surge",
                title = "Monsoon Flood Surge",
                subtitle = "Reservoir Rising Rapidly",
                iconName = "water",
                initialDescription = "Mountain torrential rainfall is causing river inflow to surge to 250 m³/s. Water head is 212m. Dispatch maximum MW and prevent dam crest overtopping.",
                reservoirInflow = 250f,
                initialWaterLevel = 212f,
                spotTariff = 98f,
                targetChallenge = "Ramp wicket gates to 90% and fully energize all substation feeders."
            ),
            PlantScenario(
                id = "peak_demand",
                title = "Grid Peak Demand Arbitrage",
                subtitle = "Spot Price Spike: $135/MWh",
                iconName = "attach_money",
                initialDescription = "Severe regional heatwave has driven grid electricity spot prices to $135/MWh. Maximize power generation while keeping thrust bearing temperature below 75°C.",
                reservoirInflow = 160f,
                initialWaterLevel = 195f,
                spotTariff = 135f,
                targetChallenge = "Deliver >270 MW to the grid to earn record revenue."
            ),
            PlantScenario(
                id = "islanding_trip",
                title = "Grid Islanding Drill",
                subtitle = "Frequency Stabilization",
                iconName = "warning",
                initialDescription = "A transmission fault disconnected the national intertie. External load collapsed. Stabilize governor droop quickly to maintain system frequency within 49.5 - 50.5 Hz.",
                reservoirInflow = 140f,
                initialWaterLevel = 188f,
                spotTariff = 85f,
                targetChallenge = "Switch to Auto-Governor mode and isolate unstable feeders."
            ),
            PlantScenario(
                id = "drought_cavitation",
                title = "Low-Head Drought Cavitation",
                subtitle = "Thoma Sigma Critical",
                iconName = "speed",
                initialDescription = "Extended dry season has lowered reservoir head to 155m. Operating at mid gate openings poses high risk of runner cavitation pitting. Trim flow carefully.",
                reservoirInflow = 65f,
                initialWaterLevel = 155f,
                spotTariff = 110f,
                targetChallenge = "Keep cavitation index σ > 0.09 and vibration < 3.0 mm/s."
            ),
            PlantScenario(
                id = "cold_black_start",
                title = "Cold Station Black Start",
                subtitle = "Plant Re-energization",
                iconName = "refresh",
                initialDescription = "Plant is in a cold shutdown state (MIV closed, 0 RPM). Execute the proper start sequence: Equalize bypass valve -> Open MIV -> Roll turbine -> Synchronize to grid.",
                reservoirInflow = 150f,
                initialWaterLevel = 190f,
                spotTariff = 75f,
                targetChallenge = "Achieve 375 RPM, 13.8 kV, and lock synchronizer breaker in phase."
            )
        )
    }

    private fun initInitialChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                id = "chat_welcome",
                isUser = false,
                message = "Welcome to the HydroPlant SCADA AI Advisor. I am continuously monitoring your reservoir head, penstock pressure transients, Francis runner cavitation parameters, shaft vibration spectrum, and 230 kV substation feeder dispatch. How can I assist your operations today?",
                timestamp = "SCADA Online"
            )
        )
    }

    fun selectComponent(component: CoreComponent?) {
        _selectedComponent.value = component
    }

    fun setActiveTab(tab: HydroTab) {
        _activeTab.value = tab
    }

    fun setOperationalMode(mode: OperationalMode) {
        _plantState.value = _plantState.value.copy(operationalMode = mode)
    }

    fun updateIntakeGate(percent: Float) {
        _plantState.value = _plantState.value.copy(intakeGatePercent = percent.coerceIn(0f, 100f))
    }

    fun updateMivOpening(percent: Float) {
        val opening = percent.coerceIn(0f, 100f)
        val state = when {
            opening <= 0f -> MivState.CLOSED
            opening >= 99f -> MivState.OPEN
            opening > _plantState.value.mivOpeningPercent -> MivState.OPENING
            else -> MivState.CLOSING
        }
        _plantState.value = _plantState.value.copy(
            mivOpeningPercent = opening,
            mivState = state
        )
    }

    fun toggleBypassValve() {
        val current = _plantState.value.bypassValveOpen
        _plantState.value = _plantState.value.copy(bypassValveOpen = !current)
    }

    fun updateGuideVanes(percent: Float) {
        _plantState.value = _plantState.value.copy(guideVanePercent = percent.coerceIn(0f, 100f))
    }

    fun updateExciterCurrent(amps: Float) {
        _plantState.value = _plantState.value.copy(exciterCurrentAmps = amps.coerceIn(200f, 480f))
    }

    fun toggleSynchronize() {
        val currentSync = _plantState.value.isSynchronized
        val freqSlip = kotlin.math.abs(_plantState.value.frequencyHz - 50.00f)

        if (!currentSync) {
            // Check sync criteria
            if (freqSlip < 0.25f && _plantState.value.terminalVoltageKv > 10.0f) {
                _plantState.value = _plantState.value.copy(isSynchronized = true)
            } else {
                // Flash trip warning if closed out of phase
                _plantState.value = _plantState.value.copy(
                    isSynchronized = true,
                    shaftVibrationMmS = _plantState.value.shaftVibrationMmS + 2.5f
                )
            }
        } else {
            // Disconnect from grid
            _plantState.value = _plantState.value.copy(isSynchronized = false)
        }
    }

    fun emergencyTrip() {
        _plantState.value = _plantState.value.copy(
            isEmergencyTrip = true,
            status = PlantStatus.TRIPPED,
            tripReason = "Manual Operator Emergency Trip Pushbutton Engaged"
        )
    }

    fun resetTrip() {
        _plantState.value = _plantState.value.copy(
            isEmergencyTrip = false,
            status = PlantStatus.NORMAL,
            tripReason = null,
            mivOpeningPercent = 80f,
            mivState = MivState.OPEN,
            guideVanePercent = 65f,
            isSynchronized = true
        )
    }

    fun toggleFeeder(feederId: String) {
        val updatedFeeders = _plantState.value.feeders.map {
            if (it.id == feederId) it.copy(isConnected = !it.isConnected) else it
        }
        _plantState.value = _plantState.value.copy(feeders = updatedFeeders)
    }

    fun updateFeederAllocation(feederId: String, mw: Float) {
        val updatedFeeders = _plantState.value.feeders.map {
            if (it.id == feederId) it.copy(allocatedMw = mw.coerceIn(0f, it.maxCapacityMw)) else it
        }
        _plantState.value = _plantState.value.copy(feeders = updatedFeeders)
    }

    fun performMaintenance(component: CoreComponent) {
        when (component) {
            CoreComponent.MAIN_INLET_VALVE -> {
                _plantState.value = _plantState.value.copy(mivSealIntegrity = 100f)
            }
            CoreComponent.TURBINE -> {
                _plantState.value = _plantState.value.copy(turbineRunnerHealth = 100f)
            }
            CoreComponent.SHAFT -> {
                _plantState.value = _plantState.value.copy(
                    shaftBearingLubricationHealth = 100f,
                    bearingTempC = 42.0f,
                    shaftVibrationMmS = 1.2f
                )
            }
            CoreComponent.TRANSFORMER -> {
                _plantState.value = _plantState.value.copy(
                    transformerDielectricOilHealth = 100f,
                    transformerOilTempC = 48.0f
                )
            }
            CoreComponent.GENERATOR -> {
                _plantState.value = _plantState.value.copy(generatorInsulationHealth = 100f)
            }
            else -> {}
        }
    }

    fun performMaintenanceAction(actionType: String) {
        when (actionType) {
            "LUBRICATION" -> {
                _plantState.value = _plantState.value.copy(
                    shaftBearingLubricationHealth = 100f,
                    shaftVibrationMmS = 1.2f,
                    bearingTempC = 43.0f
                )
            }
            "CAVITATION" -> {
                _plantState.value = _plantState.value.copy(turbineRunnerHealth = 100f)
            }
            "TRANSFORMER" -> {
                _plantState.value = _plantState.value.copy(
                    transformerDielectricOilHealth = 100f,
                    transformerOilTempC = 48.0f
                )
            }
            "MIV" -> {
                _plantState.value = _plantState.value.copy(mivSealIntegrity = 100f)
            }
        }
    }

    fun resolveAlert(alertId: String) {
        val updated = _plantState.value.alerts.map {
            if (it.id == alertId) it.copy(resolved = true) else it
        }.filter { !it.resolved }
        _plantState.value = _plantState.value.copy(alerts = updated)
    }

    fun loadScenario(scenario: PlantScenario) {
        when (scenario.id) {
            "flood_surge" -> {
                _plantState.value = _plantState.value.copy(
                    riverInflowM3s = 250f,
                    reservoirWaterLevelM = 212f,
                    intakeGatePercent = 100f,
                    mivOpeningPercent = 100f,
                    guideVanePercent = 88f,
                    isEmergencyTrip = false,
                    status = PlantStatus.NORMAL
                )
            }
            "peak_demand" -> {
                val updatedFeeders = _plantState.value.feeders.map {
                    if (it.id == "feeder_spot") it.copy(tariffUsdPerMwh = 135f, isConnected = true) else it
                }
                _plantState.value = _plantState.value.copy(
                    feeders = updatedFeeders,
                    intakeGatePercent = 100f,
                    mivOpeningPercent = 100f,
                    guideVanePercent = 90f,
                    isEmergencyTrip = false,
                    status = PlantStatus.NORMAL
                )
            }
            "islanding_trip" -> {
                val updatedFeeders = _plantState.value.feeders.map {
                    if (it.id == "feeder_spot") it.copy(isConnected = false, allocatedMw = 0f) else it
                }
                _plantState.value = _plantState.value.copy(
                    feeders = updatedFeeders,
                    operationalMode = OperationalMode.AUTO_GOVERNOR
                )
            }
            "drought_cavitation" -> {
                _plantState.value = _plantState.value.copy(
                    reservoirWaterLevelM = 155f,
                    riverInflowM3s = 65f,
                    guideVanePercent = 38f
                )
            }
            "cold_black_start" -> {
                _plantState.value = _plantState.value.copy(
                    intakeGatePercent = 0f,
                    mivOpeningPercent = 0f,
                    mivState = MivState.CLOSED,
                    bypassValveOpen = false,
                    guideVanePercent = 0f,
                    exciterCurrentAmps = 200f,
                    isSynchronized = false,
                    turbineRpm = 0f,
                    waterFlowRateQ = 0f,
                    activePowerMw = 0f,
                    deliveredGridMw = 0f,
                    isEmergencyTrip = false,
                    status = PlantStatus.NORMAL
                )
            }
        }
        refreshAiAnalysis()
    }

    fun refreshAiAnalysis() {
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val recs = aiService.generateRecommendations(_plantState.value)
                _recommendations.value = recs
            } catch (e: Exception) {
                _recommendations.value = aiService.generateDeterministicRecommendations(_plantState.value)
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun askAi(question: String) {
        val userMsg = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            isUser = true,
            message = question,
            timestamp = "Just now"
        )
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isAiLoading.value = true
            val answer = aiService.askHydroAssistant(question, _plantState.value)
            val aiMsg = ChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                isUser = false,
                message = answer,
                timestamp = "Just now"
            )
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiLoading.value = false
        }
    }

    fun dismissBannerAlert(alertId: String) {
        TurbineWearMonitorBridge.dismissAlert(alertId)
    }

    fun quickServiceAlert(alert: MaintenanceBannerAlert, context: Context?) {
        performMaintenanceAction(alert.actionType)
        TurbineWearMonitorBridge.dismissAlert(alert.id)
        if (context != null) {
            val notifId = when (alert.component) {
                CoreComponent.TURBINE -> 1001
                CoreComponent.SHAFT -> 1002
                CoreComponent.MAIN_INLET_VALVE -> 1003
                CoreComponent.TRANSFORMER -> 1004
                else -> 1005
            }
            TurbineWearNotificationManager.dismissNotification(context, notifId)
        }
    }

    fun toggleBackgroundMonitoring(context: Context) {
        if (TurbineWearMonitorBridge.isServiceRunning.value) {
            TurbineWearMonitorService.stop(context)
        } else {
            TurbineWearMonitorService.start(context)
        }
    }

    fun triggerSimulatedWear(component: CoreComponent, context: Context?) {
        // Degrade component health in plant state
        when (component) {
            CoreComponent.TURBINE -> {
                _plantState.value = _plantState.value.copy(
                    turbineRunnerHealth = 44.0f,
                    cavitationIndex = 0.078f
                )
            }
            CoreComponent.SHAFT -> {
                _plantState.value = _plantState.value.copy(
                    shaftBearingLubricationHealth = 48.0f,
                    bearingTempC = 74.5f,
                    shaftVibrationMmS = 4.4f
                )
            }
            CoreComponent.MAIN_INLET_VALVE -> {
                _plantState.value = _plantState.value.copy(
                    mivSealIntegrity = 55.0f
                )
            }
            CoreComponent.TRANSFORMER -> {
                _plantState.value = _plantState.value.copy(
                    transformerDielectricOilHealth = 52.0f,
                    transformerOilTempC = 78.0f
                )
            }
            else -> {}
        }

        if (context != null) {
            TurbineWearMonitorService.triggerSimulatedWearAlert(context, component)
        } else {
            evaluateTelemetryWearThresholds(_plantState.value)
        }
    }
}
