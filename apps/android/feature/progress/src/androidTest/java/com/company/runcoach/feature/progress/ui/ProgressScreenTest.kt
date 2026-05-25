package com.company.runcoach.feature.progress.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.company.runcoach.feature.progress.ui.model.ProgressContentUiModel
import com.company.runcoach.feature.progress.ui.model.ProgressUiState
import com.company.runcoach.feature.progress.ui.model.LongRunProgressionUiModel
import com.company.runcoach.feature.progress.ui.model.ReadinessStateUi
import com.company.runcoach.feature.progress.ui.model.ReadinessTrendUiModel
import com.company.runcoach.feature.progress.ui.model.RecentStatusDistributionUiModel
import com.company.runcoach.feature.progress.ui.model.WeeklyCompletionUiModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProgressScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingStateRenders() {
        composeRule.setContent {
            ProgressScreen(state = ProgressUiState(isLoading = true), onRetry = {})
        }

        composeRule.onNodeWithTag("progress_loading").assertIsDisplayed()
    }

    @Test
    fun emptyStateRenders() {
        composeRule.setContent {
            ProgressScreen(
                state = ProgressUiState(isLoading = false, emptyStateMessage = "No active plan yet."),
                onRetry = {}
            )
        }

        composeRule.onNodeWithTag("progress_empty").assertIsDisplayed()
        composeRule.onNodeWithText("No active plan yet.").assertIsDisplayed()
    }

    @Test
    fun dataRichStateRenders() {
        composeRule.setContent {
            ProgressScreen(
                state = ProgressUiState(
                    isLoading = false,
                    content = ProgressContentUiModel(
                        completionPercentage = 80,
                        completedWorkouts = 8,
                        currentTrainingWeek = 6,
                        readinessTrendLabel = "Trending ready",
                        weeklyCompletion = listOf(WeeklyCompletionUiModel("Week 6", "4/5 (80%)", 80)),
                        longRunProgression = emptyList(),
                        readinessTrend = listOf(ReadinessTrendUiModel("2026-06-15", ReadinessStateUi.READY, "READY")),
                        recentStatusDistribution = RecentStatusDistributionUiModel(2, 8, 1, 0),
                        insightMessage = "Great consistency"
                    )
                ),
                onRetry = {}
            )
        }

        composeRule.onNodeWithTag("progress_content").assertIsDisplayed()
        composeRule.onNodeWithText("80%").assertIsDisplayed()
        composeRule.onNodeWithText("Great consistency").assertIsDisplayed()
    }

    @Test
    fun errorStateRenders() {
        composeRule.setContent {
            ProgressScreen(
                state = ProgressUiState(isLoading = false, errorMessage = "Could not load progress summary."),
                onRetry = {}
            )
        }

        composeRule.onNodeWithTag("progress_error").assertIsDisplayed()
        composeRule.onNodeWithTag("progress_retry").assertIsDisplayed()
    }

    @Test
    fun longRunProgressionRendersUsingTypedDistanceModel() {
        composeRule.setContent {
            ProgressScreen(
                state = ProgressUiState(
                    isLoading = false,
                    content = ProgressContentUiModel(
                        completionPercentage = 60,
                        completedWorkouts = 6,
                        currentTrainingWeek = 4,
                        readinessTrendLabel = "Monitoring",
                        weeklyCompletion = listOf(WeeklyCompletionUiModel("Week 4", "3/5 (60%)", 60)),
                        longRunProgression = listOf(
                            LongRunProgressionUiModel(
                                label = "Week 4",
                                detail = "Planned 18.0 km · Actual 16.5 km",
                                status = "COMPLETED",
                                plannedDistanceKm = 18.0
                            )
                        ),
                        readinessTrend = listOf(ReadinessTrendUiModel("2026-06-15", ReadinessStateUi.READY, "READY")),
                        recentStatusDistribution = RecentStatusDistributionUiModel(2, 6, 1, 0),
                        insightMessage = "Consistency is improving."
                    )
                ),
                onRetry = {}
            )
        }

        composeRule.onNodeWithText("Week 4").assertIsDisplayed()
        composeRule.onNodeWithText("COMPLETED").assertIsDisplayed()
    }

    @Test
    fun errorRetryInvokesCallback() {
        var retried = false
        composeRule.setContent {
            ProgressScreen(
                state = ProgressUiState(isLoading = false, errorMessage = "Could not load progress summary."),
                onRetry = { retried = true }
            )
        }

        composeRule.onNodeWithTag("progress_retry").performClick()
        assertTrue(retried)
    }
}
