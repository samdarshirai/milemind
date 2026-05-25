package com.company.runcoach.feature.today.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.runcoach.core.designsystem.RunCoachColors
import com.company.runcoach.feature.today.TodayViewModel
import com.company.runcoach.feature.today.ui.model.ReadinessBannerStatus
import com.company.runcoach.feature.today.ui.model.TodayUiState
import com.company.runcoach.feature.today.ui.model.TodayWorkoutUiModel

@Composable
fun TodayRoute(
    onOpenCheckIn: () -> Unit,
    onOpenWorkout: (String, String) -> Unit,
    onOpenPlan: () -> Unit,
    onOpenProgress: () -> Unit,
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
        onOpenProgress = onOpenProgress,
        onRetry = viewModel::load,
        onRetryWorkout = viewModel::retryWorkoutLoad,
        onOpenWhatChanged = viewModel::openWhatChanged,
        onDismissWhatChanged = viewModel::dismissWhatChanged
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    state: TodayUiState,
    onPrimaryAction: () -> Unit,
    onOpenWorkout: (String, String) -> Unit,
    onOpenPlan: () -> Unit,
    onOpenProgress: () -> Unit,
    onRetry: () -> Unit,
    onRetryWorkout: () -> Unit,
    onOpenWhatChanged: () -> Unit,
    onDismissWhatChanged: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("today_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Today", style = MaterialTheme.typography.headlineMedium, color = RunCoachColors.TextPrimary)

        OutlinedButton(onClick = onOpenProgress, modifier = Modifier.testTag("today_open_progress")) {
            Text("View Progress")
        }

        val heroGradient = when (state.readinessBanner.status) {
            ReadinessBannerStatus.READY -> listOf(Color(0xFF103526), Color(0xFF1A4835))
            ReadinessBannerStatus.CAUTION -> listOf(Color(0xFF342B12), Color(0xFF4A3A17))
            ReadinessBannerStatus.HIGH_RISK -> listOf(Color(0xFF3A1C1C), Color(0xFF4C2424))
            ReadinessBannerStatus.ERROR -> listOf(Color(0xFF341717), Color(0xFF462020))
            else -> listOf(RunCoachColors.SurfacePrimary, RunCoachColors.SurfaceHighlight)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(heroGradient), RoundedCornerShape(14.dp))
                .testTag("today_readiness_banner")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    when (state.readinessBanner.status) {
                        ReadinessBannerStatus.HIGH_RISK -> "READINESS WARNING"
                        ReadinessBannerStatus.CAUTION -> "READINESS CAUTION"
                        ReadinessBannerStatus.READY -> "READY TO TRAIN"
                        ReadinessBannerStatus.NO_CHECK_IN -> "CHECK-IN REQUIRED"
                        ReadinessBannerStatus.ERROR -> "READINESS UNAVAILABLE"
                        ReadinessBannerStatus.LOADING -> "READINESS"
                    },
                    color = RunCoachColors.TextMuted,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(state.readinessBanner.title, color = RunCoachColors.TextStrong, style = MaterialTheme.typography.headlineSmall)
                Text(state.readinessBanner.message, color = RunCoachColors.TextSecondary)

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

        state.latestAdaptation?.let { adaptation ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth().testTag("today_adaptation_banner")
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0x33FC4C02), Color(0x26EF2CC1), Color(0x1FBDBBFF))
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("ACTIVE ADJUSTMENT", style = MaterialTheme.typography.labelMedium, color = RunCoachColors.TextMuted)
                    Text(adaptation.summary, color = RunCoachColors.TextStrong, style = MaterialTheme.typography.headlineSmall)
                    Text("Today window: ${adaptation.affectedFromDate} to ${adaptation.affectedToDate}", color = RunCoachColors.TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onOpenPlan,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("today_view_updated_plan")
                        ) {
                            Text("View Updated Plan")
                        }
                        OutlinedButton(
                            onClick = onOpenWhatChanged,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("see_what_changed")
                        ) {
                            Text("What Changed")
                        }
                    }
                }
            }
        }

        state.insightMessage?.let {
            Card(colors = CardDefaults.cardColors(containerColor = RunCoachColors.SurfacePrimary), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Insight", style = MaterialTheme.typography.titleSmall, color = RunCoachColors.TextStrong)
                    Text(it, color = RunCoachColors.TextSecondary)
                }
            }
        }

        if (state.recommendedTone != null || state.warnings.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = RunCoachColors.SurfacePrimary),
                modifier = Modifier.fillMaxWidth().testTag("today_guidance")
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.recommendedTone?.let {
                        Text("Recommended tone: $it", color = RunCoachColors.TextSecondary, modifier = Modifier.testTag("today_recommended_tone"))
                    }
                    state.warnings.forEachIndexed { index, warning ->
                        Text("• $warning", color = RunCoachColors.TextSecondary, modifier = Modifier.testTag("today_warning_$index"))
                    }
                }
            }
        }

        if (state.fatigueSummary != null || state.painSummary != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = RunCoachColors.SurfaceAccent),
                modifier = Modifier.fillMaxWidth().testTag("today_readiness_metrics")
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.fatigueSummary?.let {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fatigue", style = MaterialTheme.typography.labelMedium, color = RunCoachColors.TextMuted)
                            Text(it, color = RunCoachColors.TextSecondary, modifier = Modifier.testTag("today_fatigue_summary"))
                        }
                    }
                    state.painSummary?.let {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pain", style = MaterialTheme.typography.labelMedium, color = RunCoachColors.TextMuted)
                            Text(it, color = RunCoachColors.TextSecondary, modifier = Modifier.testTag("today_pain_summary"))
                        }
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
            Text(state.workoutErrorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("today_workout_error_message"))
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
            Box(modifier = Modifier.fillMaxSize().testTag("today_workout_placeholder"), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No workout is scheduled for today.", color = RunCoachColors.TextMuted)
                    OutlinedButton(onClick = onOpenPlan, modifier = Modifier.testTag("today_open_plan")) {
                        Text("Open Plan")
                    }
                }
            }
        }
    }

    if (state.showWhatChanged && state.latestAdaptation != null) {
        ModalBottomSheet(onDismissRequest = onDismissWhatChanged) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("What changed?", style = MaterialTheme.typography.titleLarge)
                Text(state.latestAdaptation.summary)
                Text("Affected range: ${state.latestAdaptation.affectedFromDate} to ${state.latestAdaptation.affectedToDate}")
                Text("Changed workouts: ${state.latestAdaptation.changedWorkoutIds.size}")
                Button(onClick = onDismissWhatChanged, modifier = Modifier.testTag("what_changed_close")) { Text("Close") }
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
