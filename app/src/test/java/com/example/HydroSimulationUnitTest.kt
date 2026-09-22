package com.example

import com.example.model.CoreComponent
import com.example.model.OperationalMode
import com.example.model.PlantState
import com.example.model.PlantStatus
import com.example.simulation.HydroEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HydroSimulationUnitTest {

    @Test
    fun testCoreComponentsCount() {
        assertEquals("All 9 required core components must be modeled", 9, CoreComponent.values().size)
        val names = CoreComponent.values().map { it.name }
        assertTrue(names.contains("DAM"))
        assertTrue(names.contains("PENSTOCK"))
        assertTrue(names.contains("MAIN_INLET_VALVE"))
        assertTrue(names.contains("TURBINE"))
        assertTrue(names.contains("SHAFT"))
        assertTrue(names.contains("GENERATOR"))
        assertTrue(names.contains("TRANSFORMER"))
        assertTrue(names.contains("POWER_HOUSE"))
        assertTrue(names.contains("SUBSTATION"))
    }

    @Test
    fun testNormalSimulationTickProducesPower() {
        val initialState = PlantState(
            intakeGatePercent = 100f,
            mivOpeningPercent = 100f,
            guideVanePercent = 82f,
            isSynchronized = true,
            isEmergencyTrip = false
        )

        val nextState = HydroEngine.tick(initialState, dtSeconds = 1.0f)

        assertTrue("Water flow rate should be positive", nextState.waterFlowRateQ > 50f)
        assertTrue("Mechanical power should be generated", nextState.mechanicalPowerMw > 50f)
        assertTrue("Active electrical power should be delivered", nextState.activePowerMw > 50f)
        assertTrue("Grid revenue should accumulate", nextState.accumulatedRevenueUsd > 0.0)
        assertEquals(PlantStatus.NORMAL, nextState.status)
    }

    @Test
    fun testEmergencyTripClosesValvesAndDeEnergizes() {
        val runningState = PlantState(
            intakeGatePercent = 100f,
            mivOpeningPercent = 100f,
            guideVanePercent = 85f,
            activePowerMw = 220f,
            waterFlowRateQ = 140f,
            isEmergencyTrip = true,
            status = PlantStatus.TRIPPED
        )

        val trippedState = HydroEngine.tick(runningState, dtSeconds = 1.0f)

        assertTrue("MIV should be rapidly closing", trippedState.mivOpeningPercent < 100f)
        assertTrue("Guide vanes should be closing", trippedState.guideVanePercent < 85f)
        assertTrue("Tripped state should be preserved", trippedState.isEmergencyTrip)
        assertEquals(PlantStatus.TRIPPED, trippedState.status)
    }

    @Test
    fun testSubstationFeederDispatchesPower() {
        val state = PlantState(
            activePowerMw = 250f,
            deliveredGridMw = 245f
        )
        val tickedState = HydroEngine.tick(state, dtSeconds = 1.0f)

        val totalAllocated = tickedState.feeders.filter { it.isConnected }.sumOf { it.allocatedMw.toDouble() }
        assertTrue("Feeders should absorb dispatched power", totalAllocated > 100.0)
        assertTrue("Hourly revenue should reflect customer tariffs", tickedState.hourlyRevenueRateUsd > 1000f)
    }
}
