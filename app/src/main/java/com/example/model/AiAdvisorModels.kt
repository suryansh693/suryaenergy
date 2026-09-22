package com.example.model

enum class RecommendationCategory(val label: String) {
    DISPATCH("Power Dispatch"),
    MAINTENANCE("Predictive Maintenance"),
    GRID_STABILITY("Grid Frequency & Stability"),
    EFFICIENCY("Hydraulic Optimization")
}

data class AdvisorRecommendation(
    val id: String,
    val category: RecommendationCategory,
    val title: String,
    val summary: String,
    val technicalRationale: String,
    val recommendedActions: List<String>,
    val estimatedFinancialImpact: String,
    val urgencyLevel: String // "LOW", "MEDIUM", "HIGH", "CRITICAL"
)

data class PlantScenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconName: String,
    val initialDescription: String,
    val reservoirInflow: Float,
    val initialWaterLevel: Float,
    val spotTariff: Float,
    val targetChallenge: String
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val message: String,
    val timestamp: String,
    val technicalTags: List<String> = emptyList()
)
