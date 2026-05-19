package com.company.runcoach.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.runcoach.feature.auth.data.AuthRepository
import com.company.runcoach.feature.auth.domain.AuthFailure
import com.company.runcoach.feature.auth.ui.model.SignInUiState
import com.company.runcoach.feature.auth.ui.model.SignUpUiState
import com.company.runcoach.feature.auth.ui.model.SplashDestination
import com.company.runcoach.feature.auth.ui.model.SplashUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        restoreSession()
    }

    fun restoreSession() {
        _uiState.value = SplashUiState(isLoading = true)
        viewModelScope.launch {
            authRepository.restoreSession()
                .onSuccess { session ->
                    _uiState.value = SplashUiState(
                        isLoading = false,
                        destination = if (session.onboardingRequired) {
                            SplashDestination.ONBOARDING
                        } else {
                            SplashDestination.MAIN
                        }
                    )
                }
                .onFailure { error ->
                    if (error is AuthFailure.Unauthorized) {
                        _uiState.value = SplashUiState(
                            isLoading = false,
                            destination = SplashDestination.SIGN_IN
                        )
                    } else {
                        _uiState.value = SplashUiState(
                            isLoading = false,
                            errorMessage = (error as? AuthFailure)?.message ?: "Failed to restore session."
                        )
                    }
                }
        }
    }
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }

    fun submit() {
        val current = _uiState.value
        val emailError = if (current.email.contains("@")) null else "Enter a valid email"
        val passwordError = if (current.password.length >= 10) null else "Password must be at least 10 characters"
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.signIn(current.email, current.password)
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            onboardingRequired = session.onboardingRequired
                        )
                    }
                }
                .onFailure { error ->
                    val failure = error as? AuthFailure
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = failure?.message ?: "Sign in failed"
                        )
                    }
                }
        }
    }
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    fun onConfirmPasswordChanged(value: String) = _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null) }

    fun submit() {
        val current = _uiState.value
        val emailError = if (current.email.contains("@")) null else "Enter a valid email"
        val passwordError = if (current.password.length >= 10) null else "Password must be at least 10 characters"
        val confirmError = if (current.confirmPassword == current.password) null else "Passwords do not match"

        if (emailError != null || passwordError != null || confirmError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.signUp(current.email, current.password)
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            onboardingRequired = session.onboardingRequired
                        )
                    }
                }
                .onFailure { error ->
                    val failure = error as? AuthFailure
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = failure?.message ?: "Sign up failed"
                        )
                    }
                }
        }
    }
}
