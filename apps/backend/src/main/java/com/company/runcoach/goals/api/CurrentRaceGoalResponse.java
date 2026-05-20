package com.company.runcoach.goals.api;

import java.time.LocalDate;
import java.util.UUID;

public record CurrentRaceGoalResponse(
    UUID raceGoalId,
    String raceDistanceType,
    LocalDate raceDate,
    String goalStyle,
    Integer targetTimeSeconds,
    String status
) {
}
