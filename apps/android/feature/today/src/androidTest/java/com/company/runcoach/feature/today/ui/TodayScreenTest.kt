package com.company.runcoach.feature.today.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.company.runcoach.feature.today.ui.model.ReadinessBannerStatus
import com.company.runcoach.feature.today.ui.model.ReadinessBannerUiModel
import com.company.runcoach.feature.today.ui.model.TodayWorkoutUiModel
import com.company.runcoach.feature.today.ui.model.TodayUiState
import org.junit.Assert.assertTrue
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
                onOpenProgress = {},
                onRetry = {},
                onRetryWorkout = {},
                onOpenWhatChanged = {},
                onDismissWhatChanged = {}
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
                onOpenProgress = {},
                onRetry = {},
                onRetryWorkout = {},
                onOpenWhatChanged = {},
                onDismissWhatChanged = {}
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
                onOpenProgress = {},
                onRetry = {},
                onRetryWorkout = {},
                onOpenWhatChanged = {},
                onDismissWhatChanged = {}
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
                onOpenProgress = {},
                onRetry = {},
                onRetryWorkout = {},
                onOpenWhatChanged = {},
                onDismissWhatChanged = {}
            )
        }

        composeRule.onNodeWithTag("today_workout_error_message").assertIsDisplayed()
        composeRule.onNodeWithTag("today_workout_retry").assertIsDisplayed()
    }

    @Test
    fun todayScreen_rendersAdaptationBanner() {
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
                    latestAdaptation = com.company.runcoach.feature.today.ui.model.LatestAdaptationUiModel(
                        summary = "Your next 10 days were adjusted.",
                        affectedFromDate = "2026-06-01",
                        affectedToDate = "2026-06-10",
                        changedWorkoutIds = listOf("w1")
                    )
                ),
                onPrimaryAction = {},
                onOpenWorkout = { _, _ -> },
                onOpenPlan = {},
                onOpenProgress = {},
                onRetry = {},
                onRetryWorkout = {},
                onOpenWhatChanged = {},
                onDismissWhatChanged = {}
            )
        }

        composeRule.onNodeWithTag("today_adaptation_banner").assertIsDisplayed()
        composeRule.onNodeWithTag("see_what_changed").assertIsDisplayed()
    }

    @Test
    fun todayScreen_rendersWhatChangedSheetAndCloseAction() {
        var dismissed = false
        composeRule.setContent {
            TodayScreen(
                state = TodayUiState(
                    isLoading = false,
                    showWhatChanged = true,
                    readinessBanner = ReadinessBannerUiModel(
                        title = "Ready",
                        message = "You are set.",
                        ctaLabel = "Update readiness",
                        status = ReadinessBannerStatus.READY
                    ),
                    latestAdaptation = com.company.runcoach.feature.today.ui.model.LatestAdaptationUiModel(
                        summary = "Your next 10 days were adjusted.",
                        affectedFromDate = "2026-06-01",
                        affectedToDate = "2026-06-10",
                        changedWorkoutIds = listOf("w1")
                    )
                ),
                onPrimaryAction = {},
                onOpenWorkout = { _, _ -> },
                onOpenPlan = {},
                onOpenProgress = {},
                onRetry = {},
                onRetryWorkout = {},
                onOpenWhatChanged = {},
                onDismissWhatChanged = { dismissed = true }
            )
        }

        composeRule.onNodeWithText("What changed?").assertIsDisplayed()
        composeRule.onNodeWithText("Your next 10 days were adjusted.").assertIsDisplayed()
        composeRule.onNodeWithTag("what_changed_close").performClick()
        assertTrue(dismissed)
    }

    @Test
    fun todayScreen_rendersReadinessAndAdaptationInsights() {
        composeRule.setContent {
            TodayScreen(
                state = TodayUiState(
                    isLoading = false,
                    readinessBanner = ReadinessBannerUiModel(
                        title = "Take it easy",
                        message = "Lower readiness today.",
                        ctaLabel = "Update readiness",
                        status = ReadinessBannerStatus.HIGH_RISK
                    ),
                    insightMessage = "Plan adjusted for recovery.",
                    fatigueSummary = "Sleep 2 · Stress 4",
                    painSummary = "Pain 4/10 · Knee"
                ),
                onPrimaryAction = {},
                onOpenWorkout = { _, _ -> },
                onOpenPlan = {},
                onOpenProgress = {},
                onRetry = {},
                onRetryWorkout = {},
                onOpenWhatChanged = {},
                onDismissWhatChanged = {}
            )
        }

        composeRule.onNodeWithText("Plan adjusted for recovery.").assertIsDisplayed()
        composeRule.onNodeWithTag("today_fatigue_summary").assertIsDisplayed()
        composeRule.onNodeWithTag("today_pain_summary").assertIsDisplayed()
    }

    @Test
    fun todayScreen_rendersPostAdaptationState() {
        composeRule.setContent {
            TodayScreen(
                state = TodayUiState(
                    isLoading = false,
                    readinessBanner = ReadinessBannerUiModel(
                        title = "Caution",
                        message = "Adaptation applied",
                        ctaLabel = "Update readiness",
                        status = ReadinessBannerStatus.CAUTION
                    ),
                    latestAdaptation = com.company.runcoach.feature.today.ui.model.LatestAdaptationUiModel(
                        summary = "Converted intensity run to recovery session.",
                        affectedFromDate = "2026-06-15",
                        affectedToDate = "2026-06-21",
                        changedWorkoutIds = listOf("w1")
                    )
                ),
                onPrimaryAction = {},
                onOpenWorkout = { _, _ -> },
                onOpenPlan = {},
                onOpenProgress = {},
                onRetry = {},
                onRetryWorkout = {},
                onOpenWhatChanged = {},
                onDismissWhatChanged = {}
            )
        }

        composeRule.onNodeWithTag("today_adaptation_banner").assertIsDisplayed()
        composeRule.onNodeWithText("Converted intensity run to recovery session.").assertIsDisplayed()
    }

    @Test
    fun todayScreen_openProgressTriggersCallback() {
        var opened = false
        composeRule.setContent {
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
                onOpenProgress = { opened = true },
                onRetry = {},
                onRetryWorkout = {},
                onOpenWhatChanged = {},
                onDismissWhatChanged = {}
            )
        }

        composeRule.onNodeWithTag("today_open_progress").performClick()
        assertTrue(opened)
    }

    @Test
    fun todayScreen_rendersRecommendedToneAndWarnings() {
        composeRule.setContent {
            TodayScreen(
                state = TodayUiState(
                    isLoading = false,
                    readinessBanner = ReadinessBannerUiModel(
                        title = "Caution",
                        message = "Adapt effort today.",
                        ctaLabel = "Update readiness",
                        status = ReadinessBannerStatus.CAUTION
                    ),
                    recommendedTone = "supportive",
                    warnings = listOf("Readiness signals indicate reduced training tolerance today.")
                ),
                onPrimaryAction = {},
                onOpenWorkout = { _, _ -> },
                onOpenPlan = {},
                onOpenProgress = {},
                onRetry = {},
                onRetryWorkout = {},
                onOpenWhatChanged = {},
                onDismissWhatChanged = {}
            )
        }

        composeRule.onNodeWithTag("today_recommended_tone").assertIsDisplayed()
        composeRule.onNodeWithTag("today_warning_0").assertIsDisplayed()
    }
}
