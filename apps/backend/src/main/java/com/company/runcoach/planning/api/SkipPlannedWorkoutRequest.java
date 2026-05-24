package com.company.runcoach.planning.api;

import com.company.runcoach.adaptation.domain.AdaptationReason;
import jakarta.validation.constraints.NotNull;

public record SkipPlannedWorkoutRequest(
    @NotNull AdaptationReason reason,
    @NotNull Integer expectedPlanVersion
) {
}
