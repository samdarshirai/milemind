package com.company.runcoach.feature.strava.ui.model

import java.time.OffsetDateTime

data class StravaConnectionStatus(
    val connected: Boolean,
    val connectionStatus: String? = null,
    val grantedScopes: List<String> = emptyList(),
    val lastSyncAt: OffsetDateTime? = null
)

data class StravaUiState(
    val isLoading: Boolean = true,
    val isStartingConnection: Boolean = false,
    val isFinishingConnection: Boolean = false,
    val isDisconnecting: Boolean = false,
    val showDisconnectConfirmation: Boolean = false,
    val isConnected: Boolean = false,
    val connectionStatusLabel: String? = null,
    val grantedScopesLabel: String? = null,
    val lastSyncAtLabel: String? = null,
    val errorMessage: String? = null,
    val cancellationMessage: String? = null
)

sealed interface StravaEffect {
    data class OpenAuthorizationUrl(val url: String) : StravaEffect
}
