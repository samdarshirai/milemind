package com.company.runcoach.feature.profile.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ProfileResponse(
    val userId: String,
    val email: String,
    val timezone: String,
    val profile: ProfileData
)

@Serializable
data class ProfileData(
    val experienceLevel: String,
    val typicalWeeklyDistanceKm: Double,
    val longestRecentRunKm: Double,
    val preferredRunDays: List<String>,
    val preferredLongRunDay: String,
    val goalStyle: String,
    val strengthDaysPerWeek: Int,
    val units: String,
    val injuryHistory: Map<String, JsonElement>? = null
)

@Serializable
data class ProfileUpdateRequest(
    val preferredRunDays: List<String>,
    val preferredLongRunDay: String,
    val strengthDaysPerWeek: Int,
    val units: String,
    val timezone: String,
    val injuryHistory: InjuryHistoryUpdateRequest? = null
)

@Serializable
data class InjuryHistoryUpdateRequest(
    val hadRunningInjuryLast12Months: Boolean? = null,
    val summary: String? = null
)

@Serializable
data class ApiFieldDetail(
    val field: String? = null,
    val issue: String? = null
)

@Serializable
data class ApiErrorPayload(
    val code: String,
    val message: String,
    val details: List<ApiFieldDetail> = emptyList(),
    val correlationId: String? = null
)

@Serializable
data class ApiErrorEnvelope(
    val error: ApiErrorPayload
)
