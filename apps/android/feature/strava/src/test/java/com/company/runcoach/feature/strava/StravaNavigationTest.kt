package com.company.runcoach.feature.strava

import com.company.runcoach.feature.strava.ui.navigation.StravaRoutes
import org.junit.Assert.assertEquals
import org.junit.Test

class StravaNavigationTest {

    @Test
    fun connectionRouteWithoutCallbackParams_usesBaseRoute() {
        assertEquals("strava/connection", StravaRoutes.connectionRoute())
    }

    @Test
    fun connectionRouteWithResult_buildsCallbackRoute() {
        assertEquals("strava/connection?oauthResult=success", StravaRoutes.connectionRoute(oauthResult = "success"))
    }

    @Test
    fun connectionRouteWithResultAndReason_buildsCallbackRouteWithReason() {
        assertEquals(
            "strava/connection?oauthResult=error&oauthReason=invalid_state",
            StravaRoutes.connectionRoute(oauthResult = "error", oauthReason = "invalid_state")
        )
    }

    @Test
    fun appLinkPatterns_areStable() {
        assertEquals(
            "https://app.example.com/strava/connected?result={oauthResult}",
            StravaRoutes.AppLinkCallbackPattern
        )
        assertEquals(
            "https://app.example.com/strava/connected?result={oauthResult}&reason={oauthReason}",
            StravaRoutes.AppLinkCallbackWithReasonPattern
        )
    }

    @Test
    fun customSchemePatterns_areStable() {
        assertEquals(
            "milemind://integrations/strava/callback?result={oauthResult}",
            StravaRoutes.CustomSchemeCallbackPattern
        )
        assertEquals(
            "milemind://integrations/strava/callback?result={oauthResult}&reason={oauthReason}",
            StravaRoutes.CustomSchemeCallbackWithReasonPattern
        )
    }
}
