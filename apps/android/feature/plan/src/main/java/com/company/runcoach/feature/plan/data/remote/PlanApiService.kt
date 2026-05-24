package com.company.runcoach.feature.plan.data.remote

import retrofit2.http.GET

interface PlanApiService {
    @GET("v1/plans/current")
    suspend fun getCurrentPlan(): CurrentPlanResponse
}
