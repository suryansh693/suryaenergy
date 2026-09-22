package com.example.model

enum class OperationalMode(val title: String) {
    MANUAL("Manual SCADA"),
    AUTO_GOVERNOR("Auto Frequency Governor"),
    AI_OPTIMIZED("AI Smart Dispatch")
}

enum class PlantStatus(val label: String) {
    NORMAL("Normal Operations"),
    WARNING("Operational Advisory"),
    TRIPPED("Emergency Trip Shutdown")
}

enum class MivState {
    CLOSED,
    OPENING,
    OPEN,
    CLOSING,
    TRIPPED
}

data class GridFeeder(
    val id: String,
    val name: String,
    val customerType: String,
    val allocatedMw: Float,
    val maxCapacityMw: Float,
    val tariffUsdPerMwh: Float,
    val isConnected: Boolean,
    val description: String
)

data class MaintenanceLog(
    val id: String,
    val timestamp: String,
    val component: CoreComponent,
    val severity: String,
    val message: String,
    val resolved: Boolean = false
)

data class PlantState(
    // Global & Operational
    val status: PlantStatus = PlantStatus.NORMAL,
    val operationalMode: OperationalMode = OperationalMode.AUTO_GOVERNOR,
    val isEmergencyTrip: Boolean = false,
    val tripReason: String? = null,
    val simulationSeconds: Long = 0L,

    // Dam & Reservoir
    val reservoirWaterLevelM: Float = 192.5f, // Range: 140 - 220 m
    val riverInflowM3s: Float = 145.0f,
    val intakeGatePercent: Float = 90.0f, // 0 - 100%
    val reservoirStorageMcm: Float = 840.0f,

    // Penstock
    val penstockVelocityMs: Float = 5.2f,
    val grossHeadM: Float = 192.5f,
    val netHeadM: Float = 186.8f,
    val penstockPressureBar: Float = 18.3f,
    val waterHammerPressureBar: Float = 0.0f,

    // Main Inlet Valve (MIV)
    val mivOpeningPercent: Float = 100.0f, // 0 - 100%
    val mivState: MivState = MivState.OPEN,
    val bypassValveOpen: Boolean = false,

    // Turbine (Francis)
    val guideVanePercent: Float = 75.0f, // Governor wicket gate opening 0 - 100%
    val waterFlowRateQ: Float = 135.0f, // m^3 / s
    val turbineRpm: Float = 375.0f, // Rated 375 RPM
    val turbineTorqueKNm: Float = 5450.0f,
    val turbineEfficiency: Float = 92.8f, // %
    val cavitationIndex: Float = 0.14f, // Safe > 0.09

    // Shaft
    val shaftVibrationMmS: Float = 1.45f, // mm/s RMS (normal < 2.5)
    val bearingTempC: Float = 54.2f, // Celsius
    val lubeOilPressureBar: Float = 4.8f,

    // Generator (Alternator)
    val exciterCurrentAmps: Float = 380.0f,
    val terminalVoltageKv: Float = 13.8f,
    val frequencyHz: Float = 50.00f, // Target 50.00 Hz
    val activePowerMw: Float = 215.0f, // Up to 300 MW
    val reactivePowerMvar: Float = 28.5f,
    val powerFactor: Float = 0.98f,
    val isSynchronized: Boolean = true,
    val generatorStatorTempC: Float = 72.0f,

    // Transformer (Step-Up)
    val secondaryVoltageKv: Float = 230.0f,
    val transformerLoadMva: Float = 218.0f,
    val transformerOilTempC: Float = 58.5f,
    val transformerEfficiency: Float = 99.1f,

    // Power House
    val drainageSumpLevelPercent: Float = 28.0f,
    val machineHallTempC: Float = 23.5f,

    // Substation & Grid Feeders
    val feeders: List<GridFeeder> = listOf(
        GridFeeder(
            id = "f_ind",
            name = "Industrial Heavy Corridor",
            customerType = "Smelters & Steelworks",
            allocatedMw = 95.0f,
            maxCapacityMw = 140.0f,
            tariffUsdPerMwh = 76.50f,
            isConnected = true,
            description = "Continuous baseload demand with firm contractual supply."
        ),
        GridFeeder(
            id = "f_metro",
            name = "Metro City Municipality",
            customerType = "Urban Residential & Hospitals",
            allocatedMw = 75.0f,
            maxCapacityMw = 110.0f,
            tariffUsdPerMwh = 92.00f,
            isConnected = true,
            description = "High peak morning and evening residential tariff."
        ),
        GridFeeder(
            id = "f_intertie",
            name = "National HV Intertie",
            customerType = "Wholesale Spot Arbitrage",
            allocatedMw = 45.0f,
            maxCapacityMw = 90.0f,
            tariffUsdPerMwh = 68.00f,
            isConnected = true,
            description = "Spot wholesale market with flexible real-time dispatch pricing."
        )
    ),
    val gridStabilityIndex: Float = 98.4f, // 0 - 100%
    val hourlyRevenueRateUsd: Float = 17235.0f,
    val accumulatedRevenueUsd: Double = 148920.0,
    val totalEnergyGeneratedMwh: Double = 1940.5,

    // Maintenance Health Telemetry (0 - 100%, 100% = Brand new)
    val mivSealIntegrity: Float = 94.0f,
    val turbineRunnerHealth: Float = 88.5f,
    val shaftBearingLubricationHealth: Float = 92.0f,
    val generatorInsulationHealth: Float = 96.0f,
    val transformerDielectricOilHealth: Float = 91.0f,
    val penstockWallIntegrity: Float = 97.5f,

    // Maintenance logs & alerts
    val alerts: List<MaintenanceLog> = emptyList(),

    // Energy conversion stages for real-time visualization (in MW equivalent)
    val potentialEnergyMw: Float = 250.0f,
    val kineticEnergyMw: Float = 242.0f,
    val mechanicalPowerMw: Float = 224.5f,
    val electricalGeneratedMw: Float = 215.0f,
    val deliveredGridMw: Float = 213.1f
) {
    val totalFeederDemandMw: Float
        get() = feeders.filter { it.isConnected }.sumOf { it.allocatedMw.toDouble() }.toFloat()

    val netGridBalanceMw: Float
        get() = activePowerMw - totalFeederDemandMw

    val overallPlantEfficiency: Float
        get() = if (potentialEnergyMw > 0f) (deliveredGridMw / potentialEnergyMw) * 100f else 0f
}
