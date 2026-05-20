package com.company.runcoach.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.runcoach.feature.onboarding.data.OnboardingInput
import com.company.runcoach.feature.onboarding.data.OnboardingRepository
import com.company.runcoach.feature.onboarding.data.OnboardingSubmissionException
import com.company.runcoach.feature.onboarding.ui.model.AvailabilityForm
import com.company.runcoach.feature.onboarding.ui.model.OnboardingEffect
import com.company.runcoach.feature.onboarding.ui.model.OnboardingStep
import com.company.runcoach.feature.onboarding.ui.model.OnboardingUiState
import com.company.runcoach.feature.onboarding.ui.model.RunningHistoryForm
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.Year

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: OnboardingRepository
) : ViewModel() {
    private val minimumRunDaysPerWeek = 3

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _effects = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun nextFromIntro() = _uiState.update { it.copy(step = OnboardingStep.RUNNING_HISTORY) }

    fun back() = _uiState.update {
        val prev = when (it.step) {
            OnboardingStep.INTRO -> OnboardingStep.INTRO
            OnboardingStep.RUNNING_HISTORY -> OnboardingStep.INTRO
            OnboardingStep.AVAILABILITY -> OnboardingStep.RUNNING_HISTORY
        }
        it.copy(step = prev)
    }

    fun updateRunningHistory(form: RunningHistoryForm) {
        _uiState.update { it.copy(runningHistoryForm = form, fieldErrors = emptyMap(), submitError = null) }
    }

    fun updateAvailability(form: AvailabilityForm) {
        _uiState.update { it.copy(availabilityForm = form, fieldErrors = emptyMap(), submitError = null) }
    }

    fun nextFromRunningHistory() {
        val errors = validateRunningHistory(_uiState.value.runningHistoryForm)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }
        _uiState.update { it.copy(step = OnboardingStep.AVAILABILITY, fieldErrors = emptyMap()) }
    }

    fun submit() {
        val runningErrors = validateRunningHistory(_uiState.value.runningHistoryForm)
        val availabilityErrors = validateAvailability(_uiState.value.availabilityForm)
        val allErrors = runningErrors + availabilityErrors
        if (allErrors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = allErrors) }
            return
        }

        val rh = _uiState.value.runningHistoryForm
        val av = _uiState.value.availabilityForm
        _uiState.update { it.copy(isLoading = true, submitError = null) }
        viewModelScope.launch {
            repository.submitOnboarding(
                OnboardingInput(
                    birthYear = rh.birthYear.toInt(),
                    sex = rh.sex!!,
                    experienceLevel = rh.experienceLevel!!,
                    typicalWeeklyDistanceKm = rh.weeklyDistance.toDouble(),
                    longestRecentRunKm = rh.longestRun.toDouble(),
                    hadRunningInjuryLast12Months = rh.hadRunningInjuryLast12Months,
                    injuryHistory = rh.injuryHistory,
                    preferredRunDays = av.preferredRunDays.toList(),
                    preferredLongRunDay = av.preferredLongRunDay!!,
                    strengthDaysPerWeek = av.strengthDaysPerWeek,
                    units = av.units,
                    timezone = av.timezone
                )
            ).onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                _effects.send(OnboardingEffect.NavigateToRaceGoalPlaceholder)
            }.onFailure { err ->
                val fieldErrors = (err as? OnboardingSubmissionException)?.fieldErrors.orEmpty()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fieldErrors = fieldErrors,
                        submitError = if (fieldErrors.isEmpty()) err.message ?: "Unable to save profile" else null
                    )
                }
                _effects.send(OnboardingEffect.ShowMessage("Please check your details and try again."))
            }
        }
    }

    private fun validateRunningHistory(form: RunningHistoryForm): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        val birthYear = form.birthYear.toIntOrNull()
        val nowYear = Year.now().value
        if (birthYear == null || nowYear - birthYear < 18) {
            errors["birthYear"] = "You must be at least 18 years old."
        }
        if ((form.weeklyDistance.toDoubleOrNull() ?: 0.0) <= 0.0) {
            errors["weeklyDistance"] = "Weekly distance must be positive."
        }
        if ((form.longestRun.toDoubleOrNull() ?: 0.0) <= 0.0) {
            errors["longestRun"] = "Longest recent run must be positive."
        }
        if (form.sex.isNullOrBlank()) {
            errors["sex"] = "Select your sex."
        }
        if (form.experienceLevel.isNullOrBlank()) {
            errors["experienceLevel"] = "Select your experience level."
        }
        return errors
    }

    private fun validateAvailability(form: AvailabilityForm): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (form.preferredRunDays.size < minimumRunDaysPerWeek) {
            errors["preferredRunDays"] = "Choose at least $minimumRunDaysPerWeek run days."
        }
        if (form.preferredLongRunDay.isNullOrBlank()) {
            errors["preferredLongRunDay"] = "Choose a long run day."
        } else if (!form.preferredRunDays.contains(form.preferredLongRunDay)) {
            errors["preferredLongRunDay"] = "Long run day must be one of your selected run days."
        }
        if (form.strengthDaysPerWeek !in 0..2) {
            errors["strengthDaysPerWeek"] = "Strength days must be 0, 1, or 2."
        }
        if (form.units !in setOf("KM", "MILES")) {
            errors["units"] = "Units must be KM or MILES."
        }
        if (form.timezone.isBlank()) {
            errors["timezone"] = "Timezone is required."
        } else {
            try {
                ZoneId.of(form.timezone)
            } catch (_: Exception) {
                errors["timezone"] = "Enter a valid timezone (for example, Europe/Berlin)."
            }
        }
        return errors
    }
}
