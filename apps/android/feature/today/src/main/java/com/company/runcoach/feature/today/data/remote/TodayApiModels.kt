package com.company.runcoach.feature.today.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class TodayInsightsResponse(
    val date: String,
    val readinessState: String? = null,
    val readinessLabel: String? = null,
    val readinessMessage: String? = null,
    val hasCheckInToday: Boolean = false,
    val latestAdaptation: LatestAdaptationResponse? = null,
    val todaysPlannedWorkout: TodaysPlannedWorkoutResponse? = null,
    val latestFatigueSignal: FatigueSignalSummaryResponse? = null,
    val latestInjuryFeedback: InjuryFeedbackSummaryResponse? = null,
    val recommendedTone: String? = null,
    val insightMessages: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

@Serializable
data class LatestAdaptationResponse(
    val adaptationDecisionId: String,
    val summary: String,
    val affectedFromDate: String,
    val affectedToDate: String,
    val changedWorkoutIds: List<String> = emptyList()
)

@Serializable
data class TodaysPlannedWorkoutResponse(
    val plannedWorkoutId: String,
    val workoutType: String,
    val status: String,
    val plannedDistanceKm: Double? = null,
    val plannedDurationMin: Int? = null,
    val intensityZone: String? = null
)

@Serializable
data class FatigueSignalSummaryResponse(
    val signalDate: String,
    val sleepScore: Int? = null,
    val stressScore: Int? = null,
    val sorenessScore: Int? = null,
    val motivationScore: Int? = null,
    val illnessFlag: Boolean = false,
    val tooBusyFlag: Boolean = false,
    val travellingFlag: Boolean = false,
    val notes: String? = null
)

@Serializable
data class InjuryFeedbackSummaryResponse(
    val reportedAt: String,
    val hasPain: Boolean,
    val severity: Int? = null,
    val bodyRegion: String? = null
)

@Serializable
data class CurrentPlanResponse(
    val trainingPlanId: String,
    val currentWeekIndex: Int,
    val weeks: List<WeekSummary>
)

@Serializable
data class WeekSummary(
    val weekIndex: Int,
    val workouts: List<WorkoutSummary>
)

@Serializable
data class WorkoutSummary(
    val plannedWorkoutId: String,
    val scheduledDate: String,
    val workoutType: String,
    val status: String,
    val plannedDistanceKm: Double? = null,
    val plannedDurationMin: Int? = null,
    val intensityZone: String? = null
)
