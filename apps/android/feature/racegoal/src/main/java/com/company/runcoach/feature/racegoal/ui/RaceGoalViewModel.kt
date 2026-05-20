package com.company.runcoach.feature.racegoal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.runcoach.feature.racegoal.data.RaceGoalInput
import com.company.runcoach.feature.racegoal.data.RaceGoalRepository
import com.company.runcoach.feature.racegoal.data.RaceGoalSubmissionException
import com.company.runcoach.feature.racegoal.ui.model.RaceGoalEffect
import com.company.runcoach.feature.racegoal.ui.model.RaceGoalForm
import com.company.runcoach.feature.racegoal.ui.model.RaceGoalStep
import com.company.runcoach.feature.racegoal.ui.model.RaceGoalUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RaceGoalViewModel @Inject constructor(
    private val repository: RaceGoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RaceGoalUiState())
    val uiState: StateFlow<RaceGoalUiState> = _uiState.asStateFlow()

    private val _effects = Channel<RaceGoalEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadCurrentGoal()
    }

    fun loadCurrentGoal() {
        _uiState.update { it.copy(isLoading = true, submitError = null, activeGoalMessage = null) }
        viewModelScope.launch {
            repository.loadCurrentGoal()
                .onSuccess { currentGoal ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentGoal = currentGoal,
                            activeGoalMessage = if (currentGoal != null) "You already have an active race goal." else null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, submitError = error.message ?: "Failed to load race goal") }
                }
        }
    }

    fun updateForm(form: RaceGoalForm) {
        _uiState.update {
            it.copy(form = form, fieldErrors = emptyMap(), submitError = null, tooSoonMessage = null, activeGoalMessage = null)
        }
    }

    fun continueToReview() {
        val errors = validate(_uiState.value.form)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }
        _uiState.update { it.copy(step = RaceGoalStep.REVIEW, fieldErrors = emptyMap()) }
    }

    fun backToSetup() {
        _uiState.update { it.copy(step = RaceGoalStep.SETUP) }
    }

    fun saveGoal() {
        val form = _uiState.value.form
        val errors = validate(form)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors, step = RaceGoalStep.SETUP) }
            return
        }

        val input = RaceGoalInput(
            raceDistanceType = form.raceDistanceType!!,
            raceName = form.raceName,
            raceDate = form.raceDate,
            goalStyle = form.goalStyle!!,
            targetTimeSeconds = parseTargetTime(form.targetTime)
        )

        _uiState.update { it.copy(isSaving = true, submitError = null, tooSoonMessage = null, activeGoalMessage = null) }
        viewModelScope.launch {
            repository.createGoal(input)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, step = RaceGoalStep.SAVED) }
                }
                .onFailure { throwable ->
                    val submissionError = throwable as? RaceGoalSubmissionException
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            fieldErrors = submissionError?.fieldErrors.orEmpty(),
                            tooSoonMessage = if (submissionError?.isRaceDateTooSoon == true) {
                                submissionError.message
                            } else null,
                            activeGoalMessage = if (submissionError?.isActiveGoalConflict == true) {
                                "You already have an active goal. Keep training with it or update it later."
                            } else null,
                            submitError = if (
                                submissionError == null ||
                                (!submissionError.isRaceDateTooSoon && !submissionError.isActiveGoalConflict && submissionError.fieldErrors.isEmpty())
                            ) throwable.message ?: "Failed to save race goal" else null,
                            step = RaceGoalStep.SETUP
                        )
                    }
                    _effects.send(RaceGoalEffect.ShowMessage("Please review your goal details."))
                }
        }
    }

    fun continueAfterSaved() {
        viewModelScope.launch { _effects.send(RaceGoalEffect.NavigateToPlanPlaceholder) }
    }

    fun continueWithExistingGoal() {
        viewModelScope.launch { _effects.send(RaceGoalEffect.NavigateToPlanPlaceholder) }
    }

    private fun validate(form: RaceGoalForm): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (form.raceDistanceType.isNullOrBlank()) {
            errors["raceDistanceType"] = "Choose a race distance."
        }
        if (form.raceDate.isBlank()) {
            errors["raceDate"] = "Choose a race date."
        }
        if (form.goalStyle.isNullOrBlank()) {
            errors["goalStyle"] = "Choose a goal style."
        }

        if (form.targetTime.isNotBlank()) {
            val parsed = parseTargetTime(form.targetTime)
            if (parsed == null || parsed <= 0) {
                errors["targetTimeSeconds"] = "Target time must be a positive HH:MM:SS value."
            }
        }

        val date = runCatching { LocalDate.parse(form.raceDate) }.getOrNull()
        if (date != null && !form.raceDistanceType.isNullOrBlank()) {
            val minWeeks = if (form.raceDistanceType == "MARATHON") 12 else 8
            val minimumDate = LocalDate.now(ZoneId.of("UTC")).plusWeeks(minWeeks.toLong())
            if (date.isBefore(minimumDate)) {
                errors["raceDate"] = "This race date is too soon. ${form.raceDistanceType.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }} goals need at least $minWeeks weeks."
            }
        }

        return errors
    }

    internal fun parseTargetTime(value: String): Int? {
        if (value.isBlank()) return null
        val parts = value.split(":")
        if (parts.size != 3) return null
        val hours = parts[0].toIntOrNull() ?: return null
        val minutes = parts[1].toIntOrNull() ?: return null
        val seconds = parts[2].toIntOrNull() ?: return null
        if (hours < 0 || minutes !in 0..59 || seconds !in 0..59) return null
        return hours * 3600 + minutes * 60 + seconds
    }
}
