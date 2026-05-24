package com.company.runcoach.feature.plan.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CurrentPlanResponse(
    val trainingPlanId: String,
    val planVersion: Int,
    val methodologyCode: String,
    val raceGoal: RaceGoalSummary,
    val currentWeekIndex: Int,
    val weeks: List<WeekSummary>
)

@Serializable
data class RaceGoalSummary(
    val raceDistanceType: String,
    val raceDate: String
)

@Serializable
data class WeekSummary(
    val weekIndex: Int,
    val phase: String,
    val recoveryWeek: Boolean,
    val targetDistanceKm: Double? = null,
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
    val intensityZone: String? = null,
    val changeReasonCodes: List<String> = emptyList()
)
