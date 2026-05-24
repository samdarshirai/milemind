package com.company.runcoach.feature.today.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.company.runcoach.feature.today.ui.TodayRoute

object TodayRoutes {
    const val HomeBase = "today_home"
    const val Home = "today_home?refresh={refresh}"

    fun homeRoute(refresh: String? = null): String {
        return if (refresh == null) HomeBase else "$HomeBase?refresh=$refresh"
    }
}

fun NavGraphBuilder.todayGraph(
    onOpenCheckIn: () -> Unit,
    onOpenWorkout: (String, String) -> Unit,
    onOpenPlan: () -> Unit
) {
    composable(
        route = TodayRoutes.Home,
        arguments = listOf(
            navArgument("refresh") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        TodayRoute(
            onOpenCheckIn = onOpenCheckIn,
            onOpenWorkout = onOpenWorkout,
            onOpenPlan = onOpenPlan,
            refreshToken = backStackEntry.arguments?.getString("refresh")
        )
    }
}
