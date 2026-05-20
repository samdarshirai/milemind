package com.company.runcoach.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.company.runcoach.feature.auth.ui.navigation.AuthRoutes
import com.company.runcoach.feature.auth.ui.navigation.authGraph
import com.company.runcoach.feature.onboarding.ui.navigation.OnboardingRoutes
import com.company.runcoach.feature.onboarding.ui.navigation.onboardingGraph
import com.company.runcoach.feature.profile.ui.navigation.ProfileRoutes
import com.company.runcoach.feature.profile.ui.navigation.profileGraph

private object AppRoutes {
    const val SessionHome = "session_home"
}

@Composable
fun RunCoachNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AuthRoutes.Splash) {
        authGraph(
            navController = navController,
            onboardingRoute = OnboardingRoutes.Intro,
            mainRoute = AppRoutes.SessionHome
        )

        onboardingGraph(
            onComplete = {
                navController.navigate(OnboardingRoutes.RaceGoalPlaceholder) {
                    popUpTo(OnboardingRoutes.Intro) { inclusive = true }
                }
            }
        )

        composable(OnboardingRoutes.RaceGoalPlaceholder) {
            RaceGoalPlaceholderScreen(onContinue = {
                navController.navigate(AppRoutes.SessionHome) {
                    popUpTo(OnboardingRoutes.RaceGoalPlaceholder) { inclusive = true }
                }
            })
        }

        composable(AppRoutes.SessionHome) {
            SessionHomePlaceholder(onEditProfile = { navController.navigate(ProfileRoutes.Edit) })
        }

        profileGraph()
    }
}

@Composable
private fun RaceGoalPlaceholderScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Race goal setup comes in Slice 3")
        androidx.compose.material3.Button(onClick = onContinue) { Text("Continue") }
    }
}

@Composable
private fun SessionHomePlaceholder(onEditProfile: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Session is active")
        androidx.compose.material3.Button(onClick = onEditProfile) { Text("Edit profile") }
    }
}
