package com.company.runcoach.feature.strava.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.compose.runtime.Composable
import com.company.runcoach.feature.strava.ui.StravaRoute

object StravaRoutes {
    const val ConnectionBase = "strava/connection"
    const val Connection = "strava/connection?oauthResult={oauthResult}&oauthReason={oauthReason}"
    const val AppLinkCallbackPattern = "https://app.example.com/strava/connected?result={oauthResult}"
    const val AppLinkCallbackWithReasonPattern = "https://app.example.com/strava/connected?result={oauthResult}&reason={oauthReason}"
    const val CustomSchemeCallbackPattern = "milemind://integrations/strava/callback?result={oauthResult}"
    const val CustomSchemeCallbackWithReasonPattern = "milemind://integrations/strava/callback?result={oauthResult}&reason={oauthReason}"

    fun connectionRoute(oauthResult: String? = null, oauthReason: String? = null): String {
        if (oauthResult == null && oauthReason == null) {
            return ConnectionBase
        }

        val params = buildList {
            oauthResult?.let { add("oauthResult=$it") }
            oauthReason?.let { add("oauthReason=$it") }
        }.joinToString("&")
        return "$ConnectionBase?$params"
    }
}

fun NavGraphBuilder.stravaGraph(
    destination: @Composable (oauthResult: String?, oauthReason: String?) -> Unit = { oauthResult, oauthReason ->
        StravaRoute(oauthResult = oauthResult, oauthReason = oauthReason)
    }
) {
    composable(
        route = StravaRoutes.Connection,
        arguments = listOf(
            navArgument("oauthResult") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument("oauthReason") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = StravaRoutes.AppLinkCallbackPattern },
            navDeepLink { uriPattern = StravaRoutes.AppLinkCallbackWithReasonPattern },
            navDeepLink { uriPattern = StravaRoutes.CustomSchemeCallbackPattern },
            navDeepLink { uriPattern = StravaRoutes.CustomSchemeCallbackWithReasonPattern }
        )
    ) { backStackEntry ->
        destination(
            backStackEntry.arguments?.getString("oauthResult"),
            backStackEntry.arguments?.getString("oauthReason")
        )
    }
}
