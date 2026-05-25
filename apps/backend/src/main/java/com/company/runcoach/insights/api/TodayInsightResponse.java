package com.company.runcoach.insights.api;

import com.company.runcoach.adaptation.domain.ReadinessState;

import java.time.LocalDate;

public record TodayInsightResponse(
    LocalDate date,
    String planId,
    Integer planVersion,
    PlannedWorkoutSummary todaysPlannedWorkout,
    ReadinessState readinessState,
    String readinessLabel,
    String readinessMessage,
    FatigueSignalSummary latestFatigueSignal,
    InjuryFeedbackSummary latestInjuryFeedback,
    boolean hasCheckInToday,
    String recommendedTone,
    LatestAdaptationSummary latestAdaptation,
    java.util.List<String> insightMessages,
    java.util.List<String> warnings
) {
    public record PlannedWorkoutSummary(
        String plannedWorkoutId,
        String workoutType,
        String status,
        java.math.BigDecimal plannedDistanceKm,
        Integer plannedDurationMin,
        String intensityZone
    ) {
    }

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
        boolean redFlag
    ) {
    }

    public record LatestAdaptationSummary(
        String adaptationDecisionId,
        String summary,
        LocalDate affectedFromDate,
        LocalDate affectedToDate,
        java.util.List<String> changedWorkoutIds
    ) {
    }
}
