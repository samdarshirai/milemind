package com.company.runcoach.planning.api;

import java.util.UUID;

public record GeneratePlanResponse(
    UUID trainingPlanId,
    int planVersion,
    String status
) {
}
