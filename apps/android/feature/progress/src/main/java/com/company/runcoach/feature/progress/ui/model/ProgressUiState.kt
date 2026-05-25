package com.company.runcoach.feature.progress.ui.model

data class ProgressUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val content: ProgressContentUiModel? = null,
    val emptyStateMessage: String? = null
)

data class ProgressContentUiModel(
    val completionPercentage: Int,
    val completedWorkouts: Int,
    val currentTrainingWeek: Int?,
    val readinessTrendLabel: String,
    val weeklyCompletion: List<WeeklyCompletionUiModel>,
    val longRunProgression: List<LongRunProgressionUiModel>,
    val readinessTrend: List<ReadinessTrendUiModel>,
    val recentStatusDistribution: RecentStatusDistributionUiModel?,
    val insightMessage: String?
)

data class WeeklyCompletionUiModel(
    val label: String,
    val completion: String,
    val completionPercentage: Int
)

data class LongRunProgressionUiModel(
    val label: String,
    val detail: String,
    val status: String,
    val plannedDistanceKm: Double
)

data class ReadinessTrendUiModel(
    val label: String,
    val state: ReadinessStateUi,
    val detail: String
)

data class RecentStatusDistributionUiModel(
    val planned: Int,
    val completed: Int,
    val skipped: Int,
    val rescheduled: Int
)

enum class ReadinessStateUi {
    READY,
    CAUTION,
    HIGH_RISK,
    UNKNOWN
}
