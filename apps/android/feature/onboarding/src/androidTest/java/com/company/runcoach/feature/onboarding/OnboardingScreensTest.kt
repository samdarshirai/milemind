package com.company.runcoach.feature.onboarding

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.company.runcoach.feature.onboarding.ui.AvailabilityScreen
import com.company.runcoach.feature.onboarding.ui.RunningHistoryScreen
import com.company.runcoach.feature.onboarding.ui.model.AvailabilityForm
import com.company.runcoach.feature.onboarding.ui.model.RunningHistoryForm
import org.junit.Rule
import org.junit.Test

class OnboardingScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun runningHistory_showsValidationErrors() {
        composeRule.setContent {
            RunningHistoryScreen(
                form = RunningHistoryForm(),
                errors = mapOf(
                    "birthYear" to "You must be at least 18 years old.",
                    "weeklyDistance" to "Weekly distance must be positive.",
                    "longestRun" to "Longest recent run must be positive."
                ),
                onChange = {},
                onNext = {},
                onBack = {},
                snackbarHostState = SnackbarHostState()
            )
        }

        composeRule.onNodeWithText("You must be at least 18 years old.").assertIsDisplayed()
        composeRule.onNodeWithText("Weekly distance must be positive.").assertIsDisplayed()
        composeRule.onNodeWithText("Longest recent run must be positive.").assertIsDisplayed()
        composeRule.onNodeWithText("Sex").assertIsDisplayed()
        composeRule.onNodeWithText("Experience level").assertIsDisplayed()
        composeRule.onNodeWithTag("running_history_hero_block").assertIsDisplayed()
        composeRule.onNodeWithTag("running_history_metrics_card").assertIsDisplayed()
        composeRule.onNodeWithTag("running_history_profile_card").assertIsDisplayed()
        composeRule.onNodeWithTag("running_history_injury_card").assertIsDisplayed()
        composeRule.onNodeWithTag("running_history_cta_group").assertIsDisplayed()
    }

    @Test
    fun availability_showsValidationAndSubmitErrorAndLoadingState() {
        composeRule.setContent {
            AvailabilityScreen(
                form = AvailabilityForm(preferredRunDays = setOf("MONDAY"), timezone = "Europe/Berlin"),
                errors = mapOf(
                    "preferredRunDays" to "Choose at least 3 run days.",
                    "preferredLongRunDay" to "Choose a long run day."
                ),
                isLoading = true,
                submitError = "Unable to save profile",
                onChange = {},
                onSubmit = {},
                onBack = {},
                snackbarHostState = SnackbarHostState()
            )
        }

        composeRule.onNodeWithText("Choose at least 3 run days.").assertIsDisplayed()
        composeRule.onNodeWithText("Choose a long run day.").assertIsDisplayed()
        composeRule.onNodeWithText("Unable to save profile").assertIsDisplayed()
        composeRule.onNodeWithText("Timezone").assertIsDisplayed()
        composeRule.onNodeWithTag("availability_hero_block").assertIsDisplayed()
        composeRule.onNodeWithTag("availability_schedule_card").assertIsDisplayed()
        composeRule.onNodeWithTag("availability_preferences_card").assertIsDisplayed()
        composeRule.onNodeWithTag("availability_cta_group").assertIsDisplayed()
        composeRule.onNodeWithText("Save and continue").assertDoesNotExist()
    }
}
