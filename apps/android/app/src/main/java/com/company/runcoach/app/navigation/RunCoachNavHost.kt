package com.company.runcoach.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object RunCoachRoutes {
    const val Splash = "splash"
}

@Composable
fun RunCoachNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = RunCoachRoutes.Splash) {
        composable(RunCoachRoutes.Splash) {
            SplashRoute()
        }
    }
}

@Composable
private fun SplashRoute() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(
                text = "Initializing RunCoach",
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
