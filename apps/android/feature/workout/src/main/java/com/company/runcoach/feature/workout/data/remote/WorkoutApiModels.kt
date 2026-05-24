package com.company.runcoach.feature.workout.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PlannedWorkoutDetailResponse(
    val plannedWorkoutId: String,
    val scheduledDate: String,
    val workoutType: String,
    val workoutSubtype: String? = null,
    val status: String? = null,
    val plannedDistanceKm: Double? = null,
    val plannedDurationMin: Int? = null,
    val intensityZone: String? = null,
    val structure: List<Map<String, JsonElement>> = emptyList(),
    val whyThisWorkout: String,
    val changeReasonCodes: List<String> = emptyList(),
    val planVersion: Int = 1
)

@Serializable
data class SkipWorkoutRequest(
    val reason: String,
    val expectedPlanVersion: Int
)

@Serializable
data class RescheduleWorkoutRequest(
    val targetDate: String,
    val expectedPlanVersion: Int
)

@Serializable
data class AdaptationMutationResponse(
    val planVersion: Int,
    val adaptation: AdaptationSummaryResponse? = null
)

@Serializable
data class AdaptationSummaryResponse(
    val id: String,
    val summary: String,
    val affectedFromDate: String,
    val affectedToDate: String,
    val changedWorkoutIds: List<String> = emptyList()
)

@Serializable
data class ApiErrorEnvelope(
    val error: ApiErrorPayload
)

@Serializable
data class ApiErrorPayload(
    val code: String,
    val message: String
)
