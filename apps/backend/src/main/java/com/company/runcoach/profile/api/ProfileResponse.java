package com.company.runcoach.profile.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProfileResponse(
    UUID userId,
    String email,
    String timezone,
    ProfileData profile
) {
    public record ProfileData(
        Integer birthYear,
        String sex,
        String experienceLevel,
        BigDecimal typicalWeeklyDistanceKm,
        BigDecimal longestRecentRunKm,
        List<String> preferredRunDays,
        String preferredLongRunDay,
        String goalStyle,
        Integer strengthDaysPerWeek,
        String units,
        Map<String, Object> injuryHistory
    ) {
    }
}
