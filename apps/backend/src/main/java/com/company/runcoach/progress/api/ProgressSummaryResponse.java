package com.company.runcoach.progress.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProgressSummaryResponse(
    UUID planId,
    Integer planVersion,
    Integer currentTrainingWeek,
    Summary summary,
    List<WeeklyCompletion> weeklyCompletion,
    List<LongRunProgression> longRunProgression,
    List<ReadinessTrendPoint> readinessTrend,
    RecentStatusDistribution recentStatusDistribution,
    boolean emptyState,
    String message
) {
    public record Summary(
        int plannedWorkouts,
        int completedWorkouts,
        int skippedWorkouts,
        int rescheduledWorkouts,
        int adherencePercentage
    ) {
    }

    public record WeeklyCompletion(
        int weekNumber,
        int planned,
        int completed,
        int skipped,
        int completionPercentage
    ) {
    }

    public record LongRunProgression(
        int weekNumber,
        BigDecimal plannedDistanceKm,
        BigDecimal actualDistanceKm,
        String status
    ) {
    }

    public record ReadinessTrendPoint(
        LocalDate date,
        String readinessState,
        Integer fatigueLevel,
        Integer painSeverity
    ) {
    }

    public record RecentStatusDistribution(
        int planned,
        int completed,
        int skipped,
        int rescheduled
    ) {
    }
}
