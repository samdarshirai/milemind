package com.company.runcoach.feature.workout.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface WorkoutApiService {
    @GET("v1/planned-workouts/{plannedWorkoutId}")
    suspend fun getWorkoutDetail(@Path("plannedWorkoutId") plannedWorkoutId: String): PlannedWorkoutDetailResponse
}
