package com.company.runcoach.planning.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record GeneratePlanRequest(
    @NotNull UUID raceGoalId,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
    Boolean forceRegenerate
) {
}
