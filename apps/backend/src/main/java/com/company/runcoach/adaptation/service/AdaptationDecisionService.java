package com.company.runcoach.adaptation.service;

import com.company.runcoach.adaptation.domain.AdaptationDecision;
import com.company.runcoach.adaptation.domain.AdaptationReason;
import com.company.runcoach.adaptation.domain.AdaptationReasonCode;
import com.company.runcoach.adaptation.domain.AdaptationTriggerType;
import com.company.runcoach.adaptation.domain.FatigueSignal;
import com.company.runcoach.adaptation.domain.InjuryFeedback;
import com.company.runcoach.adaptation.domain.ReadinessState;
import com.company.runcoach.adaptation.repo.AdaptationDecisionRepository;
import com.company.runcoach.common.api.ApiErrorDetail;
import com.company.runcoach.common.api.ApiException;
import com.company.runcoach.planning.api.PlannedWorkoutMutationResponse;
import com.company.runcoach.planning.domain.PlanStatus;
import com.company.runcoach.planning.domain.PlannedWorkout;
import com.company.runcoach.planning.domain.PlannedWorkoutStatus;
import com.company.runcoach.planning.domain.PlannedWorkoutType;
import com.company.runcoach.planning.domain.TrainingPlan;
import com.company.runcoach.planning.repo.PlannedWorkoutRepository;
import com.company.runcoach.planning.repo.TrainingPlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AdaptationDecisionService {

    private static final Set<PlannedWorkoutType> HARD_TYPES = Set.of(
        PlannedWorkoutType.INTERVALS,
        PlannedWorkoutType.TEMPO_RUN,
        PlannedWorkoutType.LONG_RUN
    );

    private final PlannedWorkoutRepository plannedWorkoutRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final AdaptationDecisionRepository adaptationDecisionRepository;

    public AdaptationDecisionService(
        PlannedWorkoutRepository plannedWorkoutRepository,
        TrainingPlanRepository trainingPlanRepository,
        AdaptationDecisionRepository adaptationDecisionRepository
    ) {
        this.plannedWorkoutRepository = plannedWorkoutRepository;
        this.trainingPlanRepository = trainingPlanRepository;
        this.adaptationDecisionRepository = adaptationDecisionRepository;
    }

    @Transactional
    public PlannedWorkoutMutationResponse skipWorkout(UUID userId, UUID plannedWorkoutId, AdaptationReason reason, Integer expectedPlanVersion) {
        PlannedWorkout workout = getOwnedWorkout(userId, plannedWorkoutId);
        TrainingPlan activePlan = getActivePlan(userId);
        ensureWorkoutInActivePlan(workout, activePlan);
        ensureWorkoutMutableForMutation(workout, "skip");
        enforceExpectedVersion(activePlan, expectedPlanVersion);

        int planVersionBefore = activePlan.getPlanVersion();
        LocalDate fromDate = workout.getScheduledDate();
        LocalDate toDate = fromDate.plusDays(14);
        AdaptationReason normalizedReason = normalizeReason(reason);
        Set<AdaptationReasonCode> reasonCodes = reasonCodesForSkip(workout, normalizedReason);

        List<PlannedWorkout> impacted = plannedWorkoutRepository
            .findByTrainingPlan_IdAndScheduledDateBetweenOrderByScheduledDateAsc(activePlan.getId(), fromDate, toDate);

        Map<String, Object> beforeState = snapshot(impacted);
        workout.setStatus(PlannedWorkoutStatus.SKIPPED);
        applyConservativeAdaptation(
            impacted,
            workout,
            shouldDownshiftForReason(normalizedReason),
            priorLongRunDistance(activePlan.getId(), fromDate)
        );

        int nextVersion = incrementPlanVersion(activePlan);
        List<UUID> changedIds = persistWorkoutChanges(impacted, workout.getId(), nextVersion, reasonCodes);

        AdaptationDecision decision = persistDecision(
            activePlan,
            planVersionBefore,
            nextVersion,
            AdaptationTriggerType.SKIP,
            workout.getId(),
            normalizedReason,
            "NEAR_TERM_REGENERATION",
            "WEEK",
            BigDecimal.valueOf(0.91),
            reasonCodes,
            fromDate,
            toDate,
            changedIds,
            "Your next 14 days were adjusted after skipping a workout.",
            beforeState,
            snapshot(impacted)
        );

        return toResponse(nextVersion, decision, changedIds);
    }

    @Transactional
    public PlannedWorkoutMutationResponse rescheduleWorkout(UUID userId, UUID plannedWorkoutId, LocalDate targetDate, Integer expectedPlanVersion) {
        PlannedWorkout workout = getOwnedWorkout(userId, plannedWorkoutId);
        TrainingPlan activePlan = getActivePlan(userId);
        ensureWorkoutInActivePlan(workout, activePlan);
        ensureWorkoutMutableForMutation(workout, "reschedule");
        enforceExpectedVersion(activePlan, expectedPlanVersion);
        validateRescheduleTarget(workout, targetDate, activePlan);

        int planVersionBefore = activePlan.getPlanVersion();
        LocalDate fromDate = workout.getScheduledDate().isBefore(targetDate) ? workout.getScheduledDate() : targetDate;
        LocalDate toDate = fromDate.plusDays(14);
        Set<AdaptationReasonCode> reasonCodes = reasonCodesForReschedule(workout);

        List<PlannedWorkout> impacted = plannedWorkoutRepository
            .findByTrainingPlan_IdAndScheduledDateBetweenOrderByScheduledDateAsc(activePlan.getId(), fromDate, toDate);

        Map<String, Object> beforeState = snapshot(impacted);
        workout.setScheduledDate(targetDate);
        applyConservativeAdaptation(
            impacted,
            workout,
            false,
            priorLongRunDistance(activePlan.getId(), fromDate)
        );

        int nextVersion = incrementPlanVersion(activePlan);
        List<UUID> changedIds = persistWorkoutChanges(impacted, workout.getId(), nextVersion, reasonCodes);

        AdaptationDecision decision = persistDecision(
            activePlan,
            planVersionBefore,
            nextVersion,
            AdaptationTriggerType.RESCHEDULE,
            workout.getId(),
            AdaptationReason.NO_TIME,
            "NEAR_TERM_REGENERATION",
            "WEEK",
            BigDecimal.valueOf(0.89),
            reasonCodes,
            fromDate,
            toDate,
            changedIds,
            "Your near-term workouts were adjusted after rescheduling.",
            beforeState,
            snapshot(impacted)
        );

        return toResponse(nextVersion, decision, changedIds);
    }

    @Transactional
    public AdaptationDecision adaptFromWorkoutCompletion(
        UUID userId,
        UUID plannedWorkoutId,
        BigDecimal actualDistanceKm,
        Integer actualDurationMin
    ) {
        PlannedWorkout workout = getOwnedWorkout(userId, plannedWorkoutId);
        TrainingPlan activePlan = getActivePlan(userId);
        ensureWorkoutInActivePlan(workout, activePlan);

        workout.setStatus(PlannedWorkoutStatus.COMPLETED);
        CompletionAssessment completion = assessCompletion(workout, actualDistanceKm, actualDurationMin);
        plannedWorkoutRepository.save(workout);

        if (completion.type == CompletionType.NORMAL) {
            return null;
        }

        int planVersionBefore = activePlan.getPlanVersion();
        LocalDate fromDate = workout.getScheduledDate();
        LocalDate toDate = fromDate.plusDays(7);
        List<PlannedWorkout> impacted = plannedWorkoutRepository
            .findByTrainingPlan_IdAndScheduledDateBetweenOrderByScheduledDateAsc(activePlan.getId(), fromDate, toDate);
        if (impacted.isEmpty()) {
            return null;
        }

        Map<String, Object> beforeState = snapshot(impacted);
        applyConservativeAdaptation(
            impacted,
            workout,
            true,
            priorLongRunDistance(activePlan.getId(), fromDate)
        );

        int nextVersion = incrementPlanVersion(activePlan);
        List<UUID> changedIds = persistWorkoutChanges(impacted, workout.getId(), nextVersion, completion.reasonCodes);

        return persistDecision(
            activePlan,
            planVersionBefore,
            nextVersion,
            completion.type == CompletionType.PARTIAL
                ? AdaptationTriggerType.PARTIAL_COMPLETION
                : AdaptationTriggerType.OVERDONE_WORKOUT,
            workout.getId(),
            AdaptationReason.OTHER,
            "NEAR_TERM_REGENERATION",
            "WEEK",
            completion.type == CompletionType.PARTIAL ? BigDecimal.valueOf(0.86) : BigDecimal.valueOf(0.9),
            completion.reasonCodes,
            fromDate,
            toDate,
            changedIds,
            completion.type == CompletionType.PARTIAL
                ? "Your near-term plan was softened after an under-completed workout."
                : "Your near-term plan was softened to protect recovery after an overdone workout.",
            beforeState,
            snapshot(impacted)
        );
    }

    @Transactional
    public AdaptationDecision adaptFromFatigueSignal(UUID userId, FatigueSignal signal, ReadinessState readinessState) {
        if (readinessState == ReadinessState.READY) {
            return null;
        }

        TrainingPlan activePlan = findActivePlan(userId);
        if (activePlan == null) {
            return null;
        }
        int planVersionBefore = activePlan.getPlanVersion();
        LocalDate fromDate = signal.getSignalDate();
        LocalDate toDate = readinessState == ReadinessState.HIGH_RISK ? fromDate.plusDays(14) : fromDate.plusDays(7);

        List<PlannedWorkout> impacted = plannedWorkoutRepository
            .findByTrainingPlan_IdAndScheduledDateBetweenOrderByScheduledDateAsc(activePlan.getId(), fromDate, toDate);
        if (impacted.isEmpty()) {
            return null;
        }

        Set<AdaptationReasonCode> reasonCodes = new LinkedHashSet<>();
        reasonCodes.add(AdaptationReasonCode.HIGH_FATIGUE_SCORE);
        reasonCodes.add(AdaptationReasonCode.PROTECT_CONSISTENCY);
        if (signal.isIllnessFlag()) {
            reasonCodes.add(AdaptationReasonCode.ILLNESS_FLAG);
            reasonCodes.add(AdaptationReasonCode.INSERT_RECOVERY_WEEK);
        }
        reasonCodes.add(AdaptationReasonCode.REDUCE_INTENSITY);

        Map<String, Object> beforeState = snapshot(impacted);
        applyConservativeAdaptation(
            impacted,
            null,
            true,
            priorLongRunDistance(activePlan.getId(), fromDate)
        );

        int nextVersion = incrementPlanVersion(activePlan);
        List<UUID> changedIds = persistWorkoutChanges(impacted, null, nextVersion, reasonCodes);

        return persistDecision(
            activePlan,
            planVersionBefore,
            nextVersion,
            AdaptationTriggerType.FATIGUE_SIGNAL,
            null,
            AdaptationReason.TOO_TIRED,
            readinessState == ReadinessState.HIGH_RISK ? "RECOVERY_WEEK_CONVERSION" : "DOWNSHIFT_WEEK",
            "WEEK",
            BigDecimal.valueOf(0.9),
            reasonCodes,
            fromDate,
            toDate,
            changedIds,
            "Your near-term training was softened due to readiness signals.",
            beforeState,
            snapshot(impacted)
        );
    }

    @Transactional
    public AdaptationDecision adaptFromInjuryFeedback(UUID userId, InjuryFeedback feedback, ReadinessState readinessState) {
        if (!feedback.isHasPain() && readinessState != ReadinessState.HIGH_RISK) {
            return null;
        }

        TrainingPlan activePlan = findActivePlan(userId);
        if (activePlan == null) {
            return null;
        }
        int planVersionBefore = activePlan.getPlanVersion();
        LocalDate fromDate = feedback.getReportedAt().toLocalDate();
        LocalDate toDate = fromDate.plusDays(14);

        List<PlannedWorkout> impacted = plannedWorkoutRepository
            .findByTrainingPlan_IdAndScheduledDateBetweenOrderByScheduledDateAsc(activePlan.getId(), fromDate, toDate);
        if (impacted.isEmpty()) {
            return null;
        }

        Set<AdaptationReasonCode> reasonCodes = new LinkedHashSet<>();
        reasonCodes.add(AdaptationReasonCode.PAIN_SIGNAL_PRESENT);
        reasonCodes.add(AdaptationReasonCode.REDUCE_INTENSITY);
        reasonCodes.add(AdaptationReasonCode.PROTECT_CONSISTENCY);
        if ("SHARP".equals(feedback.getPainType()) && "DURING_RUN".equals(feedback.getOnsetContext())) {
            reasonCodes.add(AdaptationReasonCode.SHARP_LOCALIZED_PAIN);
        }
        if (feedback.getSeverity() != null && feedback.getSeverity() >= 7) {
            reasonCodes.add(AdaptationReasonCode.INSERT_RECOVERY_WEEK);
        }

        Map<String, Object> beforeState = snapshot(impacted);
        applyConservativeAdaptation(
            impacted,
            null,
            true,
            priorLongRunDistance(activePlan.getId(), fromDate)
        );

        int nextVersion = incrementPlanVersion(activePlan);
        List<UUID> changedIds = persistWorkoutChanges(impacted, null, nextVersion, reasonCodes);

        return persistDecision(
            activePlan,
            planVersionBefore,
            nextVersion,
            AdaptationTriggerType.PAIN_FEEDBACK,
            null,
            AdaptationReason.PAIN,
            "RECOVERY_WEEK_CONVERSION",
            "WEEK",
            BigDecimal.valueOf(0.95),
            reasonCodes,
            fromDate,
            toDate,
            changedIds,
            "Intensity was removed to protect recovery after pain feedback.",
            beforeState,
            snapshot(impacted)
        );
    }

    private PlannedWorkout getOwnedWorkout(UUID userId, UUID plannedWorkoutId) {
        return plannedWorkoutRepository.findByIdAndUser_Id(plannedWorkoutId, userId)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "Planned workout not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("plannedWorkoutId", "not_found"))));
    }

    private TrainingPlan getActivePlan(UUID userId) {
        return trainingPlanRepository.findFirstByUser_IdAndStatusInOrderByCreatedAtDesc(userId, List.of(PlanStatus.ACTIVE, PlanStatus.GENERATED))
            .orElseThrow(() -> new ApiException("NOT_FOUND", "Active training plan not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("plan", "not_found"))));
    }

    private TrainingPlan findActivePlan(UUID userId) {
        return trainingPlanRepository.findFirstByUser_IdAndStatusInOrderByCreatedAtDesc(userId, List.of(PlanStatus.ACTIVE, PlanStatus.GENERATED))
            .orElse(null);
    }

    private void ensureWorkoutInActivePlan(PlannedWorkout workout, TrainingPlan activePlan) {
        if (!workout.getTrainingPlan().getId().equals(activePlan.getId())) {
            throw new ApiException("VALIDATION_ERROR", "Workout is not in the active training plan.", HttpStatus.BAD_REQUEST,
                List.of(new ApiErrorDetail("plannedWorkoutId", "outside_active_plan")));
        }
    }

    private void enforceExpectedVersion(TrainingPlan plan, Integer expectedPlanVersion) {
        if (plan.getPlanVersion() != expectedPlanVersion) {
            throw new ApiException(
                "STALE_PLAN_VERSION",
                "Your plan changed recently. Refresh before making this change.",
                HttpStatus.CONFLICT,
                List.of(new ApiErrorDetail("expectedPlanVersion", "stale"))
            );
        }
    }

    private void ensureWorkoutMutableForMutation(PlannedWorkout workout, String operation) {
        if (workout.getStatus() != PlannedWorkoutStatus.PLANNED) {
            throw new ApiException(
                "VALIDATION_ERROR",
                "Validation failed.",
                HttpStatus.BAD_REQUEST,
                List.of(new ApiErrorDetail("plannedWorkoutId", operation + "_not_allowed_for_status"))
            );
        }
    }

    private void validateRescheduleTarget(PlannedWorkout workout, LocalDate targetDate, TrainingPlan activePlan) {
        if (targetDate == null) {
            throw new ApiException("VALIDATION_ERROR", "Validation failed.", HttpStatus.BAD_REQUEST,
                List.of(new ApiErrorDetail("targetDate", "required")));
        }
        if (targetDate.isBefore(activePlan.getStartDate()) || targetDate.isAfter(activePlan.getEndDate())) {
            throw new ApiException("VALIDATION_ERROR", "Validation failed.", HttpStatus.BAD_REQUEST,
                List.of(new ApiErrorDetail("targetDate", "outside_plan_dates")));
        }
        if (Math.abs(targetDate.toEpochDay() - workout.getScheduledDate().toEpochDay()) > 1) {
            throw new ApiException("VALIDATION_ERROR", "Validation failed.", HttpStatus.BAD_REQUEST,
                List.of(new ApiErrorDetail("targetDate", "move_window_exceeded")));
        }

        List<PlannedWorkout> allWorkouts = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(activePlan.getId());
        for (PlannedWorkout candidate : allWorkouts) {
            if (candidate.getId().equals(workout.getId())) {
                continue;
            }
            long dayDiff = Math.abs(candidate.getScheduledDate().toEpochDay() - targetDate.toEpochDay());
            if (dayDiff <= 1
                && isHard(candidate)
                && isHard(workout)
                && workout.getWorkoutType() != PlannedWorkoutType.LONG_RUN
                && candidate.getWorkoutType() != PlannedWorkoutType.LONG_RUN) {
                throw new ApiException("VALIDATION_ERROR", "Validation failed.", HttpStatus.BAD_REQUEST,
                    List.of(new ApiErrorDetail("targetDate", "unsafe_quality_spacing")));
            }
        }

        if (workout.getWorkoutType() == PlannedWorkoutType.LONG_RUN) {
            for (PlannedWorkout candidate : allWorkouts) {
                if (candidate.getId().equals(workout.getId())) {
                    continue;
                }
                if (candidate.getScheduledDate().equals(targetDate) && candidate.getWorkoutType() != PlannedWorkoutType.REST) {
                    throw new ApiException("VALIDATION_ERROR", "Validation failed.", HttpStatus.BAD_REQUEST,
                        List.of(new ApiErrorDetail("targetDate", "long_run_day_not_free")));
                }
                if (Math.abs(candidate.getScheduledDate().toEpochDay() - targetDate.toEpochDay()) <= 1
                    && (candidate.getWorkoutType() == PlannedWorkoutType.INTERVALS
                    || candidate.getWorkoutType() == PlannedWorkoutType.TEMPO_RUN)) {
                    throw new ApiException("VALIDATION_ERROR", "Validation failed.", HttpStatus.BAD_REQUEST,
                        List.of(new ApiErrorDetail("targetDate", "unsafe_long_run_spacing")));
                }
            }
        }
    }

    private Set<AdaptationReasonCode> reasonCodesForSkip(PlannedWorkout workout, AdaptationReason reason) {
        Set<AdaptationReasonCode> codes = new LinkedHashSet<>();
        if (workout.getWorkoutType() == PlannedWorkoutType.LONG_RUN) {
            codes.add(AdaptationReasonCode.MISSED_LONG_RUN);
        } else if (workout.getWorkoutType() == PlannedWorkoutType.INTERVALS || workout.getWorkoutType() == PlannedWorkoutType.TEMPO_RUN) {
            codes.add(AdaptationReasonCode.MISSED_QUALITY_RUN);
        } else {
            codes.add(AdaptationReasonCode.MISSED_EASY_RUN);
        }
        codes.add(AdaptationReasonCode.PROTECT_CONSISTENCY);
        if (shouldDownshiftForReason(reason)) {
            codes.add(AdaptationReasonCode.REDUCE_INTENSITY);
            if (reason == AdaptationReason.PAIN) {
                codes.add(AdaptationReasonCode.PAIN_SIGNAL_PRESENT);
            }
        }
        return codes;
    }

    private Set<AdaptationReasonCode> reasonCodesForReschedule(PlannedWorkout workout) {
        Set<AdaptationReasonCode> codes = new LinkedHashSet<>();
        codes.add(AdaptationReasonCode.PROTECT_CONSISTENCY);
        return codes;
    }

    private CompletionAssessment assessCompletion(
        PlannedWorkout workout,
        BigDecimal actualDistanceKm,
        Integer actualDurationMin
    ) {
        BigDecimal ratio = completionRatio(workout, actualDistanceKm, actualDurationMin);
        if (ratio == null) {
            return CompletionAssessment.normal();
        }
        if (ratio.compareTo(BigDecimal.valueOf(0.8)) < 0) {
            return CompletionAssessment.partial();
        }
        BigDecimal overdoneThreshold = isQualityWorkout(workout) ? BigDecimal.valueOf(1.15) : BigDecimal.valueOf(1.20);
        if (ratio.compareTo(overdoneThreshold) > 0) {
            return CompletionAssessment.overdone();
        }
        return CompletionAssessment.normal();
    }

    private BigDecimal completionRatio(
        PlannedWorkout workout,
        BigDecimal actualDistanceKm,
        Integer actualDurationMin
    ) {
        if (workout.getPlannedDistanceKm() != null && actualDistanceKm != null && workout.getPlannedDistanceKm().signum() > 0) {
            return actualDistanceKm.divide(workout.getPlannedDistanceKm(), 3, java.math.RoundingMode.HALF_UP);
        }
        if (workout.getPlannedDurationMin() != null && actualDurationMin != null && workout.getPlannedDurationMin() > 0) {
            return BigDecimal.valueOf(actualDurationMin)
                .divide(BigDecimal.valueOf(workout.getPlannedDurationMin()), 3, java.math.RoundingMode.HALF_UP);
        }
        return null;
    }

    private boolean isQualityWorkout(PlannedWorkout workout) {
        return workout.getWorkoutType() == PlannedWorkoutType.INTERVALS
            || workout.getWorkoutType() == PlannedWorkoutType.TEMPO_RUN;
    }

    private boolean shouldDownshiftForReason(AdaptationReason reason) {
        return reason == AdaptationReason.PAIN || reason == AdaptationReason.TOO_TIRED;
    }

    private int incrementPlanVersion(TrainingPlan activePlan) {
        int nextVersion = activePlan.getPlanVersion() + 1;
        activePlan.setPlanVersion(nextVersion);
        activePlan.setLastRegeneratedAt(OffsetDateTime.now());
        activePlan.setUpdatedAt(OffsetDateTime.now());
        trainingPlanRepository.save(activePlan);
        return nextVersion;
    }

    private void applyConservativeAdaptation(
        List<PlannedWorkout> workouts,
        PlannedWorkout trigger,
        boolean downshiftIntensity,
        BigDecimal priorLongRunDistance
    ) {
        workouts.sort(Comparator.comparing(PlannedWorkout::getScheduledDate));
        for (int i = 0; i < workouts.size(); i++) {
            PlannedWorkout current = workouts.get(i);
            if (trigger != null && current.getId().equals(trigger.getId())) {
                continue;
            }
            if (!isAdaptableWorkout(current)) {
                continue;
            }

            if (downshiftIntensity) {
                softenIntensity(current);
            }

            if (i > 0 && isHard(current) && isHard(workouts.get(i - 1))) {
                softenIntensity(current);
            }
            if (i + 1 < workouts.size() && isHard(current) && isHard(workouts.get(i + 1))) {
                softenIntensity(current);
            }
        }
        enforceLongRunSafety(workouts, priorLongRunDistance);
    }

    private void enforceLongRunSafety(List<PlannedWorkout> workouts, BigDecimal priorLongRunDistance) {
        BigDecimal previousLongRun = priorLongRunDistance;
        for (PlannedWorkout workout : workouts) {
            if (workout.getWorkoutType() != PlannedWorkoutType.LONG_RUN || workout.getPlannedDistanceKm() == null) {
                continue;
            }
            if (!isAdaptableWorkout(workout)) {
                continue;
            }
            if (previousLongRun != null) {
                BigDecimal maxAllowed = previousLongRun.multiply(BigDecimal.valueOf(1.10));
                if (workout.getPlannedDistanceKm().compareTo(maxAllowed) > 0) {
                    workout.setPlannedDistanceKm(maxAllowed);
                }
            }
            previousLongRun = workout.getPlannedDistanceKm();
        }
    }

    private BigDecimal priorLongRunDistance(UUID trainingPlanId, LocalDate windowStartDate) {
        List<PlannedWorkout> workouts = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(trainingPlanId);
        BigDecimal latest = null;
        for (PlannedWorkout workout : workouts) {
            if (!workout.getScheduledDate().isBefore(windowStartDate)) {
                break;
            }
            if (workout.getWorkoutType() == PlannedWorkoutType.LONG_RUN && workout.getPlannedDistanceKm() != null) {
                latest = workout.getPlannedDistanceKm();
            }
        }
        return latest;
    }

    private void softenIntensity(PlannedWorkout workout) {
        if (workout.getWorkoutType() == PlannedWorkoutType.INTERVALS || workout.getWorkoutType() == PlannedWorkoutType.TEMPO_RUN) {
            workout.setWorkoutType(PlannedWorkoutType.EASY_RUN);
            workout.setWorkoutSubtype("ADAPTED_EASY");
            workout.setIntensityZone("EASY");
        }
        if (workout.getPlannedDurationMin() != null) {
            int reduced = Math.max(20, (int) Math.floor(workout.getPlannedDurationMin() * 0.85));
            workout.setPlannedDurationMin(Math.min(reduced, workout.getPlannedDurationMin()));
        }
        if (workout.getPlannedDistanceKm() != null) {
            BigDecimal reduced = workout.getPlannedDistanceKm().multiply(BigDecimal.valueOf(0.85));
            if (reduced.compareTo(workout.getPlannedDistanceKm()) < 0) {
                workout.setPlannedDistanceKm(reduced.max(BigDecimal.valueOf(2)));
            }
        }
    }

    private boolean isHard(PlannedWorkout workout) {
        return HARD_TYPES.contains(workout.getWorkoutType());
    }

    private List<UUID> persistWorkoutChanges(
        List<PlannedWorkout> impacted,
        UUID triggerWorkoutId,
        int nextPlanVersion,
        Set<AdaptationReasonCode> reasonCodes
    ) {
        List<UUID> changed = new ArrayList<>();
        for (PlannedWorkout workout : impacted) {
            if (!shouldPersistWorkoutChange(workout, triggerWorkoutId)) {
                continue;
            }
            workout.setPlanVersion(nextPlanVersion);
            if (triggerWorkoutId != null && !workout.getId().equals(triggerWorkoutId)) {
                workout.setAdaptedFromWorkoutId(triggerWorkoutId);
            }
            addChangeReasonCodes(workout, reasonCodes);
            plannedWorkoutRepository.save(workout);
            changed.add(workout.getId());
        }
        return changed;
    }

    private boolean isAdaptableWorkout(PlannedWorkout workout) {
        return workout.getStatus() == PlannedWorkoutStatus.PLANNED;
    }

    private boolean shouldPersistWorkoutChange(PlannedWorkout workout, UUID triggerWorkoutId) {
        if (triggerWorkoutId != null && workout.getId().equals(triggerWorkoutId)) {
            return true;
        }
        return workout.getStatus() == PlannedWorkoutStatus.PLANNED;
    }

    @SuppressWarnings("unchecked")
    private void addChangeReasonCodes(PlannedWorkout workout, Set<AdaptationReasonCode> reasonCodes) {
        Map<String, Object> rationale = workout.getRationale() != null ? new HashMap<>(workout.getRationale()) : new HashMap<>();
        List<String> existing = rationale.get("changeReasonCodes") instanceof List<?> list
            ? new ArrayList<>((List<String>) list)
            : new ArrayList<>();
        for (AdaptationReasonCode code : reasonCodes) {
            if (!existing.contains(code.name())) {
                existing.add(code.name());
            }
        }
        rationale.put("changeReasonCodes", existing);
        workout.setRationale(rationale);
    }

    private AdaptationReason normalizeReason(AdaptationReason reason) {
        if (reason == AdaptationReason.PAIN_DISCOMFORT) {
            return AdaptationReason.PAIN;
        }
        return reason;
    }

    private Map<String, Object> snapshot(List<PlannedWorkout> workouts) {
        List<Map<String, Object>> items = workouts.stream()
            .map(w -> {
                Map<String, Object> item = new HashMap<>();
                item.put("plannedWorkoutId", w.getId().toString());
                item.put("scheduledDate", w.getScheduledDate().toString());
                item.put("workoutType", w.getWorkoutType().name());
                item.put("plannedDistanceKm", w.getPlannedDistanceKm());
                item.put("plannedDurationMin", w.getPlannedDurationMin());
                item.put("intensityZone", w.getIntensityZone());
                item.put("status", w.getStatus().name());
                item.put("planVersion", w.getPlanVersion());
                return item;
            })
            .toList();
        Map<String, Object> payload = new HashMap<>();
        payload.put("workouts", items);
        return payload;
    }

    private AdaptationDecision persistDecision(
        TrainingPlan plan,
        int beforeVersion,
        int afterVersion,
        AdaptationTriggerType triggerType,
        UUID triggerWorkoutId,
        AdaptationReason reason,
        String decisionType,
        String decisionScope,
        BigDecimal confidence,
        Set<AdaptationReasonCode> reasonCodes,
        LocalDate fromDate,
        LocalDate toDate,
        List<UUID> changedIds,
        String summary,
        Map<String, Object> beforeState,
        Map<String, Object> afterState
    ) {
        AdaptationDecision decision = new AdaptationDecision();
        decision.setId(UUID.randomUUID());
        decision.setUser(plan.getUser());
        decision.setTrainingPlan(plan);
        decision.setPlanVersionBefore(beforeVersion);
        decision.setPlanVersionAfter(afterVersion);
        decision.setTriggerType(triggerType);
        decision.setTriggerWorkoutId(triggerWorkoutId);
        decision.setReason(reason);
        decision.setDecisionType(decisionType);
        decision.setDecisionScope(decisionScope);
        decision.setConfidence(confidence);
        decision.setReasonCodes(reasonCodes.stream().map(Enum::name).toList());
        decision.setAffectedFromDate(fromDate);
        decision.setAffectedToDate(toDate);
        decision.setDecisionSummary(summary);
        decision.setChangedWorkoutIds(changedIds.stream().distinct().map(UUID::toString).toList());
        decision.setBeforeState(beforeState);
        decision.setAfterState(afterState);
        decision.setCreatedAt(OffsetDateTime.now());
        return adaptationDecisionRepository.save(decision);
    }

    private PlannedWorkoutMutationResponse toResponse(int planVersion, AdaptationDecision decision, List<UUID> changedIds) {
        return new PlannedWorkoutMutationResponse(
            planVersion,
            new PlannedWorkoutMutationResponse.AdaptationSummary(
                decision.getId(),
                decision.getDecisionSummary(),
                decision.getAffectedFromDate(),
                decision.getAffectedToDate(),
                changedIds.stream().distinct().toList()
            )
        );
    }

    private enum CompletionType {
        NORMAL,
        PARTIAL,
        OVERDONE
    }

    private record CompletionAssessment(
        CompletionType type,
        Set<AdaptationReasonCode> reasonCodes
    ) {
        private static CompletionAssessment normal() {
            return new CompletionAssessment(CompletionType.NORMAL, Set.of());
        }

        private static CompletionAssessment partial() {
            return new CompletionAssessment(
                CompletionType.PARTIAL,
                Set.of(
                    AdaptationReasonCode.WORKOUT_UNDER_COMPLETED,
                    AdaptationReasonCode.PROTECT_CONSISTENCY,
                    AdaptationReasonCode.REDUCE_INTENSITY
                )
            );
        }

        private static CompletionAssessment overdone() {
            return new CompletionAssessment(
                CompletionType.OVERDONE,
                Set.of(
                    AdaptationReasonCode.WORKOUT_OVERDONE,
                    AdaptationReasonCode.PROTECT_CONSISTENCY,
                    AdaptationReasonCode.REDUCE_INTENSITY
                )
            );
        }
    }
}
