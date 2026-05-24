package com.company.runcoach.feature.today.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.company.runcoach.feature.today.ui.model.TodayUiState
import org.junit.Rule
import org.junit.Test

class TodayScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun todayShell_renders() {
        composeRule.setContent {
            TodayScreen(state = TodayUiState())
        }

        composeRule.onNodeWithTag("today_shell").assertIsDisplayed()
    }
}
