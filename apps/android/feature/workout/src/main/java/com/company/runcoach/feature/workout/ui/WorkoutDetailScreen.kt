package com.company.runcoach.feature.workout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.runcoach.core.designsystem.RunCoachColors
import com.company.runcoach.feature.workout.ui.model.SkipReason
import com.company.runcoach.feature.workout.ui.model.WorkoutDetailUiState

@Composable
fun WorkoutDetailRoute(viewModel: WorkoutDetailViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    WorkoutDetailScreen(
        state = state,
        onRetry = viewModel::load,
        onOpenSkip = viewModel::openSkipSheet,
        onDismissSkip = viewModel::dismissSkipSheet,
        onSelectSkipReason = viewModel::selectSkipReason,
        onSubmitSkip = viewModel::submitSkip,
        onOpenReschedule = viewModel::openRescheduleSheet,
        onDismissReschedule = viewModel::dismissRescheduleSheet,
        onChangeRescheduleDate = viewModel::updateRescheduleDate,
        onSubmitReschedule = viewModel::submitReschedule,
        onConflictRefresh = viewModel::refreshForConflict,
        onConflictRetry = viewModel::retryAfterConflict
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    state: WorkoutDetailUiState,
    onRetry: () -> Unit,
    onOpenSkip: () -> Unit,
    onDismissSkip: () -> Unit,
    onSelectSkipReason: (SkipReason) -> Unit,
    onSubmitSkip: () -> Unit,
    onOpenReschedule: () -> Unit,
    onDismissReschedule: () -> Unit,
    onChangeRescheduleDate: (String) -> Unit,
    onSubmitReschedule: () -> Unit,
    onConflictRefresh: () -> Unit,
    onConflictRetry: () -> Unit
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
                    Button(onClick = onOpenSkip, enabled = state.canMarkSkipped && !state.mutationInFlight, modifier = Modifier.fillMaxWidth().testTag("mark_skipped")) {
                        Text("Skip workout")
                    }
                }
                item {
                    OutlinedButton(
                        onClick = onOpenReschedule,
                        enabled = state.canReschedule && !state.mutationInFlight,
                        modifier = Modifier.fillMaxWidth().testTag("reschedule_workout")
                    ) {
                        Text("Reschedule workout")
                    }
                }
                state.conflictMessage?.let {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = RunCoachColors.SurfaceSecondary),
                            modifier = Modifier.fillMaxWidth().testTag("stale_plan_conflict")
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(it, color = RunCoachColors.TextStrong)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = onConflictRefresh, modifier = Modifier.testTag("conflict_refresh")) { Text("Refresh") }
                                    OutlinedButton(onClick = onConflictRetry, modifier = Modifier.testTag("conflict_retry")) { Text("Retry") }
                                }
                            }
                        }
                    }
                }
                state.latestAdaptation?.let { adaptation ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = RunCoachColors.SurfaceHighlight),
                            modifier = Modifier.fillMaxWidth().testTag("adaptation_summary_card")
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("What changed?", color = RunCoachColors.TextStrong, style = MaterialTheme.typography.titleMedium)
                                Text(adaptation.summary, color = RunCoachColors.TextSecondary)
                                Text(
                                    "${adaptation.affectedFromDate} to ${adaptation.affectedToDate}",
                                    color = RunCoachColors.TextMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                state.mutationError?.let {
                    item {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("mutation_error"))
                    }
                }
            }
        }
    }

    if (state.showSkipSheet) {
        ModalBottomSheet(onDismissRequest = onDismissSkip) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Skip this workout?", style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("skip_sheet_title"))
                Text("Your plan may adapt to keep training safe and realistic.")
                SkipReason.entries.forEach { reason ->
                    OutlinedButton(
                        onClick = { onSelectSkipReason(reason) },
                        modifier = Modifier.fillMaxWidth().testTag("skip_reason_${reason.apiValue}")
                    ) { Text(reason.label) }
                }
                if (state.mutationInFlight) {
                    CircularProgressIndicator(modifier = Modifier.testTag("skip_loading"))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismissSkip, enabled = !state.mutationInFlight) { Text("Cancel") }
                    Button(onClick = onSubmitSkip, enabled = !state.mutationInFlight, modifier = Modifier.testTag("skip_confirm")) { Text("Confirm") }
                }
            }
        }
    }

    if (state.showRescheduleSheet) {
        ModalBottomSheet(onDismissRequest = onDismissReschedule) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Reschedule workout", style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("reschedule_sheet_title"))
                Text("Moving this workout can affect nearby days and may trigger adaptation.")
                OutlinedTextField(
                    value = state.rescheduleDate,
                    onValueChange = onChangeRescheduleDate,
                    label = { Text("Target date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth().testTag("reschedule_date_input")
                )
                if (state.mutationInFlight) {
                    CircularProgressIndicator(modifier = Modifier.testTag("reschedule_loading"))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismissReschedule, enabled = !state.mutationInFlight) { Text("Cancel") }
                    Button(onClick = onSubmitReschedule, enabled = !state.mutationInFlight, modifier = Modifier.testTag("reschedule_confirm")) { Text("Confirm") }
                }
            }
        }
    }
}
