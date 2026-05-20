package com.company.runcoach.feature.onboarding.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.company.runcoach.feature.onboarding.ui.OnboardingRoute

object OnboardingRoutes {
    const val Intro = "onboarding_intro"
    const val RaceGoalPlaceholder = "onboarding_race_goal_placeholder"
}

fun NavGraphBuilder.onboardingGraph(onComplete: () -> Unit) {
    composable(OnboardingRoutes.Intro) {
        OnboardingRoute(onComplete = onComplete)
    }
}
