package com.company.runcoach.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.runcoach.feature.progress.data.ProgressRepository
import com.company.runcoach.feature.progress.ui.model.LongRunProgressionUiModel
import com.company.runcoach.feature.progress.ui.model.ProgressContentUiModel
import com.company.runcoach.feature.progress.ui.model.ProgressUiState
import com.company.runcoach.feature.progress.ui.model.ReadinessStateUi
import com.company.runcoach.feature.progress.ui.model.ReadinessTrendUiModel
import com.company.runcoach.feature.progress.ui.model.RecentStatusDistributionUiModel
import com.company.runcoach.feature.progress.ui.model.WeeklyCompletionUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: ProgressRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.loadSummary()
                .onSuccess { data ->
                    if (data.isEmptyState) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                content = null,
                                emptyStateMessage = data.message ?: "No active plan yet. Set a race goal to start tracking progress."
                            )
                        }
                        return@onSuccess
                    }

                    if ((data.summary?.completedWorkouts ?: 0) == 0) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                content = null,
                                errorMessage = null,
                                emptyStateMessage = data.message ?: "Start completing workouts to unlock progress trends."
                            )
                        }
                        return@onSuccess
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            emptyStateMessage = null,
                            content = ProgressContentUiModel(
                                completionPercentage = data.summary?.adherencePercentage ?: 0,
                                completedWorkouts = data.summary?.completedWorkouts ?: 0,
                                currentTrainingWeek = data.summary?.currentTrainingWeek,
                                readinessTrendLabel = data.summary?.readinessTrend ?: "Monitoring",
                                weeklyCompletion = data.weeklyCompletion.map { week ->
                                    WeeklyCompletionUiModel(
                                        label = "Week ${week.weekNumber}",
                                        completion = "${week.completed}/${week.planned} (${week.completionPercentage}%)",
                                        completionPercentage = week.completionPercentage
                                    )
                                },
                                longRunProgression = data.longRunProgression.map { item ->
                                    LongRunProgressionUiModel(
                                        label = "Week ${item.weekNumber}",
                                        detail = if (item.actualDistanceKm != null) {
                                            String.format(
                                                Locale.US,
                                                "Planned %.1f km · Actual %.1f km",
                                                item.plannedDistanceKm,
                                                item.actualDistanceKm
                                            )
                                        } else {
                                            String.format(Locale.US, "Planned %.1f km", item.plannedDistanceKm)
                                        },
                                        status = item.status,
                                        plannedDistanceKm = item.plannedDistanceKm
                                    )
                                },
                                readinessTrend = data.readinessTrend.takeLast(7).map { item ->
                                    ReadinessTrendUiModel(
                                        label = item.date,
                                        state = toReadinessStateUi(item.readinessState),
                                        detail = buildReadinessDetail(
                                            item.readinessState,
                                            item.fatigueLevel,
                                            item.painSeverity
                                        )
                                    )
                                },
                                recentStatusDistribution = data.recentStatus?.let {
                                    RecentStatusDistributionUiModel(
                                        planned = it.planned,
                                        completed = it.completed,
                                        skipped = it.skipped,
                                        rescheduled = it.rescheduled
                                    )
                                },
                                insightMessage = data.message
                            )
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Could not load progress summary.",
                            content = null,
                            emptyStateMessage = null
                        )
                    }
                }
        }
    }
}

private fun toReadinessStateUi(readinessState: String): ReadinessStateUi {
    return when (readinessState.uppercase(Locale.US)) {
        "READY" -> ReadinessStateUi.READY
        "CAUTION" -> ReadinessStateUi.CAUTION
        "HIGH_RISK" -> ReadinessStateUi.HIGH_RISK
        else -> ReadinessStateUi.UNKNOWN
    }
}

private fun buildReadinessDetail(
    readinessState: String,
    fatigueLevel: Int?,
    painSeverity: Int?
): String {
    val readiness = readinessState.replace('_', ' ')
    val fatigue = fatigueLevel?.toString() ?: "--"
    val pain = painSeverity?.toString() ?: "--"
    return "$readiness · fatigue $fatigue · pain $pain"
}
