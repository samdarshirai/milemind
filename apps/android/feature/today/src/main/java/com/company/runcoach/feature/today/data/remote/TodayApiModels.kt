package com.company.runcoach.feature.today.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class TodayInsightsResponse(
    val date: String,
    val readinessState: String? = null,
    val readinessLabel: String? = null,
    val readinessMessage: String? = null,
    val hasCheckInToday: Boolean = false
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
