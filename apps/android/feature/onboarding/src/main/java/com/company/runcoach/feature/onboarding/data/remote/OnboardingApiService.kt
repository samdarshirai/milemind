package com.company.runcoach.feature.onboarding.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OnboardingApiService {
    @POST("/v1/users/onboarding")
    suspend fun submitOnboarding(@Body request: OnboardingRequest): OnboardingResponse

    @GET("/v1/profile")
    suspend fun getProfile(): ProfileResponse
}
