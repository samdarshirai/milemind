package com.company.runcoach.feature.today.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.company.runcoach.feature.today.ui.model.ReadinessBannerStatus
import com.company.runcoach.feature.today.ui.model.ReadinessBannerUiModel
import com.company.runcoach.feature.today.ui.model.TodayUiState
import org.junit.Rule
import org.junit.Test

class TodayToProgressNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tapViewProgress_navigatesToProgressRoute() {
        composeRule.setContent {
            TodayToProgressTestNavHost()
        }

        composeRule.onNodeWithTag("today_open_progress").performClick()
        composeRule.onNodeWithTag("progress_destination").assertIsDisplayed()
    }
}

@Composable
private fun TodayToProgressTestNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "today_test") {
        composable("today_test") {
            TodayScreen(
                state = TodayUiState(
                    isLoading = false,
                    readinessBanner = ReadinessBannerUiModel(
                        title = "Ready",
                        message = "You are set.",
                        ctaLabel = "Update readiness",
                        status = ReadinessBannerStatus.READY
                    )
                ),
                onPrimaryAction = {},
                onOpenWorkout = { _, _ -> },
                onOpenPlan = {},
                onOpenProgress = { navController.navigate("progress_test") },
                onRetry = {},
                onRetryWorkout = {},
                onOpenWhatChanged = {},
                onDismissWhatChanged = {}
            )
        }
        composable("progress_test") {
            Text("Progress destination", modifier = Modifier.testTag("progress_destination"))
        }
    }
}
