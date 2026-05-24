package com.company.runcoach.feature.today.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.runcoach.core.designsystem.RunCoachColors
import com.company.runcoach.feature.today.TodayViewModel
import com.company.runcoach.feature.today.ui.model.ReadinessBannerStatus
import com.company.runcoach.feature.today.ui.model.TodayWorkoutUiModel
import com.company.runcoach.feature.today.ui.model.TodayUiState

@Composable
fun TodayRoute(
    onOpenCheckIn: () -> Unit,
    onOpenWorkout: (String, String) -> Unit,
    onOpenPlan: () -> Unit,
    refreshToken: String?,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    LaunchedEffect(refreshToken) {
        if (refreshToken == "refresh") {
            viewModel.refreshAfterCheckIn()
        }
    }
    TodayScreen(
        state = state,
        onPrimaryAction = {
            if (state.readinessBanner.status == ReadinessBannerStatus.ERROR) {
                viewModel.load()
            } else {
                onOpenCheckIn()
            }
        },
        onOpenWorkout = onOpenWorkout,
        onOpenPlan = onOpenPlan,
        onRetry = viewModel::load,
        onRetryWorkout = viewModel::retryWorkoutLoad
    )
}

@Composable
fun TodayScreen(
    state: TodayUiState,
    onPrimaryAction: () -> Unit,
    onOpenWorkout: (String, String) -> Unit,
    onOpenPlan: () -> Unit,
    onRetry: () -> Unit,
    onRetryWorkout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("today_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Today", style = MaterialTheme.typography.headlineMedium, color = RunCoachColors.TextPrimary)

        Card(
            colors = CardDefaults.cardColors(
                containerColor = when (state.readinessBanner.status) {
                    ReadinessBannerStatus.READY -> Color(0xFF133427)
                    ReadinessBannerStatus.CAUTION -> Color(0xFF3A2D14)
                    ReadinessBannerStatus.HIGH_RISK -> Color(0xFF3B1E1E)
                    else -> RunCoachColors.SurfacePrimary
                }
            ),
            modifier = Modifier.fillMaxWidth().testTag("today_readiness_banner")
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(state.readinessBanner.title, color = RunCoachColors.TextStrong, style = MaterialTheme.typography.titleLarge)
                Text(state.readinessBanner.message, color = RunCoachColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)

                if (state.readinessBanner.status == ReadinessBannerStatus.LOADING || state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.testTag("today_readiness_loading"))
                }

                if (state.readinessBanner.ctaLabel.isNotBlank()) {
                    Button(onClick = onPrimaryAction, modifier = Modifier.testTag("today_readiness_cta")) {
                        Text(state.readinessBanner.ctaLabel)
                    }
                }
            }
        }

        if (state.errorMessage != null) {
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("today_error_message"))
            Button(onClick = onRetry, modifier = Modifier.testTag("today_retry")) {
                Text("Retry")
            }
        }

        if (state.workoutLoadFailed && state.workoutErrorMessage != null) {
            Text(
                state.workoutErrorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("today_workout_error_message")
            )
            OutlinedButton(onClick = onRetryWorkout, modifier = Modifier.testTag("today_workout_retry")) {
                Text("Retry Workout")
            }
        }

        state.todayWorkout?.let { workout ->
            WorkoutCard(
                workout = workout,
                onOpenWorkout = { onOpenWorkout(workout.plannedWorkoutId, workout.status) },
                onOpenPlan = onOpenPlan
            )
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize().testTag("today_workout_placeholder"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "No workout is scheduled for today.",
                        color = RunCoachColors.TextMuted
                    )
                    OutlinedButton(onClick = onOpenPlan, modifier = Modifier.testTag("today_open_plan")) {
                        Text("Open Plan")
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    workout: TodayWorkoutUiModel,
    onOpenWorkout: () -> Unit,
    onOpenPlan: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = RunCoachColors.SurfaceAccent),
        modifier = Modifier.fillMaxWidth().testTag("today_workout_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Today's workout", style = MaterialTheme.typography.titleMedium, color = RunCoachColors.TextStrong)
            Text(workout.title, style = MaterialTheme.typography.headlineSmall, color = RunCoachColors.TextPrimary)
            Text(workout.detail, color = RunCoachColors.TextSecondary)
            Text("Intensity: ${workout.intensity}", color = RunCoachColors.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenWorkout, modifier = Modifier.testTag("today_open_workout")) {
                    Text("Open Workout")
                }
                OutlinedButton(onClick = onOpenPlan, modifier = Modifier.testTag("today_open_plan")) {
                    Text("Open Plan")
                }
            }
        }
    }
}
