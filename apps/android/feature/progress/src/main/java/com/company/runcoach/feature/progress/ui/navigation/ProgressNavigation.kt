package com.company.runcoach.feature.progress.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.company.runcoach.feature.progress.ui.ProgressRoute

object ProgressRoutes {
    const val Overview = "progress_overview"
}

fun NavGraphBuilder.progressGraph() {
    composable(ProgressRoutes.Overview) {
        ProgressRoute()
    }
}
