package com.company.runcoach.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.runcoach.feature.profile.data.EditableProfile
import com.company.runcoach.feature.profile.data.ProfileSaveException
import com.company.runcoach.feature.profile.data.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {
    private val minimumRunDays = 3
    private val validDays = setOf(
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY",
        "SATURDAY",
        "SUNDAY"
    )
    private val validUnits = setOf("KM", "MILES")

    private val _uiState = MutableStateFlow(ProfileEditUiState(isLoading = true))
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, fieldErrors = emptyMap()) }
        viewModelScope.launch {
            repository.loadProfile()
                .onSuccess { profile -> _uiState.update { it.copy(isLoading = false, profile = profile, saveSuccess = false) } }
                .onFailure { err -> _uiState.update { it.copy(isLoading = false, errorMessage = err.message ?: "Failed to load profile") } }
        }
    }

    fun updateProfile(profile: EditableProfile) {
        _uiState.update { it.copy(profile = profile, errorMessage = null, saveSuccess = false, fieldErrors = emptyMap()) }
    }

    fun save() {
        val profile = _uiState.value.profile
        val validationErrors = validate(profile)
        if (validationErrors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = validationErrors, errorMessage = null, saveSuccess = false) }
            return
        }

        _uiState.update { it.copy(isSaving = true, fieldErrors = emptyMap(), errorMessage = null) }
        viewModelScope.launch {
            repository.saveProfile(profile)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }
                .onFailure { err ->
                    val fieldErrors = (err as? ProfileSaveException)?.fieldErrors.orEmpty()
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            fieldErrors = fieldErrors,
                            errorMessage = if (fieldErrors.isEmpty()) "Unable to save profile." else null
                        )
                    }
                }
        }
    }

    private fun validate(profile: EditableProfile): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (profile.preferredRunDays.size < minimumRunDays) {
            errors["preferredRunDays"] = "Choose at least 3 run days."
        }
        if (!validDays.contains(profile.preferredLongRunDay)) {
            errors["preferredLongRunDay"] = "Choose a valid long run day."
        } else if (!profile.preferredRunDays.contains(profile.preferredLongRunDay)) {
            errors["preferredLongRunDay"] = "Long run day must be one of your selected run days."
        }
        if (profile.strengthDaysPerWeek !in 0..2) {
            errors["strengthDaysPerWeek"] = "Strength days must be 0, 1, or 2."
        }
        if (!validUnits.contains(profile.units)) {
            errors["units"] = "Units must be KM or MILES."
        }
        if (profile.timezone.isBlank()) {
            errors["timezone"] = "Timezone is required."
        } else {
            try {
                ZoneId.of(profile.timezone)
            } catch (_: Exception) {
                errors["timezone"] = "Enter a valid timezone (for example, Europe/Berlin)."
            }
        }
        return errors
    }
}

data class ProfileEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val profile: EditableProfile = EditableProfile()
)
