package com.company.runcoach.feature.strava.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.runcoach.feature.strava.data.StravaRepository
import com.company.runcoach.feature.strava.ui.model.StravaEffect
import com.company.runcoach.feature.strava.ui.model.StravaUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class StravaViewModel @Inject constructor(
    private val repository: StravaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StravaUiState())
    val uiState: StateFlow<StravaUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<StravaEffect>()
    val effects: SharedFlow<StravaEffect> = _effects.asSharedFlow()

    private var lastHandledOauthCallback: String? = null

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                cancellationMessage = null,
                isFinishingConnection = false
            )
        }
        viewModelScope.launch {
            repository.fetchStatus()
                .onSuccess { status ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isConnected = status.connected,
                            connectionStatusLabel = status.connectionStatus,
                            grantedScopesLabel = status.grantedScopes.takeIf { scopes -> scopes.isNotEmpty() }?.joinToString(", "),
                            lastSyncAtLabel = status.lastSyncAt?.let(::formatDate),
                            errorMessage = null,
                            cancellationMessage = null
                        )
                    }
                }
                .onFailure {
                    val throwable = it
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = if (repository.isNetworkError(throwable)) {
                                NETWORK_ERROR
                            } else {
                                GENERIC_ERROR
                            }
                        )
                    }
                }
        }
    }

    fun connectStrava() {
        _uiState.update { it.copy(isStartingConnection = true, errorMessage = null, cancellationMessage = null) }
        viewModelScope.launch {
            repository.startConnectSession()
                .onSuccess { url ->
                    _uiState.update { it.copy(isStartingConnection = false) }
                    _effects.emit(StravaEffect.OpenAuthorizationUrl(url))
                }
                .onFailure {
                    val throwable = it
                    _uiState.update {
                        it.copy(
                            isStartingConnection = false,
                            errorMessage = if (repository.isNetworkError(throwable)) NETWORK_ERROR else GENERIC_ERROR
                        )
                    }
                }
        }
    }

    fun onOAuthCallback(result: String?, reason: String?) {
        if (result == null) {
            return
        }

        val normalizedReason = reason?.trim()?.lowercase()
        val callbackKey = "${result.lowercase()}|${normalizedReason ?: ""}"
        if (callbackKey == lastHandledOauthCallback) {
            return
        }
        lastHandledOauthCallback = callbackKey

        when (result.lowercase()) {
            "success" -> {
                _uiState.update { it.copy(isFinishingConnection = true, errorMessage = null, cancellationMessage = null) }
                viewModelScope.launch {
                    repository.fetchStatus()
                        .onSuccess { status ->
                            if (status.connected) {
                                _uiState.update {
                                    it.copy(
                                        isFinishingConnection = false,
                                        isConnected = true,
                                        connectionStatusLabel = status.connectionStatus,
                                        grantedScopesLabel = status.grantedScopes.takeIf { scopes -> scopes.isNotEmpty() }?.joinToString(", "),
                                        lastSyncAtLabel = status.lastSyncAt?.let(::formatDate)
                                    )
                                }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isFinishingConnection = false,
                                        errorMessage = GENERIC_ERROR
                                    )
                                }
                            }
                        }
                        .onFailure {
                            val throwable = it
                            _uiState.update {
                                it.copy(
                                    isFinishingConnection = false,
                                    errorMessage = if (repository.isNetworkError(throwable)) NETWORK_ERROR else GENERIC_ERROR
                                )
                            }
                        }
                }
            }
            "cancelled", "canceled" -> {
                _uiState.update {
                    it.copy(
                        isFinishingConnection = false,
                        cancellationMessage = "Strava connection was cancelled.",
                        errorMessage = null
                    )
                }
            }
            "error" -> {
                _uiState.update {
                    it.copy(
                        isFinishingConnection = false,
                        errorMessage = if (normalizedReason == "authorization_denied") null else GENERIC_ERROR,
                        cancellationMessage = if (normalizedReason == "authorization_denied") {
                            "Strava connection was cancelled."
                        } else {
                            null
                        }
                    )
                }
            }
            else -> {
                _uiState.update {
                    it.copy(
                        isFinishingConnection = false,
                        errorMessage = if (normalizedReason == "authorization_denied") null else GENERIC_ERROR,
                        cancellationMessage = if (normalizedReason == "authorization_denied") {
                            "Strava connection was cancelled."
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    fun requestDisconnect() {
        _uiState.update { it.copy(showDisconnectConfirmation = true) }
    }

    fun dismissDisconnectDialog() {
        _uiState.update { it.copy(showDisconnectConfirmation = false) }
    }

    fun confirmDisconnect() {
        _uiState.update {
            it.copy(
                isDisconnecting = true,
                showDisconnectConfirmation = false,
                errorMessage = null,
                cancellationMessage = null
            )
        }
        viewModelScope.launch {
            repository.disconnect()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isDisconnecting = false,
                            isConnected = false,
                            connectionStatusLabel = "DISCONNECTED",
                            grantedScopesLabel = null,
                            lastSyncAtLabel = null
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isDisconnecting = false,
                            errorMessage = "Could not disconnect Strava. Please try again."
                        )
                    }
                }
        }
    }

    private fun formatDate(value: java.time.OffsetDateTime): String {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        return value.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate().format(formatter)
    }

    companion object {
        private const val NETWORK_ERROR = "Could not reach the server. Please try again."
        private const val GENERIC_ERROR = "Could not complete Strava connection."
    }
}
