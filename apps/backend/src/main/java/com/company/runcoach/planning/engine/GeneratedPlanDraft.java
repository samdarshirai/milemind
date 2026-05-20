package com.company.runcoach.planning.engine;

import com.company.runcoach.planning.domain.PlannedWorkoutType;
import com.company.runcoach.planning.domain.TrainingPhase;
import com.company.runcoach.planning.domain.WorkoutIntensity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record GeneratedPlanDraft(LocalDate startDate, LocalDate endDate, List<GeneratedWeekDraft> weeks) {

    public record GeneratedWeekDraft(
        int weekNumber,
        TrainingPhase phase,
        boolean recoveryWeek,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalPlannedDistanceKm,
        List<GeneratedWorkoutDraft> workouts
    ) {
    }

    public record GeneratedWorkoutDraft(
        LocalDate date,
        PlannedWorkoutType type,
        String workoutSubtype,
        BigDecimal plannedDistanceKm,
        Integer plannedDurationMin,
        WorkoutIntensity intensity,
        Map<String, Object> structure,
        Map<String, Object> rationale
    ) {
    }
}
