package com.company.runcoach.feature.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.company.runcoach.feature.onboarding.ui.IntroScreen
import org.junit.Rule
import org.junit.Test

class OnboardingIntroScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun introRenders() {
        composeRule.setContent {
            IntroScreen(onContinue = {})
        }

        composeRule.onNodeWithText("Welcome to MILEMIND").assertIsDisplayed()
        composeRule.onNodeWithTag("intro_hero_block").assertIsDisplayed()
        composeRule.onNodeWithTag("intro_hero_image").assertIsDisplayed()
        composeRule.onNodeWithTag("intro_cta_group").assertIsDisplayed()
        composeRule.onNodeWithText("Start setup").assertIsDisplayed()
    }
}
