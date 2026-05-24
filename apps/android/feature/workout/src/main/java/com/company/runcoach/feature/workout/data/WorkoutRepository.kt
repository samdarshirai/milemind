package com.company.runcoach.feature.workout.data

import com.company.runcoach.feature.workout.data.remote.WorkoutApiService
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val apiService: WorkoutApiService
) {
    suspend fun loadWorkoutDetail(plannedWorkoutId: String): Result<WorkoutDetailData> = runCatching {
        val response = apiService.getWorkoutDetail(plannedWorkoutId)
        WorkoutDetailData(
            plannedWorkoutId = response.plannedWorkoutId,
            date = LocalDate.parse(response.scheduledDate),
            workoutType = response.workoutType,
            workoutSubtype = response.workoutSubtype,
            status = response.status,
            plannedDistanceKm = response.plannedDistanceKm,
            plannedDurationMin = response.plannedDurationMin,
            intensityZone = response.intensityZone,
            structure = response.structure.map { it.entries.joinToString(" | ") { entry -> "${entry.key}: ${entry.value}" } },
            whyThisWorkout = response.whyThisWorkout,
            changeReasonCodes = response.changeReasonCodes
        )
    }
}

data class WorkoutDetailData(
    val plannedWorkoutId: String,
    val date: LocalDate,
    val workoutType: String,
    val workoutSubtype: String?,
    val status: String?,
    val plannedDistanceKm: Double?,
    val plannedDurationMin: Int?,
    val intensityZone: String?,
    val structure: List<String>,
    val whyThisWorkout: String,
    val changeReasonCodes: List<String>
)
