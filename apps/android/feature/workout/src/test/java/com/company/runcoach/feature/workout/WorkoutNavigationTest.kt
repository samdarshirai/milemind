package com.company.runcoach.feature.workout

import com.company.runcoach.feature.workout.ui.navigation.WorkoutRoutes
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutNavigationTest {

    @Test
    fun detailRoute_withoutStatus_buildsRouteWithWorkoutIdOnly() {
        val route = WorkoutRoutes.detailRoute(plannedWorkoutId = "workout-1")

        assertEquals("workout_detail/workout-1", route)
    }

    @Test
    fun detailRoute_withStatus_buildsRouteWithEncodedStatus() {
        val route = WorkoutRoutes.detailRoute(plannedWorkoutId = "workout-1", status = "REST_DAY")

        assertEquals("workout_detail/workout-1?status=REST_DAY", route)
    }

    @Test
    fun detailRoute_roundTripPreservesEncodedInputs() {
        val route = WorkoutRoutes.detailRoute(plannedWorkoutId = "workout/1", status = "MISSED RUN")

        assertEquals("workout_detail/workout%2F1?status=MISSED%20RUN", route)
    }
}
