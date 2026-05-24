package com.company.runcoach.feature.profile.data

import com.company.runcoach.feature.profile.data.remote.ProfileApiService
import com.company.runcoach.feature.profile.data.remote.ApiErrorEnvelope
import com.company.runcoach.feature.profile.data.remote.InjuryHistoryUpdateRequest
import com.company.runcoach.feature.profile.data.remote.ProfileUpdateRequest
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class ProfileRepository @Inject constructor(
    private val apiService: ProfileApiService,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    private val fallbackFieldMessages = mapOf(
        "preferredRunDays" to "Choose at least 3 run days.",
        "preferredLongRunDay" to "Long run day must be one of your selected run days.",
        "strengthDaysPerWeek" to "Strength days must be 0, 1, or 2.",
        "units" to "Units must be KM or MILES.",
        "timezone" to "Enter a valid timezone (for example, Europe/Berlin).",
        "injuryHistory.hadRunningInjuryLast12Months" to "Choose yes or no for recent injury history.",
        "injuryHistory.summary" to "Injury history summary must be plain text."
    )

    suspend fun loadProfile(): Result<EditableProfile> = runCatching {
        val response = apiService.getProfile()
        EditableProfile(
            weeklyDistance = response.profile.typicalWeeklyDistanceKm.toString(),
            longestRun = response.profile.longestRecentRunKm.toString(),
            preferredRunDays = response.profile.preferredRunDays.toSet(),
            preferredLongRunDay = response.profile.preferredLongRunDay,
            strengthDaysPerWeek = response.profile.strengthDaysPerWeek,
            units = response.profile.units,
            timezone = response.timezone,
            hadRunningInjuryLast12Months = response.profile.injuryHistory
                ?.get("hadRunningInjuryLast12Months")
                ?.jsonPrimitive
                ?.contentOrNull
                ?.toBooleanStrictOrNull(),
            injuryHistorySummary = response.profile.injuryHistory
                ?.get("summary")
                ?.jsonPrimitive
                ?.contentOrNull
                .orEmpty()
        )
    }

    suspend fun saveProfile(profile: EditableProfile): Result<Unit> = runCatching {
        apiService.updateProfile(
            ProfileUpdateRequest(
                preferredRunDays = profile.preferredRunDays.toList(),
                preferredLongRunDay = profile.preferredLongRunDay,
                strengthDaysPerWeek = profile.strengthDaysPerWeek,
                units = profile.units,
                timezone = profile.timezone,
                injuryHistory = InjuryHistoryUpdateRequest(
                    hadRunningInjuryLast12Months = profile.hadRunningInjuryLast12Months,
                    summary = profile.injuryHistorySummary.ifBlank { null }
                )
            )
        )
        Unit
    }.recoverCatching { throwable ->
        throw if (throwable is HttpException) {
            val envelope = throwable.response()?.errorBody()?.string()?.let {
                runCatching { json.decodeFromString<ApiErrorEnvelope>(it) }.getOrNull()
            }?.error
            ProfileSaveException(
                message = envelope?.message ?: throwable.message ?: "Unknown error",
                fieldErrors = envelope?.details?.mapNotNull { detail ->
                    val key = detail.field ?: return@mapNotNull null
                    key to mapFieldErrorMessage(key, detail.issue, envelope.message)
                }?.toMap() ?: emptyMap()
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

data class EditableProfile(
    val weeklyDistance: String = "",
    val longestRun: String = "",
    val preferredRunDays: Set<String> = emptySet(),
    val preferredLongRunDay: String = "SUNDAY",
    val strengthDaysPerWeek: Int = 1,
    val units: String = "KM",
    val timezone: String = java.time.ZoneId.systemDefault().id,
    val hadRunningInjuryLast12Months: Boolean? = null,
    val injuryHistorySummary: String = ""
)

class ProfileSaveException(
    override val message: String,
    val fieldErrors: Map<String, String> = emptyMap()
) : Exception(message)
