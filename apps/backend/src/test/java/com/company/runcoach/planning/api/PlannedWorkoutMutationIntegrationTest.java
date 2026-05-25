package com.company.runcoach.planning.api;

import com.company.runcoach.RunCoachApplication;
import com.company.runcoach.adaptation.repo.AdaptationDecisionRepository;
import com.company.runcoach.adaptation.repo.FatigueSignalRepository;
import com.company.runcoach.adaptation.repo.InjuryFeedbackRepository;
import com.company.runcoach.goals.repo.RaceGoalRepository;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.identity.repo.RefreshTokenRepository;
import com.company.runcoach.planning.domain.PlannedWorkout;
import com.company.runcoach.planning.domain.PlannedWorkoutType;
import com.company.runcoach.planning.domain.WorkoutCompletion;
import com.company.runcoach.planning.repo.PlannedWorkoutRepository;
import com.company.runcoach.planning.repo.TrainingPlanRepository;
import com.company.runcoach.planning.repo.TrainingPlanWeekRepository;
import com.company.runcoach.planning.repo.WorkoutCompletionRepository;
import com.company.runcoach.profile.repo.RunnerProfileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RunCoachApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlannedWorkoutMutationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlannedWorkoutRepository plannedWorkoutRepository;

    @Autowired
    private TrainingPlanWeekRepository trainingPlanWeekRepository;

    @Autowired
    private TrainingPlanRepository trainingPlanRepository;

    @Autowired
    private RaceGoalRepository raceGoalRepository;

    @Autowired
    private RunnerProfileRepository runnerProfileRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AdaptationDecisionRepository adaptationDecisionRepository;
    @Autowired
    private FatigueSignalRepository fatigueSignalRepository;
    @Autowired
    private InjuryFeedbackRepository injuryFeedbackRepository;
    @Autowired
    private WorkoutCompletionRepository workoutCompletionRepository;

    @BeforeEach
    void setUp() {
        adaptationDecisionRepository.deleteAll();
        workoutCompletionRepository.deleteAll();
        injuryFeedbackRepository.deleteAll();
        fatigueSignalRepository.deleteAll();
        plannedWorkoutRepository.deleteAll();
        trainingPlanWeekRepository.deleteAll();
        trainingPlanRepository.deleteAll();
        raceGoalRepository.deleteAll();
        runnerProfileRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void skipWorkoutCreatesAdaptationDecisionAndIncrementsPlanVersion() throws Exception {
        String token = registerAndGetAccessToken("skip-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        String workoutId = current.at("/weeks/0/workouts/0/plannedWorkoutId").asText();
        String secondWorkoutId = current.at("/weeks/0/workouts/1/plannedWorkoutId").asText();
        int beforeVersion = current.get("planVersion").asInt();

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/skip", secondWorkoutId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "TOO_TIRED",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(beforeVersion)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.planVersion").value(beforeVersion + 1))
            .andExpect(jsonPath("$.adaptation.id").isString())
            .andExpect(jsonPath("$.adaptation.changedWorkoutIds").isArray());

        Assertions.assertEquals(1, adaptationDecisionRepository.count());
        var decision = adaptationDecisionRepository.findAll().getFirst();
        Assertions.assertTrue(decision.getPlanVersionAfter() > decision.getPlanVersionBefore());
        Assertions.assertTrue(decision.getReasonCodes().contains("PROTECT_CONSISTENCY"));
        Assertions.assertNotNull(decision.getBeforeState());
        Assertions.assertNotNull(decision.getAfterState());
    }

    @Test
    void rescheduleCreatesAdaptationDecisionAndIncrementsPlanVersion() throws Exception {
        String token = registerAndGetAccessToken("reschedule-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        String workoutId = current.at("/weeks/0/workouts/0/plannedWorkoutId").asText();
        String secondWorkoutId = current.at("/weeks/0/workouts/1/plannedWorkoutId").asText();
        int beforeVersion = current.get("planVersion").asInt();
        LocalDate scheduledDate = LocalDate.parse(current.at("/weeks/0/workouts/0/scheduledDate").asText());
        LocalDate targetDate = scheduledDate.plusDays(1);

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/reschedule", workoutId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetDate": "%s",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(targetDate, beforeVersion)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.planVersion").value(beforeVersion + 1));

        Assertions.assertEquals(1, adaptationDecisionRepository.count());
    }

    @Test
    void rescheduleUsesWorkoutTypeAwareReasonCodes() throws Exception {
        String token = registerAndGetAccessToken("reschedule-reason-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        UUID planId = UUID.fromString(current.get("trainingPlanId").asText());
        int beforeVersion = current.get("planVersion").asInt();

        PlannedWorkout easyWorkout = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(planId).stream()
            .filter(w -> w.getWorkoutType() == PlannedWorkoutType.EASY_RUN || w.getWorkoutType() == PlannedWorkoutType.RECOVERY_RUN)
            .findFirst()
            .orElseThrow();

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/reschedule", easyWorkout.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetDate": "%s",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(easyWorkout.getScheduledDate().plusDays(1), beforeVersion)))
            .andExpect(status().isOk());

        var decision = adaptationDecisionRepository.findAll().getFirst();
        Assertions.assertTrue(decision.getReasonCodes().contains("PROTECT_CONSISTENCY"));
        Assertions.assertFalse(decision.getReasonCodes().contains("MISSED_EASY_RUN"));
        Assertions.assertFalse(decision.getReasonCodes().contains("MISSED_QUALITY_RUN"));
        Assertions.assertFalse(decision.getReasonCodes().contains("REDUCE_INTENSITY"));
    }

    @Test
    void rescheduleDoesNotApplyBlanketIntensityDownshift() throws Exception {
        String token = registerAndGetAccessToken("reschedule-no-downshift-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        UUID planId = UUID.fromString(current.get("trainingPlanId").asText());
        int beforeVersion = current.get("planVersion").asInt();

        List<PlannedWorkout> workouts = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(planId);
        PlannedWorkout trigger = workouts.stream()
            .filter(w -> w.getWorkoutType() == PlannedWorkoutType.EASY_RUN || w.getWorkoutType() == PlannedWorkoutType.RECOVERY_RUN)
            .findFirst()
            .orElseThrow();
        Map<UUID, PlannedWorkout> beforeById = workouts.stream()
            .collect(java.util.stream.Collectors.toMap(PlannedWorkout::getId, w -> w));

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/reschedule", trigger.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetDate": "%s",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(trigger.getScheduledDate().plusDays(1), beforeVersion)))
            .andExpect(status().isOk());

        List<PlannedWorkout> after = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(planId);
        long unchangedCount = after.stream()
            .filter(w -> !w.getId().equals(trigger.getId()))
            .filter(w -> {
                PlannedWorkout before = beforeById.get(w.getId());
                return before != null
                    && before.getWorkoutType() == w.getWorkoutType()
                    && java.util.Objects.equals(before.getPlannedDistanceKm(), w.getPlannedDistanceKm())
                    && java.util.Objects.equals(before.getPlannedDurationMin(), w.getPlannedDurationMin());
            })
            .count();
        Assertions.assertTrue(unchangedCount > 0, "Expected at least one nearby workout to remain unchanged.");
    }

    @Test
    void rescheduleRejectsMovesOutsideOneDayWindow() throws Exception {
        String token = registerAndGetAccessToken("reschedule-window@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        String workoutId = current.at("/weeks/0/workouts/0/plannedWorkoutId").asText();
        String secondWorkoutId = current.at("/weeks/0/workouts/1/plannedWorkoutId").asText();
        int beforeVersion = current.get("planVersion").asInt();
        LocalDate scheduledDate = LocalDate.parse(current.at("/weeks/0/workouts/0/scheduledDate").asText());

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/reschedule", workoutId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetDate": "%s",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(scheduledDate.plusDays(3), beforeVersion)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.details[?(@.field=='targetDate' && @.issue=='move_window_exceeded')]").exists());
    }

    @Test
    void stalePlanVersionReturnsConflict() throws Exception {
        String token = registerAndGetAccessToken("stale-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        String workoutId = current.at("/weeks/0/workouts/0/plannedWorkoutId").asText();
        String secondWorkoutId = current.at("/weeks/0/workouts/1/plannedWorkoutId").asText();
        int beforeVersion = current.get("planVersion").asInt();

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/skip", secondWorkoutId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "NO_TIME",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(beforeVersion)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/skip", workoutId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "NO_TIME",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(beforeVersion)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code").value("STALE_PLAN_VERSION"));

        JsonNode latest = readCurrentPlan(token);
        String rescheduleWorkoutId = latest.at("/weeks/0/workouts/0/plannedWorkoutId").asText();
        LocalDate scheduledDate = LocalDate.parse(latest.at("/weeks/0/workouts/0/scheduledDate").asText());
        int staleVersion = latest.get("planVersion").asInt() - 1;
        int currentVersion = latest.get("planVersion").asInt();

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/reschedule", rescheduleWorkoutId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetDate": "%s",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(scheduledDate.plusDays(1), staleVersion)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code").value("STALE_PLAN_VERSION"));

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/reschedule", rescheduleWorkoutId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetDate": "%s",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(scheduledDate.plusDays(1), currentVersion)))
            .andExpect(status().isOk());
    }

    @Test
    void skipAndRescheduleRequireExpectedPlanVersion() throws Exception {
        String token = registerAndGetAccessToken("required-version-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        String workoutId = current.at("/weeks/0/workouts/0/plannedWorkoutId").asText();
        String scheduledDate = current.at("/weeks/0/workouts/0/scheduledDate").asText();

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/skip", workoutId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "NO_TIME"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/reschedule", workoutId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetDate": "%s"
                    }
                    """.formatted(scheduledDate)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void userCannotMutateAnotherUsersWorkout() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-mutate@example.com");
        onboard(ownerToken);
        String raceGoalId = createRaceGoal(ownerToken);
        generatePlan(ownerToken, raceGoalId, false);

        JsonNode current = readCurrentPlan(ownerToken);
        String workoutId = current.at("/weeks/0/workouts/0/plannedWorkoutId").asText();

        String otherToken = registerAndGetAccessToken("other-mutate@example.com");
        onboard(otherToken);

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/skip", workoutId)
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "OTHER",
                      "expectedPlanVersion": 1
                    }
                    """))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/reschedule", workoutId)
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetDate": "%s",
                      "expectedPlanVersion": 1
                    }
                    """.formatted(LocalDate.now(ZoneId.of("UTC")).plusDays(1))))
            .andExpect(status().isNotFound());
    }

    @Test
    void mutationRejectedForWorkoutOutsideCurrentActivePlan() throws Exception {
        String token = registerAndGetAccessToken("outside-active@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode firstPlan = readCurrentPlan(token);
        String staleWorkoutId = firstPlan.at("/weeks/0/workouts/0/plannedWorkoutId").asText();

        generatePlan(token, raceGoalId, true);

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/skip", staleWorkoutId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "OTHER",
                      "expectedPlanVersion": 2
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.details[?(@.field=='plannedWorkoutId')]").exists());
    }

    @Test
    void skipAndRescheduleRejectedForNonPlannedStatus() throws Exception {
        String token = registerAndGetAccessToken("status-guard-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        UUID planId = UUID.fromString(current.get("trainingPlanId").asText());
        int beforeVersion = current.get("planVersion").asInt();

        PlannedWorkout workout = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(planId).getFirst();
        workout.setStatus(com.company.runcoach.planning.domain.PlannedWorkoutStatus.COMPLETED);
        plannedWorkoutRepository.save(workout);

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/skip", workout.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "NO_TIME",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(beforeVersion)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.details[?(@.field=='plannedWorkoutId' && @.issue=='skip_not_allowed_for_status')]").exists());

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/reschedule", workout.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetDate": "%s",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(workout.getScheduledDate().plusDays(1), beforeVersion)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.details[?(@.field=='plannedWorkoutId' && @.issue=='reschedule_not_allowed_for_status')]").exists());
    }

    @Test
    void longRunRescheduleRejectsUnsafeSpacingAndNonFreeTargetDay() throws Exception {
        String token = registerAndGetAccessToken("longrun-reschedule-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        UUID planId = UUID.fromString(current.get("trainingPlanId").asText());
        int beforeVersion = current.get("planVersion").asInt();

        List<PlannedWorkout> workouts = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(planId);
        PlannedWorkout longRun = workouts.stream()
            .filter(w -> w.getWorkoutType() == PlannedWorkoutType.LONG_RUN)
            .findFirst()
            .orElseThrow();

        PlannedWorkout sameDayWorkout = workouts.stream()
            .filter(w -> !w.getId().equals(longRun.getId()))
            .findFirst()
            .orElseThrow();
        sameDayWorkout.setScheduledDate(longRun.getScheduledDate().plusDays(1));
        sameDayWorkout.setStatus(com.company.runcoach.planning.domain.PlannedWorkoutStatus.PLANNED);
        plannedWorkoutRepository.save(sameDayWorkout);

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/reschedule", longRun.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetDate": "%s",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(longRun.getScheduledDate().plusDays(1), beforeVersion)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.details[?(@.field=='targetDate' && @.issue=='long_run_day_not_free')]").exists());

        sameDayWorkout.setScheduledDate(longRun.getScheduledDate().plusDays(2));
        sameDayWorkout.setWorkoutType(PlannedWorkoutType.TEMPO_RUN);
        plannedWorkoutRepository.save(sameDayWorkout);

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/reschedule", longRun.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "targetDate": "%s",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(longRun.getScheduledDate().plusDays(1), beforeVersion)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.details[?(@.field=='targetDate' && @.issue=='unsafe_long_run_spacing')]").exists());
    }

    @Test
    void adaptationAvoidsUnsafeLongRunSpikeAndCatchupStacking() throws Exception {
        String token = registerAndGetAccessToken("safety-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        UUID planId = UUID.fromString(current.get("trainingPlanId").asText());
        String workoutId = current.at("/weeks/0/workouts/0/plannedWorkoutId").asText();
        int version = current.get("planVersion").asInt();

        List<PlannedWorkout> beforeWorkouts = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(planId);
        Map<UUID, BigDecimal> beforeDistance = new HashMap<>();
        Map<UUID, Integer> beforeDuration = new HashMap<>();
        beforeWorkouts.forEach(w -> {
            beforeDistance.put(w.getId(), w.getPlannedDistanceKm());
            beforeDuration.put(w.getId(), w.getPlannedDurationMin());
        });

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/skip", workoutId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "PAIN",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(version)))
            .andExpect(status().isOk());

        List<PlannedWorkout> afterWorkouts = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(planId);
        LocalDate horizonEnd = LocalDate.now(ZoneId.of("UTC")).plusDays(14);
        List<PlannedWorkout> longRuns = afterWorkouts.stream()
            .filter(w -> w.getWorkoutType() == PlannedWorkoutType.LONG_RUN && w.getPlannedDistanceKm() != null)
            .filter(w -> !w.getScheduledDate().isAfter(horizonEnd))
            .sorted(Comparator.comparing(PlannedWorkout::getScheduledDate))
            .toList();

        for (int i = 1; i < longRuns.size(); i++) {
            BigDecimal previous = longRuns.get(i - 1).getPlannedDistanceKm();
            BigDecimal currentDistance = longRuns.get(i).getPlannedDistanceKm();
            Assertions.assertTrue(currentDistance.compareTo(previous.multiply(BigDecimal.valueOf(1.10))) <= 0);
        }

        for (PlannedWorkout after : afterWorkouts) {
            BigDecimal beforeKm = beforeDistance.get(after.getId());
            Integer beforeMin = beforeDuration.get(after.getId());
            if (beforeKm != null && after.getPlannedDistanceKm() != null) {
                Assertions.assertTrue(after.getPlannedDistanceKm().compareTo(beforeKm) <= 0);
            }
            if (beforeMin != null && after.getPlannedDurationMin() != null) {
                Assertions.assertTrue(after.getPlannedDurationMin() <= beforeMin);
            }
        }

        UUID triggerId = UUID.fromString(workoutId);
        Assertions.assertTrue(afterWorkouts.stream()
            .filter(w -> !w.getId().equals(triggerId))
            .anyMatch(w -> triggerId.equals(w.getAdaptedFromWorkoutId())));

        LocalDate triggerDate = afterWorkouts.stream()
            .filter(w -> w.getId().equals(triggerId))
            .findFirst()
            .map(PlannedWorkout::getScheduledDate)
            .orElseThrow();
        LocalDate horizonEndInclusive = triggerDate.plusDays(14);
        Assertions.assertTrue(afterWorkouts.stream()
            .filter(w -> w.getAdaptedFromWorkoutId() != null && triggerId.equals(w.getAdaptedFromWorkoutId()))
            .allMatch(w -> !w.getScheduledDate().isAfter(horizonEndInclusive)));
    }

    @Test
    void adaptationDoesNotMutateAlreadyCompletedWorkoutInWindow() throws Exception {
        String token = registerAndGetAccessToken("history-protect-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        UUID planId = UUID.fromString(current.get("trainingPlanId").asText());
        int version = current.get("planVersion").asInt();

        List<PlannedWorkout> workouts = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(planId);
        PlannedWorkout trigger = workouts.getFirst();
        PlannedWorkout completedInWindow = workouts.stream()
            .filter(w -> !w.getId().equals(trigger.getId()))
            .filter(w -> !w.getScheduledDate().isBefore(trigger.getScheduledDate()))
            .filter(w -> !w.getScheduledDate().isAfter(trigger.getScheduledDate().plusDays(14)))
            .findFirst()
            .orElseThrow();

        completedInWindow.setStatus(com.company.runcoach.planning.domain.PlannedWorkoutStatus.COMPLETED);
        completedInWindow.setWorkoutType(PlannedWorkoutType.TEMPO_RUN);
        completedInWindow.setWorkoutSubtype("LOCKED_COMPLETED");
        completedInWindow.setPlannedDurationMin(60);
        completedInWindow.setPlanVersion(version);
        plannedWorkoutRepository.save(completedInWindow);

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/skip", trigger.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "NO_TIME",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(version)))
            .andExpect(status().isOk());

        PlannedWorkout reloaded = plannedWorkoutRepository.findById(completedInWindow.getId()).orElseThrow();
        Assertions.assertEquals(com.company.runcoach.planning.domain.PlannedWorkoutStatus.COMPLETED, reloaded.getStatus());
        Assertions.assertEquals(PlannedWorkoutType.TEMPO_RUN, reloaded.getWorkoutType());
        Assertions.assertEquals("LOCKED_COMPLETED", reloaded.getWorkoutSubtype());
        Assertions.assertEquals(Integer.valueOf(60), reloaded.getPlannedDurationMin());
        Assertions.assertEquals(version, reloaded.getPlanVersion());
        Assertions.assertNull(reloaded.getAdaptedFromWorkoutId());
    }

    @Test
    void adaptationCapsFirstInWindowLongRunUsingPriorLongRunBaseline() throws Exception {
        String token = registerAndGetAccessToken("longrun-baseline-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        UUID planId = UUID.fromString(current.get("trainingPlanId").asText());
        int version = current.get("planVersion").asInt();

        List<PlannedWorkout> workouts = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(planId);

        PlannedWorkout triggerWorkout = null;
        PlannedWorkout priorLongRun = null;
        PlannedWorkout firstWindowLongRun = null;
        for (PlannedWorkout candidate : workouts) {
            PlannedWorkout latestPriorLongRun = workouts.stream()
                .filter(w -> w.getWorkoutType() == PlannedWorkoutType.LONG_RUN)
                .filter(w -> w.getScheduledDate().isBefore(candidate.getScheduledDate()))
                .max(Comparator.comparing(PlannedWorkout::getScheduledDate))
                .orElse(null);

            if (latestPriorLongRun == null) {
                continue;
            }

            PlannedWorkout windowLongRun = workouts.stream()
                .filter(w -> w.getWorkoutType() == PlannedWorkoutType.LONG_RUN)
                .filter(w -> !w.getScheduledDate().isBefore(candidate.getScheduledDate()))
                .filter(w -> !w.getScheduledDate().isAfter(candidate.getScheduledDate().plusDays(14)))
                .min(Comparator.comparing(PlannedWorkout::getScheduledDate))
                .orElse(null);

            if (windowLongRun != null) {
                triggerWorkout = candidate;
                priorLongRun = latestPriorLongRun;
                firstWindowLongRun = windowLongRun;
                break;
            }
        }

        Assertions.assertNotNull(triggerWorkout);
        Assertions.assertNotNull(priorLongRun);
        Assertions.assertNotNull(firstWindowLongRun);

        priorLongRun.setPlannedDistanceKm(BigDecimal.valueOf(10.0));
        firstWindowLongRun.setPlannedDistanceKm(BigDecimal.valueOf(14.0));
        plannedWorkoutRepository.save(priorLongRun);
        plannedWorkoutRepository.save(firstWindowLongRun);

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/skip", triggerWorkout.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "PAIN",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(version)))
            .andExpect(status().isOk());

        PlannedWorkout updatedWindowLongRun = plannedWorkoutRepository.findById(firstWindowLongRun.getId()).orElseThrow();
        Assertions.assertTrue(updatedWindowLongRun.getPlannedDistanceKm().compareTo(BigDecimal.valueOf(11.0)) <= 0);
    }

    @Test
    void workoutCompletionTriggersPartialAndOverdoneAdaptationAudit() throws Exception {
        String token = registerAndGetAccessToken("completion-adapt-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        UUID planId = UUID.fromString(current.get("trainingPlanId").asText());
        PlannedWorkout firstWorkout = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(planId).getFirst();
        BigDecimal partialDistance = firstWorkout.getPlannedDistanceKm().multiply(BigDecimal.valueOf(0.50));
        int beforeVersion = current.get("planVersion").asInt();

        String partialCompletionResponse = mockMvc.perform(post("/v1/workout-completions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "plannedWorkoutId": "%s",
                      "actualDistanceKm": %s
                    }
                    """.formatted(firstWorkout.getId(), partialDistance)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.adaptationTriggered").value(true))
            .andExpect(jsonPath("$.completionId").isString())
            .andReturn().getResponse().getContentAsString();

        UUID partialCompletionId = UUID.fromString(objectMapper.readTree(partialCompletionResponse).get("completionId").asText());
        WorkoutCompletion partialCompletion = workoutCompletionRepository.findById(partialCompletionId).orElseThrow();
        Assertions.assertEquals(firstWorkout.getId(), partialCompletion.getPlannedWorkout().getId());
        Assertions.assertEquals(
            partialDistance.setScale(2, RoundingMode.HALF_UP),
            partialCompletion.getActualDistanceKm().setScale(2, RoundingMode.HALF_UP)
        );
        Assertions.assertEquals(1, workoutCompletionRepository.count());

        var partialDecision = adaptationDecisionRepository.findAll().getFirst();
        Assertions.assertEquals("PARTIAL_COMPLETION", partialDecision.getTriggerType().name());
        Assertions.assertTrue(partialDecision.getReasonCodes().contains("WORKOUT_UNDER_COMPLETED"));
        Assertions.assertTrue(partialDecision.getPlanVersionAfter() > beforeVersion);

        JsonNode refreshed = readCurrentPlan(token);
        int secondBeforeVersion = refreshed.get("planVersion").asInt();
        PlannedWorkout secondWorkout = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(planId).stream()
            .filter(w -> !w.getId().equals(firstWorkout.getId()))
            .findFirst()
            .orElseThrow();
        BigDecimal overdoneDistance = secondWorkout.getPlannedDistanceKm().multiply(BigDecimal.valueOf(1.40));

        mockMvc.perform(post("/v1/workout-completions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "plannedWorkoutId": "%s",
                      "actualDistanceKm": %s
                    }
                    """.formatted(secondWorkout.getId(), overdoneDistance)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.adaptationTriggered").value(true));

        var decisions = adaptationDecisionRepository.findAll();
        Assertions.assertTrue(decisions.stream().anyMatch(d -> "OVERDONE_WORKOUT".equals(d.getTriggerType().name())));
        Assertions.assertTrue(decisions.stream().anyMatch(d -> d.getReasonCodes().contains("WORKOUT_OVERDONE")));
        Assertions.assertTrue(readCurrentPlan(token).get("planVersion").asInt() > secondBeforeVersion);
        Assertions.assertEquals(2, workoutCompletionRepository.count());
    }

    @Test
    void workoutCompletionRejectedForNonPlannedStatus() throws Exception {
        String token = registerAndGetAccessToken("completion-status-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        UUID planId = UUID.fromString(current.get("trainingPlanId").asText());
        PlannedWorkout firstWorkout = plannedWorkoutRepository.findByTrainingPlan_IdOrderByScheduledDateAsc(planId).getFirst();
        firstWorkout.setStatus(com.company.runcoach.planning.domain.PlannedWorkoutStatus.COMPLETED);
        plannedWorkoutRepository.save(firstWorkout);

        mockMvc.perform(post("/v1/workout-completions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "plannedWorkoutId": "%s",
                      "actualDistanceKm": 5.0
                    }
                    """.formatted(firstWorkout.getId())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[?(@.field=='plannedWorkoutId' && @.issue=='complete_not_allowed_for_status')]").exists());
    }

    @Test
    void currentPlanAndTodayExposeLatestAdaptationSummary() throws Exception {
        String token = registerAndGetAccessToken("summary-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        JsonNode current = readCurrentPlan(token);
        String workoutId = current.at("/weeks/0/workouts/0/plannedWorkoutId").asText();
        int beforeVersion = current.get("planVersion").asInt();

        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/skip", workoutId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "PAIN_DISCOMFORT",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(beforeVersion)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/v1/plans/current")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.latestAdaptation.changedWorkoutIds").isArray())
            .andExpect(jsonPath("$.weeks[0].workouts[0].changeReasonCodes").isArray());

        mockMvc.perform(get("/v1/insights/today")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void fatigueAndPainCheckInsTriggerAdaptationAuditAndVersionBump() throws Exception {
        String token = registerAndGetAccessToken("checkin-adapt-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId, false);

        int baselineVersion = readCurrentPlan(token).get("planVersion").asInt();

        mockMvc.perform(post("/v1/fatigue-signals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "signalDate": "%s",
                      "sleepScore": 1,
                      "stressScore": 4,
                      "sorenessScore": 4,
                      "motivationScore": 1,
                      "illnessFlag": true,
                      "tooBusyFlag": false,
                      "travellingFlag": false
                    }
                    """.formatted(LocalDate.now(ZoneId.of("UTC")))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.readinessState").value("HIGH_RISK"));

        int afterFatigueVersion = readCurrentPlan(token).get("planVersion").asInt();
        Assertions.assertTrue(afterFatigueVersion > baselineVersion);

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-15T08:00:00Z",
                      "bodyRegion": "LEFT_CALF",
                      "painType": "SHARP",
                      "severity": 8,
                      "onsetContext": "DURING_RUN",
                      "canRun": false
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.readinessState").value("HIGH_RISK"));

        int afterPainVersion = readCurrentPlan(token).get("planVersion").asInt();
        Assertions.assertTrue(afterPainVersion > afterFatigueVersion);
        Assertions.assertTrue(adaptationDecisionRepository.count() >= 2);
    }

    private String registerAndGetAccessToken(String email) throws Exception {
        String response = mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "password": "Password12345!",
                      "timezone": "Europe/Berlin"
                    }
                    """.formatted(email)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private void onboard(String accessToken) throws Exception {
        mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "profile": {
                        "birthYear": 1990,
                        "sex": "FEMALE",
                        "experienceLevel": "BEGINNER",
                        "typicalWeeklyDistanceKm": 24.0,
                        "longestRecentRunKm": 10.0,
                        "preferredRunDays": ["TUESDAY", "THURSDAY", "SATURDAY", "SUNDAY"],
                        "preferredLongRunDay": "SUNDAY",
                        "goalStyle": "FINISH",
                        "strengthDaysPerWeek": 1,
                        "units": "KM",
                        "timezone": "Europe/Berlin",
                        "injuryHistory": {
                          "hadRunningInjuryLast12Months": false
                        }
                      }
                    }
                    """))
            .andExpect(status().isOk());
    }

    private String createRaceGoal(String token) throws Exception {
        String response = mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "raceName": "Berlin Half",
                      "raceDistanceType": "HALF_MARATHON",
                      "raceDate": "%s",
                      "goalStyle": "FINISH"
                    }
                    """.formatted(LocalDate.now(ZoneId.of("UTC")).plusWeeks(14))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("raceGoalId").asText();
    }

    private void generatePlan(String token, String raceGoalId, boolean forceRegenerate) throws Exception {
        mockMvc.perform(post("/v1/plans/generate")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "raceGoalId": "%s",
                      "forceRegenerate": %s
                    }
                    """.formatted(raceGoalId, forceRegenerate)))
            .andExpect(status().isOk());
    }

    private JsonNode readCurrentPlan(String token) throws Exception {
        String current = mockMvc.perform(get("/v1/plans/current")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(current);
    }
}
