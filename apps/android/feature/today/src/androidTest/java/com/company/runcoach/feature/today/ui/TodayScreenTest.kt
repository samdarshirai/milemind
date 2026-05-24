package com.company.runcoach.feature.today.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.company.runcoach.feature.today.ui.model.ReadinessBannerStatus
import com.company.runcoach.feature.today.ui.model.ReadinessBannerUiModel
import com.company.runcoach.feature.today.ui.model.TodayWorkoutUiModel
import com.company.runcoach.feature.today.ui.model.TodayUiState
import org.junit.Rule
import org.junit.Test

class TodayScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun todayScreen_rendersNoCheckInBanner() {
        composeRule.setContent {
            TodayScreen(
                state = TodayUiState(
                    isLoading = false,
                    readinessBanner = ReadinessBannerUiModel(
                        title = "How are you feeling today?",
                        message = "Complete your daily check-in to calibrate today’s training effort.",
                        ctaLabel = "Check in",
                        status = ReadinessBannerStatus.NO_CHECK_IN
                    )
                ),
                onPrimaryAction = {},
                onOpenWorkout = { _, _ -> },
                onOpenPlan = {},
                onRetry = {},
                onRetryWorkout = {}
            )
        }

        composeRule.onNodeWithTag("today_readiness_banner").assertIsDisplayed()
        composeRule.onNodeWithText("Check in").assertIsDisplayed()
    }

    @Test
    fun todayScreen_rendersCautionBanner() {
        composeRule.setContent {
            TodayScreen(
                state = TodayUiState(
                    isLoading = false,
                    readinessBanner = ReadinessBannerUiModel(
                        title = "Caution",
                        message = "Consider keeping effort conservative today.",
                        ctaLabel = "Update readiness",
                        status = ReadinessBannerStatus.CAUTION
                    )
                ),
                onPrimaryAction = {},
                onOpenWorkout = { _, _ -> },
                onOpenPlan = {},
                onRetry = {},
                onRetryWorkout = {}
            )
        }

        composeRule.onNodeWithText("Caution").assertIsDisplayed()
        composeRule.onNodeWithText("Update readiness").assertIsDisplayed()
    }

    @Test
    fun todayScreen_rendersWorkoutCardWhenWorkoutExists() {
        composeRule.setContent {
            TodayScreen(
                state = TodayUiState(
                    isLoading = false,
                    readinessBanner = ReadinessBannerUiModel(
                        title = "Ready",
                        message = "You are set.",
                        ctaLabel = "Update readiness",
                        status = ReadinessBannerStatus.READY
                    ),
                    todayWorkout = TodayWorkoutUiModel(
                        plannedWorkoutId = "workout-1",
                        title = "EASY RUN",
                        status = "PLANNED",
                        detail = "45 min planned",
                        intensity = "EASY"
                    )
                ),
                onPrimaryAction = {},
                onOpenWorkout = { _, _ -> },
                onOpenPlan = {},
                onRetry = {},
                onRetryWorkout = {}
            )
        }

        composeRule.onNodeWithTag("today_workout_card").assertIsDisplayed()
        composeRule.onNodeWithTag("today_open_workout").assertIsDisplayed()
    }

    @Test
    fun todayScreen_rendersWorkoutErrorRetry() {
        composeRule.setContent {
            TodayScreen(
                state = TodayUiState(
                    isLoading = false,
                    workoutLoadFailed = true,
                    workoutErrorMessage = "Today's workout could not be loaded. Try again.",
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
                onRetry = {},
                onRetryWorkout = {}
            )
        }

        composeRule.onNodeWithTag("today_workout_error_message").assertIsDisplayed()
        composeRule.onNodeWithTag("today_workout_retry").assertIsDisplayed()
    }
}
