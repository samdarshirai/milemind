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
            latestAdaptation = insights.latestAdaptation?.let {
                LatestAdaptationData(
                    id = it.id,
                    summary = it.summary,
                    affectedFromDate = it.affectedFromDate,
                    affectedToDate = it.affectedToDate,
                    changedWorkoutIds = it.changedWorkoutIds
                )
            },
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
    val latestAdaptation: LatestAdaptationData? = null,
    val todayWorkout: TodayWorkoutData?,
    val workoutLoadFailed: Boolean = false
)

data class LatestAdaptationData(
    val id: String,
    val summary: String,
    val affectedFromDate: String,
    val affectedToDate: String,
    val changedWorkoutIds: List<String>
)

data class TodayWorkoutData(
    val plannedWorkoutId: String,
    val workoutType: String,
    val status: String,
    val intensityZone: String?,
    val plannedDistanceKm: Double?,
    val plannedDurationMin: Int?
)
