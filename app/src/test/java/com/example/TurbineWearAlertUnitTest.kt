package com.example

import com.example.model.CoreComponent
import com.example.model.MaintenanceBannerAlert
import com.example.model.WearSeverity
import com.example.service.TurbineWearMonitorBridge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TurbineWearAlertUnitTest {

    @Before
    fun setUp() {
        TurbineWearMonitorBridge.clearAllAlerts()
        TurbineWearMonitorBridge.setServiceRunning(false)
    }

    @Test
    fun testTurbineWearBridgePostAndDismiss() {
        assertEquals(0, TurbineWearMonitorBridge.activeBanners.value.size)

        val alert = MaintenanceBannerAlert(
            id = "test_cavitation_1",
            component = CoreComponent.TURBINE,
            severity = WearSeverity.CRITICAL,
            title = "Francis Runner Cavitation Alert",
            description = "Blade pitting detected due to aggressive low water head operation.",
            currentHealthPercent = 42.5f,
            actionType = "CAVITATION",
            actionLabel = "Hone Runner Blades"
        )

        TurbineWearMonitorBridge.postAlert(alert)
        assertEquals(1, TurbineWearMonitorBridge.activeBanners.value.size)
        assertEquals("test_cavitation_1", TurbineWearMonitorBridge.activeBanners.value.first().id)
        assertEquals(CoreComponent.TURBINE, TurbineWearMonitorBridge.activeBanners.value.first().component)

        // Dismiss alert
        TurbineWearMonitorBridge.dismissAlert("test_cavitation_1")
        assertEquals(0, TurbineWearMonitorBridge.activeBanners.value.size)
    }

    @Test
    fun testBackgroundServiceStateToggle() {
        assertFalse(TurbineWearMonitorBridge.isServiceRunning.value)
        TurbineWearMonitorBridge.setServiceRunning(true)
        assertTrue(TurbineWearMonitorBridge.isServiceRunning.value)
        TurbineWearMonitorBridge.setServiceRunning(false)
        assertFalse(TurbineWearMonitorBridge.isServiceRunning.value)
    }

    @Test
    fun testMultipleComponentAlertsDeduplication() {
        val alert1 = MaintenanceBannerAlert(
            id = "alert_1",
            component = CoreComponent.TURBINE,
            severity = WearSeverity.CRITICAL,
            title = "Cavitation Alert",
            description = "Turbine wear high",
            currentHealthPercent = 45f,
            actionType = "CAVITATION",
            actionLabel = "Hone"
        )
        val alert2 = MaintenanceBannerAlert(
            id = "alert_2",
            component = CoreComponent.SHAFT,
            severity = WearSeverity.WARNING,
            title = "Bearing Overheat",
            description = "Guide bearing lube hot",
            currentHealthPercent = 55f,
            actionType = "LUBRICATION",
            actionLabel = "Flush"
        )

        TurbineWearMonitorBridge.postAlert(alert1)
        TurbineWearMonitorBridge.postAlert(alert2)

        assertEquals(2, TurbineWearMonitorBridge.activeBanners.value.size)

        // Posting update with same id replaces cleanly
        val alert1Updated = alert1.copy(currentHealthPercent = 38f)
        TurbineWearMonitorBridge.postAlert(alert1Updated)
        assertEquals(2, TurbineWearMonitorBridge.activeBanners.value.size)
        assertEquals(38f, TurbineWearMonitorBridge.activeBanners.value.first { it.id == "alert_1" }.currentHealthPercent)
    }
}
