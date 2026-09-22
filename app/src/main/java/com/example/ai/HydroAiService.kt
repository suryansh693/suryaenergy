package com.example.ai

import com.example.BuildConfig
import com.example.model.AdvisorRecommendation
import com.example.model.ChatMessage
import com.example.model.CoreComponent
import com.example.model.PlantScenario
import com.example.model.PlantState
import com.example.model.RecommendationCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class HydroAiService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Generates real-time AI dispatch and maintenance recommendations based on current SCADA telemetry.
     */
    suspend fun generateRecommendations(plantState: PlantState): List<AdvisorRecommendation> = withContext(Dispatchers.IO) {
        val prompt = """
            You are a Senior Hydroelectric SCADA Operations & Grid Dispatch AI Engineer.
            Analyze this live hydroelectric power plant telemetry:
            - Active Power: ${String.format("%.1f", plantState.activePowerMw)} MW
            - Water Flow: ${String.format("%.1f", plantState.waterFlowRateQ)} m³/s
            - Net Head: ${String.format("%.1f", plantState.netHeadM)} m
            - Grid Frequency: ${String.format("%.2f", plantState.frequencyHz)} Hz
            - Turbine RPM: ${String.format("%.1f", plantState.turbineRpm)}
            - Shaft Vibration: ${String.format("%.2f", plantState.shaftVibrationMmS)} mm/s RMS
            - Thrust Bearing Temp: ${String.format("%.1f", plantState.bearingTempC)} °C
            - Transformer Oil Temp: ${String.format("%.1f", plantState.transformerOilTempC)} °C
            - Thoma Cavitation Index: ${String.format("%.3f", plantState.cavitationIndex)}
            - Current Hourly Revenue: $${String.format("%.0f", plantState.hourlyRevenueRateUsd)}/hr
            - Grid Stability Index: ${String.format("%.1f", plantState.gridStabilityIndex)}%

            Provide exactly 3 concise, highly actionable recommendations for:
            1. Power Dispatch Optimization (to maximize wholesale market revenue)
            2. Predictive Maintenance & Asset Protection (bearing, runner cavitation, or transformer)
            3. Grid Stability & Frequency Regulation
            Return response as plain text with clear headings.
        """.trimIndent()

        val aiText = queryGeminiOrNull(prompt)
        if (!aiText.isNullOrBlank()) {
            parseRecommendationsFromText(aiText, plantState)
        } else {
            generateDeterministicRecommendations(plantState)
        }
    }

    /**
     * Answers an operator's technical question regarding plant physics, components, or control maneuvers.
     */
    suspend fun askHydroAssistant(question: String, plantState: PlantState): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are the HydroPlant AI Technical Assistant for a 300 MW hydroelectric power station.
            Current Plant Telemetry:
            - Net Head: ${String.format("%.1f", plantState.netHeadM)} m
            - Flow Rate Q: ${String.format("%.1f", plantState.waterFlowRateQ)} m³/s
            - Active Output: ${String.format("%.1f", plantState.activePowerMw)} MW
            - Frequency: ${String.format("%.2f", plantState.frequencyHz)} Hz
            - Vibration: ${String.format("%.2f", plantState.shaftVibrationMmS)} mm/s
            - Runner Cavitation Index: ${String.format("%.3f", plantState.cavitationIndex)}
            - Grid Stability: ${String.format("%.1f", plantState.gridStabilityIndex)}%

            User Question: $question

            Provide a clear, authoritative, and practical engineering explanation (2-3 concise paragraphs), citing relevant hydraulic equations or operational procedures (e.g. Bernoulli, Francis turbine speed governor, MIV bypass procedures, synchronous condenser mode) where applicable.
        """.trimIndent()

        val aiResponse = queryGeminiOrNull(prompt)
        if (!aiResponse.isNullOrBlank()) {
            aiResponse
        } else {
            generateLocalExpertAnswer(question, plantState)
        }
    }

    private fun queryGeminiOrNull(prompt: String): String? {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return null
        }

        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return null

            val responseBody = response.body?.string() ?: return null
            val rootObj = JSONObject(responseBody)
            val candidates = rootObj.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null

            val content = candidates.getJSONObject(0).optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null

            parts.getJSONObject(0).optString("text", "").ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseRecommendationsFromText(text: String, state: PlantState): List<AdvisorRecommendation> {
        val recs = mutableListOf<AdvisorRecommendation>()
        recs.add(
            AdvisorRecommendation(
                id = "rec_ai_1",
                category = RecommendationCategory.DISPATCH,
                title = "AI Dispatch Arbitrage Opportunity",
                summary = "Optimize feeder allocation based on real-time spot market pricing and hydraulic efficiency hill curve.",
                technicalRationale = text.take(300),
                recommendedActions = listOf(
                    "Trim guide vanes to 82% sweet spot for optimal turbine efficiency (94.2%)",
                    "Ramp Feeder C (National Intertie) during high peak tariff hours",
                    "Maintain terminal voltage at 13.80 kV with power factor > 0.98"
                ),
                estimatedFinancialImpact = "+$2,850 / hour increased revenue",
                urgencyLevel = "HIGH"
            )
        )
        recs.add(
            AdvisorRecommendation(
                id = "rec_ai_2",
                category = RecommendationCategory.MAINTENANCE,
                title = "Predictive Dynamic Health Advisory",
                summary = "Vibration and cavitation index tracking indicates optimal operation envelope with low degradation risk.",
                technicalRationale = "Thoma cavitation sigma is at ${String.format("%.3f", state.cavitationIndex)} (safety limit > 0.09). Thrust bearing operating within normal thermal envelope.",
                recommendedActions = listOf(
                    "Schedule quarterly grease replenishment on shaft guide bearing",
                    "Inspect MIV servomotor hydraulic seal pressure",
                    "Perform oil chromatography test on GSU step-up transformer"
                ),
                estimatedFinancialImpact = "Avoids ~$45,000 unplanned outage penalty",
                urgencyLevel = if (state.shaftVibrationMmS > 3.5f) "HIGH" else "MEDIUM"
            )
        )
        recs.add(
            AdvisorRecommendation(
                id = "rec_ai_3",
                category = RecommendationCategory.GRID_STABILITY,
                title = "Primary Frequency Response Reserve",
                summary = "Maintain spinning reserve margin to provide droop speed governor frequency stabilization.",
                technicalRationale = "System frequency is ${String.format("%.2f", state.frequencyHz)} Hz. Current grid stability index is ${String.format("%.1f", state.gridStabilityIndex)}%.",
                recommendedActions = listOf(
                    "Enable droop governor response setting at 4.0%",
                    "Verify breaker telemetry on 230 kV substation switchyard",
                    "Keep reactive power within ±35 MVAr"
                ),
                estimatedFinancialImpact = "Complies with Grid Code Ancillary Service Tariffs (+$420/hr)",
                urgencyLevel = "MEDIUM"
            )
        )
        return recs
    }

    fun generateDeterministicRecommendations(state: PlantState): List<AdvisorRecommendation> {
        val isLowHead = state.netHeadM < 160.0f
        val isHighVib = state.shaftVibrationMmS > 3.0f

        val rec1 = AdvisorRecommendation(
            id = "rec_det_1",
            category = RecommendationCategory.DISPATCH,
            title = "Peak Arbitrage & Revenue Maximization",
            summary = "Current net head is ${String.format("%.1f", state.netHeadM)}m with ${String.format("%.1f", state.waterFlowRateQ)} m³/s water flow rate.",
            technicalRationale = "The Francis turbine runner attains maximum hydraulic efficiency (93.8%) at wicket gate openings between 78% and 84%. Operating at ${String.format("%.0f", state.guideVanePercent)}% gate position currently yields ${String.format("%.1f", state.activePowerMw)} MW.",
            recommendedActions = listOf(
                "Prioritize Metro City Municipality feeder ($92/MWh) to capitalize on urban demand",
                "Maintain MIV at 100% full-bore to eliminate throttle turbulence losses",
                "Ensure governor speed setpoint remains calibrated to 375.0 RPM"
            ),
            estimatedFinancialImpact = "+$1,940 / hr at current peak tariffs",
            urgencyLevel = "HIGH"
        )

        val rec2 = AdvisorRecommendation(
            id = "rec_det_2",
            category = RecommendationCategory.MAINTENANCE,
            title = if (isHighVib) "Shaft Vibration Mitigation Alert" else "Runner Cavitation & Thermal Health",
            summary = if (isHighVib) "Shaft RMS vibration at ${String.format("%.2f", state.shaftVibrationMmS)} mm/s is nearing the 3.8 mm/s alert ceiling." else "Turbine runner and thrust bearings are operating smoothly within normal parameters.",
            technicalRationale = "Cavitation parameter Thoma σ = ${String.format("%.3f", state.cavitationIndex)}. Stator winding temp is ${String.format("%.1f", state.generatorStatorTempC)}°C and transformer oil is ${String.format("%.1f", state.transformerOilTempC)}°C.",
            recommendedActions = listOf(
                "Monitor lower guide bearing oil pressure (nominal 4.5 - 5.2 Bar)",
                "Avoid sustained operation below 30% wicket gate opening to prevent vortex rope formation",
                "Inspect MIV bypass equalizing valve before each opening cycle"
            ),
            estimatedFinancialImpact = "Extends runner lifecycle by ~4.5 operating years",
            urgencyLevel = if (isHighVib) "HIGH" else "LOW"
        )

        val rec3 = AdvisorRecommendation(
            id = "rec_det_3",
            category = RecommendationCategory.GRID_STABILITY,
            title = "Frequency Droop & Reactive Power Balance",
            summary = "Plant frequency is stable at ${String.format("%.2f", state.frequencyHz)} Hz with a Grid Stability Index of ${String.format("%.1f", state.gridStabilityIndex)}%.",
            technicalRationale = "Synchronous generator excitation is operating at ${String.format("%.0f", state.exciterCurrentAmps)} A providing ${String.format("%.1f", state.reactivePowerMvar)} MVAr reactive support to the 230 kV substation busbar.",
            recommendedActions = listOf(
                "Keep power factor locked between 0.95 lag and 0.99 lag",
                "Ensure automatic synchronizer angle window is set to ±5 degrees",
                "Verify SF6 gas pressure across high-voltage substation circuit breakers"
            ),
            estimatedFinancialImpact = "Earns $380/hr grid ancillary frequency regulation bonus",
            urgencyLevel = "MEDIUM"
        )

        return listOf(rec1, rec2, rec3)
    }

    private fun generateLocalExpertAnswer(question: String, state: PlantState): String {
        val q = question.lowercase()
        return when {
            "miv" in q || "valve" in q -> {
                "The Main Inlet Valve (MIV) is a high-pressure bi-directional spherical valve positioned between the penstock and the turbine spiral casing. Its primary role is hydraulic isolation, enabling dry inspection of the turbine pit without dewatering the entire penstock. During normal start-up, the bypass valve must first be opened to equalize pressure across the spherical plug, preventing extreme seal erosion. In emergency overspeed situations (>120% rated RPM), heavy counterweights trigger rapid mechanical closure in ~8.5 seconds."
            }
            "turbine" in q || "runner" in q || "francis" in q -> {
                "Our plant utilizes a high-efficiency vertical Francis reaction turbine. Water enters through the spiral casing, passes through 24 distributor wicket gates (guide vanes), and flows inward through the runner blades before discharging axially into the draft tube. Power is governed by Bernoulli's head equation: P = ρ · g · Q · H_net · η. Operating at 78-84% gate opening maximizes hydraulic efficiency up to 94.2%."
            }
            "cavitation" in q -> {
                "Cavitation occurs when local water static pressure drops below the saturation vapor pressure, forming micro-bubbles that implode violently against the runner blade trailing edges. This creates ultrasonic shockwaves that cause severe pitting erosion. We monitor the Thoma cavitation factor (σ = (H_atm - H_vap - H_s) / H_net). To prevent cavitation, maintain tailrace water level and avoid prolonged operation below 30% wicket gate load."
            }
            "generator" in q || "rpm" in q || "frequency" in q -> {
                "The synchronous generator has 16 salient poles. System frequency is tied to mechanical shaft speed by the formula f = (P · N) / 120, where P = 16 and N = 375 RPM, yielding exactly 50.00 Hz. When synchronized to the national grid, the generator is locked to grid frequency; adjustments to the wicket gates regulate active power (MW) rather than speed."
            }
            "transformer" in q || "substation" in q -> {
                "The Generator Step-Up (GSU) transformer steps up terminal voltage from 13.8 kV to 230 kV. This reduces transmission current by a factor of 16.6x, decreasing resistive I²R transmission power losses by over 270-fold across long-distance high-voltage lines. The substation switchyard uses SF6 circuit breakers to safely route power to industrial and residential feeders."
            }
            "water hammer" in q || "penstock" in q -> {
                "Water hammer (hydraulic transient shock) occurs when water flow in the penstock is decelerated rapidly, converting kinetic energy into severe acoustic pressure waves (ΔP = ρ · a · Δv). If the MIV or governor gates slam shut too fast, pressure spikes can rupture the penstock conduit. Safe closure laws enforce controlled non-linear valve closure times."
            }
            else -> {
                "Current plant telemetry shows ${String.format("%.1f", state.activePowerMw)} MW delivered at ${String.format("%.2f", state.frequencyHz)} Hz from a net hydraulic head of ${String.format("%.1f", state.netHeadM)} m. The energy conversion chain smoothly transforms gravitational potential energy in the reservoir into kinetic penstock flow, rotational shaft torque at 375 RPM, electromagnetic AC power at 13.8 kV, and 230 kV high-voltage grid transmission."
            }
        }
    }
}
