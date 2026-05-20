package com.company.runcoach.goals.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateRaceGoalRequest(
    String raceName,
    @NotBlank String raceDistanceType,
    @NotNull LocalDate raceDate,
    @NotBlank String goalStyle,
    Integer targetTimeSeconds
) {
}
