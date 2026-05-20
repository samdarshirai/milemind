package com.company.runcoach.planning.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PlannedWorkoutDetailResponse(
    UUID plannedWorkoutId,
    LocalDate scheduledDate,
    String workoutType,
    String workoutSubtype,
    BigDecimal plannedDistanceKm,
    Integer plannedDurationMin,
    String intensityZone,
    List<Map<String, Object>> structure,
    String whyThisWorkout,
    List<String> changeReasonCodes
) {
}
