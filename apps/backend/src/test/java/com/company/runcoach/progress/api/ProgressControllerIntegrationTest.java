package com.company.runcoach.progress.api;

import com.company.runcoach.RunCoachApplication;
import com.company.runcoach.adaptation.domain.AdaptationDecision;
import com.company.runcoach.adaptation.domain.AdaptationReason;
import com.company.runcoach.adaptation.domain.AdaptationTriggerType;
import com.company.runcoach.adaptation.repo.AdaptationDecisionRepository;
import com.company.runcoach.adaptation.repo.FatigueSignalRepository;
import com.company.runcoach.adaptation.repo.InjuryFeedbackRepository;
import com.company.runcoach.identity.domain.AppUser;
import com.company.runcoach.planning.domain.PlannedWorkout;
import com.company.runcoach.planning.domain.TrainingPlan;
import com.company.runcoach.goals.repo.RaceGoalRepository;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.identity.repo.RefreshTokenRepository;
import com.company.runcoach.planning.repo.PlannedWorkoutRepository;
import com.company.runcoach.planning.repo.TrainingPlanRepository;
import com.company.runcoach.planning.repo.TrainingPlanWeekRepository;
import com.company.runcoach.planning.repo.WorkoutCompletionRepository;
import com.company.runcoach.profile.repo.RunnerProfileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = RunCoachApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProgressControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdaptationDecisionRepository adaptationDecisionRepository;
    @Autowired
    private WorkoutCompletionRepository workoutCompletionRepository;
    @Autowired
    private InjuryFeedbackRepository injuryFeedbackRepository;
    @Autowired
    private FatigueSignalRepository fatigueSignalRepository;
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
    void summaryReturnsEmptyStateWhenNoActivePlan() throws Exception {
        String token = registerAndGetAccessToken("progress-empty-plan@example.com");

        mockMvc.perform(get("/v1/progress/summary")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.emptyState").value(true))
            .andExpect(jsonPath("$.planId").doesNotExist())
            .andExpect(jsonPath("$.summary.plannedWorkouts").value(0));
    }

    @Test
    void summaryUnauthorizedRejected() throws Exception {
        mockMvc.perform(get("/v1/progress/summary"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void summaryReturnsEarlyStateWithActivePlanAndNoCompletions() throws Exception {
        String token = registerAndGetAccessToken("progress-early@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId);

        mockMvc.perform(get("/v1/progress/summary")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.emptyState").value(true))
            .andExpect(jsonPath("$.summary.plannedWorkouts").value(org.hamcrest.Matchers.greaterThan(0)))
            .andExpect(jsonPath("$.summary.completedWorkouts").value(0))
            .andExpect(jsonPath("$.summary.adherencePercentage").value(0));
    }

    @Test
    void summaryCountsRescheduledWorkoutsUniquelyAcrossRepeatedAdaptations() throws Exception {
        String token = registerAndGetAccessToken("progress-reschedule-dedupe@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId);

        JsonNode plan = readCurrentPlan(token);
        String workoutId = plan.at("/weeks/0/workouts/0/plannedWorkoutId").asText();
        PlannedWorkout workout = plannedWorkoutRepository.findById(UUID.fromString(workoutId)).orElseThrow();
        workout.setScheduledDate(LocalDate.now(ZoneId.of("Europe/Berlin")));
        plannedWorkoutRepository.save(workout);

        AppUser user = appUserRepository.findAll().getFirst();
        TrainingPlan trainingPlan = trainingPlanRepository.findAll().getFirst();
        adaptationDecisionRepository.save(rescheduleDecision(user, trainingPlan, List.of(workoutId)));
        adaptationDecisionRepository.save(rescheduleDecision(user, trainingPlan, List.of(workoutId)));

        mockMvc.perform(get("/v1/progress/summary")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.summary.rescheduledWorkouts").value(1))
            .andExpect(jsonPath("$.recentStatusDistribution.rescheduled").value(1));
    }

    @Test
    void summaryAggregatesWeeklyLongRunReadinessAndStatusCounts() throws Exception {
        String token = registerAndGetAccessToken("progress-rich@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId);
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Berlin"));

        JsonNode plan = readCurrentPlan(token);
        String firstWorkoutId = plan.at("/weeks/0/workouts/0/plannedWorkoutId").asText();

        mockMvc.perform(post("/v1/workout-completions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "plannedWorkoutId": "%s",
                      "actualDistanceKm": 7.5,
                      "actualDurationMin": 48
                    }
                    """.formatted(firstWorkoutId)))
            .andExpect(status().isOk());

        int updatedPlanVersion = readCurrentPlan(token).get("planVersion").asInt();

        String secondWorkoutId = plan.at("/weeks/0/workouts/1/plannedWorkoutId").asText();
        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/skip", secondWorkoutId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "TOO_TIRED",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(updatedPlanVersion)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/v1/fatigue-signals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "signalDate": "%s",
                      "sleepScore": 2,
                      "stressScore": 4,
                      "sorenessScore": 4,
                      "motivationScore": 2,
                      "illnessFlag": false,
                      "tooBusyFlag": false,
                      "travellingFlag": false
                    }
                    """.formatted(today)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "%s",
                      "bodyRegion": "LEFT_CALF",
                      "painType": "SHARP",
                      "severity": 6,
                      "onsetContext": "DURING_RUN",
                      "canRun": true
                    }
                    """.formatted(today.atTime(8, 0).atZone(ZoneId.of("Europe/Berlin")).toOffsetDateTime())))
            .andExpect(status().isOk());

        mockMvc.perform(get("/v1/progress/summary")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.planId").isString())
            .andExpect(jsonPath("$.weeklyCompletion").isArray())
            .andExpect(jsonPath("$.weeklyCompletion[0].planned").value(org.hamcrest.Matchers.greaterThan(0)))
            .andExpect(jsonPath("$.weeklyCompletion[0].completed").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.longRunProgression").isArray())
            .andExpect(jsonPath("$.longRunProgression[0].plannedDistanceKm").value(org.hamcrest.Matchers.greaterThan(0.0)))
            .andExpect(jsonPath("$.longRunProgression[0].status").isString())
            .andExpect(jsonPath("$.readinessTrend").isArray())
            .andExpect(jsonPath("$.readinessTrend[0].date").value(today.toString()))
            .andExpect(jsonPath("$.readinessTrend[0].fatigueLevel").value(3))
            .andExpect(jsonPath("$.readinessTrend[0].painSeverity").value(6))
            .andExpect(jsonPath("$.summary.skippedWorkouts").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.summary.rescheduledWorkouts").value(0));
    }

    @Test
    void readinessTrendUsesNullPainSeverityWhenNoPainSignalExists() throws Exception {
        String token = registerAndGetAccessToken("progress-null-pain@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId);
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Berlin"));

        mockMvc.perform(post("/v1/fatigue-signals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "signalDate": "%s",
                      "sleepScore": 3,
                      "stressScore": 3,
                      "sorenessScore": 3,
                      "motivationScore": 3,
                      "illnessFlag": false,
                      "tooBusyFlag": false,
                      "travellingFlag": false
                    }
                    """.formatted(today)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/v1/progress/summary")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.readinessTrend[0].date").value(today.toString()))
            .andExpect(jsonPath("$.readinessTrend[0].painSeverity").doesNotExist());
    }

    @Test
    void summaryIsUserScoped() throws Exception {
        String ownerToken = registerAndGetAccessToken("progress-owner@example.com");
        onboard(ownerToken);
        String raceGoalId = createRaceGoal(ownerToken);
        generatePlan(ownerToken, raceGoalId);

        String otherToken = registerAndGetAccessToken("progress-other@example.com");

        mockMvc.perform(get("/v1/progress/summary")
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.emptyState").value(true))
            .andExpect(jsonPath("$.planId").doesNotExist());
    }

    @Test
    void summaryWeeklyCompletionPercentageMatchesExactComputation() throws Exception {
        String token = registerAndGetAccessToken("progress-weekly-exact@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId);
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Berlin"));

        JsonNode plan = readCurrentPlan(token);
        String firstWorkoutId = plan.at("/weeks/0/workouts/0/plannedWorkoutId").asText();
        String secondWorkoutId = plan.at("/weeks/0/workouts/1/plannedWorkoutId").asText();

        PlannedWorkout firstWorkout = plannedWorkoutRepository.findById(UUID.fromString(firstWorkoutId)).orElseThrow();
        PlannedWorkout secondWorkout = plannedWorkoutRepository.findById(UUID.fromString(secondWorkoutId)).orElseThrow();
        firstWorkout.setScheduledDate(today.minusDays(1));
        secondWorkout.setScheduledDate(today);
        plannedWorkoutRepository.saveAll(List.of(firstWorkout, secondWorkout));

        mockMvc.perform(post("/v1/workout-completions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "plannedWorkoutId": "%s",
                      "actualDistanceKm": 6.0,
                      "actualDurationMin": 40
                    }
                    """.formatted(firstWorkoutId)))
            .andExpect(status().isOk());

        int updatedPlanVersion = readCurrentPlan(token).get("planVersion").asInt();
        mockMvc.perform(post("/v1/planned-workouts/{plannedWorkoutId}/skip", secondWorkoutId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "TOO_TIRED",
                      "expectedPlanVersion": %s
                    }
                    """.formatted(updatedPlanVersion)))
            .andExpect(status().isOk());

        String summaryJson = mockMvc.perform(get("/v1/progress/summary")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode summary = objectMapper.readTree(summaryJson);
        JsonNode firstWeek = summary.get("weeklyCompletion").get(0);
        int planned = firstWeek.get("planned").asInt();
        int completed = firstWeek.get("completed").asInt();
        int completionPercentage = firstWeek.get("completionPercentage").asInt();
        int expectedPercentage = (int) Math.round((completed * 100.0) / planned);
        assertEquals(expectedPercentage, completionPercentage);
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

    private void onboard(String token) throws Exception {
        mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + token)
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

    private void generatePlan(String token, String raceGoalId) throws Exception {
        mockMvc.perform(post("/v1/plans/generate")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "raceGoalId": "%s",
                      "forceRegenerate": false
                    }
                    """.formatted(raceGoalId)))
            .andExpect(status().isOk());
    }

    private JsonNode readCurrentPlan(String token) throws Exception {
        String response = mockMvc.perform(get("/v1/plans/current")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private AdaptationDecision rescheduleDecision(AppUser user, TrainingPlan trainingPlan, List<String> changedWorkoutIds) {
        AdaptationDecision decision = new AdaptationDecision();
        decision.setId(UUID.randomUUID());
        decision.setUser(user);
        decision.setTrainingPlan(trainingPlan);
        decision.setPlanVersionBefore(trainingPlan.getPlanVersion());
        decision.setPlanVersionAfter(trainingPlan.getPlanVersion() + 1);
        decision.setTriggerType(AdaptationTriggerType.RESCHEDULE);
        decision.setTriggerWorkoutId(null);
        decision.setReason(AdaptationReason.OTHER);
        decision.setDecisionType("RESCHEDULE");
        decision.setDecisionScope("NEAR_TERM");
        decision.setConfidence(new java.math.BigDecimal("0.900"));
        decision.setReasonCodes(List.of("SCHEDULE_CONFLICT"));
        decision.setBeforeState(Map.of("planVersion", trainingPlan.getPlanVersion()));
        decision.setAfterState(Map.of("planVersion", trainingPlan.getPlanVersion() + 1));
        decision.setAffectedFromDate(LocalDate.now(ZoneId.of("Europe/Berlin")));
        decision.setAffectedToDate(LocalDate.now(ZoneId.of("Europe/Berlin")).plusDays(3));
        decision.setDecisionSummary("Rescheduled workout due to scheduling conflict.");
        decision.setChangedWorkoutIds(changedWorkoutIds);
        decision.setCreatedAt(OffsetDateTime.now());
        return decision;
    }
}
