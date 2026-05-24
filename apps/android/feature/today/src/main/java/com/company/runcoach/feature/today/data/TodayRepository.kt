package com.company.runcoach.feature.today.data

import com.company.runcoach.feature.today.data.remote.TodayApiService
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodayRepository @Inject constructor(
    private val apiService: TodayApiService
) {
    suspend fun loadTodayInsights(): Result<TodayInsightsData> = runCatching {
        val insights = apiService.getTodayInsights()
        val workoutResult = loadTodayWorkout(insights.date)
        TodayInsightsData(
            date = insights.date,
            readinessState = insights.readinessState,
            readinessLabel = insights.readinessLabel,
            readinessMessage = insights.readinessMessage,
            hasCheckInToday = insights.hasCheckInToday,
            todayWorkout = workoutResult.getOrNull(),
            workoutLoadFailed = workoutResult.isFailure
        )
    }

    suspend fun loadTodayWorkout(insightsDate: String): Result<TodayWorkoutData?> = runCatching {
        val planResponse = apiService.getCurrentPlan()

        val today = LocalDate.parse(insightsDate)
        val workout = planResponse.weeks
            .flatMap { it.workouts }
            .firstOrNull { LocalDate.parse(it.scheduledDate) == today }
            ?: return@runCatching null

        TodayWorkoutData(
            plannedWorkoutId = workout.plannedWorkoutId,
            workoutType = workout.workoutType,
            status = workout.status,
            intensityZone = workout.intensityZone,
            plannedDistanceKm = workout.plannedDistanceKm,
            plannedDurationMin = workout.plannedDurationMin
        )
    }
}

data class TodayInsightsData(
    val date: String,
    val readinessState: String?,
    val readinessLabel: String?,
    val readinessMessage: String?,
    val hasCheckInToday: Boolean,
    val todayWorkout: TodayWorkoutData?,
    val workoutLoadFailed: Boolean = false
)

data class TodayWorkoutData(
    val plannedWorkoutId: String,
    val workoutType: String,
    val status: String,
    val intensityZone: String?,
    val plannedDistanceKm: Double?,
    val plannedDurationMin: Int?
)
