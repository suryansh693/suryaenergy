package com.example.service

import com.example.model.CoreComponent
import com.example.model.MaintenanceBannerAlert
import com.example.model.PlantState
import com.example.model.WearSeverity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object TurbineWearMonitorBridge {

    private val _activeBanners = MutableStateFlow<List<MaintenanceBannerAlert>>(emptyList())
    val activeBanners: StateFlow<List<MaintenanceBannerAlert>> = _activeBanners.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    // Shared flow for triggering external actions
    private val _quickMaintenanceEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val quickMaintenanceEvents: SharedFlow<String> = _quickMaintenanceEvents.asSharedFlow()

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }

    fun postAlert(alert: MaintenanceBannerAlert) {
        val current = _activeBanners.value.filter { it.id != alert.id }
        _activeBanners.value = listOf(alert) + current
    }

    fun dismissAlert(alertId: String) {
        _activeBanners.value = _activeBanners.value.filter { it.id != alertId }
    }

    fun clearAllAlerts() {
        _activeBanners.value = emptyList()
    }

    fun requestQuickMaintenance(actionType: String, alertId: String) {
        _quickMaintenanceEvents.tryEmit(actionType)
        dismissAlert(alertId)
    }
}
