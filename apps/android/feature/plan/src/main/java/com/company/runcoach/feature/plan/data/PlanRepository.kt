package com.company.runcoach.feature.plan.data

import com.company.runcoach.feature.plan.data.remote.PlanApiService
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

@Singleton
class PlanRepository @Inject constructor(
    private val apiService: PlanApiService
) {
    suspend fun loadCurrentPlan(): Result<PlanOverviewData?> = runCatching {
        val response = apiService.getCurrentPlan()
        PlanOverviewData(
            trainingPlanId = response.trainingPlanId,
            raceDistanceType = response.raceGoal.raceDistanceType,
            raceDate = response.raceGoal.raceDate,
            currentWeekIndex = response.currentWeekIndex,
            latestChangedWorkoutIds = response.latestAdaptation?.changedWorkoutIds.orEmpty(),
            weeks = response.weeks.map { week ->
                WeekData(
                    weekIndex = week.weekIndex,
                    phase = week.phase,
                    recoveryWeek = week.recoveryWeek,
                    targetDistanceKm = week.targetDistanceKm,
                    workouts = week.workouts.map { workout ->
                        WorkoutSummaryData(
                            plannedWorkoutId = workout.plannedWorkoutId,
                            scheduledDate = LocalDate.parse(workout.scheduledDate),
                            workoutType = workout.workoutType,
                            status = workout.status,
                            plannedDistanceKm = workout.plannedDistanceKm,
                            plannedDurationMin = workout.plannedDurationMin,
                            intensityZone = workout.intensityZone,
                            changeReasonCodes = workout.changeReasonCodes,
                            adaptedFromWorkoutId = workout.adaptedFromWorkoutId
                        )
                    }
                )
            }
        )
    }.recoverCatching { throwable ->
        if (throwable is HttpException && throwable.code() == 404) {
            null
        } else {
            throw throwable
        }
    }
}

data class PlanOverviewData(
    val trainingPlanId: String,
    val raceDistanceType: String,
    val raceDate: String,
    val currentWeekIndex: Int,
    val latestChangedWorkoutIds: List<String>,
    val weeks: List<WeekData>
)

data class WeekData(
    val weekIndex: Int,
    val phase: String,
    val recoveryWeek: Boolean,
    val targetDistanceKm: Double?,
    val workouts: List<WorkoutSummaryData>
)

data class WorkoutSummaryData(
    val plannedWorkoutId: String,
    val scheduledDate: LocalDate,
    val workoutType: String,
    val status: String,
    val plannedDistanceKm: Double?,
    val plannedDurationMin: Int?,
    val intensityZone: String?,
    val changeReasonCodes: List<String>,
    val adaptedFromWorkoutId: String?
)
