package com.company.runcoach.feature.workout

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.company.runcoach.feature.workout.ui.WorkoutDetailScreen
import com.company.runcoach.feature.workout.ui.model.SkipReason
import com.company.runcoach.feature.workout.ui.model.WorkoutDetailUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WorkoutDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingErrorAndSuccess_render() {
        composeRule.setContent {
            WorkoutDetailScreen(
                state = WorkoutDetailUiState(isLoading = true),
                onRetry = {},
                onOpenSkip = {},
                onDismissSkip = {},
                onSelectSkipReason = {},
                onSubmitSkip = {},
                onOpenReschedule = {},
                onDismissReschedule = {},
                onChangeRescheduleDate = {},
                onSubmitReschedule = {},
                onConflictRefresh = {},
                onConflictRetry = {}
            )
        }
        composeRule.onNodeWithTag("workout_detail_loading").assertIsDisplayed()

        composeRule.setContent {
            WorkoutDetailScreen(
                state = WorkoutDetailUiState(isLoading = false, errorMessage = "Error"),
                onRetry = {},
                onOpenSkip = {},
                onDismissSkip = {},
                onSelectSkipReason = {},
                onSubmitSkip = {},
                onOpenReschedule = {},
                onDismissReschedule = {},
                onChangeRescheduleDate = {},
                onSubmitReschedule = {},
                onConflictRefresh = {},
                onConflictRetry = {}
            )
        }
        composeRule.onNodeWithTag("workout_detail_retry").assertIsDisplayed()

        composeRule.setContent {
            WorkoutDetailScreen(
                state = WorkoutDetailUiState(
                    isLoading = false,
                    title = "Tempo Run",
                    dateLabel = "Wednesday, May 20",
                    workoutType = "TEMPO RUN",
                    plannedLabel = "8.0 km planned",
                    intensityLabel = "MODERATE",
                    instructions = "Build threshold endurance.",
                    structureLines = listOf("step: Warm-up"),
                    statusLabel = "PLANNED"
                ),
                onRetry = {},
                onOpenSkip = {},
                onDismissSkip = {},
                onSelectSkipReason = {},
                onSubmitSkip = {},
                onOpenReschedule = {},
                onDismissReschedule = {},
                onChangeRescheduleDate = {},
                onSubmitReschedule = {},
                onConflictRefresh = {},
                onConflictRetry = {}
            )
        }
        composeRule.onNodeWithTag("detail_status").assertIsDisplayed()
        composeRule.onNodeWithTag("detail_instructions").assertIsDisplayed()
    }

    @Test
    fun skipAndRescheduleFlows_openAndCallbacks() {
        var skipOpened = false
        var rescheduleOpened = false
        var selectedReason: SkipReason? = null
        var skipSubmitted = false
        var rescheduleSubmitted = false

        composeRule.setContent {
            WorkoutDetailScreen(
                state = WorkoutDetailUiState(
                    isLoading = false,
                    canMarkSkipped = true,
                    canReschedule = true,
                    showSkipSheet = true
                ),
                onRetry = {},
                onOpenSkip = { skipOpened = true },
                onDismissSkip = {},
                onSelectSkipReason = { selectedReason = it },
                onSubmitSkip = { skipSubmitted = true },
                onOpenReschedule = { rescheduleOpened = true },
                onDismissReschedule = {},
                onChangeRescheduleDate = {},
                onSubmitReschedule = { rescheduleSubmitted = true },
                onConflictRefresh = {},
                onConflictRetry = {}
            )
        }

        composeRule.onNodeWithTag("mark_skipped").performClick()
        composeRule.onNodeWithTag("reschedule_workout").performClick()
        composeRule.onNodeWithTag("skip_reason_TOO_TIRED").performClick()
        composeRule.onNodeWithTag("skip_confirm").performClick()

        assertEquals(true, skipOpened)
        assertEquals(true, rescheduleOpened)
        assertEquals(SkipReason.TOO_TIRED, selectedReason)
        assertEquals(true, skipSubmitted)
        assertEquals(false, rescheduleSubmitted)
    }

    @Test
    fun stalePlanConflict_rendersRefreshMessage() {
        composeRule.setContent {
            WorkoutDetailScreen(
                state = WorkoutDetailUiState(
                    isLoading = false,
                    conflictMessage = "Your plan changed recently. Refresh to continue."
                ),
                onRetry = {},
                onOpenSkip = {},
                onDismissSkip = {},
                onSelectSkipReason = {},
                onSubmitSkip = {},
                onOpenReschedule = {},
                onDismissReschedule = {},
                onChangeRescheduleDate = {},
                onSubmitReschedule = {},
                onConflictRefresh = {},
                onConflictRetry = {}
            )
        }
        composeRule.onNodeWithTag("stale_plan_conflict").assertIsDisplayed()
        composeRule.onNodeWithTag("conflict_refresh").assertIsDisplayed()
    }

    @Test
    fun rescheduleSuccess_rendersAdaptationSummaryCard() {
        composeRule.setContent {
            WorkoutDetailScreen(
                state = WorkoutDetailUiState(
                    isLoading = false,
                    latestAdaptation = com.company.runcoach.feature.workout.ui.model.AdaptationSummaryUiModel(
                        id = "adapt-1",
                        summary = "Your near-term workouts were adjusted after rescheduling.",
                        affectedFromDate = "2026-06-01",
                        affectedToDate = "2026-06-14",
                        changedWorkoutIds = listOf("w1", "w2")
                    )
                ),
                onRetry = {},
                onOpenSkip = {},
                onDismissSkip = {},
                onSelectSkipReason = {},
                onSubmitSkip = {},
                onOpenReschedule = {},
                onDismissReschedule = {},
                onChangeRescheduleDate = {},
                onSubmitReschedule = {},
                onConflictRefresh = {},
                onConflictRetry = {}
            )
        }
        composeRule.onNodeWithTag("adaptation_summary_card").assertIsDisplayed()
    }

    @Test
    fun skipSheet_loadingIndicatorRenders() {
        composeRule.setContent {
            WorkoutDetailScreen(
                state = WorkoutDetailUiState(
                    isLoading = false,
                    showSkipSheet = true,
                    mutationInFlight = true
                ),
                onRetry = {},
                onOpenSkip = {},
                onDismissSkip = {},
                onSelectSkipReason = {},
                onSubmitSkip = {},
                onOpenReschedule = {},
                onDismissReschedule = {},
                onChangeRescheduleDate = {},
                onSubmitReschedule = {},
                onConflictRefresh = {},
                onConflictRetry = {}
            )
        }
        composeRule.onNodeWithTag("skip_loading").assertIsDisplayed()
    }

    @Test
    fun rescheduleSheet_loadingIndicatorRenders() {
        composeRule.setContent {
            WorkoutDetailScreen(
                state = WorkoutDetailUiState(
                    isLoading = false,
                    showRescheduleSheet = true,
                    mutationInFlight = true,
                    rescheduleDate = "2026-06-14"
                ),
                onRetry = {},
                onOpenSkip = {},
                onDismissSkip = {},
                onSelectSkipReason = {},
                onSubmitSkip = {},
                onOpenReschedule = {},
                onDismissReschedule = {},
                onChangeRescheduleDate = {},
                onSubmitReschedule = {},
                onConflictRefresh = {},
                onConflictRetry = {}
            )
        }
        composeRule.onNodeWithTag("reschedule_loading").assertIsDisplayed()
    }

    @Test
    fun mutationError_rendersErrorMessage() {
        composeRule.setContent {
            WorkoutDetailScreen(
                state = WorkoutDetailUiState(
                    isLoading = false,
                    mutationError = "Could not reschedule this workout. Try again."
                ),
                onRetry = {},
                onOpenSkip = {},
                onDismissSkip = {},
                onSelectSkipReason = {},
                onSubmitSkip = {},
                onOpenReschedule = {},
                onDismissReschedule = {},
                onChangeRescheduleDate = {},
                onSubmitReschedule = {},
                onConflictRefresh = {},
                onConflictRetry = {}
            )
        }
        composeRule.onNodeWithTag("mutation_error").assertIsDisplayed()
    }
}
