package com.company.runcoach.feature.onboarding.data

import com.company.runcoach.feature.onboarding.data.remote.OnboardingApiService
import com.company.runcoach.feature.onboarding.data.remote.ApiErrorEnvelope
import com.company.runcoach.feature.onboarding.data.remote.InjuryHistoryRequest
import com.company.runcoach.feature.onboarding.data.remote.OnboardingProfileRequest
import com.company.runcoach.feature.onboarding.data.remote.OnboardingRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class OnboardingRepository @Inject constructor(
    private val apiService: OnboardingApiService,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    private val fallbackFieldMessages = mapOf(
        "birthYear" to "You must be at least 18 years old.",
        "preferredRunDays" to "Choose at least 3 run days.",
        "preferredLongRunDay" to "Long run day must be one of your selected run days.",
        "injuryHistory.hadRunningInjuryLast12Months" to "Choose yes or no for recent injury history.",
        "injuryHistory.summary" to "Injury history summary must be plain text.",
        "timezone" to "Enter a valid timezone (for example, Europe/Berlin)."
    )

    suspend fun isOnboardingComplete(): Result<Boolean> = runCatching {
        apiService.getProfile()
        true
    }.recoverCatching { throwable ->
        if (throwable is HttpException && throwable.code() == 404) {
            false
        } else {
            throw throwable
        }
    }

    suspend fun submitOnboarding(input: OnboardingInput): Result<Unit> = runCatching {
        apiService.submitOnboarding(
            OnboardingRequest(
                profile = OnboardingProfileRequest(
                    birthYear = input.birthYear,
                    sex = input.sex,
                    experienceLevel = input.experienceLevel,
                    typicalWeeklyDistanceKm = input.typicalWeeklyDistanceKm,
                    longestRecentRunKm = input.longestRecentRunKm,
                    preferredRunDays = input.preferredRunDays,
                    preferredLongRunDay = input.preferredLongRunDay,
                    goalStyle = input.goalStyle,
                    injuryHistory = InjuryHistoryRequest(
                        hadRunningInjuryLast12Months = input.hadRunningInjuryLast12Months,
                        summary = input.injuryHistory.ifBlank { null }
                    ),
                    strengthDaysPerWeek = input.strengthDaysPerWeek,
                    units = input.units,
                    timezone = input.timezone
                )
            )
        )
        Unit
    }.recoverCatching { throwable ->
        throw if (throwable is HttpException) {
            val envelope = throwable.response()?.errorBody()?.string()?.let {
                runCatching { json.decodeFromString<ApiErrorEnvelope>(it) }.getOrNull()
            }?.error
            OnboardingSubmissionException(
                httpCode = throwable.code(),
                message = envelope?.message ?: throwable.message(),
                fieldErrors = envelope?.details?.mapNotNull { detail ->
                    val key = detail.field ?: return@mapNotNull null
                    key to mapFieldErrorMessage(key, detail.issue, envelope.message)
                }?.toMap().orEmpty()
            )
        } else throwable
    }

    private fun mapFieldErrorMessage(field: String, issue: String?, fallback: String?): String {
        return when (field) {
            "preferredRunDays" -> when (issue) {
                "too_few", "empty" -> "Choose at least 3 run days."
                else -> fallbackFieldMessages[field] ?: fallback ?: "Please check this field."
            }
            "preferredLongRunDay" -> when (issue) {
                "not_in_preferred_run_days" -> "Long run day must be one of your selected run days."
                else -> fallbackFieldMessages[field] ?: fallback ?: "Please check this field."
            }
            "injuryHistory.hadRunningInjuryLast12Months" -> when (issue) {
                "invalid_type" -> "Choose yes or no for recent injury history."
                else -> fallbackFieldMessages[field] ?: fallback ?: "Please check this field."
            }
            "injuryHistory.summary" -> when (issue) {
                "invalid_type" -> "Injury history summary must be plain text."
                else -> fallbackFieldMessages[field] ?: fallback ?: "Please check this field."
            }
            else -> fallbackFieldMessages[field] ?: fallback ?: "Please check this field."
        }
    }
}

data class OnboardingInput(
    val birthYear: Int,
    val sex: String,
    val experienceLevel: String,
    val typicalWeeklyDistanceKm: Double,
    val longestRecentRunKm: Double,
    val hadRunningInjuryLast12Months: Boolean?,
    val injuryHistory: String,
    val preferredRunDays: List<String>,
    val preferredLongRunDay: String,
    val strengthDaysPerWeek: Int,
    val units: String,
    val timezone: String,
    val goalStyle: String = "FINISH"
)

class OnboardingSubmissionException(
    val httpCode: Int,
    override val message: String,
    val fieldErrors: Map<String, String> = emptyMap()
) : Exception(message)
