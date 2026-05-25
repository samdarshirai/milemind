package com.company.runcoach.feature.progress.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.runcoach.core.designsystem.RunCoachColors
import com.company.runcoach.feature.progress.ProgressViewModel
import com.company.runcoach.feature.progress.ui.model.ProgressContentUiModel
import com.company.runcoach.feature.progress.ui.model.ProgressUiState
import com.company.runcoach.feature.progress.ui.model.ReadinessStateUi

@Composable
fun ProgressRoute(viewModel: ProgressViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    ProgressScreen(state = state, onRetry = viewModel::load)
}

@Composable
fun ProgressScreen(state: ProgressUiState, onRetry: () -> Unit) {
    if (state.isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("progress_loading"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text("Loading progress", color = RunCoachColors.TextSecondary)
        }
        return
    }

    if (state.errorMessage != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("progress_error"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Progress", style = MaterialTheme.typography.headlineMedium, color = RunCoachColors.TextPrimary)
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry, modifier = Modifier.testTag("progress_retry")) { Text("Retry") }
        }
        return
    }

    if (state.emptyStateMessage != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .testTag("progress_empty"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Progress", style = MaterialTheme.typography.headlineMedium, color = RunCoachColors.TextPrimary)
            Card(colors = CardDefaults.cardColors(containerColor = RunCoachColors.SurfacePrimary)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your data will live here", color = RunCoachColors.TextStrong, style = MaterialTheme.typography.titleMedium)
                    Text(state.emptyStateMessage, color = RunCoachColors.TextSecondary)
                }
            }
            OutlinedButton(onClick = onRetry, modifier = Modifier.testTag("progress_refresh")) { Text("Refresh") }
        }
        return
    }

    state.content?.let { content ->
        ProgressContent(content = content, onRetry = onRetry)
    }
}

@Composable
private fun ProgressContent(content: ProgressContentUiModel, onRetry: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("progress_content"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("PROGRESS", style = MaterialTheme.typography.labelMedium, color = RunCoachColors.TextMuted)
            Text("Your Performance", style = MaterialTheme.typography.headlineMedium, color = RunCoachColors.TextPrimary)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RunCoachColors.SurfacePrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Weekly adherence", color = RunCoachColors.TextMuted, style = MaterialTheme.typography.labelMedium)
                        Text("Week ${content.currentTrainingWeek ?: "-"}", color = RunCoachColors.TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        CircularProgressIndicator(
                            progress = { (content.completionPercentage.coerceIn(0, 100) / 100f) },
                            modifier = Modifier
                                .width(56.dp)
                                .height(56.dp),
                            color = RunCoachColors.TextStrong,
                            trackColor = RunCoachColors.SurfaceSecondary,
                            strokeWidth = 6.dp
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("${content.completionPercentage}%", style = MaterialTheme.typography.displaySmall, color = RunCoachColors.TextStrong)
                            Text("${content.completedWorkouts} workouts completed", color = RunCoachColors.TextSecondary)
                        }
                    }
                    Text(content.insightMessage ?: "Keep stacking consistent weeks.", color = RunCoachColors.TextSecondary)
                }
            }
        }

        item {
            SectionCard("Long Run Progression") {
                if (content.longRunProgression.isEmpty()) {
                    Text("No long-run data yet.", color = RunCoachColors.TextSecondary)
                } else {
                    val maxDistance = content.longRunProgression.maxOf { it.plannedDistanceKm }.toFloat().coerceAtLeast(1f)
                    content.longRunProgression.forEach { model ->
                        Text(model.label, color = RunCoachColors.TextStrong, style = MaterialTheme.typography.labelLarge)
                        Text(model.detail, color = RunCoachColors.TextSecondary)
                        LinearProgressIndicator(
                            progress = { (model.plannedDistanceKm.toFloat() / maxDistance).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .testTag("progress_longrun_bar"),
                            color = RunCoachColors.ProgressAccent,
                            trackColor = RunCoachColors.SurfaceSecondary
                        )
                        Text(model.status.replace('_', ' '), color = statusColor(model.status), style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        item {
            SectionCard("Weekly Completion") {
                content.weeklyCompletion.forEach {
                    Text("${it.label}  ${it.completion}", color = RunCoachColors.TextSecondary)
                    ProgressBar(
                        fraction = (it.completionPercentage.coerceIn(0, 100) / 100f),
                        color = RunCoachColors.TextPrimary,
                        trackColor = RunCoachColors.SurfaceSecondary,
                        tag = "progress_weekly_bar_fill"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        item {
            SectionCard("Readiness Trend") {
                Text(content.readinessTrendLabel, color = RunCoachColors.TextStrong, style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                    val bars = content.readinessTrend.ifEmpty { listOf() }
                    if (bars.isEmpty()) {
                        Text("No readiness trend yet.", color = RunCoachColors.TextSecondary)
                    } else {
                        bars.forEach { point ->
                            val fraction = when (point.state) {
                                ReadinessStateUi.READY -> 0.9f
                                ReadinessStateUi.CAUTION -> 0.55f
                                ReadinessStateUi.HIGH_RISK -> 0.25f
                                ReadinessStateUi.UNKNOWN -> 0.45f
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .width(20.dp)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(RunCoachColors.SurfaceSecondary),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(fraction)
                                            .background(readinessColor(point.state))
                                    )
                                }
                                Text(point.label.takeLast(2), color = RunCoachColors.TextMuted, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0x33FC4C02), Color(0x33EF2CC1), Color(0x33BDBBFF))
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Recent status", color = RunCoachColors.TextStrong)
                    val dist = content.recentStatusDistribution
                    if (dist == null) {
                        Text("No status distribution yet.", color = RunCoachColors.TextSecondary)
                    } else {
                        Text("Planned ${dist.planned}  Completed ${dist.completed}  Skipped ${dist.skipped}  Rescheduled ${dist.rescheduled}", color = RunCoachColors.TextSecondary)
                    }
                }
            }
        }

        item {
            OutlinedButton(onClick = onRetry, modifier = Modifier.testTag("progress_refresh")) {
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = RunCoachColors.SurfaceAccent), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = RunCoachColors.TextStrong, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun ProgressBar(fraction: Float, color: Color, trackColor: Color, tag: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .testTag(tag)
        )
    }
}

private fun statusColor(status: String): Color = when (status.uppercase()) {
    "COMPLETED" -> RunCoachColors.StatusCompleted
    "SKIPPED" -> RunCoachColors.StatusSkipped
    "RESCHEDULED" -> RunCoachColors.StatusRest
    else -> RunCoachColors.TextMuted
}

private fun readinessColor(readinessState: ReadinessStateUi): Color = when (readinessState) {
    ReadinessStateUi.READY -> Color(0xFF4CD67A)
    ReadinessStateUi.CAUTION -> Color(0xFFF3C15A)
    ReadinessStateUi.HIGH_RISK -> Color(0xFFFF7E79)
    ReadinessStateUi.UNKNOWN -> RunCoachColors.TextMuted
}
