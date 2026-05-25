package com.company.runcoach.feature.today.data

import com.company.runcoach.feature.today.data.remote.TodayApiService
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodayRepository @Inject constructor(
    private val apiService: TodayApiService
) {
    suspend fun loadTodayInsights(): Result<TodayInsightsData> = runCatching {
        val insights = apiService.getTodayInsights()

        val workout = insights.todaysPlannedWorkout?.let {
            TodayWorkoutData(
                plannedWorkoutId = it.plannedWorkoutId,
                workoutType = it.workoutType,
                status = it.status,
                intensityZone = it.intensityZone,
                plannedDistanceKm = it.plannedDistanceKm,
                plannedDurationMin = it.plannedDurationMin
            )
        }

        TodayInsightsData(
            date = insights.date,
            readinessState = insights.readinessState,
            readinessLabel = insights.readinessLabel,
            readinessMessage = insights.readinessMessage,
            hasCheckInToday = insights.hasCheckInToday,
            latestAdaptation = insights.latestAdaptation?.let {
                LatestAdaptationData(
                    adaptationDecisionId = it.adaptationDecisionId,
                    summary = it.summary,
                    affectedFromDate = it.affectedFromDate,
                    affectedToDate = it.affectedToDate,
                    changedWorkoutIds = it.changedWorkoutIds
                )
            },
            todayWorkout = workout,
            workoutLoadFailed = false,
            fatigueSummary = insights.latestFatigueSignal?.let {
                FatigueSummaryData(
                    sleepScore = it.sleepScore,
                    stressScore = it.stressScore,
                    sorenessScore = it.sorenessScore,
                    motivationScore = it.motivationScore,
                    illnessFlag = it.illnessFlag,
                    tooBusyFlag = it.tooBusyFlag,
                    travellingFlag = it.travellingFlag,
                    notes = it.notes
                )
            },
            painSummary = insights.latestInjuryFeedback?.let {
                PainSummaryData(hasPain = it.hasPain, severity = it.severity, bodyRegion = it.bodyRegion)
            },
            insightMessage = selectPrimaryInsightMessage(
                messages = insights.insightMessages,
                readinessMessage = insights.readinessMessage,
                adaptationSummary = insights.latestAdaptation?.summary
            ),
            warnings = insights.warnings,
            recommendedTone = insights.recommendedTone
        )
    }

    suspend fun loadTodayWorkout(insightsDate: String): Result<TodayWorkoutData?> = runCatching {
        val insights = apiService.getTodayInsights()
        insights.todaysPlannedWorkout?.let {
            TodayWorkoutData(
                plannedWorkoutId = it.plannedWorkoutId,
                workoutType = it.workoutType,
                status = it.status,
                intensityZone = it.intensityZone,
                plannedDistanceKm = it.plannedDistanceKm,
                plannedDurationMin = it.plannedDurationMin
            )
        }
    }
}

private fun selectPrimaryInsightMessage(
    messages: List<String>,
    readinessMessage: String?,
    adaptationSummary: String?
): String? {
    val normalized = messages.filter { it.isNotBlank() }
    val adaptationKeywordMessage = normalized.firstOrNull {
        val upper = it.uppercase(Locale.US)
        upper.contains("ADAPTATION") || upper.contains("RECOVERY") || upper.contains("ADJUST")
    }
    if (adaptationKeywordMessage != null) return adaptationKeywordMessage
    if (!adaptationSummary.isNullOrBlank()) return adaptationSummary
    if (!readinessMessage.isNullOrBlank()) return readinessMessage
    return normalized.firstOrNull()
}

data class TodayInsightsData(
    val date: String,
    val readinessState: String?,
    val readinessLabel: String?,
    val readinessMessage: String?,
    val hasCheckInToday: Boolean,
    val latestAdaptation: LatestAdaptationData? = null,
    val todayWorkout: TodayWorkoutData?,
    val workoutLoadFailed: Boolean = false,
    val fatigueSummary: FatigueSummaryData? = null,
    val painSummary: PainSummaryData? = null,
    val insightMessage: String? = null,
    val warnings: List<String> = emptyList(),
    val recommendedTone: String? = null
)

data class LatestAdaptationData(
    val adaptationDecisionId: String,
    val summary: String,
    val affectedFromDate: String,
    val affectedToDate: String,
    val changedWorkoutIds: List<String>
)

data class FatigueSummaryData(
    val sleepScore: Int?,
    val stressScore: Int?,
    val sorenessScore: Int?,
    val motivationScore: Int?,
    val illnessFlag: Boolean,
    val tooBusyFlag: Boolean,
    val travellingFlag: Boolean,
    val notes: String?
)

data class PainSummaryData(
    val hasPain: Boolean,
    val severity: Int?,
    val bodyRegion: String?
)

data class TodayWorkoutData(
    val plannedWorkoutId: String,
    val workoutType: String,
    val status: String,
    val intensityZone: String?,
    val plannedDistanceKm: Double?,
    val plannedDurationMin: Int?
)
