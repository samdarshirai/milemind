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
    val canMarkSkipped: Boolean = false,
    val canReschedule: Boolean = false,
    val showSkipSheet: Boolean = false,
    val showRescheduleSheet: Boolean = false,
    val selectedSkipReason: SkipReason = SkipReason.TOO_TIRED,
    val rescheduleDate: String = "",
    val mutationInFlight: Boolean = false,
    val mutationError: String? = null,
    val conflictMessage: String? = null,
    val latestPlanVersion: Int = 1,
    val latestAdaptation: AdaptationSummaryUiModel? = null
)

data class AdaptationSummaryUiModel(
    val id: String,
    val summary: String,
    val affectedFromDate: String,
    val affectedToDate: String,
    val changedWorkoutIds: List<String>
)

enum class PendingMutationAction {
    SKIP,
    RESCHEDULE
}

enum class SkipReason(val apiValue: String, val label: String) {
    TOO_TIRED("TOO_TIRED", "Too tired"),
    PAIN_DISCOMFORT("PAIN", "Pain/discomfort"),
    NO_TIME("NO_TIME", "No time"),
    OTHER("OTHER", "Other")
}
