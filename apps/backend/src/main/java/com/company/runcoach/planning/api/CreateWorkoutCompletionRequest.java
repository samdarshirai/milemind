package com.company.runcoach.planning.api;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWorkoutCompletionRequest(
    @NotNull UUID plannedWorkoutId,
    BigDecimal actualDistanceKm,
    Integer actualDurationMin
) {
}
