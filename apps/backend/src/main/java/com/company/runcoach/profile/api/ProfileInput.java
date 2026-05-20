package com.company.runcoach.profile.api;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProfileInput(
    @NotNull Integer birthYear,
    String sex,
    @NotNull String experienceLevel,
    @NotNull BigDecimal typicalWeeklyDistanceKm,
    @NotNull BigDecimal longestRecentRunKm,
    @NotEmpty List<String> preferredRunDays,
    @NotNull String preferredLongRunDay,
    @NotNull String goalStyle,
    Map<String, Object> injuryHistory,
    @NotNull Integer strengthDaysPerWeek,
    @NotNull String units,
    String timezone
) {
}
