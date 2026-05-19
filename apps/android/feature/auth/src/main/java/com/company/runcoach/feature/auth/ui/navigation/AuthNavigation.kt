package com.company.runcoach.feature.auth.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.company.runcoach.feature.auth.ui.SignInScreen
import com.company.runcoach.feature.auth.ui.SignInViewModel
import com.company.runcoach.feature.auth.ui.SignUpScreen
import com.company.runcoach.feature.auth.ui.SignUpViewModel
import com.company.runcoach.feature.auth.ui.SplashScreen
import com.company.runcoach.feature.auth.ui.SplashViewModel
import com.company.runcoach.feature.auth.ui.model.SplashDestination

object AuthRoutes {
    const val Splash = "splash"
    const val SignIn = "sign_in"
    const val SignUp = "sign_up"
}

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onboardingRoute: String,
    mainRoute: String
) {
    composable(AuthRoutes.Splash) {
        val viewModel: SplashViewModel = hiltViewModel()
        val state = viewModel.uiState.collectAsStateWithLifecycle().value

        LaunchedEffect(state.destination) {
            when (state.destination) {
                SplashDestination.SIGN_IN -> navController.navigate(AuthRoutes.SignIn) {
                    popUpTo(AuthRoutes.Splash) { inclusive = true }
                }
                SplashDestination.ONBOARDING -> navController.navigate(onboardingRoute) {
                    popUpTo(AuthRoutes.Splash) { inclusive = true }
                }
                SplashDestination.MAIN -> navController.navigate(mainRoute) {
                    popUpTo(AuthRoutes.Splash) { inclusive = true }
                }
                null -> Unit
            }
        }

        SplashScreen(
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            onRetry = viewModel::restoreSession
        )
    }

    composable(AuthRoutes.SignIn) {
        val viewModel: SignInViewModel = hiltViewModel()
        val state = viewModel.uiState.collectAsStateWithLifecycle().value

        LaunchedEffect(state.isSuccess, state.onboardingRequired) {
            if (state.isSuccess) {
                navController.navigate(
                    if (state.onboardingRequired) onboardingRoute else mainRoute
                ) {
                    popUpTo(AuthRoutes.SignIn) { inclusive = true }
                }
            }
        }

        SignInScreen(
            state = state,
            onEmailChanged = viewModel::onEmailChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onSubmit = viewModel::submit,
            onNavigateSignUp = { navController.navigate(AuthRoutes.SignUp) }
        )
    }

    composable(AuthRoutes.SignUp) {
        val viewModel: SignUpViewModel = hiltViewModel()
        val state = viewModel.uiState.collectAsStateWithLifecycle().value

        LaunchedEffect(state.isSuccess, state.onboardingRequired) {
            if (state.isSuccess) {
                navController.navigate(
                    if (state.onboardingRequired) onboardingRoute else mainRoute
                ) {
                    popUpTo(AuthRoutes.SignUp) { inclusive = true }
                }
            }
        }

        SignUpScreen(
            state = state,
            onEmailChanged = viewModel::onEmailChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
            onSubmit = viewModel::submit,
            onNavigateSignIn = { navController.popBackStack() }
        )
    }
}
