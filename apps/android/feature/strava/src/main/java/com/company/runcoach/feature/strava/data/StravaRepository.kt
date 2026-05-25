package com.company.runcoach.feature.strava.data

import com.company.runcoach.feature.strava.data.remote.StravaApiService
import com.company.runcoach.feature.strava.ui.model.StravaConnectionStatus
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StravaRepository @Inject constructor(
    private val apiService: StravaApiService
) {
    suspend fun fetchStatus(): Result<StravaConnectionStatus> = runCatching {
        val response = apiService.getStatus()
        StravaConnectionStatus(
            connected = response.connected,
            connectionStatus = response.connectionStatus,
            grantedScopes = response.grantedScopes,
            lastSyncAt = response.lastSyncAt?.let(::parseTimestamp)
        )
    }

    suspend fun startConnectSession(): Result<String> = runCatching {
        apiService.createConnectSession().authorizationUrl
    }

    suspend fun disconnect(): Result<Unit> = runCatching {
        apiService.disconnect()
        Unit
    }

    fun isNetworkError(throwable: Throwable): Boolean = throwable is IOException

    private fun parseTimestamp(value: String): OffsetDateTime? {
        return try {
            OffsetDateTime.parse(value)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
