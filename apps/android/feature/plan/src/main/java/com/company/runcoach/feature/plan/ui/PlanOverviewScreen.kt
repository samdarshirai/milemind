package com.company.runcoach.feature.plan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.runcoach.core.designsystem.RunCoachColors
import com.company.runcoach.feature.plan.ui.model.CalendarViewMode
import com.company.runcoach.feature.plan.ui.model.PlanOverviewUiState
import com.company.runcoach.feature.plan.ui.model.WorkoutCardUiModel
import java.util.Locale

@Composable
fun PlanOverviewRoute(
    onOpenWorkout: (String, String) -> Unit,
    viewModel: PlanOverviewViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    PlanOverviewScreen(
        state = state,
        onRetry = viewModel::load,
        onPreviousWeek = viewModel::onPreviousWeek,
        onNextWeek = viewModel::onNextWeek,
        onSelectWeekView = viewModel::onSelectWeekView,
        onSelectDayView = viewModel::onSelectDayView,
        onSelectDay = viewModel::onSelectDay,
        onOpenWorkout = onOpenWorkout
    )
}

@Composable
fun PlanOverviewScreen(
    state: PlanOverviewUiState,
    onRetry: () -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectWeekView: () -> Unit,
    onSelectDayView: () -> Unit,
    onSelectDay: (String) -> Unit,
    onOpenWorkout: (String, String) -> Unit
) {
    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.testTag("plan_loading"))
            }
        }

        state.errorMessage != null -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.errorMessage)
                Button(onClick = onRetry, modifier = Modifier.testTag("plan_retry")) { Text("Retry") }
            }
        }

        state.emptyMessage != null -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.emptyMessage)
                Button(onClick = onRetry, modifier = Modifier.testTag("plan_empty_cta")) { Text("Try again") }
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(RunCoachColors.ScreenBackground).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = RunCoachColors.SurfacePrimary)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Plan Overview", style = MaterialTheme.typography.titleLarge, color = RunCoachColors.TextPrimary)
                            Text("${state.raceDistanceType} · ${state.raceDate}", color = RunCoachColors.TextSecondary)
                            Text("Current training week: ${state.currentWeekIndex}", color = RunCoachColors.TextSecondary)
                            Text(state.planProgressText, color = RunCoachColors.ProgressAccent, fontWeight = FontWeight.SemiBold)
                            Text("Phase: ${state.selectedWeekPhaseLabel}", color = RunCoachColors.TextStrong, modifier = Modifier.testTag("phase_label"))
                            if (state.isRecoveryWeek) {
                                Text(
                                    "Recovery Week",
                                    color = RunCoachColors.StatusSkipped,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.testTag("recovery_chip")
                                )
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("calendar_mode_toggle"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSelectWeekView,
                            enabled = state.calendarViewMode != CalendarViewMode.WEEK,
                            modifier = Modifier.testTag("mode_week")
                        ) { Text("Week") }
                        Button(
                            onClick = onSelectDayView,
                            enabled = state.calendarViewMode != CalendarViewMode.DAY,
                            modifier = Modifier.testTag("mode_day")
                        ) { Text("Day") }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("week_nav"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = onPreviousWeek, modifier = Modifier.testTag("week_prev")) { Text("Previous") }
                        Text("Week ${state.selectedWeekIndex}", color = RunCoachColors.TextPrimary, modifier = Modifier.testTag("week_indicator"))
                        Button(onClick = onNextWeek, modifier = Modifier.testTag("week_next")) { Text("Next") }
                    }
                }
                if (state.calendarViewMode == CalendarViewMode.DAY) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().testTag("day_selector"),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.availableDayLabels.forEach { day ->
                                TextButton(
                                    onClick = { onSelectDay(day) },
                                    enabled = state.selectedDayLabel != day,
                                    modifier = Modifier.testTag("day_$day")
                                ) {
                                    Text(day)
                                }
                            }
                        }
                    }
                }
                val workouts = state.selectedWeek?.workouts.orEmpty()
                items(workouts) { workout ->
                    WorkoutCard(
                        workout = workout,
                        onClick = { onOpenWorkout(workout.plannedWorkoutId, workout.status) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(workout: WorkoutCardUiModel, onClick: () -> Unit) {
    val status = normalizeStatus(workout.status)
    val statusColor = when (status) {
        "COMPLETED" -> RunCoachColors.StatusCompleted
        "MISSED" -> RunCoachColors.StatusMissed
        "SKIPPED" -> RunCoachColors.StatusSkipped
        "REST" -> RunCoachColors.StatusRest
        else -> RunCoachColors.StatusPlanned
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag("workout_card_${workout.plannedWorkoutId}"),
        colors = CardDefaults.cardColors(
            containerColor = if (workout.isToday) RunCoachColors.SurfaceHighlight else RunCoachColors.SurfaceSecondary
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${workout.dayLabel}, ${workout.dateLabel}", color = RunCoachColors.TextSecondary)
            Text(workout.workoutType, color = RunCoachColors.TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(workout.distanceOrDurationLabel, color = RunCoachColors.TextStrong)
            Text(workout.summaryLabel, color = RunCoachColors.TextMuted, style = MaterialTheme.typography.bodySmall)
            Text(statusDisplayLabel(workout.status), color = statusColor, modifier = Modifier.testTag("status_${workout.plannedWorkoutId}"))
        }
    }
}

private fun normalizeStatus(rawStatus: String): String {
    return when (rawStatus.trim().uppercase(Locale.US)) {
        "REST_DAY" -> "REST"
        else -> rawStatus.trim().uppercase(Locale.US)
    }
}

private fun statusDisplayLabel(rawStatus: String): String {
    return when (normalizeStatus(rawStatus)) {
        "PLANNED" -> "Planned"
        "COMPLETED" -> "Completed"
        "MISSED" -> "Missed"
        "SKIPPED" -> "Skipped"
        "REST" -> "Rest day"
        else -> "Unknown"
    }
}
