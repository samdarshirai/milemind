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
    val changeReasonCodes: List<String> = emptyList()
)
