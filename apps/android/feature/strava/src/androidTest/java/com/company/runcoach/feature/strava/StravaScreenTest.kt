package com.company.runcoach.feature.strava

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.company.runcoach.feature.strava.ui.StravaScreen
import com.company.runcoach.feature.strava.ui.model.StravaUiState
import org.junit.Rule
import org.junit.Test

class StravaScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun notConnectedScreen_renders() {
        composeRule.setContent {
            StravaScreen(
                state = StravaUiState(isLoading = false, isConnected = false),
                onConnectClick = {},
                onRetryClick = {},
                onDisconnectClick = {},
                onDismissDisconnectDialog = {},
                onConfirmDisconnect = {}
            )
        }

        composeRule.onNodeWithTag("strava_not_connected_card").assertIsDisplayed()
    }

    @Test
    fun connectedScreen_renders() {
        composeRule.setContent {
            StravaScreen(
                state = StravaUiState(isLoading = false, isConnected = true, connectionStatusLabel = "ACTIVE"),
                onConnectClick = {},
                onRetryClick = {},
                onDisconnectClick = {},
                onDismissDisconnectDialog = {},
                onConfirmDisconnect = {}
            )
        }

        composeRule.onNodeWithTag("strava_connected_card").assertIsDisplayed()
    }

    @Test
    fun errorState_renders() {
        composeRule.setContent {
            StravaScreen(
                state = StravaUiState(isLoading = false, errorMessage = "Could not complete Strava connection."),
                onConnectClick = {},
                onRetryClick = {},
                onDisconnectClick = {},
                onDismissDisconnectDialog = {},
                onConfirmDisconnect = {}
            )
        }

        composeRule.onNodeWithTag("strava_error_message").assertIsDisplayed()
    }

    @Test
    fun loadingState_renders() {
        composeRule.setContent {
            StravaScreen(
                state = StravaUiState(isLoading = true),
                onConnectClick = {},
                onRetryClick = {},
                onDisconnectClick = {},
                onDismissDisconnectDialog = {},
                onConfirmDisconnect = {}
            )
        }

        composeRule.onNodeWithTag("strava_loading_indicator").assertIsDisplayed()
    }

    @Test
    fun cancelledState_renders() {
        composeRule.setContent {
            StravaScreen(
                state = StravaUiState(isLoading = false, cancellationMessage = "Strava connection was cancelled."),
                onConnectClick = {},
                onRetryClick = {},
                onDisconnectClick = {},
                onDismissDisconnectDialog = {},
                onConfirmDisconnect = {}
            )
        }

        composeRule.onNodeWithTag("strava_cancel_message").assertIsDisplayed()
    }

    @Test
    fun disconnectingState_rendersConnectedCard() {
        composeRule.setContent {
            StravaScreen(
                state = StravaUiState(isLoading = false, isConnected = true, isDisconnecting = true),
                onConnectClick = {},
                onRetryClick = {},
                onDisconnectClick = {},
                onDismissDisconnectDialog = {},
                onConfirmDisconnect = {}
            )
        }

        composeRule.onNodeWithTag("strava_connected_card").assertIsDisplayed()
        composeRule.onNodeWithTag("strava_disconnect_button").assertIsDisplayed()
    }
}
