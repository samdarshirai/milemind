package com.company.runcoach.feature.racegoal.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.company.runcoach.feature.racegoal.ui.RaceGoalRoute

object RaceGoalRoutes {
    const val Setup = "race_goal_setup"
}

fun NavGraphBuilder.raceGoalGraph(onComplete: () -> Unit) {
    composable(RaceGoalRoutes.Setup) {
        RaceGoalRoute(onComplete = onComplete)
    }
}
