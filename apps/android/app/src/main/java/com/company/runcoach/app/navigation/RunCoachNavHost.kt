package com.company.runcoach.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.company.runcoach.feature.auth.ui.navigation.AuthRoutes
import com.company.runcoach.feature.auth.ui.navigation.authGraph
import com.company.runcoach.feature.checkin.ui.navigation.CheckInRoutes
import com.company.runcoach.feature.checkin.ui.navigation.checkInGraph
import com.company.runcoach.feature.onboarding.ui.navigation.OnboardingRoutes
import com.company.runcoach.feature.onboarding.ui.navigation.onboardingGraph
import com.company.runcoach.feature.plan.ui.navigation.PlanRoutes
import com.company.runcoach.feature.plan.ui.navigation.planGraph
import com.company.runcoach.feature.profile.ui.navigation.profileGraph
import com.company.runcoach.feature.progress.ui.navigation.ProgressRoutes
import com.company.runcoach.feature.progress.ui.navigation.progressGraph
import com.company.runcoach.feature.racegoal.ui.navigation.RaceGoalRoutes
import com.company.runcoach.feature.racegoal.ui.navigation.raceGoalGraph
import com.company.runcoach.feature.strava.ui.navigation.StravaRoutes
import com.company.runcoach.feature.strava.ui.navigation.stravaGraph
import com.company.runcoach.feature.today.ui.navigation.TodayRoutes
import com.company.runcoach.feature.today.ui.navigation.todayGraph
import com.company.runcoach.feature.workout.ui.navigation.WorkoutRoutes
import com.company.runcoach.feature.workout.ui.navigation.workoutGraph

@Composable
fun RunCoachNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AuthRoutes.Splash) {
        authGraph(
            navController = navController,
            onboardingRoute = OnboardingRoutes.Intro,
            mainRoute = TodayRoutes.homeRoute()
        )

        onboardingGraph(
            onComplete = {
                navController.navigate(RaceGoalRoutes.Setup) {
                    popUpTo(OnboardingRoutes.Intro) { inclusive = true }
                }
            }
        )

        raceGoalGraph(
            onComplete = {
                navController.navigate(TodayRoutes.homeRoute()) {
                    popUpTo(RaceGoalRoutes.Setup) { inclusive = true }
                }
            }
        )

        todayGraph(
            onOpenCheckIn = { navController.navigate(CheckInRoutes.Fatigue) },
            onOpenWorkout = { plannedWorkoutId, status ->
                navController.navigate(WorkoutRoutes.detailRoute(plannedWorkoutId, status))
            },
            onOpenPlan = { navController.navigate(PlanRoutes.Overview) },
            onOpenProgress = { navController.navigate(ProgressRoutes.Overview) }
        )
        checkInGraph(
            openPainCheckIn = { navController.navigate(CheckInRoutes.Pain) },
            openSummary = { readiness -> navController.navigate(CheckInRoutes.summaryRoute(readiness)) },
            onCancel = { navController.popBackStack(TodayRoutes.homeRoute(), inclusive = false) },
            onDone = {
                navController.navigate(TodayRoutes.homeRoute(refresh = "refresh")) {
                    popUpTo(CheckInRoutes.Fatigue) { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() }
        )
        planGraph(onOpenWorkout = { plannedWorkoutId, status ->
            navController.navigate(WorkoutRoutes.detailRoute(plannedWorkoutId, status))
        })
        workoutGraph()
        progressGraph()

        profileGraph(onOpenStrava = { navController.navigate(StravaRoutes.ConnectionBase) })
        stravaGraph()
    }
}
