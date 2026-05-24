package com.company.runcoach.feature.workout.ui.model

data class WorkoutDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val title: String = "",
    val dateLabel: String = "",
    val workoutType: String = "",
    val plannedLabel: String = "",
    val intensityLabel: String = "",
    val warmupCooldownLabel: String = "",
    val structureLines: List<String> = emptyList(),
    val instructions: String = "",
    val statusLabel: String = "Unknown",
    val canMarkComplete: Boolean = false,
    val canMarkSkipped: Boolean = false
)
