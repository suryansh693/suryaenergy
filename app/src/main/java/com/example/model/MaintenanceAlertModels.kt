package com.example.model

enum class WearSeverity {
    WARNING,
    CRITICAL
}

data class MaintenanceBannerAlert(
    val id: String,
    val component: CoreComponent,
    val severity: WearSeverity,
    val title: String,
    val description: String,
    val currentHealthPercent: Float,
    val actionType: String,
    val actionLabel: String = "Service Component",
    val timestampMs: Long = System.currentTimeMillis()
)
