package com.company.runcoach.planning.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PlannedWorkoutMutationResponse(
    int planVersion,
    AdaptationSummary adaptation
) {
    public record AdaptationSummary(
        UUID id,
        String summary,
        LocalDate affectedFromDate,
        LocalDate affectedToDate,
        List<UUID> changedWorkoutIds
    ) {
    }
}
