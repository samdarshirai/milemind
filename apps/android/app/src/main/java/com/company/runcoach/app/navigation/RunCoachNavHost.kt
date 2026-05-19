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

private object AppRoutes {
    const val OnboardingHome = "onboarding_home"
    const val SessionHome = "session_home"
}

@Composable
fun RunCoachNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AuthRoutes.Splash) {
        authGraph(
            navController = navController,
            onboardingRoute = AppRoutes.OnboardingHome,
            mainRoute = AppRoutes.SessionHome
        )
        composable(AppRoutes.OnboardingHome) {
            OnboardingHomePlaceholder()
        }
        composable(AppRoutes.SessionHome) {
            SessionHomePlaceholder()
        }
    }
}

@Composable
private fun OnboardingHomePlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Onboarding is required")
    }
}

@Composable
private fun SessionHomePlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Session is active")
    }
}
