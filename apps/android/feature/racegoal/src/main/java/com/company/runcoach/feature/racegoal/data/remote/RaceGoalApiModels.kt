package com.company.runcoach.feature.racegoal.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CreateRaceGoalRequest(
    val raceName: String? = null,
    val raceDistanceType: String,
    val raceDate: String,
    val goalStyle: String,
    val targetTimeSeconds: Int? = null
)

@Serializable
data class CreateRaceGoalResponse(
    val raceGoalId: String,
    val status: String
)

@Serializable
data class CurrentRaceGoalResponse(
    val raceGoalId: String,
    val raceDistanceType: String,
    val raceDate: String,
    val goalStyle: String,
    val targetTimeSeconds: Int? = null,
    val status: String
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
