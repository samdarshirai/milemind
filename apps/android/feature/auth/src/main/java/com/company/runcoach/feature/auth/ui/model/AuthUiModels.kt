package com.company.runcoach.feature.auth.ui.model

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isSuccess: Boolean = false,
    val onboardingRequired: Boolean = false
)

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSuccess: Boolean = false,
    val onboardingRequired: Boolean = true
)

enum class SplashDestination {
    SIGN_IN,
    ONBOARDING,
    MAIN
}

data class SplashUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val destination: SplashDestination? = null
)
