package com.company.runcoach.feature.strava.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class StravaConnectSessionResponse(
    val authorizationUrl: String,
    val state: String
)

@Serializable
data class StravaConnectionStatusResponse(
    val connected: Boolean,
    val connectionStatus: String? = null,
    val grantedScopes: List<String> = emptyList(),
    val lastSyncAt: String? = null
)
