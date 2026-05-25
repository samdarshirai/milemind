package com.company.runcoach.feature.strava

import com.company.runcoach.feature.strava.data.StravaRepository
import com.company.runcoach.feature.strava.data.remote.StravaApiService
import com.company.runcoach.feature.strava.data.remote.StravaConnectSessionResponse
import com.company.runcoach.feature.strava.data.remote.StravaConnectionStatusResponse
import com.company.runcoach.feature.strava.ui.StravaViewModel
import com.company.runcoach.feature.strava.ui.model.StravaEffect
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StravaViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStatusLoading_connectedStateRendered() = runTest(dispatcher) {
        val viewModel = StravaViewModel(StravaRepository(ConnectedApi()))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isConnected)
    }

    @Test
    fun notConnectedStatus_showsDisconnectedState() = runTest(dispatcher) {
        val viewModel = StravaViewModel(StravaRepository(NotConnectedApi()))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isConnected)
    }

    @Test
    fun connectSessionSuccess_emitsOpenAuthorizationUrlEffect() = runTest(dispatcher) {
        val viewModel = StravaViewModel(StravaRepository(ConnectSessionApi()))
        advanceUntilIdle()

        val effectDeferred = async { viewModel.effects.firstOpenAuthUrl() }
        viewModel.connectStrava()
        advanceUntilIdle()

        assertEquals("https://www.strava.com/oauth/authorize?state=abc", effectDeferred.await())
    }

    @Test
    fun connectSessionFailure_showsError() = runTest(dispatcher) {
        val viewModel = StravaViewModel(StravaRepository(ConnectFailureApi()))
        advanceUntilIdle()

        viewModel.connectStrava()
        advanceUntilIdle()

        assertEquals("Could not reach the server. Please try again.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun deepLinkSuccess_refreshesStatus() = runTest(dispatcher) {
        val api = CallbackRefreshApi()
        val viewModel = StravaViewModel(StravaRepository(api))
        advanceUntilIdle()

        viewModel.onOAuthCallback("success", null)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isConnected)
        assertTrue(api.statusCalls >= 2)
    }

    @Test
    fun duplicateDeepLinkCallback_isHandledOnce() = runTest(dispatcher) {
        val api = CallbackRefreshApi()
        val viewModel = StravaViewModel(StravaRepository(api))
        advanceUntilIdle()

        viewModel.onOAuthCallback("success", null)
        advanceUntilIdle()
        val callsAfterFirst = api.statusCalls

        viewModel.onOAuthCallback("success", null)
        advanceUntilIdle()

        assertEquals(callsAfterFirst, api.statusCalls)
    }

    @Test
    fun deepLinkCancelled_showsCancelledMessage() = runTest(dispatcher) {
        val viewModel = StravaViewModel(StravaRepository(NotConnectedApi()))
        advanceUntilIdle()

        viewModel.onOAuthCallback("cancelled", "authorization_denied")
        advanceUntilIdle()

        assertEquals("Strava connection was cancelled.", viewModel.uiState.value.cancellationMessage)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun deepLinkErrorWithAuthorizationDenied_showsCancelledMessage() = runTest(dispatcher) {
        val viewModel = StravaViewModel(StravaRepository(NotConnectedApi()))
        advanceUntilIdle()

        viewModel.onOAuthCallback("error", "authorization_denied")
        advanceUntilIdle()

        assertEquals("Strava connection was cancelled.", viewModel.uiState.value.cancellationMessage)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun deepLinkErrorWithInvalidState_showsError() = runTest(dispatcher) {
        val viewModel = StravaViewModel(StravaRepository(NotConnectedApi()))
        advanceUntilIdle()

        viewModel.onOAuthCallback("error", "invalid_state")
        advanceUntilIdle()

        assertEquals("Could not complete Strava connection.", viewModel.uiState.value.errorMessage)
        assertEquals(null, viewModel.uiState.value.cancellationMessage)
    }

    @Test
    fun disconnectConfirmation_callsApiAndUpdatesState() = runTest(dispatcher) {
        val api = DisconnectApi()
        val viewModel = StravaViewModel(StravaRepository(api))
        advanceUntilIdle()

        viewModel.requestDisconnect()
        assertTrue(viewModel.uiState.value.showDisconnectConfirmation)

        viewModel.confirmDisconnect()
        advanceUntilIdle()

        assertTrue(api.disconnected)
        assertFalse(viewModel.uiState.value.isConnected)
    }

    @Test
    fun disconnectFailure_showsFriendlyError() = runTest(dispatcher) {
        val viewModel = StravaViewModel(StravaRepository(DisconnectFailureApi()))
        advanceUntilIdle()

        viewModel.requestDisconnect()
        viewModel.confirmDisconnect()
        advanceUntilIdle()

        assertEquals("Could not disconnect Strava. Please try again.", viewModel.uiState.value.errorMessage)
    }
}

private suspend fun SharedFlow<StravaEffect>.firstOpenAuthUrl(): String {
    return first { it is StravaEffect.OpenAuthorizationUrl }
        .let { (it as StravaEffect.OpenAuthorizationUrl).url }
}

private class ConnectedApi : StravaApiService {
    override suspend fun createConnectSession(): StravaConnectSessionResponse =
        StravaConnectSessionResponse("https://www.strava.com/oauth/authorize", "state-1")

    override suspend fun getStatus(): StravaConnectionStatusResponse =
        StravaConnectionStatusResponse(
            connected = true,
            connectionStatus = "ACTIVE",
            grantedScopes = listOf("read", "activity:read"),
            lastSyncAt = "2026-05-21T10:15:30Z"
        )

    override suspend fun disconnect(): StravaConnectionStatusResponse = StravaConnectionStatusResponse(false, "DISCONNECTED")
}

private class NotConnectedApi : StravaApiService {
    override suspend fun createConnectSession(): StravaConnectSessionResponse =
        StravaConnectSessionResponse("https://www.strava.com/oauth/authorize", "state-2")

    override suspend fun getStatus(): StravaConnectionStatusResponse = StravaConnectionStatusResponse(connected = false, connectionStatus = "DISCONNECTED")

    override suspend fun disconnect(): StravaConnectionStatusResponse = StravaConnectionStatusResponse(false, "DISCONNECTED")
}

private class ConnectSessionApi : StravaApiService {
    override suspend fun createConnectSession(): StravaConnectSessionResponse =
        StravaConnectSessionResponse("https://www.strava.com/oauth/authorize?state=abc", "abc")

    override suspend fun getStatus(): StravaConnectionStatusResponse = StravaConnectionStatusResponse(connected = false, connectionStatus = "DISCONNECTED")

    override suspend fun disconnect(): StravaConnectionStatusResponse = StravaConnectionStatusResponse(false, "DISCONNECTED")
}

private class ConnectFailureApi : StravaApiService {
    override suspend fun createConnectSession(): StravaConnectSessionResponse {
        throw IOException("offline")
    }

    override suspend fun getStatus(): StravaConnectionStatusResponse = StravaConnectionStatusResponse(connected = false, connectionStatus = "DISCONNECTED")

    override suspend fun disconnect(): StravaConnectionStatusResponse = StravaConnectionStatusResponse(false, "DISCONNECTED")
}

private class CallbackRefreshApi : StravaApiService {
    var statusCalls = 0

    override suspend fun createConnectSession(): StravaConnectSessionResponse =
        StravaConnectSessionResponse("https://www.strava.com/oauth/authorize", "state-4")

    override suspend fun getStatus(): StravaConnectionStatusResponse {
        statusCalls += 1
        return if (statusCalls == 1) {
            StravaConnectionStatusResponse(connected = false)
        } else {
            StravaConnectionStatusResponse(connected = true, connectionStatus = "ACTIVE", grantedScopes = listOf("read"))
        }
    }

    override suspend fun disconnect(): StravaConnectionStatusResponse = StravaConnectionStatusResponse(false, "DISCONNECTED")
}

private class DisconnectApi : StravaApiService {
    var disconnected = false

    override suspend fun createConnectSession(): StravaConnectSessionResponse =
        StravaConnectSessionResponse("https://www.strava.com/oauth/authorize", "state-5")

    override suspend fun getStatus(): StravaConnectionStatusResponse =
        StravaConnectionStatusResponse(connected = true, connectionStatus = "ACTIVE")

    override suspend fun disconnect(): StravaConnectionStatusResponse {
        disconnected = true
        return StravaConnectionStatusResponse(false, "DISCONNECTED")
    }
}

private class DisconnectFailureApi : StravaApiService {
    override suspend fun createConnectSession(): StravaConnectSessionResponse =
        StravaConnectSessionResponse("https://www.strava.com/oauth/authorize", "state-6")

    override suspend fun getStatus(): StravaConnectionStatusResponse =
        StravaConnectionStatusResponse(connected = true, connectionStatus = "ACTIVE")

    override suspend fun disconnect(): StravaConnectionStatusResponse {
        throw IllegalStateException("server")
    }
}
