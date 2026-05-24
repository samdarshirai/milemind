package com.company.runcoach.feature.checkin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.runcoach.feature.checkin.data.CheckInRepository
import com.company.runcoach.feature.checkin.data.CheckInSubmitException
import com.company.runcoach.feature.checkin.data.FatigueInput
import com.company.runcoach.feature.checkin.data.PainInput
import com.company.runcoach.feature.checkin.ui.model.FatigueFormState
import com.company.runcoach.feature.checkin.ui.model.FatigueUiState
import com.company.runcoach.feature.checkin.ui.model.PainFormState
import com.company.runcoach.feature.checkin.ui.model.PainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val repository: CheckInRepository
) : ViewModel() {

    private val _fatigueState = MutableStateFlow(FatigueUiState())
    val fatigueState: StateFlow<FatigueUiState> = _fatigueState.asStateFlow()

    private val _painState = MutableStateFlow(PainUiState())
    val painState: StateFlow<PainUiState> = _painState.asStateFlow()

    fun updateFatigueForm(form: FatigueFormState) {
        _fatigueState.update { it.copy(form = form, fieldErrors = emptyMap(), errorMessage = null, submitSuccess = false) }
    }

    fun updatePainForm(form: PainFormState) {
        _painState.update { it.copy(form = form, fieldErrors = emptyMap(), errorMessage = null, submitSuccess = false) }
    }

    fun submitFatigue(onSuccess: () -> Unit) {
        val form = _fatigueState.value.form
        val validationErrors = validateFatigue(form)
        if (validationErrors.isNotEmpty()) {
            _fatigueState.update { it.copy(fieldErrors = validationErrors, errorMessage = null) }
            return
        }

        _fatigueState.update { it.copy(isSubmitting = true, errorMessage = null, fieldErrors = emptyMap()) }
        viewModelScope.launch {
            repository.submitFatigue(
                FatigueInput(
                    energyLevel = form.energyLevel ?: 0,
                    sleepQuality = form.sleepQuality ?: 0,
                    muscleSoreness = form.muscleSoreness ?: 0,
                    stressLevel = form.stressLevel ?: 0,
                    illnessFlag = form.illnessFlag,
                    tooBusyFlag = form.tooBusyFlag,
                    travellingFlag = form.travellingFlag,
                    notes = form.notes
                )
            ).onSuccess {
                _fatigueState.update { it.copy(isSubmitting = false, submitSuccess = true) }
                onSuccess()
            }.onFailure { err ->
                val fieldErrors = (err as? CheckInSubmitException)?.fieldErrors.orEmpty()
                _fatigueState.update {
                    it.copy(
                        isSubmitting = false,
                        fieldErrors = fieldErrors,
                        errorMessage = if (fieldErrors.isEmpty()) err.message ?: "Unable to submit fatigue check-in." else null
                    )
                }
            }
        }
    }

    fun submitPain(onSuccess: (String?) -> Unit) {
        val form = _painState.value.form
        val validationErrors = validatePain(form)
        if (validationErrors.isNotEmpty()) {
            _painState.update { it.copy(fieldErrors = validationErrors, errorMessage = null) }
            return
        }

        _painState.update { it.copy(isSubmitting = true, errorMessage = null, fieldErrors = emptyMap()) }
        viewModelScope.launch {
            repository.submitPain(
                PainInput(
                    hasPain = form.hasPain == true,
                    bodyRegion = if (form.hasPain == true) form.bodyRegion else null,
                    painType = if (form.hasPain == true) form.painType else null,
                    severity = if (form.hasPain == true) form.severity else null,
                    onsetContext = if (form.hasPain == true) form.onsetContext else null,
                    canRun = if (form.hasPain == true) form.canRun else true,
                    notes = form.notes
                )
            ).onSuccess { readinessState ->
                _painState.update {
                    it.copy(
                        isSubmitting = false,
                        submitSuccess = true,
                        readinessState = readinessState
                    )
                }
                onSuccess(readinessState)
            }.onFailure { err ->
                val fieldErrors = (err as? CheckInSubmitException)?.fieldErrors.orEmpty()
                _painState.update {
                    it.copy(
                        isSubmitting = false,
                        fieldErrors = fieldErrors,
                        errorMessage = if (fieldErrors.isEmpty()) err.message ?: "Unable to submit pain check-in." else null
                    )
                }
            }
        }
    }

    private fun validateFatigue(form: FatigueFormState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (form.energyLevel == null) errors["energyLevel"] = "Select your energy level."
        if (form.sleepQuality == null) errors["sleepQuality"] = "Select your sleep quality."
        if (form.muscleSoreness == null) errors["muscleSoreness"] = "Select your muscle soreness."
        if (form.stressLevel == null) errors["stressLevel"] = "Select your stress level."
        return errors
    }

    private fun validatePain(form: PainFormState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (form.hasPain == null) {
            errors["hasPain"] = "Select yes or no."
            return errors
        }
        if (form.hasPain == true) {
            if (form.bodyRegion.isNullOrBlank()) errors["bodyRegion"] = "Select pain location."
            if (form.painType.isNullOrBlank()) errors["painType"] = "Select pain type."
            if (form.severity == null) errors["severity"] = "Select pain severity."
            if (form.onsetContext.isNullOrBlank()) errors["onsetContext"] = "Select when pain started."
            if (form.canRun == null) errors["canRun"] = "Select whether you can run comfortably today."
        }
        return errors
    }
}
