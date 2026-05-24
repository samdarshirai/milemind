package com.company.runcoach.feature.workout.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.runcoach.feature.workout.data.WorkoutRepository
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

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.loadWorkoutDetail(plannedWorkoutId)
                .onSuccess { detail ->
                    val rawStatus = detail.status ?: fallbackStatus
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
                        canMarkSkipped = false
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
