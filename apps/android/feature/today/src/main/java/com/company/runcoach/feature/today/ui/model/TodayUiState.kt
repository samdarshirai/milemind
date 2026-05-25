package com.company.runcoach.feature.today.ui.model

data class TodayUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val insightsDate: String? = null,
    val errorMessage: String? = null,
    val workoutErrorMessage: String? = null,
    val workoutLoadFailed: Boolean = false,
    val insightMessage: String? = null,
    val warnings: List<String> = emptyList(),
    val recommendedTone: String? = null,
    val fatigueSummary: String? = null,
    val painSummary: String? = null,
    val readinessBanner: ReadinessBannerUiModel = ReadinessBannerUiModel.loading(),
    val todayWorkout: TodayWorkoutUiModel? = null,
    val latestAdaptation: LatestAdaptationUiModel? = null,
    val showWhatChanged: Boolean = false
)

data class ReadinessBannerUiModel(
    val title: String,
    val message: String,
    val ctaLabel: String,
    val status: ReadinessBannerStatus
) {
    companion object {
        fun loading() = ReadinessBannerUiModel(
            title = "Loading readiness",
            message = "Checking your latest readiness insight.",
            ctaLabel = "",
            status = ReadinessBannerStatus.LOADING
        )
    }
}

enum class ReadinessBannerStatus {
    NO_CHECK_IN,
    READY,
    CAUTION,
    HIGH_RISK,
    LOADING,
    ERROR
}

data class TodayWorkoutUiModel(
    val plannedWorkoutId: String,
    val title: String,
    val status: String,
    val detail: String,
    val intensity: String
)

data class LatestAdaptationUiModel(
    val summary: String,
    val affectedFromDate: String,
    val affectedToDate: String,
    val changedWorkoutIds: List<String>
)
