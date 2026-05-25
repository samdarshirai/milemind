package com.company.runcoach.feature.strava

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.company.runcoach.feature.strava.ui.navigation.stravaGraph
import org.junit.Rule
import org.junit.Test

class StravaDeepLinkNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun oauthReturnDeepLink_navigatesToStravaDestination_withExpectedArgs() {
        composeRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "entry") {
                composable("entry") {
                    LaunchedEffect(Unit) {
                        navController.handleDeepLink(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://app.example.com/strava/connected?result=success&reason=authorization_denied")
                            )
                        )
                    }
                }
                stravaGraph { oauthResult, oauthReason ->
                    Text(
                        text = "$oauthResult|$oauthReason",
                        modifier = Modifier.testTag("strava_deeplink_destination")
                    )
                }
            }
        }

        composeRule.onNodeWithTag("strava_deeplink_destination").assertIsDisplayed()
        composeRule.onNodeWithText("success|authorization_denied").assertIsDisplayed()
    }
}
