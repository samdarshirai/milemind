package com.company.runcoach.adaptation.api;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record CreateInjuryFeedbackRequest(
    @NotNull OffsetDateTime reportedAt,
    Boolean hasPain,
    String bodyRegion,
    String painType,
    Integer severity,
    String onsetContext,
    Boolean canRun,
    Boolean redFlag,
    String freeText
) {
}
