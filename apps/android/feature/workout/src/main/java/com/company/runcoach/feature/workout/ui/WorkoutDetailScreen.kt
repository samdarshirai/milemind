package com.company.runcoach.feature.workout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.runcoach.core.designsystem.RunCoachColors
import com.company.runcoach.feature.workout.ui.model.WorkoutDetailUiState

@Composable
fun WorkoutDetailRoute(viewModel: WorkoutDetailViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    WorkoutDetailScreen(state = state, onRetry = viewModel::load)
}

@Composable
fun WorkoutDetailScreen(
    state: WorkoutDetailUiState,
    onRetry: () -> Unit
) {
    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.testTag("workout_detail_loading"))
            }
        }

        state.errorMessage != null -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.errorMessage)
                Button(onClick = onRetry, modifier = Modifier.testTag("workout_detail_retry")) { Text("Retry") }
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = RunCoachColors.SurfaceAccent)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(state.title, style = MaterialTheme.typography.headlineSmall, color = RunCoachColors.TextPrimary)
                            Text(state.dateLabel, color = RunCoachColors.TextSecondary)
                            Text(state.workoutType, color = RunCoachColors.ProgressAccent)
                            Text(state.plannedLabel, color = RunCoachColors.TextPrimary)
                            Text("Intensity: ${state.intensityLabel}", color = RunCoachColors.TextStrong)
                            Text("Status: ${state.statusLabel}", color = RunCoachColors.TextStrong, modifier = Modifier.testTag("detail_status"))
                        }
                    }
                }
                item { Text(state.warmupCooldownLabel) }
                items(state.structureLines) { line -> Text("- $line") }
                item { Text(state.instructions, modifier = Modifier.testTag("detail_instructions")) }
                item {
                    Button(onClick = {}, enabled = state.canMarkComplete, modifier = Modifier.fillMaxWidth().testTag("mark_complete")) {
                        Text("Mark completed")
                    }
                }
                item {
                    Button(onClick = {}, enabled = state.canMarkSkipped, modifier = Modifier.fillMaxWidth().testTag("mark_skipped")) {
                        Text("Mark skipped")
                    }
                }
            }
        }
    }
}
