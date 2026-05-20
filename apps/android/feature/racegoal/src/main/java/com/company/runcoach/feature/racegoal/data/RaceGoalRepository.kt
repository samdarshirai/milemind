package com.company.runcoach.feature.racegoal.data

import com.company.runcoach.feature.racegoal.data.remote.ApiErrorEnvelope
import com.company.runcoach.feature.racegoal.data.remote.CreateRaceGoalRequest
import com.company.runcoach.feature.racegoal.data.remote.RaceGoalApiService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class RaceGoalRepository @Inject constructor(
    private val apiService: RaceGoalApiService,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    suspend fun loadCurrentGoal(): Result<RaceGoal?> = runCatching {
        val response = apiService.getCurrentRaceGoal()
        RaceGoal(
            id = response.raceGoalId,
            raceDistanceType = response.raceDistanceType,
            raceDate = response.raceDate,
            goalStyle = response.goalStyle,
            targetTimeSeconds = response.targetTimeSeconds,
            status = response.status
        )
    }.recoverCatching { throwable ->
        if (throwable is HttpException && throwable.code() == 404) {
            null
        } else {
            throw throwable
        }
    }

    suspend fun createGoal(input: RaceGoalInput): Result<Unit> = runCatching {
        apiService.createRaceGoal(
            CreateRaceGoalRequest(
                raceName = input.raceName.ifBlank { null },
                raceDistanceType = input.raceDistanceType,
                raceDate = input.raceDate,
                goalStyle = input.goalStyle,
                targetTimeSeconds = input.targetTimeSeconds
            )
        )
        Unit
    }.recoverCatching { throwable ->
        throw if (throwable is HttpException) {
            val envelope = throwable.response()?.errorBody()?.string()?.let {
                runCatching { json.decodeFromString<ApiErrorEnvelope>(it) }.getOrNull()
            }?.error
            RaceGoalSubmissionException(
                message = envelope?.message ?: throwable.message(),
                fieldErrors = envelope?.details?.mapNotNull { detail ->
                    val field = detail.field ?: return@mapNotNull null
                    field to mapFieldErrorMessage(field, detail.issue, envelope.message)
                }?.toMap().orEmpty(),
                isRaceDateTooSoon = envelope?.details?.any { it.field == "raceDate" && it.issue == "too_soon" } == true,
                isActiveGoalConflict = envelope?.code == "CONFLICT" || envelope?.details?.any { it.issue == "active_goal_exists" } == true
            )
        } else throwable
    }

    private fun mapFieldErrorMessage(field: String, issue: String?, fallback: String): String {
        return when (field) {
            "raceDate" -> if (issue == "too_soon") {
                "This race date is too soon for a safe build. Choose a later date."
            } else fallback
            "targetTimeSeconds" -> "Target time must be positive if provided."
            else -> fallback
        }
    }
}

data class RaceGoalInput(
    val raceDistanceType: String,
    val raceName: String,
    val raceDate: String,
    val goalStyle: String,
    val targetTimeSeconds: Int?
)

data class RaceGoal(
    val id: String,
    val raceDistanceType: String,
    val raceDate: String,
    val goalStyle: String,
    val targetTimeSeconds: Int?,
    val status: String
)

class RaceGoalSubmissionException(
    override val message: String,
    val fieldErrors: Map<String, String> = emptyMap(),
    val isRaceDateTooSoon: Boolean = false,
    val isActiveGoalConflict: Boolean = false
) : Exception(message)
