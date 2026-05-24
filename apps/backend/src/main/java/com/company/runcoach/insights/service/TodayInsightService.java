package com.company.runcoach.insights.service;

import com.company.runcoach.adaptation.domain.AdaptationDecision;
import com.company.runcoach.adaptation.domain.FatigueSignal;
import com.company.runcoach.adaptation.domain.InjuryFeedback;
import com.company.runcoach.adaptation.domain.ReadinessState;
import com.company.runcoach.adaptation.repo.AdaptationDecisionRepository;
import com.company.runcoach.adaptation.repo.FatigueSignalRepository;
import com.company.runcoach.adaptation.repo.InjuryFeedbackRepository;
import com.company.runcoach.adaptation.service.ReadinessService;
import com.company.runcoach.common.api.ApiErrorDetail;
import com.company.runcoach.common.api.ApiException;
import com.company.runcoach.identity.domain.AppUser;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.insights.api.TodayInsightResponse;
import com.company.runcoach.planning.domain.PlanStatus;
import com.company.runcoach.planning.repo.TrainingPlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TodayInsightService {

    private final FatigueSignalRepository fatigueSignalRepository;
    private final InjuryFeedbackRepository injuryFeedbackRepository;
    private final AppUserRepository appUserRepository;
    private final ReadinessService readinessService;
    private final TrainingPlanRepository trainingPlanRepository;
    private final AdaptationDecisionRepository adaptationDecisionRepository;

    public TodayInsightService(
        FatigueSignalRepository fatigueSignalRepository,
        InjuryFeedbackRepository injuryFeedbackRepository,
        AppUserRepository appUserRepository,
        ReadinessService readinessService,
        TrainingPlanRepository trainingPlanRepository,
        AdaptationDecisionRepository adaptationDecisionRepository
    ) {
        this.fatigueSignalRepository = fatigueSignalRepository;
        this.injuryFeedbackRepository = injuryFeedbackRepository;
        this.appUserRepository = appUserRepository;
        this.readinessService = readinessService;
        this.trainingPlanRepository = trainingPlanRepository;
        this.adaptationDecisionRepository = adaptationDecisionRepository;
    }

    @Transactional(readOnly = true)
    public TodayInsightResponse getToday(UUID userId) {
        AppUser user = appUserRepository.findById(userId)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "User not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("userId", "not_found"))));
        ZoneId userZone = ZoneId.of(user.getTimezone());
        LocalDate today = LocalDate.now(userZone);
        ZonedDateTime startOfToday = today.atStartOfDay(userZone);
        OffsetDateTime startOfTodayUtc = startOfToday.withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime endOfTodayUtc = startOfToday.plusDays(1).minusSeconds(1).withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();

        FatigueSignal fatigueSignal = fatigueSignalRepository
            .findFirstByUser_IdAndSignalDateOrderByCreatedAtDesc(userId, today)
            .orElse(null);
        InjuryFeedback injuryFeedback = injuryFeedbackRepository
            .findFirstByUser_IdAndReportedAtBetweenOrderByReportedAtDescCreatedAtDesc(userId, startOfTodayUtc, endOfTodayUtc)
            .orElse(null);

        boolean hasCheckInToday = fatigueSignal != null || injuryFeedback != null;
        ReadinessState readinessState = hasCheckInToday
            ? readinessService.evaluate(fatigueSignal, injuryFeedback)
            : ReadinessState.READY;

        TodayInsightResponse.LatestAdaptationSummary latestAdaptation = trainingPlanRepository
            .findFirstByUser_IdAndStatusInOrderByCreatedAtDesc(userId, List.of(PlanStatus.ACTIVE, PlanStatus.GENERATED))
            .flatMap(plan -> adaptationDecisionRepository.findFirstByTrainingPlan_IdOrderByCreatedAtDesc(plan.getId()))
            .map(this::toLatestAdaptationSummary)
            .orElse(null);

        return new TodayInsightResponse(
            today,
            readinessState,
            label(readinessState),
            message(readinessState),
            toFatigueSummary(fatigueSignal),
            toInjurySummary(injuryFeedback),
            hasCheckInToday,
            tone(readinessState),
            latestAdaptation
        );
    }

    private TodayInsightResponse.FatigueSignalSummary toFatigueSummary(FatigueSignal signal) {
        if (signal == null) {
            return null;
        }
        return new TodayInsightResponse.FatigueSignalSummary(
            signal.getSignalDate(),
            signal.getSleepScore(),
            signal.getStressScore(),
            signal.getSorenessScore(),
            signal.getMotivationScore(),
            signal.isIllnessFlag(),
            signal.isTooBusyFlag(),
            signal.isTravellingFlag(),
            signal.getNotes()
        );
    }

    private TodayInsightResponse.InjuryFeedbackSummary toInjurySummary(InjuryFeedback feedback) {
        if (feedback == null) {
            return null;
        }
        return new TodayInsightResponse.InjuryFeedbackSummary(
            feedback.getReportedAt().toString(),
            feedback.isHasPain(),
            feedback.getBodyRegion(),
            feedback.getPainType(),
            feedback.getSeverity(),
            feedback.getOnsetContext(),
            feedback.getCanRun(),
            feedback.isRedFlag(),
            feedback.getFreeText()
        );
    }

    private String label(ReadinessState state) {
        return switch (state) {
            case READY -> "Ready";
            case CAUTION -> "Caution";
            case HIGH_RISK -> "High risk";
        };
    }

    private String message(ReadinessState state) {
        return switch (state) {
            case READY -> "No concerning readiness signals were detected today.";
            case CAUTION -> "Some readiness signals suggest a conservative effort today.";
            case HIGH_RISK -> "Risk signals are elevated. Keep training load conservative today.";
        };
    }

    private String tone(ReadinessState state) {
        return switch (state) {
            case READY -> "steady";
            case CAUTION -> "supportive";
            case HIGH_RISK -> "protective";
        };
    }

    private TodayInsightResponse.LatestAdaptationSummary toLatestAdaptationSummary(AdaptationDecision decision) {
        return new TodayInsightResponse.LatestAdaptationSummary(
            decision.getId().toString(),
            decision.getDecisionSummary(),
            decision.getAffectedFromDate(),
            decision.getAffectedToDate(),
            decision.getChangedWorkoutIds()
        );
    }
}
