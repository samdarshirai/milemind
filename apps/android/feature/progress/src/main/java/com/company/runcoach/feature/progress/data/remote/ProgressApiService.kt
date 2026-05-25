package com.company.runcoach.feature.progress.data.remote

import retrofit2.http.GET

interface ProgressApiService {
    @GET("/v1/progress/summary")
    suspend fun getProgressSummary(): ProgressSummaryResponse
}
