package com.company.runcoach.feature.strava.data.remote

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface StravaApiService {
    @POST("/v1/integrations/strava/connect-session")
    suspend fun createConnectSession(): StravaConnectSessionResponse

    @GET("/v1/integrations/strava/status")
    suspend fun getStatus(): StravaConnectionStatusResponse

    @DELETE("/v1/integrations/strava/connection")
    suspend fun disconnect(): StravaConnectionStatusResponse
}
