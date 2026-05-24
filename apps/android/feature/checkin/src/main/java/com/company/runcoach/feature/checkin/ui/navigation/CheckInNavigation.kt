package com.company.runcoach.feature.checkin.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.company.runcoach.feature.checkin.ui.FatigueCheckInRoute
import com.company.runcoach.feature.checkin.ui.PainCheckInRoute
import com.company.runcoach.feature.checkin.ui.ReadinessSummaryRoute

object CheckInRoutes {
    const val Fatigue = "checkin/fatigue"
    const val Pain = "checkin/pain"
    const val Summary = "checkin/summary?readiness={readiness}"

    fun summaryRoute(readiness: String?): String {
        return if (readiness.isNullOrBlank()) "checkin/summary" else "checkin/summary?readiness=$readiness"
    }
}

fun NavGraphBuilder.checkInGraph(
    openPainCheckIn: () -> Unit,
    openSummary: (String?) -> Unit,
    onCancel: () -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    composable(CheckInRoutes.Fatigue) {
        FatigueCheckInRoute(onNext = openPainCheckIn, onCancel = onCancel)
    }
    composable(CheckInRoutes.Pain) {
        PainCheckInRoute(onFinish = openSummary, onBack = onBack)
    }
    composable(
        route = CheckInRoutes.Summary,
        arguments = listOf(
            navArgument("readiness") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val readiness = backStackEntry.arguments?.getString("readiness")
        ReadinessSummaryRoute(readinessState = readiness, onDone = onDone)
    }
}
