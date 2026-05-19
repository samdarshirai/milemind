package com.company.runcoach.feature.auth.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): RefreshResponse
}
