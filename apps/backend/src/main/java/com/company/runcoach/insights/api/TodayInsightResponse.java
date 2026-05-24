package com.company.runcoach.insights.api;

import com.company.runcoach.adaptation.domain.ReadinessState;

import java.time.LocalDate;

public record TodayInsightResponse(
    LocalDate date,
    ReadinessState readinessState,
    String readinessLabel,
    String readinessMessage,
    FatigueSignalSummary latestFatigueSignal,
    InjuryFeedbackSummary latestInjuryFeedback,
    boolean hasCheckInToday,
    String recommendedTone
) {
    public record FatigueSignalSummary(
        LocalDate signalDate,
        Integer sleepScore,
        Integer stressScore,
        Integer sorenessScore,
        Integer motivationScore,
        boolean illnessFlag,
        boolean tooBusyFlag,
        boolean travellingFlag,
        String notes
    ) {
    }

    public record InjuryFeedbackSummary(
        String reportedAt,
        boolean hasPain,
        String bodyRegion,
        String painType,
        Integer severity,
        String onsetContext,
        Boolean canRun,
        boolean redFlag,
        String freeText
    ) {
    }
}
