package com.company.runcoach.planning.service;

import com.company.runcoach.common.api.ApiErrorDetail;
import com.company.runcoach.common.api.ApiException;
import com.company.runcoach.goals.domain.RaceDistanceType;
import com.company.runcoach.goals.domain.RaceGoal;
import com.company.runcoach.goals.domain.RaceGoalStatus;
import com.company.runcoach.goals.repo.RaceGoalRepository;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.planning.api.CurrentTrainingPlanResponse;
import com.company.runcoach.planning.api.GeneratePlanRequest;
import com.company.runcoach.planning.api.GeneratePlanResponse;
import com.company.runcoach.planning.api.PlanByIdResponse;
import com.company.runcoach.planning.api.PlannedWorkoutDetailResponse;
import com.company.runcoach.planning.domain.PlanStatus;
import com.company.runcoach.planning.domain.PlannedWorkout;
import com.company.runcoach.planning.domain.PlannedWorkoutStatus;
import com.company.runcoach.planning.domain.TrainingPlan;
import com.company.runcoach.planning.domain.TrainingPlanWeek;
import com.company.runcoach.planning.engine.GeneratedPlanDraft;
import com.company.runcoach.planning.repo.PlannedWorkoutRepository;
import com.company.runcoach.planning.repo.TrainingPlanRepository;
import com.company.runcoach.planning.repo.TrainingPlanWeekRepository;
import com.company.runcoach.profile.domain.RunnerProfile;
import com.company.runcoach.profile.repo.RunnerProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingPlanWeekRepository trainingPlanWeekRepository;
    private final PlannedWorkoutRepository plannedWorkoutRepository;
    private final RunnerProfileRepository runnerProfileRepository;
    private final RaceGoalRepository raceGoalRepository;
    private final AppUserRepository appUserRepository;
    private final PlanGenerationService planGenerationService;

    public TrainingPlanService(
        TrainingPlanRepository trainingPlanRepository,
        TrainingPlanWeekRepository trainingPlanWeekRepository,
        PlannedWorkoutRepository plannedWorkoutRepository,
        RunnerProfileRepository runnerProfileRepository,
        RaceGoalRepository raceGoalRepository,
        AppUserRepository appUserRepository,
        PlanGenerationService planGenerationService
    ) {
        this.trainingPlanRepository = trainingPlanRepository;
        this.trainingPlanWeekRepository = trainingPlanWeekRepository;
        this.plannedWorkoutRepository = plannedWorkoutRepository;
        this.runnerProfileRepository = runnerProfileRepository;
        this.raceGoalRepository = raceGoalRepository;
        this.appUserRepository = appUserRepository;
        this.planGenerationService = planGenerationService;
    }

    @Transactional
    public GeneratePlanResponse generatePlan(UUID userId, GeneratePlanRequest request) {
        appUserRepository.findById(userId)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "User not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("userId", "not_found"))));

        RunnerProfile profile = runnerProfileRepository.findByUser_Id(userId)
            .orElseThrow(() -> validation("profile", "missing", "Runner profile must exist before plan generation."));

        if (profile.getPreferredRunDays() == null || profile.getPreferredRunDays().isEmpty()) {
            throw validation("preferredRunDays", "empty", "Preferred run days cannot be empty.");
        }
        if (profile.getTypicalWeeklyDistanceKm().compareTo(BigDecimal.ZERO) < 0) {
            throw validation("typicalWeeklyDistanceKm", "negative", "Current weekly volume must be non-negative.");
        }

        RaceGoal raceGoal = raceGoalRepository.findById(request.raceGoalId())
            .orElseThrow(() -> validation("raceGoalId", "not_found", "Race goal not found."));

        if (!raceGoal.getUser().getId().equals(userId)) {
            throw validation("raceGoalId", "not_owned", "Race goal does not belong to current user.");
        }
        if (raceGoal.getStatus() != RaceGoalStatus.ACTIVE) {
            throw validation("raceGoalId", "inactive", "Race goal must be active.");
        }
        if (!(raceGoal.getRaceDistanceType() == RaceDistanceType.HALF_MARATHON
            || raceGoal.getRaceDistanceType() == RaceDistanceType.MARATHON)) {
            throw validation("raceDistanceType", "unsupported", "Race distance is not supported.");
        }

        validateRunDaysByDistance(profile.getPreferredRunDays().size(), raceGoal.getRaceDistanceType());

        LocalDate today = LocalDate.now(ZoneId.of("UTC"));
        if (!raceGoal.getRaceDate().isAfter(today)) {
            throw validation("raceDate", "not_future", "Race date must be in the future.");
        }
        validateRunway(today, raceGoal);

        LocalDate startDate = request.startDate() == null ? today : request.startDate();
        if (!startDate.isBefore(raceGoal.getRaceDate())) {
            throw validation("startDate", "after_race_date", "Start date must be before race date.");
        }

        boolean forceRegenerate = Boolean.TRUE.equals(request.forceRegenerate());
        var existing = trainingPlanRepository.findFirstByUser_IdAndRaceGoal_IdAndStatusInOrderByCreatedAtDesc(
            userId,
            raceGoal.getId(),
            List.of(PlanStatus.ACTIVE, PlanStatus.GENERATED)
        );

        if (existing.isPresent() && !forceRegenerate) {
            TrainingPlan plan = existing.get();
            return new GeneratePlanResponse(plan.getId(), plan.getPlanVersion(), plan.getStatus().name());
        }

        int nextPlanVersion = existing.map(p -> p.getPlanVersion() + 1).orElse(1);
        if (existing.isPresent() && forceRegenerate) {
            List<TrainingPlan> activePlans = trainingPlanRepository.findByUser_IdAndRaceGoal_IdAndStatusIn(
                userId,
                raceGoal.getId(),
                List.of(PlanStatus.ACTIVE, PlanStatus.GENERATED)
            );
            OffsetDateTime archivedAt = OffsetDateTime.now();
            activePlans.forEach(plan -> {
                plan.setStatus(PlanStatus.ARCHIVED);
                plan.setUpdatedAt(archivedAt);
            });
            trainingPlanRepository.saveAll(activePlans);
        }

        GeneratedPlanDraft generated = planGenerationService.generate(profile, raceGoal, startDate);

        OffsetDateTime now = OffsetDateTime.now();
        TrainingPlan plan = new TrainingPlan();
        plan.setId(UUID.randomUUID());
        plan.setUser(raceGoal.getUser());
        plan.setRunnerProfile(profile);
        plan.setRaceGoal(raceGoal);
        plan.setStatus(PlanStatus.ACTIVE);
        plan.setPlanVersion(nextPlanVersion);
        plan.setMethodologyCode(raceGoal.getRaceDistanceType() == RaceDistanceType.MARATHON ? "ROAD_MARATHON_V1" : "ROAD_HALF_V1");
        plan.setStartDate(generated.startDate());
        plan.setEndDate(generated.endDate());
        plan.setCurrentWeekIndex(1);
        plan.setLastRegeneratedAt(forceRegenerate ? now : null);
        plan.setCreatedAt(now);
        plan.setUpdatedAt(now);
        trainingPlanRepository.save(plan);

        for (GeneratedPlanDraft.GeneratedWeekDraft weekDraft : generated.weeks()) {
            TrainingPlanWeek week = new TrainingPlanWeek();
            week.setId(UUID.randomUUID());
            week.setTrainingPlan(plan);
            week.setWeekIndex(weekDraft.weekNumber());
            week.setPhase(weekDraft.phase());
            week.setRecoveryWeek(weekDraft.recoveryWeek());
            week.setStartDate(weekDraft.startDate());
            week.setEndDate(weekDraft.endDate());
            week.setTargetDistanceKm(weekDraft.totalPlannedDistanceKm());
            week.setTargetTimeMin(null);
            trainingPlanWeekRepository.save(week);

            for (GeneratedPlanDraft.GeneratedWorkoutDraft workoutDraft : weekDraft.workouts()) {
                PlannedWorkout workout = new PlannedWorkout();
                workout.setId(UUID.randomUUID());
                workout.setTrainingPlan(plan);
                workout.setTrainingPlanWeek(week);
                workout.setUser(plan.getUser());
                workout.setScheduledDate(workoutDraft.date());
                workout.setWorkoutType(workoutDraft.type());
                workout.setWorkoutSubtype(workoutDraft.workoutSubtype());
                workout.setPlannedDistanceKm(workoutDraft.plannedDistanceKm());
                workout.setPlannedDurationMin(workoutDraft.plannedDurationMin());
                workout.setIntensityZone(intensityZone(workoutDraft.intensity()));
                workout.setStructure(workoutDraft.structure());
                workout.setRationale(workoutDraft.rationale());
                workout.setPlanVersion(plan.getPlanVersion());
                workout.setStatus(PlannedWorkoutStatus.PLANNED);
                plannedWorkoutRepository.save(workout);
            }
        }

        return new GeneratePlanResponse(plan.getId(), plan.getPlanVersion(), plan.getStatus().name());
    }

    @Transactional(readOnly = true)
    public PlanByIdResponse getPlanById(UUID userId, UUID planId) {
        TrainingPlan plan = trainingPlanRepository.findByIdAndUser_Id(planId, userId)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "Training plan not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("planId", "not_found"))));

        List<TrainingPlanWeek> weeks = trainingPlanWeekRepository.findByTrainingPlan_IdOrderByWeekIndexAsc(plan.getId());
        List<PlannedWorkout> workouts = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(plan.getId());
        Map<UUID, List<PlannedWorkout>> byWeek = workouts.stream()
            .collect(Collectors.groupingBy(w -> w.getTrainingPlanWeek().getId()));

        List<PlanByIdResponse.WeekResponse> weekResponses = weeks.stream()
            .map(week -> new PlanByIdResponse.WeekResponse(
                week.getWeekIndex(),
                week.getStartDate(),
                week.getEndDate(),
                week.getTargetDistanceKm(),
                byWeek.getOrDefault(week.getId(), List.of()).stream()
                    .map(workout -> new PlanByIdResponse.WorkoutResponse(
                        workout.getId(),
                        workout.getScheduledDate(),
                        workout.getWorkoutType().name(),
                        workout.getPlannedDistanceKm(),
                        workout.getPlannedDurationMin(),
                        workout.getIntensityZone(),
                        workout.getStatus().name()
                    )).toList()
            )).toList();

        return new PlanByIdResponse(
            plan.getId(),
            plan.getStatus().name(),
            plan.getStartDate(),
            plan.getEndDate(),
            plan.getRaceGoal().getId(),
            weekResponses
        );
    }

    @Transactional(readOnly = true)
    public CurrentTrainingPlanResponse getCurrentPlan(UUID userId) {
        TrainingPlan plan = trainingPlanRepository.findFirstByUser_IdAndStatusInOrderByCreatedAtDesc(
                userId,
                List.of(PlanStatus.ACTIVE, PlanStatus.GENERATED)
            )
            .orElseThrow(() -> new ApiException("NOT_FOUND", "Active training plan not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("plan", "not_found"))));

        List<TrainingPlanWeek> weeks = trainingPlanWeekRepository.findByTrainingPlan_IdOrderByWeekIndexAsc(plan.getId());
        List<PlannedWorkout> workouts = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(plan.getId());
        Map<UUID, List<PlannedWorkout>> byWeek = workouts.stream()
            .collect(Collectors.groupingBy(w -> w.getTrainingPlanWeek().getId()));

        List<CurrentTrainingPlanResponse.WeekSummary> weekSummaries = weeks.stream()
            .map(week -> new CurrentTrainingPlanResponse.WeekSummary(
                week.getWeekIndex(),
                week.getPhase().name(),
                week.isRecoveryWeek(),
                week.getTargetDistanceKm(),
                byWeek.getOrDefault(week.getId(), List.of()).stream()
                    .map(workout -> new CurrentTrainingPlanResponse.WorkoutSummary(
                        workout.getId(),
                        workout.getScheduledDate(),
                        workout.getWorkoutType().name(),
                        workout.getStatus().name(),
                        workout.getPlannedDistanceKm(),
                        workout.getPlannedDurationMin(),
                        workout.getIntensityZone(),
                        List.of()
                    )).toList()
            )).toList();

        return new CurrentTrainingPlanResponse(
            plan.getId(),
            plan.getPlanVersion(),
            plan.getMethodologyCode(),
            new CurrentTrainingPlanResponse.RaceGoalSummary(
                plan.getRaceGoal().getRaceDistanceType().name(),
                plan.getRaceGoal().getRaceDate()
            ),
            plan.getCurrentWeekIndex(),
            weekSummaries
        );
    }

    @Transactional(readOnly = true)
    public PlannedWorkoutDetailResponse getPlannedWorkout(UUID userId, UUID plannedWorkoutId) {
        PlannedWorkout plannedWorkout = plannedWorkoutRepository.findByIdAndUser_Id(plannedWorkoutId, userId)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "Planned workout not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("plannedWorkoutId", "not_found"))));

        Object whyValue = plannedWorkout.getRationale().getOrDefault("whyThisWorkout", "");
        @SuppressWarnings("unchecked")
        List<String> reasonCodes = (List<String>) plannedWorkout.getRationale()
            .getOrDefault("changeReasonCodes", List.of());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> structure = (List<Map<String, Object>>) plannedWorkout.getStructure()
            .getOrDefault("segments", List.of());

        return new PlannedWorkoutDetailResponse(
            plannedWorkout.getId(),
            plannedWorkout.getScheduledDate(),
            plannedWorkout.getWorkoutType().name(),
            plannedWorkout.getWorkoutSubtype(),
            plannedWorkout.getPlannedDistanceKm(),
            plannedWorkout.getPlannedDurationMin(),
            plannedWorkout.getIntensityZone(),
            structure,
            String.valueOf(whyValue),
            reasonCodes
        );
    }

    private void validateRunDaysByDistance(int runDays, RaceDistanceType distanceType) {
        if (distanceType == RaceDistanceType.HALF_MARATHON && (runDays < 3 || runDays > 5)) {
            throw validation("preferredRunDays", "invalid_frequency", "Half marathon plans require 3 to 5 run days.");
        }
        if (distanceType == RaceDistanceType.MARATHON && (runDays < 4 || runDays > 6)) {
            throw validation("preferredRunDays", "invalid_frequency", "Marathon plans require 4 to 6 run days.");
        }
    }

    private void validateRunway(LocalDate today, RaceGoal raceGoal) {
        int minWeeks = raceGoal.getRaceDistanceType() == RaceDistanceType.MARATHON ? 12 : 8;
        if (!raceGoal.getRaceDate().isAfter(today.plusWeeks(minWeeks - 1))) {
            throw validation("raceDate", "too_soon", "Race date is too soon for safe plan generation.");
        }
    }

    private String intensityZone(com.company.runcoach.planning.domain.WorkoutIntensity intensity) {
        return switch (intensity) {
            case EASY, REST -> "Z2";
            case MODERATE -> "Z3";
            case HARD -> "Z4";
        };
    }

    private ApiException validation(String field, String issue, String message) {
        return new ApiException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST,
            List.of(new ApiErrorDetail(field, issue)));
    }
}
