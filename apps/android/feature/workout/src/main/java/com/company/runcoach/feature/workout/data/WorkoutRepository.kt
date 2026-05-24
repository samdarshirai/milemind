package com.company.runcoach.feature.workout.data

import com.company.runcoach.feature.workout.data.remote.WorkoutApiService
import com.company.runcoach.feature.workout.data.remote.SkipWorkoutRequest
import com.company.runcoach.feature.workout.data.remote.RescheduleWorkoutRequest
import com.company.runcoach.feature.workout.data.remote.ApiErrorEnvelope
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class WorkoutRepository @Inject constructor(
    private val apiService: WorkoutApiService,
    private val json: Json = Json { ignoreUnknownKeys = true }
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
            changeReasonCodes = response.changeReasonCodes,
            planVersion = response.planVersion
        )
    }

    suspend fun skipWorkout(
        plannedWorkoutId: String,
        reason: String,
        expectedPlanVersion: Int
    ): Result<AdaptationResultData> = runMutation {
        apiService.skipWorkout(
            plannedWorkoutId = plannedWorkoutId,
            request = SkipWorkoutRequest(reason = reason, expectedPlanVersion = expectedPlanVersion)
        )
    }

    suspend fun rescheduleWorkout(
        plannedWorkoutId: String,
        targetDate: String,
        expectedPlanVersion: Int
    ): Result<AdaptationResultData> = runMutation {
        apiService.rescheduleWorkout(
            plannedWorkoutId = plannedWorkoutId,
            request = RescheduleWorkoutRequest(targetDate = targetDate, expectedPlanVersion = expectedPlanVersion)
        )
    }

    private suspend fun runMutation(block: suspend () -> com.company.runcoach.feature.workout.data.remote.AdaptationMutationResponse): Result<AdaptationResultData> {
        return runCatching {
            val response = block()
            AdaptationResultData(
                planVersion = response.planVersion,
                adaptation = response.adaptation?.let {
                    AdaptationSummaryData(
                        id = it.id,
                        summary = it.summary,
                        affectedFromDate = it.affectedFromDate,
                        affectedToDate = it.affectedToDate,
                        changedWorkoutIds = it.changedWorkoutIds
                    )
                }
            )
        }.recoverCatching { throwable ->
            if (throwable is HttpException && throwable.code() == 409) {
                val errorCode = throwable.response()
                    ?.errorBody()
                    ?.string()
                    ?.let { payload ->
                        runCatching { json.decodeFromString<ApiErrorEnvelope>(payload).error.code }.getOrNull()
                    }
                if (errorCode == "STALE_PLAN_VERSION") {
                    throw PlanVersionConflictException()
                }
            }
            throw throwable
        }
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
    val changeReasonCodes: List<String>,
    val planVersion: Int
)

data class AdaptationResultData(
    val planVersion: Int,
    val adaptation: AdaptationSummaryData?
)

data class AdaptationSummaryData(
    val id: String,
    val summary: String,
    val affectedFromDate: String,
    val affectedToDate: String,
    val changedWorkoutIds: List<String>
)

class PlanVersionConflictException : RuntimeException("Stale plan version")
