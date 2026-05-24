package com.company.runcoach.feature.workout.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.runcoach.core.common.AdaptationEvent
import com.company.runcoach.core.common.AdaptationEvents
import com.company.runcoach.feature.workout.data.PlanVersionConflictException
import com.company.runcoach.feature.workout.data.WorkoutRepository
import com.company.runcoach.feature.workout.ui.model.AdaptationSummaryUiModel
import com.company.runcoach.feature.workout.ui.model.PendingMutationAction
import com.company.runcoach.feature.workout.ui.model.SkipReason
import com.company.runcoach.feature.workout.ui.model.WorkoutDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val plannedWorkoutId: String = checkNotNull(savedStateHandle["plannedWorkoutId"])
    private val fallbackStatus: String? = savedStateHandle["status"]

    private val _uiState = MutableStateFlow(WorkoutDetailUiState())
    val uiState: StateFlow<WorkoutDetailUiState> = _uiState.asStateFlow()
    private var pendingConflictAction: PendingMutationAction? = null

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.loadWorkoutDetail(plannedWorkoutId)
                .onSuccess { detail ->
                    val rawStatus = detail.status ?: fallbackStatus
                    val canMutate = isMutableStatus(rawStatus)
                    _uiState.value = WorkoutDetailUiState(
                        isLoading = false,
                        title = detail.workoutSubtype ?: detail.workoutType.replace('_', ' '),
                        dateLabel = detail.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())),
                        workoutType = detail.workoutType.replace('_', ' '),
                        plannedLabel = when {
                            detail.plannedDistanceKm != null -> String.format(Locale.US, "%.1f km planned", detail.plannedDistanceKm)
                            detail.plannedDurationMin != null -> "${detail.plannedDurationMin} min planned"
                            else -> "Planned session"
                        },
                        intensityLabel = detail.intensityZone ?: "Easy",
                        warmupCooldownLabel = "Warm-up and cool-down included when specified in steps.",
                        structureLines = detail.structure,
                        instructions = detail.whyThisWorkout,
                        statusLabel = statusDisplayLabel(rawStatus),
                        canMarkComplete = false,
                        canMarkSkipped = canMutate,
                        canReschedule = canMutate,
                        rescheduleDate = detail.date.toString(),
                        latestPlanVersion = maxOf(_uiState.value.latestPlanVersion, detail.planVersion)
                    )
                }
                .onFailure {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = "Could not load workout detail."
                        )
                    }
                }
        }
    }

    fun openSkipSheet() {
        if (!_uiState.value.canMarkSkipped) {
            return
        }
        _uiState.update { it.copy(showSkipSheet = true, mutationError = null, conflictMessage = null) }
    }

    fun dismissSkipSheet() {
        _uiState.update { it.copy(showSkipSheet = false, mutationError = null) }
    }

    fun selectSkipReason(reason: SkipReason) {
        _uiState.update { it.copy(selectedSkipReason = reason) }
    }

    fun submitSkip() {
        val state = _uiState.value
        _uiState.update { it.copy(mutationInFlight = true, mutationError = null, conflictMessage = null) }
        viewModelScope.launch {
            repository.skipWorkout(
                plannedWorkoutId = plannedWorkoutId,
                reason = state.selectedSkipReason.apiValue,
                expectedPlanVersion = state.latestPlanVersion
            ).onSuccess { result ->
                pendingConflictAction = null
                val adaptation = result.adaptation?.let {
                    AdaptationSummaryUiModel(it.id, it.summary, it.affectedFromDate, it.affectedToDate, it.changedWorkoutIds)
                }
                _uiState.update {
                    it.copy(
                        mutationInFlight = false,
                        showSkipSheet = false,
                        latestPlanVersion = result.planVersion,
                        latestAdaptation = adaptation
                    )
                }
                adaptation?.let {
                    AdaptationEvents.publish(
                        AdaptationEvent(
                            summary = it.summary,
                            affectedFromDate = it.affectedFromDate,
                            affectedToDate = it.affectedToDate,
                            changedWorkoutIds = it.changedWorkoutIds
                        )
                    )
                }
                load()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        mutationInFlight = false,
                        conflictMessage = if (throwable is PlanVersionConflictException) {
                            "Your plan changed recently. Refresh to continue."
                        } else {
                            null
                        },
                        mutationError = if (throwable is PlanVersionConflictException) {
                            null
                        } else {
                            "Could not skip this workout. Try again."
                        }
                    )
                }
                if (throwable is PlanVersionConflictException) {
                    pendingConflictAction = PendingMutationAction.SKIP
                }
            }
        }
    }

    fun openRescheduleSheet() {
        if (!_uiState.value.canReschedule) {
            return
        }
        _uiState.update { it.copy(showRescheduleSheet = true, mutationError = null, conflictMessage = null) }
    }

    fun dismissRescheduleSheet() {
        _uiState.update { it.copy(showRescheduleSheet = false, mutationError = null) }
    }

    fun updateRescheduleDate(value: String) {
        _uiState.update { it.copy(rescheduleDate = value) }
    }

    fun submitReschedule() {
        val state = _uiState.value
        if (state.rescheduleDate.isBlank()) {
            _uiState.update { it.copy(mutationError = "Please choose a target date.") }
            return
        }
        _uiState.update { it.copy(mutationInFlight = true, mutationError = null, conflictMessage = null) }
        viewModelScope.launch {
            repository.rescheduleWorkout(
                plannedWorkoutId = plannedWorkoutId,
                targetDate = state.rescheduleDate,
                expectedPlanVersion = state.latestPlanVersion
            ).onSuccess { result ->
                pendingConflictAction = null
                val adaptation = result.adaptation?.let {
                    AdaptationSummaryUiModel(it.id, it.summary, it.affectedFromDate, it.affectedToDate, it.changedWorkoutIds)
                }
                _uiState.update {
                    it.copy(
                        mutationInFlight = false,
                        showRescheduleSheet = false,
                        latestPlanVersion = result.planVersion,
                        latestAdaptation = adaptation
                    )
                }
                adaptation?.let {
                    AdaptationEvents.publish(
                        AdaptationEvent(
                            summary = it.summary,
                            affectedFromDate = it.affectedFromDate,
                            affectedToDate = it.affectedToDate,
                            changedWorkoutIds = it.changedWorkoutIds
                        )
                    )
                }
                load()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        mutationInFlight = false,
                        conflictMessage = if (throwable is PlanVersionConflictException) {
                            "Your plan changed recently. Refresh to continue."
                        } else {
                            null
                        },
                        mutationError = if (throwable is PlanVersionConflictException) {
                            null
                        } else {
                            "Could not reschedule this workout. Try again."
                        }
                    )
                }
                if (throwable is PlanVersionConflictException) {
                    pendingConflictAction = PendingMutationAction.RESCHEDULE
                }
            }
        }
    }

    fun refreshForConflict() {
        pendingConflictAction = null
        load()
    }

    fun retryAfterConflict() {
        val pending = pendingConflictAction
        if (pending == null) {
            load()
            return
        }
        _uiState.update { it.copy(mutationInFlight = true, mutationError = null, conflictMessage = null) }
        viewModelScope.launch {
            repository.loadWorkoutDetail(plannedWorkoutId)
                .onSuccess { detail ->
                    _uiState.update {
                        it.copy(
                            mutationInFlight = false,
                            latestPlanVersion = detail.planVersion
                        )
                    }
                    when (pending) {
                        PendingMutationAction.SKIP -> submitSkip()
                        PendingMutationAction.RESCHEDULE -> submitReschedule()
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            mutationInFlight = false,
                            conflictMessage = "Could not refresh latest plan version. Try again."
                        )
                    }
                }
        }
    }
}

private fun isMutableStatus(rawStatus: String?): Boolean {
    return rawStatus.equals("PLANNED", ignoreCase = true)
}

internal fun statusDisplayLabel(rawStatus: String?): String {
    return when (rawStatus?.trim()?.uppercase(Locale.US)) {
        "PLANNED" -> "Planned"
        "COMPLETED" -> "Completed"
        "MISSED" -> "Missed"
        "SKIPPED" -> "Skipped"
        "REST", "REST_DAY" -> "Rest day"
        else -> "Unknown"
    }
}
