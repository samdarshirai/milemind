package com.company.runcoach.feature.racegoal.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RaceGoalApiService {
    @POST("v1/race-goals")
    suspend fun createRaceGoal(@Body request: CreateRaceGoalRequest): CreateRaceGoalResponse

    @GET("v1/race-goals/current")
    suspend fun getCurrentRaceGoal(): CurrentRaceGoalResponse
}
