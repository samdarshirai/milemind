package com.company.runcoach.feature.plan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.runcoach.feature.plan.data.PlanOverviewData
import com.company.runcoach.feature.plan.data.PlanRepository
import com.company.runcoach.feature.plan.ui.model.CalendarViewMode
import com.company.runcoach.feature.plan.ui.model.PlanOverviewUiState
import com.company.runcoach.feature.plan.ui.model.WeekUiModel
import com.company.runcoach.feature.plan.ui.model.WorkoutCardUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PlanOverviewViewModel @Inject constructor(
    private val repository: PlanRepository,
    private val clock: Clock = Clock.systemUTC()
) : ViewModel() {

    private var planData: PlanOverviewData? = null

    private val _uiState = MutableStateFlow(PlanOverviewUiState())
    val uiState: StateFlow<PlanOverviewUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, emptyMessage = null) }
        viewModelScope.launch {
            repository.loadCurrentPlan()
                .onSuccess { plan ->
                    planData = plan
                    if (plan == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                emptyMessage = "No training plan yet. Create a race goal and generate your plan."
                            )
                        }
                        return@onSuccess
                    }

                    val weekIndex = plan.currentWeekIndex
                    _uiState.value = mapState(plan, weekIndex)
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Could not load your plan right now."
                        )
                    }
                }
        }
    }

    fun onPreviousWeek() {
        val state = _uiState.value
        val minWeek = planData?.weeks?.minOfOrNull { it.weekIndex } ?: return
        if (state.selectedWeekIndex > minWeek) {
            selectWeek(state.selectedWeekIndex - 1)
        }
    }

    fun onNextWeek() {
        val state = _uiState.value
        val maxWeek = planData?.weeks?.maxOfOrNull { it.weekIndex } ?: return
        if (state.selectedWeekIndex < maxWeek) {
            selectWeek(state.selectedWeekIndex + 1)
        }
    }

    fun onSelectWeekView() {
        val plan = planData ?: return
        val current = _uiState.value
        _uiState.value = mapState(
            plan = plan,
            selectedWeekIndex = current.selectedWeekIndex,
            viewMode = CalendarViewMode.WEEK
        )
    }

    fun onSelectDayView() {
        val plan = planData ?: return
        val current = _uiState.value
        _uiState.value = mapState(
            plan = plan,
            selectedWeekIndex = current.selectedWeekIndex,
            viewMode = CalendarViewMode.DAY,
            selectedDay = current.selectedDayLabel
        )
    }

    fun onSelectDay(dayLabel: String) {
        val plan = planData ?: return
        val current = _uiState.value
        _uiState.value = mapState(
            plan = plan,
            selectedWeekIndex = current.selectedWeekIndex,
            viewMode = CalendarViewMode.DAY,
            selectedDay = dayLabel
        )
    }

    private fun selectWeek(weekIndex: Int) {
        val plan = planData ?: return
        val current = _uiState.value
        _uiState.value = mapState(
            plan = plan,
            selectedWeekIndex = weekIndex,
            viewMode = current.calendarViewMode
        )
    }

    private fun mapState(
        plan: PlanOverviewData,
        selectedWeekIndex: Int,
        viewMode: CalendarViewMode = CalendarViewMode.WEEK,
        selectedDay: String? = null
    ): PlanOverviewUiState {
        val today = LocalDate.now(clock)
        val selectedWeek = plan.weeks.firstOrNull { it.weekIndex == selectedWeekIndex } ?: return PlanOverviewUiState(
            isLoading = false,
            raceDistanceType = plan.raceDistanceType.replace('_', ' '),
            raceDate = plan.raceDate,
            currentWeekIndex = plan.currentWeekIndex,
            selectedWeekIndex = selectedWeekIndex,
            planProgressText = "Week ${selectedWeekIndex} of ${plan.weeks.size}"
        )
        val weekModels = plan.weeks.associate { week ->
            week.weekIndex to WeekUiModel(
                weekIndex = week.weekIndex,
                phase = week.phase,
                recoveryWeek = week.recoveryWeek,
                targetDistanceKm = week.targetDistanceKm,
                workouts = week.workouts.map { workout ->
                    val date = workout.scheduledDate
                    WorkoutCardUiModel(
                        plannedWorkoutId = workout.plannedWorkoutId,
                        dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        dateLabel = date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())),
                        workoutType = workout.workoutType.replace('_', ' '),
                        distanceOrDurationLabel = when {
                            workout.plannedDistanceKm != null -> String.format(Locale.US, "%.1f km", workout.plannedDistanceKm)
                            workout.plannedDurationMin != null -> "${workout.plannedDurationMin} min"
                            else -> "Planned"
                        },
                        summaryLabel = workout.intensityZone ?: "Follow easy controlled effort",
                        status = workout.status,
                        isToday = date == today
                    )
                }
            )
        }

        val selectedWeekModel = weekModels[selectedWeekIndex]
        val availableDays = selectedWeekModel
            ?.workouts
            ?.map { it.dayLabel }
            ?.distinct()
            .orEmpty()
        val resolvedSelectedDay = when {
            viewMode == CalendarViewMode.WEEK -> null
            selectedDay != null && availableDays.contains(selectedDay) -> selectedDay
            else -> selectedWeekModel?.workouts?.firstOrNull()?.dayLabel
        }
        val visibleWorkouts = when {
            selectedWeekModel == null -> emptyList()
            viewMode == CalendarViewMode.WEEK -> selectedWeekModel.workouts
            else -> selectedWeekModel.workouts.filter { it.dayLabel == resolvedSelectedDay }
        }

        val progress = "Week ${selectedWeekIndex} of ${plan.weeks.size}"
        return PlanOverviewUiState(
            isLoading = false,
            raceDistanceType = plan.raceDistanceType.replace('_', ' '),
            raceDate = plan.raceDate,
            currentWeekIndex = plan.currentWeekIndex,
            selectedWeekIndex = selectedWeekIndex,
            planProgressText = progress,
            selectedWeek = selectedWeekModel?.copy(workouts = visibleWorkouts),
            selectedWeekPhaseLabel = selectedWeek.phase.replace('_', ' '),
            isRecoveryWeek = selectedWeek.recoveryWeek,
            calendarViewMode = viewMode,
            selectedDayLabel = resolvedSelectedDay,
            availableDayLabels = availableDays
        )
    }
}
