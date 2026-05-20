package com.company.runcoach.feature.profile.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ProfileApiService {
    @GET("/v1/profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("/v1/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): ProfileResponse
}
