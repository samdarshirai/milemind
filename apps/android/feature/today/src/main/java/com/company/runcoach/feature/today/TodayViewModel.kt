package com.company.runcoach.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.runcoach.core.common.AdaptationEvents
import com.company.runcoach.feature.today.data.TodayInsightsData
import com.company.runcoach.feature.today.data.TodayRepository
import com.company.runcoach.feature.today.data.TodayWorkoutData
import com.company.runcoach.feature.today.ui.model.LatestAdaptationUiModel
import com.company.runcoach.feature.today.ui.model.ReadinessBannerStatus
import com.company.runcoach.feature.today.ui.model.ReadinessBannerUiModel
import com.company.runcoach.feature.today.ui.model.TodayWorkoutUiModel
import com.company.runcoach.feature.today.ui.model.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: TodayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            AdaptationEvents.events.collect { load() }
        }
        load()
    }

    fun load() {
        _uiState.update {
            it.copy(
                isLoading = true,
                isRefreshing = false,
                errorMessage = null,
                readinessBanner = ReadinessBannerUiModel.loading()
            )
        }
        viewModelScope.launch {
            repository.loadTodayInsights()
                .onSuccess { insights ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            insightsDate = insights.date,
                            errorMessage = null,
                            workoutErrorMessage = if (insights.workoutLoadFailed) {
                                "Today's workout could not be loaded. You can retry."
                            } else {
                                null
                            },
                            workoutLoadFailed = insights.workoutLoadFailed,
                            readinessBanner = mapReadinessBanner(insights),
                            todayWorkout = mapWorkout(insights.todayWorkout),
                            insightMessage = insights.insightMessage,
                            warnings = insights.warnings,
                            recommendedTone = insights.recommendedTone,
                            fatigueSummary = mapFatigueSummary(insights),
                            painSummary = mapPainSummary(insights),
                            latestAdaptation = insights.latestAdaptation?.let {
                                LatestAdaptationUiModel(
                                    summary = it.summary,
                                    affectedFromDate = it.affectedFromDate,
                                    affectedToDate = it.affectedToDate,
                                    changedWorkoutIds = it.changedWorkoutIds
                                )
                            }
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Could not load readiness insight.",
                            readinessBanner = ReadinessBannerUiModel(
                                title = "Readiness unavailable",
                                message = "Try again to fetch today’s readiness.",
                                ctaLabel = "Retry",
                                status = ReadinessBannerStatus.ERROR
                            )
                        )
                    }
                }
        }
    }

    fun refreshAfterCheckIn() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            repository.loadTodayInsights()
                .onSuccess { insights ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            insightsDate = insights.date,
                            errorMessage = null,
                            workoutErrorMessage = if (insights.workoutLoadFailed) {
                                "Today's workout could not be loaded. You can retry."
                            } else {
                                null
                            },
                            workoutLoadFailed = insights.workoutLoadFailed,
                            readinessBanner = mapReadinessBanner(insights),
                            todayWorkout = mapWorkout(insights.todayWorkout),
                            insightMessage = insights.insightMessage,
                            warnings = insights.warnings,
                            recommendedTone = insights.recommendedTone,
                            fatigueSummary = mapFatigueSummary(insights),
                            painSummary = mapPainSummary(insights),
                            latestAdaptation = insights.latestAdaptation?.let {
                                LatestAdaptationUiModel(
                                    summary = it.summary,
                                    affectedFromDate = it.affectedFromDate,
                                    affectedToDate = it.affectedToDate,
                                    changedWorkoutIds = it.changedWorkoutIds
                                )
                            }
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = "Readiness was saved, but refresh failed. Tap Retry.",
                            readinessBanner = it.readinessBanner.copy(status = ReadinessBannerStatus.ERROR, ctaLabel = "Retry")
                        )
                    }
                }
        }
    }

    fun retryWorkoutLoad() {
        val insightsDate = _uiState.value.insightsDate ?: return
        viewModelScope.launch {
            repository.loadTodayWorkout(insightsDate)
                .onSuccess { workout ->
                    _uiState.update {
                        it.copy(
                            todayWorkout = mapWorkout(workout),
                            workoutLoadFailed = false,
                            workoutErrorMessage = null
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            workoutLoadFailed = true,
                            workoutErrorMessage = "Today's workout could not be loaded. Try again."
                        )
                    }
                }
        }
    }

    fun openWhatChanged() {
        _uiState.update { it.copy(showWhatChanged = true) }
    }

    fun dismissWhatChanged() {
        _uiState.update { it.copy(showWhatChanged = false) }
    }
}

internal fun mapFatigueSummary(insights: TodayInsightsData): String? {
    val fatigue = insights.fatigueSummary ?: return null
    return buildList {
        fatigue.sleepScore?.let { add("Sleep $it") }
        fatigue.stressScore?.let { add("Stress $it") }
        fatigue.sorenessScore?.let { add("Soreness $it") }
        fatigue.motivationScore?.let { add("Motivation $it") }
        if (fatigue.illnessFlag) add("Illness")
        if (fatigue.tooBusyFlag) add("Too busy")
        if (fatigue.travellingFlag) add("Travel")
        fatigue.notes?.trim()?.takeIf { it.isNotEmpty() }?.let { add("Note: $it") }
    }.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

private fun mapPainSummary(insights: TodayInsightsData): String? {
    val pain = insights.painSummary ?: return null
    return if (!pain.hasPain) {
        "No pain reported"
    } else {
        "Pain ${pain.severity ?: "-"}/10${pain.bodyRegion?.let { " · $it" } ?: ""}"
    }
}

internal fun mapReadinessBanner(insights: TodayInsightsData): ReadinessBannerUiModel {
    if (!insights.hasCheckInToday || insights.readinessState.isNullOrBlank()) {
        return ReadinessBannerUiModel(
            title = "How are you feeling today?",
            message = "Complete your daily check-in to calibrate today’s training effort.",
            ctaLabel = "Check in",
            status = ReadinessBannerStatus.NO_CHECK_IN
        )
    }

    return when (insights.readinessState.uppercase(Locale.US)) {
        "READY" -> ReadinessBannerUiModel(
            title = insights.readinessLabel ?: "Ready",
            message = insights.readinessMessage ?: "You’re ready for planned training today.",
            ctaLabel = "Update readiness",
            status = ReadinessBannerStatus.READY
        )

        "CAUTION" -> ReadinessBannerUiModel(
            title = insights.readinessLabel ?: "Caution",
            message = insights.readinessMessage ?: "Consider keeping effort conservative today.",
            ctaLabel = "Update readiness",
            status = ReadinessBannerStatus.CAUTION
        )

        "HIGH_RISK" -> ReadinessBannerUiModel(
            title = insights.readinessLabel ?: "Take it easy",
            message = insights.readinessMessage ?: "Keep training light while you reassess how you feel.",
            ctaLabel = "Update readiness",
            status = ReadinessBannerStatus.HIGH_RISK
        )

        else -> ReadinessBannerUiModel(
            title = "How are you feeling today?",
            message = "Complete your daily check-in to calibrate today’s training effort.",
            ctaLabel = "Check in",
            status = ReadinessBannerStatus.NO_CHECK_IN
        )
    }
}

private fun mapWorkout(workout: TodayWorkoutData?): TodayWorkoutUiModel? {
    if (workout == null) {
        return null
    }
    val title = workout.workoutType.replace('_', ' ')
    val detail = when {
        workout.plannedDistanceKm != null -> String.format(Locale.US, "%.1f km planned", workout.plannedDistanceKm)
        workout.plannedDurationMin != null -> "${workout.plannedDurationMin} min planned"
        else -> "Planned workout"
    }
    return TodayWorkoutUiModel(
        plannedWorkoutId = workout.plannedWorkoutId,
        title = title,
        status = workout.status.replace('_', ' '),
        detail = detail,
        intensity = workout.intensityZone ?: "EASY"
    )
}
