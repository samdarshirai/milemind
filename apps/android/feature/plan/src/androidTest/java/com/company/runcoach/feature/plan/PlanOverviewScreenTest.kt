package com.company.runcoach.feature.plan

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import com.company.runcoach.feature.plan.ui.PlanOverviewScreen
import com.company.runcoach.feature.plan.ui.model.CalendarViewMode
import com.company.runcoach.feature.plan.ui.model.PlanOverviewUiState
import com.company.runcoach.feature.plan.ui.model.WeekUiModel
import com.company.runcoach.feature.plan.ui.model.WorkoutCardUiModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PlanOverviewScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingEmptyErrorAndSuccess_render() {
        composeRule.setContent {
            PlanOverviewScreen(
                state = PlanOverviewUiState(isLoading = true),
                onRetry = {},
                onPreviousWeek = {},
                onNextWeek = {},
                onSelectWeekView = {},
                onSelectDayView = {},
                onSelectDay = {},
                onOpenWorkout = { _, _ -> }
            )
        }
        composeRule.onNodeWithTag("plan_loading").assertIsDisplayed()

        composeRule.setContent {
            PlanOverviewScreen(
                state = PlanOverviewUiState(isLoading = false, emptyMessage = "No plan"),
                onRetry = {},
                onPreviousWeek = {},
                onNextWeek = {},
                onSelectWeekView = {},
                onSelectDayView = {},
                onSelectDay = {},
                onOpenWorkout = { _, _ -> }
            )
        }
        composeRule.onNodeWithTag("plan_empty_cta").assertIsDisplayed()

        composeRule.setContent {
            PlanOverviewScreen(
                state = PlanOverviewUiState(isLoading = false, errorMessage = "Error"),
                onRetry = {},
                onPreviousWeek = {},
                onNextWeek = {},
                onSelectWeekView = {},
                onSelectDayView = {},
                onSelectDay = {},
                onOpenWorkout = { _, _ -> }
            )
        }
        composeRule.onNodeWithTag("plan_retry").assertIsDisplayed()

        composeRule.setContent {
            PlanOverviewScreen(
                state = successState(),
                onRetry = {},
                onPreviousWeek = {},
                onNextWeek = {},
                onSelectWeekView = {},
                onSelectDayView = {},
                onSelectDay = {},
                onOpenWorkout = { _, _ -> }
            )
        }
        composeRule.onNodeWithTag("week_nav").assertIsDisplayed()
        composeRule.onNodeWithTag("calendar_mode_toggle").assertIsDisplayed()
        composeRule.onNodeWithTag("phase_label").assertIsDisplayed()
        composeRule.onNodeWithTag("recovery_chip").assertIsDisplayed()
        composeRule.onNodeWithTag("status_w1").assertIsDisplayed()
        composeRule.onNodeWithTag("changed_w1").assertIsDisplayed()
        composeRule.onNodeWithTag("status_w2").assertIsDisplayed()
        composeRule.onNodeWithTag("status_w3").assertIsDisplayed()
        composeRule.onNodeWithTag("status_w4").assertIsDisplayed()
    }

    @Test
    fun tappingWorkout_invokesOpenWorkout() {
        var selectedId: String? = null
        var selectedStatus: String? = null
        composeRule.setContent {
            PlanOverviewScreen(
                state = successState(),
                onRetry = {},
                onPreviousWeek = {},
                onNextWeek = {},
                onSelectWeekView = {},
                onSelectDayView = {},
                onSelectDay = {},
                onOpenWorkout = { id, status ->
                    selectedId = id
                    selectedStatus = status
                }
            )
        }

        composeRule.onNodeWithTag("workout_card_w2").performClick()
        assertEquals("w2", selectedId)
        assertEquals("PLANNED", selectedStatus)
    }

    @Test
    fun dayMode_rendersOnlySelectedDayWorkout() {
        val state = successState()
        val selectedWeek = checkNotNull(state.selectedWeek)
        composeRule.setContent {
            PlanOverviewScreen(
                state = state.copy(
                    calendarViewMode = CalendarViewMode.DAY,
                    selectedWeek = selectedWeek.copy(workouts = listOf(selectedWeek.workouts[1]))
                ),
                onRetry = {},
                onPreviousWeek = {},
                onNextWeek = {},
                onSelectWeekView = {},
                onSelectDayView = {},
                onSelectDay = {},
                onOpenWorkout = { _, _ -> }
            )
        }

        composeRule.onNodeWithTag("day_selector").assertIsDisplayed()
        composeRule.onNodeWithTag("status_w2").assertIsDisplayed()
        composeRule.onAllNodesWithTag("status_w1").assertCountEquals(0)
    }
}

private fun successState(): PlanOverviewUiState {
    val week = WeekUiModel(
        weekIndex = 2,
        phase = "BUILD",
        recoveryWeek = true,
        targetDistanceKm = 35.0,
        workouts = listOf(
            WorkoutCardUiModel("w1", "Mon", "May 20", "Easy Run", "6.0 km", "easy", "COMPLETED", false, isChanged = true),
            WorkoutCardUiModel("w2", "Tue", "May 21", "Tempo Run", "45 min", "moderate", "PLANNED", true),
            WorkoutCardUiModel("w3", "Wed", "May 22", "Easy Run", "5.0 km", "easy", "MISSED", false),
            WorkoutCardUiModel("w4", "Thu", "May 23", "Rest", "Planned", "rest", "SKIPPED", false)
        )
    )
    return PlanOverviewUiState(
        isLoading = false,
        raceDistanceType = "HALF MARATHON",
        raceDate = "2026-10-04",
        currentWeekIndex = 2,
        selectedWeekIndex = 2,
        planProgressText = "Week 2 of 12",
        selectedWeek = week,
        selectedWeekPhaseLabel = "BUILD",
        isRecoveryWeek = true,
        availableDayLabels = listOf("Mon", "Tue", "Wed", "Thu"),
        selectedDayLabel = "Tue"
    )
}
