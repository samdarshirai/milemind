package com.company.runcoach.feature.plan

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.material3.Text
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.company.runcoach.feature.plan.ui.PlanOverviewScreen
import com.company.runcoach.feature.plan.ui.model.PlanOverviewUiState
import com.company.runcoach.feature.plan.ui.model.WeekUiModel
import com.company.runcoach.feature.plan.ui.model.WorkoutCardUiModel
import com.company.runcoach.feature.workout.ui.navigation.WorkoutRoutes
import org.junit.Rule
import org.junit.Test

class PlanToWorkoutNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tapWorkoutCard_navigatesToWorkoutDetail_withExpectedArgs() {
        composeRule.setContent {
            PlanToWorkoutTestNavHost()
        }

        composeRule.onNodeWithTag("workout_card_w2").performClick()
        composeRule.onNodeWithTag("detail_plannedWorkoutId").assertIsDisplayed()
        composeRule.onNodeWithTag("detail_status").assertIsDisplayed()
    }
}

@Composable
private fun PlanToWorkoutTestNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "plan_test") {
        composable("plan_test") {
            PlanOverviewScreen(
                state = navSuccessState(),
                onRetry = {},
                onPreviousWeek = {},
                onNextWeek = {},
                onSelectWeekView = {},
                onSelectDayView = {},
                onSelectDay = {},
                onOpenWorkout = { id, status ->
                    navController.navigate(WorkoutRoutes.detailRoute(id, status))
                }
            )
        }
        composable(
            route = WorkoutRoutes.Detail,
            arguments = listOf(
                navArgument("plannedWorkoutId") { type = NavType.StringType },
                navArgument("status") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            Text(
                text = backStackEntry.arguments?.getString("plannedWorkoutId").orEmpty(),
                modifier = androidx.compose.ui.Modifier.testTag("detail_plannedWorkoutId")
            )
            Text(
                text = backStackEntry.arguments?.getString("status").orEmpty(),
                modifier = androidx.compose.ui.Modifier.testTag("detail_status")
            )
        }
    }
}

private fun navSuccessState(): PlanOverviewUiState {
    val week = WeekUiModel(
        weekIndex = 2,
        phase = "BUILD",
        recoveryWeek = false,
        targetDistanceKm = 35.0,
        workouts = listOf(
            WorkoutCardUiModel("w1", "Mon", "May 20", "Easy Run", "6.0 km", "easy", "COMPLETED", false),
            WorkoutCardUiModel("w2", "Tue", "May 21", "Tempo Run", "45 min", "moderate", "PLANNED", true)
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
        selectedWeekPhaseLabel = "BUILD"
    )
}
