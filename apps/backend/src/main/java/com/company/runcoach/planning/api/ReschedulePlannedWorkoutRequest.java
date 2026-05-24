package com.company.runcoach.planning.api;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReschedulePlannedWorkoutRequest(
    @NotNull LocalDate targetDate,
    @NotNull Integer expectedPlanVersion
) {
}
