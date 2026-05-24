package com.company.runcoach.feature.workout

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.company.runcoach.feature.workout.ui.WorkoutDetailScreen
import com.company.runcoach.feature.workout.ui.model.WorkoutDetailUiState
import org.junit.Rule
import org.junit.Test

class WorkoutDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingErrorAndSuccess_render() {
        composeRule.setContent {
            WorkoutDetailScreen(state = WorkoutDetailUiState(isLoading = true), onRetry = {})
        }
        composeRule.onNodeWithTag("workout_detail_loading").assertIsDisplayed()

        composeRule.setContent {
            WorkoutDetailScreen(state = WorkoutDetailUiState(isLoading = false, errorMessage = "Error"), onRetry = {})
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
                onRetry = {}
            )
        }
        composeRule.onNodeWithTag("detail_status").assertIsDisplayed()
        composeRule.onNodeWithTag("detail_instructions").assertIsDisplayed()
    }
}
