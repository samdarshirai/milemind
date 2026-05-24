package com.company.runcoach.feature.plan.ui.model

enum class CalendarViewMode {
    DAY,
    WEEK
}

data class PlanOverviewUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val emptyMessage: String? = null,
    val raceDistanceType: String = "",
    val raceDate: String = "",
    val currentWeekIndex: Int = 1,
    val selectedWeekIndex: Int = 1,
    val planProgressText: String = "",
    val selectedWeek: WeekUiModel? = null,
    val selectedWeekPhaseLabel: String = "",
    val isRecoveryWeek: Boolean = false,
    val calendarViewMode: CalendarViewMode = CalendarViewMode.WEEK,
    val selectedDayLabel: String? = null,
    val availableDayLabels: List<String> = emptyList()
)

data class WeekUiModel(
    val weekIndex: Int,
    val phase: String,
    val recoveryWeek: Boolean,
    val targetDistanceKm: Double?,
    val workouts: List<WorkoutCardUiModel>
)

data class WorkoutCardUiModel(
    val plannedWorkoutId: String,
    val dayLabel: String,
    val dateLabel: String,
    val workoutType: String,
    val distanceOrDurationLabel: String,
    val summaryLabel: String,
    val status: String,
    val isToday: Boolean
)
