package com.company.runcoach.progress.service;

import com.company.runcoach.adaptation.domain.FatigueSignal;
import com.company.runcoach.adaptation.domain.InjuryFeedback;
import com.company.runcoach.adaptation.domain.AdaptationDecision;
import com.company.runcoach.adaptation.domain.AdaptationTriggerType;
import com.company.runcoach.adaptation.repo.AdaptationDecisionRepository;
import com.company.runcoach.adaptation.repo.FatigueSignalRepository;
import com.company.runcoach.adaptation.repo.InjuryFeedbackRepository;
import com.company.runcoach.adaptation.service.ReadinessService;
import com.company.runcoach.identity.domain.AppUser;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.planning.domain.PlanStatus;
import com.company.runcoach.planning.domain.PlannedWorkout;
import com.company.runcoach.planning.domain.PlannedWorkoutStatus;
import com.company.runcoach.planning.domain.PlannedWorkoutType;
import com.company.runcoach.planning.domain.TrainingPlan;
import com.company.runcoach.planning.domain.WorkoutCompletion;
import com.company.runcoach.planning.repo.PlannedWorkoutRepository;
import com.company.runcoach.planning.repo.TrainingPlanRepository;
import com.company.runcoach.planning.repo.WorkoutCompletionRepository;
import com.company.runcoach.progress.api.ProgressSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    private static final int READINESS_TREND_DAYS = 14;
    private static final int RECENT_STATUS_DAYS = 7;

    private final TrainingPlanRepository trainingPlanRepository;
    private final PlannedWorkoutRepository plannedWorkoutRepository;
    private final WorkoutCompletionRepository workoutCompletionRepository;
    private final AdaptationDecisionRepository adaptationDecisionRepository;
    private final FatigueSignalRepository fatigueSignalRepository;
    private final InjuryFeedbackRepository injuryFeedbackRepository;
    private final AppUserRepository appUserRepository;
    private final ReadinessService readinessService;

    public ProgressService(
        TrainingPlanRepository trainingPlanRepository,
        PlannedWorkoutRepository plannedWorkoutRepository,
        WorkoutCompletionRepository workoutCompletionRepository,
        AdaptationDecisionRepository adaptationDecisionRepository,
        FatigueSignalRepository fatigueSignalRepository,
        InjuryFeedbackRepository injuryFeedbackRepository,
        AppUserRepository appUserRepository,
        ReadinessService readinessService
    ) {
        this.trainingPlanRepository = trainingPlanRepository;
        this.plannedWorkoutRepository = plannedWorkoutRepository;
        this.workoutCompletionRepository = workoutCompletionRepository;
        this.adaptationDecisionRepository = adaptationDecisionRepository;
        this.fatigueSignalRepository = fatigueSignalRepository;
        this.injuryFeedbackRepository = injuryFeedbackRepository;
        this.appUserRepository = appUserRepository;
        this.readinessService = readinessService;
    }

    @Transactional(readOnly = true)
    public ProgressSummaryResponse getSummary(UUID userId) {
        AppUser user = appUserRepository.findById(userId).orElse(null);
        ZoneId userZone = user == null ? ZoneId.of("UTC") : ZoneId.of(user.getTimezone());
        LocalDate today = LocalDate.now(userZone);

        TrainingPlan plan = trainingPlanRepository
            .findFirstByUser_IdAndStatusInOrderByCreatedAtDesc(userId, List.of(PlanStatus.ACTIVE))
            .orElse(null);

        if (plan == null) {
            return emptySummary("No active plan yet. Generate a plan to start tracking your progress.");
        }

        List<PlannedWorkout> workouts = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(plan.getId());
        if (workouts.isEmpty()) {
            return new ProgressSummaryResponse(
                plan.getId(),
                plan.getPlanVersion(),
                plan.getCurrentWeekIndex(),
                new ProgressSummaryResponse.Summary(0, 0, 0, 0, 0),
                List.of(),
                List.of(),
                List.of(),
                new ProgressSummaryResponse.RecentStatusDistribution(0, 0, 0, 0),
                true,
                "Your progress will appear after workouts are scheduled."
            );
        }

        List<WorkoutCompletion> completions = workoutCompletionRepository.findByPlannedWorkout_TrainingPlan_Id(plan.getId());
        Map<UUID, WorkoutCompletion> completionByWorkoutId = completions.stream()
            .collect(Collectors.toMap(c -> c.getPlannedWorkout().getId(), c -> c, (left, right) -> right));

        int plannedCount = workouts.size();
        int completedCount = countByStatus(workouts, PlannedWorkoutStatus.COMPLETED);
        int skippedCount = countByStatus(workouts, PlannedWorkoutStatus.SKIPPED);
        int rescheduledCount = countRescheduledWorkouts(plan.getId());
        List<PlannedWorkout> upToToday = workouts.stream()
            .filter(w -> !w.getScheduledDate().isAfter(today))
            .toList();
        int adherencePercentage = percentage(countByStatus(upToToday, PlannedWorkoutStatus.COMPLETED), upToToday.size());

        List<ProgressSummaryResponse.WeeklyCompletion> weeklyCompletion = buildWeeklyCompletion(workouts);
        List<ProgressSummaryResponse.LongRunProgression> longRunProgression = buildLongRunProgression(workouts, completionByWorkoutId);
        List<ProgressSummaryResponse.ReadinessTrendPoint> readinessTrend = buildReadinessTrend(userId, userZone, today);
        ProgressSummaryResponse.RecentStatusDistribution recentStatusDistribution = buildRecentStatusDistribution(workouts, today);

        boolean earlyState = countByStatus(upToToday, PlannedWorkoutStatus.COMPLETED) == 0;
        String message = earlyState
            ? "Complete your first workout to unlock adherence trends."
            : "You completed " + adherencePercentage + "% of your planned workouts so far.";

        return new ProgressSummaryResponse(
            plan.getId(),
            plan.getPlanVersion(),
            plan.getCurrentWeekIndex(),
            new ProgressSummaryResponse.Summary(plannedCount, completedCount, skippedCount, rescheduledCount, adherencePercentage),
            weeklyCompletion,
            longRunProgression,
            readinessTrend,
            recentStatusDistribution,
            earlyState,
            message
        );
    }

    private ProgressSummaryResponse emptySummary(String message) {
        return new ProgressSummaryResponse(
            null,
            null,
            null,
            new ProgressSummaryResponse.Summary(0, 0, 0, 0, 0),
            List.of(),
            List.of(),
            List.of(),
            new ProgressSummaryResponse.RecentStatusDistribution(0, 0, 0, 0),
            true,
            message
        );
    }

    private int countByStatus(List<PlannedWorkout> workouts, PlannedWorkoutStatus status) {
        return (int) workouts.stream().filter(w -> w.getStatus() == status).count();
    }

    private int percentage(int numerator, int denominator) {
        if (denominator == 0) {
            return 0;
        }
        return (int) Math.round((numerator * 100.0) / denominator);
    }

    private List<ProgressSummaryResponse.WeeklyCompletion> buildWeeklyCompletion(List<PlannedWorkout> workouts) {
        Map<Integer, List<PlannedWorkout>> byWeek = workouts.stream()
            .collect(Collectors.groupingBy(w -> w.getTrainingPlanWeek().getWeekIndex()));

        return byWeek.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                List<PlannedWorkout> weekWorkouts = entry.getValue();
                int planned = weekWorkouts.size();
                int completed = countByStatus(weekWorkouts, PlannedWorkoutStatus.COMPLETED);
                int skipped = countByStatus(weekWorkouts, PlannedWorkoutStatus.SKIPPED);
                return new ProgressSummaryResponse.WeeklyCompletion(
                    entry.getKey(),
                    planned,
                    completed,
                    skipped,
                    percentage(completed, planned)
                );
            })
            .toList();
    }

    private List<ProgressSummaryResponse.LongRunProgression> buildLongRunProgression(
        List<PlannedWorkout> workouts,
        Map<UUID, WorkoutCompletion> completionByWorkoutId
    ) {
        return workouts.stream()
            .filter(w -> w.getWorkoutType() == PlannedWorkoutType.LONG_RUN)
            .sorted(Comparator.comparing((PlannedWorkout w) -> w.getTrainingPlanWeek().getWeekIndex())
                .thenComparing(PlannedWorkout::getScheduledDate))
            .map(workout -> {
                WorkoutCompletion completion = completionByWorkoutId.get(workout.getId());
                BigDecimal actualDistance = completion == null ? null : completion.getActualDistanceKm();
                if (actualDistance != null) {
                    actualDistance = actualDistance.setScale(1, RoundingMode.HALF_UP);
                }
                return new ProgressSummaryResponse.LongRunProgression(
                    workout.getTrainingPlanWeek().getWeekIndex(),
                    workout.getPlannedDistanceKm(),
                    actualDistance,
                    workout.getStatus().name()
                );
            })
            .toList();
    }

    private List<ProgressSummaryResponse.ReadinessTrendPoint> buildReadinessTrend(UUID userId, ZoneId userZone, LocalDate today) {
        LocalDate startDate = today.minusDays(READINESS_TREND_DAYS - 1L);
        OffsetDateTime startUtc = startDate.atStartOfDay(userZone).withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime endUtc = today.plusDays(1L).atStartOfDay(userZone).withZoneSameInstant(ZoneOffset.UTC).minusNanos(1).toOffsetDateTime();

        List<FatigueSignal> fatigueSignals = fatigueSignalRepository
            .findByUser_IdAndSignalDateBetweenOrderBySignalDateAscCreatedAtAsc(userId, startDate, today);
        List<InjuryFeedback> injuryFeedbackList = injuryFeedbackRepository
            .findByUser_IdAndReportedAtBetweenOrderByReportedAtAscCreatedAtAsc(userId, startUtc, endUtc);

        Map<LocalDate, FatigueSignal> fatigueByDate = new HashMap<>();
        for (FatigueSignal signal : fatigueSignals) {
            fatigueByDate.put(signal.getSignalDate(), signal);
        }

        Map<LocalDate, InjuryFeedback> injuryByDate = new HashMap<>();
        for (InjuryFeedback injury : injuryFeedbackList) {
            LocalDate localDate = injury.getReportedAt().atZoneSameInstant(userZone).toLocalDate();
            injuryByDate.put(localDate, injury);
        }

        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            if (fatigueByDate.containsKey(date) || injuryByDate.containsKey(date)) {
                dates.add(date);
            }
        }

        return dates.stream().map(date -> {
            FatigueSignal fatigueSignal = fatigueByDate.get(date);
            InjuryFeedback injuryFeedback = injuryByDate.get(date);
            Integer fatigueLevel = fatigueSignal == null
                ? null
                : Math.round((fatigueSignal.getSleepScore() + fatigueSignal.getStressScore()
                + fatigueSignal.getSorenessScore() + fatigueSignal.getMotivationScore()) / 4.0f);
            Integer painSeverity = injuryFeedback == null ? null : injuryFeedback.getSeverity();
            return new ProgressSummaryResponse.ReadinessTrendPoint(
                date,
                readinessService.evaluate(fatigueSignal, injuryFeedback).name(),
                fatigueLevel,
                painSeverity
            );
        }).toList();
    }

    private ProgressSummaryResponse.RecentStatusDistribution buildRecentStatusDistribution(
        List<PlannedWorkout> workouts,
        LocalDate today
    ) {
        LocalDate start = today.minusDays(RECENT_STATUS_DAYS - 1L);
        List<PlannedWorkout> recent = workouts.stream()
            .filter(w -> !w.getScheduledDate().isBefore(start) && !w.getScheduledDate().isAfter(today))
            .toList();

        int planned = countByStatus(recent, PlannedWorkoutStatus.PLANNED);
        int completed = countByStatus(recent, PlannedWorkoutStatus.COMPLETED);
        int skipped = countByStatus(recent, PlannedWorkoutStatus.SKIPPED);
        int rescheduled = countRescheduledWorkoutsInWindow(recent);

        return new ProgressSummaryResponse.RecentStatusDistribution(planned, completed, skipped, rescheduled);
    }

    private int countRescheduledWorkouts(UUID trainingPlanId) {
        Set<String> uniqueChangedIds = adaptationDecisionRepository.findByTrainingPlan_IdAndTriggerTypeOrderByCreatedAtAsc(
                trainingPlanId,
                AdaptationTriggerType.RESCHEDULE
            )
            .stream()
            .map(AdaptationDecision::getChangedWorkoutIds)
            .filter(ids -> ids != null && !ids.isEmpty())
            .flatMap(List::stream)
            .collect(Collectors.toSet());
        return uniqueChangedIds.size();
    }

    private int countRescheduledWorkoutsInWindow(List<PlannedWorkout> recent) {
        if (recent.isEmpty()) {
            return 0;
        }
        UUID planId = recent.getFirst().getTrainingPlan().getId();
        Set<String> recentIds = recent.stream()
            .map(PlannedWorkout::getId)
            .map(UUID::toString)
            .collect(Collectors.toSet());
        Set<String> uniqueRecentRescheduledIds = adaptationDecisionRepository.findByTrainingPlan_IdAndTriggerTypeOrderByCreatedAtAsc(
                planId,
                AdaptationTriggerType.RESCHEDULE
            )
            .stream()
            .map(AdaptationDecision::getChangedWorkoutIds)
            .filter(ids -> ids != null && !ids.isEmpty())
            .flatMap(List::stream)
            .filter(recentIds::contains)
            .collect(Collectors.toSet());
        return uniqueRecentRescheduledIds.size();
    }
}
