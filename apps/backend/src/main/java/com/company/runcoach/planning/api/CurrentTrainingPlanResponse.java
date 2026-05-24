package com.company.runcoach.planning.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CurrentTrainingPlanResponse(
    UUID trainingPlanId,
    int planVersion,
    String methodologyCode,
    RaceGoalSummary raceGoal,
    int currentWeekIndex,
    List<WeekSummary> weeks,
    LatestAdaptationSummary latestAdaptation
) {
    public record RaceGoalSummary(
        String raceDistanceType,
        LocalDate raceDate
    ) {
    }

    public record WeekSummary(
        int weekIndex,
        String phase,
        boolean recoveryWeek,
        BigDecimal targetDistanceKm,
        List<WorkoutSummary> workouts
    ) {
    }

    public record WorkoutSummary(
        UUID plannedWorkoutId,
        LocalDate scheduledDate,
        String workoutType,
        String status,
        BigDecimal plannedDistanceKm,
        Integer plannedDurationMin,
        String intensityZone,
        List<String> changeReasonCodes,
        UUID adaptedFromWorkoutId
    ) {
    }

    public record LatestAdaptationSummary(
        List<UUID> changedWorkoutIds
    ) {
    }
}
