package com.company.runcoach.feature.auth.domain

data class AuthSession(
    val onboardingRequired: Boolean
)

sealed class AuthFailure(override val message: String) : Exception(message) {
    data class Validation(override val message: String, val field: String?) : AuthFailure(message)
    data class Unauthorized(override val message: String) : AuthFailure(message)
    data class Connectivity(override val message: String) : AuthFailure(message)
    data class Server(override val message: String) : AuthFailure(message)
    data class Unknown(override val message: String) : AuthFailure(message)
}
