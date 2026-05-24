package com.company.runcoach.adaptation.api;

import com.company.runcoach.adaptation.domain.ReadinessState;

import java.util.UUID;

public record CreateFatigueSignalResponse(
    UUID fatigueSignalId,
    ReadinessState readinessState
) {
}
