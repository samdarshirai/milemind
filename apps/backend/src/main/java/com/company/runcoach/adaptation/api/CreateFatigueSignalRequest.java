package com.company.runcoach.adaptation.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateFatigueSignalRequest(
    @NotNull LocalDate signalDate,
    @NotNull @Min(1) @Max(5) Integer sleepScore,
    @NotNull @Min(1) @Max(5) Integer stressScore,
    @NotNull @Min(1) @Max(5) Integer sorenessScore,
    @NotNull @Min(1) @Max(5) Integer motivationScore,
    Boolean illnessFlag,
    Boolean tooBusyFlag,
    Boolean travellingFlag,
    String notes
) {
}
