package com.company.runcoach.feature.today.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.company.runcoach.feature.today.ui.TodayRoute

object TodayRoutes {
    const val Home = "today_home"
}

fun NavGraphBuilder.todayGraph() {
    composable(TodayRoutes.Home) {
        TodayRoute()
    }
}
