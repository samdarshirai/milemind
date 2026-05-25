package com.company.runcoach.feature.strava.ui

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.runcoach.feature.strava.ui.model.StravaEffect

@Composable
fun StravaRoute(
    oauthResult: String?,
    oauthReason: String?,
    viewModel: StravaViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    LaunchedEffect(oauthResult, oauthReason) {
        viewModel.onOAuthCallback(oauthResult, oauthReason)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is StravaEffect.OpenAuthorizationUrl -> {
                    runCatching {
                        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(effect.url))
                    }.onFailure {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(effect.url)))
                    }
                }
            }
        }
    }

    StravaScreen(
        state = state,
        onConnectClick = viewModel::connectStrava,
        onRetryClick = viewModel::refreshStatus,
        onDisconnectClick = viewModel::requestDisconnect,
        onDismissDisconnectDialog = viewModel::dismissDisconnectDialog,
        onConfirmDisconnect = viewModel::confirmDisconnect
    )
}

@Composable
fun StravaScreen(
    state: com.company.runcoach.feature.strava.ui.model.StravaUiState,
    onConnectClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onDismissDisconnectDialog: () -> Unit,
    onConfirmDisconnect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().testTag("strava_header_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFC4C02))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Strava Integration", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Text(
                    "Strava sync is optional and MVP is read-only. You can still complete workouts manually without Strava.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }

        if (state.isLoading || state.isFinishingConnection) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF16181D))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.testTag("strava_loading_indicator"))
                    Text(if (state.isFinishingConnection) "Finishing Strava connection..." else "Loading Strava status...")
                }
            }
        } else if (!state.isConnected) {
            Card(modifier = Modifier.fillMaxWidth().testTag("strava_not_connected_card")) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Not connected", style = MaterialTheme.typography.titleMedium)
                    Text("Connect Strava if you want optional read-only sync. Manual workout completion stays available.")
                    Button(
                        onClick = onConnectClick,
                        enabled = !state.isStartingConnection,
                        modifier = Modifier.testTag("strava_connect_button")
                    ) {
                        if (state.isStartingConnection) CircularProgressIndicator() else Text("Connect Strava")
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth().testTag("strava_connected_card")) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Connected", style = MaterialTheme.typography.titleMedium)
                    state.connectionStatusLabel?.let { Text("Status: $it") }
                    state.grantedScopesLabel?.let { Text("Granted scopes: $it") }
                    state.lastSyncAtLabel?.let { Text("Last sync: $it") }
                    OutlinedButton(
                        onClick = onDisconnectClick,
                        enabled = !state.isDisconnecting,
                        modifier = Modifier.testTag("strava_disconnect_button")
                    ) {
                        if (state.isDisconnecting) CircularProgressIndicator() else Text("Disconnect Strava")
                    }
                }
            }
        }

        state.cancellationMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("strava_cancel_message"))
        }
        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("strava_error_message"))
            OutlinedButton(onClick = onRetryClick, modifier = Modifier.testTag("strava_retry_button")) {
                Text("Try again")
            }
        }
    }

    if (state.showDisconnectConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissDisconnectDialog,
            title = { Text("Disconnect Strava?") },
            text = { Text("You can reconnect at any time.") },
            confirmButton = {
                TextButton(onClick = onConfirmDisconnect) {
                    Text("Disconnect")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDisconnectDialog) {
                    Text("Cancel")
                }
            }
        )
    }
}
