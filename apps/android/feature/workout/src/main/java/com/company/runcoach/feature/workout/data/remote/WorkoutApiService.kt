package com.company.runcoach.feature.workout.data.remote

import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface WorkoutApiService {
    @GET("v1/planned-workouts/{plannedWorkoutId}")
    suspend fun getWorkoutDetail(@Path("plannedWorkoutId") plannedWorkoutId: String): PlannedWorkoutDetailResponse

    @POST("v1/planned-workouts/{plannedWorkoutId}/skip")
    suspend fun skipWorkout(
        @Path("plannedWorkoutId") plannedWorkoutId: String,
        @Body request: SkipWorkoutRequest
    ): AdaptationMutationResponse

    @POST("v1/planned-workouts/{plannedWorkoutId}/reschedule")
    suspend fun rescheduleWorkout(
        @Path("plannedWorkoutId") plannedWorkoutId: String,
        @Body request: RescheduleWorkoutRequest
    ): AdaptationMutationResponse
}
