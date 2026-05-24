package com.company.runcoach.feature.checkin.data

import com.company.runcoach.feature.checkin.data.remote.ApiErrorEnvelope
import com.company.runcoach.feature.checkin.data.remote.CheckInApiService
import com.company.runcoach.feature.checkin.data.remote.FatigueSignalRequest
import com.company.runcoach.feature.checkin.data.remote.InjuryFeedbackRequest
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class CheckInRepository @Inject constructor(
    private val apiService: CheckInApiService,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val clock: Clock = Clock.systemUTC()
) {
    private val utcClock: Clock = clock.withZone(ZoneOffset.UTC)
    private var lastKnownRunnerZoneId: ZoneId = ZoneOffset.UTC

    suspend fun submitFatigue(input: FatigueInput): Result<String> = runCatching {
        val runnerZone = resolveRunnerZoneId()
        val response = apiService.submitFatigueSignal(
            FatigueSignalRequest(
                signalDate = LocalDate.now(clock.withZone(runnerZone)).toString(),
                sleepScore = input.sleepQuality,
                stressScore = input.stressLevel,
                sorenessScore = input.muscleSoreness,
                motivationScore = input.energyLevel,
                illnessFlag = input.illnessFlag,
                tooBusyFlag = input.tooBusyFlag,
                travellingFlag = input.travellingFlag,
                notes = input.notes.ifBlank { null }
            )
        )
        response.readinessState
    }.recoverCatching { throwable -> throw mapApiError(throwable) }

    suspend fun submitPain(input: PainInput): Result<String> = runCatching {
        val response = apiService.submitInjuryFeedback(
            InjuryFeedbackRequest(
                reportedAt = Instant.now(utcClock).toString(),
                hasPain = input.hasPain,
                bodyRegion = input.bodyRegion,
                painType = input.painType,
                severity = input.severity,
                onsetContext = input.onsetContext,
                canRun = input.canRun,
                freeText = input.notes.ifBlank { null }
            )
        )
        response.readinessState
    }.recoverCatching { throwable -> throw mapApiError(throwable) }

    private fun mapApiError(throwable: Throwable): Throwable {
        if (throwable !is HttpException) return throwable
        val envelope = throwable.response()?.errorBody()?.string()?.let {
            runCatching { json.decodeFromString<ApiErrorEnvelope>(it) }.getOrNull()
        }?.error

        val fieldErrors = envelope?.details
            ?.mapNotNull { detail ->
                val field = detail.field ?: return@mapNotNull null
                field to toFieldMessage(field, detail.issue)
            }
            ?.toMap()
            .orEmpty()

        return CheckInSubmitException(
            message = envelope?.message ?: throwable.message ?: "Unable to submit check-in.",
            fieldErrors = fieldErrors
        )
    }

    private fun toFieldMessage(field: String, issue: String?): String {
        return when (field) {
            "bodyRegion" -> "Select pain location."
            "painType" -> "Select pain type."
            "severity" -> when (issue) {
                "required_when_pain_reported" -> "Select pain severity."
                "out_of_range" -> "Pain severity must be between 0 and 10."
                else -> "Select pain severity."
            }
            "onsetContext" -> "Select when pain started."
            "hasPain" -> "Select yes or no."
            "motivationScore", "energyLevel" -> "Select your energy level."
            "sleepScore", "sleepQuality" -> "Select your sleep quality."
            "sorenessScore", "muscleSoreness" -> "Select your muscle soreness."
            "stressScore", "stressLevel" -> "Select your stress level."
            else -> issue ?: "invalid"
        }
    }

    private suspend fun resolveRunnerZoneId(): ZoneId {
        val profileTimezone = runCatching { apiService.getProfile().timezone }
            .getOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return lastKnownRunnerZoneId

        val parsedZone = runCatching { ZoneId.of(profileTimezone) }.getOrNull()
            ?: return lastKnownRunnerZoneId

        lastKnownRunnerZoneId = parsedZone
        return parsedZone
    }
}

data class FatigueInput(
    val energyLevel: Int,
    val sleepQuality: Int,
    val muscleSoreness: Int,
    val stressLevel: Int,
    val illnessFlag: Boolean,
    val tooBusyFlag: Boolean,
    val travellingFlag: Boolean,
    val notes: String
)

data class PainInput(
    val hasPain: Boolean,
    val bodyRegion: String?,
    val painType: String?,
    val severity: Int?,
    val onsetContext: String?,
    val canRun: Boolean?,
    val notes: String
)

class CheckInSubmitException(
    override val message: String,
    val fieldErrors: Map<String, String> = emptyMap()
) : Exception(message)
