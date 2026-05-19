package com.company.runcoach.feature.auth.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val timezone: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val onboardingRequired: Boolean = true,
    val userId: String? = null
)

@Serializable
data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class ApiFieldDetail(
    val field: String? = null,
    val issue: String? = null
)

@Serializable
data class ApiErrorPayload(
    val code: String,
    val message: String,
    val details: List<ApiFieldDetail> = emptyList(),
    val correlationId: String? = null
)

@Serializable
data class ApiErrorEnvelope(
    val error: ApiErrorPayload
)
