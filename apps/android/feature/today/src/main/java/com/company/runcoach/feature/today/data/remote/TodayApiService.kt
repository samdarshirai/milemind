package com.company.runcoach.feature.today.data.remote

import retrofit2.http.GET

interface TodayApiService {
    @GET("/v1/insights/today")
    suspend fun getTodayInsights(): TodayInsightsResponse

    @GET("v1/plans/current")
    suspend fun getCurrentPlan(): CurrentPlanResponse
}
