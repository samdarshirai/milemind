package com.company.runcoach.feature.plan.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.company.runcoach.feature.plan.ui.PlanOverviewRoute

object PlanRoutes {
    const val Overview = "plan_overview"
}

fun NavGraphBuilder.planGraph(onOpenWorkout: (String, String) -> Unit) {
    composable(PlanRoutes.Overview) {
        PlanOverviewRoute(onOpenWorkout = onOpenWorkout)
    }
}
