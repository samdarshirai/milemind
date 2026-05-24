package com.company.runcoach.planning.api;

import java.util.UUID;

public record CreateWorkoutCompletionResponse(
    UUID completionId,
    UUID plannedWorkoutId,
    int planVersion,
    boolean adaptationTriggered,
    PlannedWorkoutMutationResponse.AdaptationSummary adaptation
) {
}
