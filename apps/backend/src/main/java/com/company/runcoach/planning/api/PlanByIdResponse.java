package com.company.runcoach.planning.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PlanByIdResponse(
    UUID planId,
    String status,
    LocalDate startDate,
    LocalDate endDate,
    UUID raceGoalId,
    List<WeekResponse> weeks
) {
    public record WeekResponse(
        int weekNumber,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalPlannedDistanceKm,
        List<WorkoutResponse> workouts
    ) {
    }

    public record WorkoutResponse(
        UUID id,
        LocalDate date,
        String type,
        BigDecimal plannedDistanceKm,
        Integer plannedDurationMin,
        String intensityZone,
        String status
    ) {
    }
}
