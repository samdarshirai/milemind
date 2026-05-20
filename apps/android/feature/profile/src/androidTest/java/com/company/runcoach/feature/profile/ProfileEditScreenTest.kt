package com.company.runcoach.feature.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.company.runcoach.feature.profile.data.EditableProfile
import com.company.runcoach.feature.profile.ui.ProfileEditScreen
import com.company.runcoach.feature.profile.ui.ProfileEditUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileEditScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun editableFields_updateProfileAndTriggerSave() {
        var onSaveCalled = false
        var state by mutableStateOf(
            ProfileEditUiState(
                profile = EditableProfile(
                    weeklyDistance = "28",
                    longestRun = "12",
                    preferredRunDays = setOf("MONDAY", "WEDNESDAY", "SATURDAY"),
                    preferredLongRunDay = "SUNDAY",
                    strengthDaysPerWeek = 1,
                    units = "KM",
                    timezone = "Europe/Berlin",
                    injuryHistorySummary = ""
                )
            )
        )

        composeRule.setContent {
            ProfileEditScreen(
                state = state,
                onProfileChange = { state = state.copy(profile = it) },
                onSave = { onSaveCalled = true }
            )
        }

        composeRule.onNodeWithTag("runDay-TUESDAY").performClick()
        composeRule.onNodeWithTag("longRunDay-FRIDAY").performClick()
        composeRule.onNodeWithTag("strength-2").performClick()
        composeRule.onNodeWithTag("units-MILES").performClick()
        composeRule.onNodeWithTag("timezoneInput").performTextClearance()
        composeRule.onNodeWithTag("timezoneInput").performTextInput("America/New_York")
        composeRule.onNodeWithTag("injurySummaryInput").performTextInput("Recovered from calf strain")
        composeRule.onNodeWithTag("saveButton").performClick()

        composeRule.runOnIdle {
            assertTrue("TUESDAY" in state.profile.preferredRunDays)
            assertEquals("FRIDAY", state.profile.preferredLongRunDay)
            assertEquals(2, state.profile.strengthDaysPerWeek)
            assertEquals("MILES", state.profile.units)
            assertEquals("America/New_York", state.profile.timezone)
            assertTrue(state.profile.injuryHistorySummary.contains("Recovered from calf strain"))
            assertTrue(onSaveCalled)
        }
    }

    @Test
    fun showsValidationGeneralErrorAndSuccessMessages() {
        composeRule.setContent {
            ProfileEditScreen(
                state = ProfileEditUiState(
                    profile = EditableProfile(
                        weeklyDistance = "28",
                        longestRun = "12",
                        preferredRunDays = setOf("MONDAY"),
                        preferredLongRunDay = "SUNDAY",
                        strengthDaysPerWeek = 1,
                        units = "KM",
                        timezone = "Invalid/Zone"
                    ),
                    fieldErrors = mapOf(
                        "preferredRunDays" to "Choose at least 3 run days.",
                        "timezone" to "Enter a valid timezone (for example, Europe/Berlin)."
                    ),
                    errorMessage = "Unable to save profile.",
                    saveSuccess = true
                ),
                onProfileChange = {},
                onSave = {}
            )
        }

        composeRule.onNodeWithTag("profile_edit_hero_block").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_baseline_card").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_schedule_card").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_preferences_card").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_injury_card").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_cta_group").assertIsDisplayed()
        composeRule.onNodeWithText("Choose at least 3 run days.").assertIsDisplayed()
        composeRule.onNodeWithText("Enter a valid timezone (for example, Europe/Berlin).")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Unable to save profile.").assertIsDisplayed()
        composeRule.onNodeWithText("Saved").assertIsDisplayed()
    }
}
