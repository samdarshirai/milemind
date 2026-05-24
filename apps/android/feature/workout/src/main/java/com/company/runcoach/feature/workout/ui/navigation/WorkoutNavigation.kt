package com.company.runcoach.feature.workout.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.company.runcoach.feature.workout.ui.WorkoutDetailRoute
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object WorkoutRoutes {
    const val Detail = "workout_detail/{plannedWorkoutId}?status={status}"

    fun detailRoute(plannedWorkoutId: String, status: String? = null): String {
        val encodedId = encodeRouteArg(plannedWorkoutId)
        val encodedStatus = status?.takeIf { it.isNotBlank() }?.let(::encodeRouteArg)
        return if (encodedStatus == null) {
            "workout_detail/$encodedId"
        } else {
            "workout_detail/$encodedId?status=$encodedStatus"
        }
    }

    private fun encodeRouteArg(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
    }
}

fun NavGraphBuilder.workoutGraph() {
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
    ) {
        WorkoutDetailRoute()
    }
}
