package com.company.runcoach.feature.checkin.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CheckInApiService {
    @GET("/v1/profile")
    suspend fun getProfile(): RunnerProfileResponse

    @POST("/v1/fatigue-signals")
    suspend fun submitFatigueSignal(@Body request: FatigueSignalRequest): FatigueSignalResponse

    @POST("/v1/injury-feedback")
    suspend fun submitInjuryFeedback(@Body request: InjuryFeedbackRequest): InjuryFeedbackResponse
}
