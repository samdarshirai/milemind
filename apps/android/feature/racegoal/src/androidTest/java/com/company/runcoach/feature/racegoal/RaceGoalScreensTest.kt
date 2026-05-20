package com.company.runcoach.feature.racegoal

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.company.runcoach.feature.racegoal.ui.ExistingGoalScreen
import com.company.runcoach.feature.racegoal.ui.GoalSavedScreen
import com.company.runcoach.feature.racegoal.ui.RaceGoalReviewScreen
import com.company.runcoach.feature.racegoal.ui.RaceGoalSetupScreen
import com.company.runcoach.feature.racegoal.ui.model.RaceGoalForm
import org.junit.Rule
import org.junit.Test

class RaceGoalScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun raceGoalSetup_renders() {
        composeRule.setContent {
            RaceGoalSetupScreen(
                form = RaceGoalForm(),
                errors = emptyMap(),
                tooSoonMessage = null,
                activeGoalMessage = null,
                submitError = null,
                onFormChange = {},
                onContinue = {},
                snackbarHostState = SnackbarHostState()
            )
        }

        composeRule.onNodeWithTag("race_distance_card").assertIsDisplayed()
        composeRule.onNodeWithTag("goal_style_selector").assertIsDisplayed()
        composeRule.onNodeWithTag("target_time_input").assertIsDisplayed()
        composeRule.onNodeWithTag("race_goal_continue").assertIsDisplayed()
    }

    @Test
    fun raceDateTooSoon_andActiveGoalErrors_render() {
        composeRule.setContent {
            RaceGoalSetupScreen(
                form = RaceGoalForm(),
                errors = emptyMap(),
                tooSoonMessage = "Race date must be at least 8 weeks away.",
                activeGoalMessage = "You already have an active goal.",
                submitError = null,
                onFormChange = {},
                onContinue = {},
                snackbarHostState = SnackbarHostState()
            )
        }

        composeRule.onNodeWithTag("race_date_too_soon_banner").assertIsDisplayed()
        composeRule.onNodeWithTag("existing_active_goal_banner").assertIsDisplayed()
    }

    @Test
    fun reviewAndSavedPlaceholders_render() {
        composeRule.setContent {
            RaceGoalReviewScreen(
                form = RaceGoalForm(
                    raceDistanceType = "HALF_MARATHON",
                    raceDate = "2099-12-30",
                    goalStyle = "FINISH"
                ),
                isSaving = false,
                onBack = {},
                onConfirm = {},
                snackbarHostState = SnackbarHostState()
            )
        }

        composeRule.onNodeWithTag("race_goal_summary_card").assertIsDisplayed()
        composeRule.onNodeWithTag("race_goal_confirm").assertIsDisplayed()

        composeRule.setContent {
            GoalSavedScreen(onContinue = {})
        }
        composeRule.onNodeWithTag("goal_saved_continue").assertIsDisplayed()
        composeRule.onNodeWithText("Plan generation is coming in the next slice.").assertIsDisplayed()
    }

    @Test
    fun existingActiveGoal_rendersSummaryAndContinueCta() {
        composeRule.setContent {
            ExistingGoalScreen(onContinue = {})
        }

        composeRule.onNodeWithTag("current_goal_summary").assertIsDisplayed()
        composeRule.onNodeWithTag("existing_goal_continue").assertIsDisplayed()
    }
}
