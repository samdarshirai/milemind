package com.company.runcoach.adaptation.api;

import com.company.runcoach.adaptation.domain.ReadinessState;

import java.util.UUID;

public record CreateInjuryFeedbackResponse(
    UUID injuryFeedbackId,
    ReadinessState readinessState
) {
}
