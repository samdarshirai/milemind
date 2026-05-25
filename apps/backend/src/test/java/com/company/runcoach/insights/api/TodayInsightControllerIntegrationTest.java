package com.company.runcoach.insights.api;

import com.company.runcoach.RunCoachApplication;
import com.company.runcoach.adaptation.domain.AdaptationDecision;
import com.company.runcoach.adaptation.domain.AdaptationReason;
import com.company.runcoach.adaptation.domain.AdaptationTriggerType;
import com.company.runcoach.adaptation.repo.AdaptationDecisionRepository;
import com.company.runcoach.adaptation.repo.FatigueSignalRepository;
import com.company.runcoach.adaptation.repo.InjuryFeedbackRepository;
import com.company.runcoach.goals.repo.RaceGoalRepository;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.identity.repo.RefreshTokenRepository;
import com.company.runcoach.planning.domain.TrainingPlan;
import com.company.runcoach.planning.repo.PlannedWorkoutRepository;
import com.company.runcoach.planning.repo.TrainingPlanRepository;
import com.company.runcoach.planning.repo.TrainingPlanWeekRepository;
import com.company.runcoach.planning.repo.WorkoutCompletionRepository;
import com.company.runcoach.profile.repo.RunnerProfileRepository;
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
import java.time.ZonedDateTime;
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
class TodayInsightControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FatigueSignalRepository fatigueSignalRepository;

    @Autowired
    private InjuryFeedbackRepository injuryFeedbackRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AdaptationDecisionRepository adaptationDecisionRepository;

    @Autowired
    private WorkoutCompletionRepository workoutCompletionRepository;

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
    void todayInsightReturnsReadinessAndLatestSignals() throws Exception {
        String token = registerAndGetAccessToken("today@example.com");
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Berlin"));

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
                      "travellingFlag": false,
                      "notes": "hard week"
                    }
                    """.formatted(today)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "%s",
                      "hasPain": true,
                      "bodyRegion": "LEFT_CALF",
                      "painType": "SHARP",
                      "severity": 5,
                      "onsetContext": "DURING_RUN",
                      "canRun": true,
                      "freeText": "Sensitive note should not be returned."
                    }
                    """.formatted(today.atTime(8, 0).atZone(ZoneId.of("Europe/Berlin")).toOffsetDateTime())))
            .andExpect(status().isOk());

        mockMvc.perform(get("/v1/insights/today")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value(today.toString()))
            .andExpect(jsonPath("$.readinessState").value("HIGH_RISK"))
            .andExpect(jsonPath("$.hasCheckInToday").value(true))
            .andExpect(jsonPath("$.latestFatigueSignal.signalDate").value(today.toString()))
            .andExpect(jsonPath("$.latestInjuryFeedback.freeText").doesNotExist())
            .andExpect(jsonPath("$.recommendedTone").isString())
            .andExpect(jsonPath("$.insightMessages").isArray())
            .andExpect(jsonPath("$.warnings").isArray())
            .andExpect(jsonPath("$.warnings[0]").isString());
    }

    @Test
    void todayInsightDateUsesUserTimezoneCalendarDay() throws Exception {
        String token = registerAndGetAccessToken("la-today@example.com", "America/Los_Angeles");
        LocalDate laToday = LocalDate.now(ZoneId.of("America/Los_Angeles"));

        mockMvc.perform(get("/v1/insights/today")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value(laToday.toString()))
            .andExpect(jsonPath("$.insightMessages").isArray())
            .andExpect(jsonPath("$.warnings").isArray());
    }

    @Test
    void todayInsightHasCheckInTodayUsesUserTimezoneWindow() throws Exception {
        String token = registerAndGetAccessToken("timezone-window@example.com", "America/Los_Angeles");
        ZoneId laZone = ZoneId.of("America/Los_Angeles");
        LocalDate laToday = LocalDate.now(laZone);
        ZonedDateTime startOfLaDay = laToday.atStartOfDay(laZone);
        OffsetDateTime beforeLaDayStartUtc = startOfLaDay.minusMinutes(30).withZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime();
        OffsetDateTime afterLaDayStartUtc = startOfLaDay.plusMinutes(30).withZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime();

        // Just before local-day start should not count for today's check-in.
        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "%s",
                      "hasPain": false
                    }
                    """.formatted(beforeLaDayStartUtc)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/v1/insights/today")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hasCheckInToday").value(false));

        // Just after local-day start should count.
        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "%s",
                      "hasPain": false
                    }
                    """.formatted(afterLaDayStartUtc)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/v1/insights/today")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hasCheckInToday").value(true));
    }

    @Test
    void todayInsightReadinessDoesNotUsePriorDayCheckIns() throws Exception {
        String token = registerAndGetAccessToken("prior-day@example.com", "America/Los_Angeles");
        ZoneId laZone = ZoneId.of("America/Los_Angeles");
        LocalDate laToday = LocalDate.now(laZone);
        ZonedDateTime startOfLaDay = laToday.atStartOfDay(laZone);
        OffsetDateTime beforeLaDayStartUtc = startOfLaDay.minusMinutes(30).withZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime();

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "%s",
                      "bodyRegion": "LEFT_CALF",
                      "painType": "SHARP",
                      "severity": 8,
                      "onsetContext": "DURING_RUN",
                      "canRun": false
                    }
                    """.formatted(beforeLaDayStartUtc)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/v1/insights/today")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hasCheckInToday").value(false))
            .andExpect(jsonPath("$.readinessState").value("READY"))
            .andExpect(jsonPath("$.latestInjuryFeedback").doesNotExist());
    }

    @Test
    void todayInsightUnauthorizedRejected() throws Exception {
        mockMvc.perform(get("/v1/insights/today"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void todayInsightShowsLatestAdaptationOnlyWhenRelevantToToday() throws Exception {
        String token = registerAndGetAccessToken("today-relevant-adaptation@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId);

        TrainingPlan activePlan = trainingPlanRepository.findAll().getFirst();
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Berlin"));
        LocalDate from = today.minusDays(1);
        LocalDate to = today.plusDays(2);

        adaptationDecisionRepository.save(adaptation(
            activePlan,
            from,
            to,
            "Kept this week recovery focused.",
            List.of()
        ));

        mockMvc.perform(get("/v1/insights/today")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.latestAdaptation").exists())
            .andExpect(jsonPath("$.latestAdaptation.adaptationDecisionId").isString())
            .andExpect(jsonPath("$.insightMessages[2]").value("Recent adaptation: Kept this week recovery focused."));
    }

    @Test
    void todayInsightHidesLatestAdaptationWhenNotRelevantToToday() throws Exception {
        String token = registerAndGetAccessToken("today-stale-adaptation@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);
        generatePlan(token, raceGoalId);

        TrainingPlan activePlan = trainingPlanRepository.findAll().getFirst();
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Berlin"));

        adaptationDecisionRepository.save(adaptation(
            activePlan,
            today.minusDays(40),
            today.minusDays(30),
            "Old adaptation outside current range.",
            List.of()
        ));

        mockMvc.perform(get("/v1/insights/today")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.latestAdaptation").doesNotExist())
            .andExpect(jsonPath("$.insightMessages.length()").value(2));
    }

    @Test
    void todayInsightIsUserScoped() throws Exception {
        String ownerToken = registerAndGetAccessToken("today-owner@example.com");
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Berlin"));

        mockMvc.perform(post("/v1/fatigue-signals")
                .header("Authorization", "Bearer " + ownerToken)
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

        String otherToken = registerAndGetAccessToken("today-other@example.com");
        mockMvc.perform(get("/v1/insights/today")
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.readinessState").value("READY"))
            .andExpect(jsonPath("$.hasCheckInToday").value(false))
            .andExpect(jsonPath("$.latestFatigueSignal").doesNotExist())
            .andExpect(jsonPath("$.latestInjuryFeedback").doesNotExist());
    }

    private String registerAndGetAccessToken(String email) throws Exception {
        return registerAndGetAccessToken(email, "Europe/Berlin");
    }

    private String registerAndGetAccessToken(String email, String timezone) throws Exception {
        String response = mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "password": "Password12345!",
                      "timezone": "%s"
                    }
                    """.formatted(email, timezone)))
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

    private AdaptationDecision adaptation(
        TrainingPlan plan,
        LocalDate from,
        LocalDate to,
        String summary,
        List<String> changedWorkoutIds
    ) {
        AdaptationDecision decision = new AdaptationDecision();
        decision.setId(UUID.randomUUID());
        decision.setUser(plan.getUser());
        decision.setTrainingPlan(plan);
        decision.setPlanVersionBefore(plan.getPlanVersion());
        decision.setPlanVersionAfter(plan.getPlanVersion() + 1);
        decision.setTriggerType(AdaptationTriggerType.FATIGUE_SIGNAL);
        decision.setReason(AdaptationReason.TOO_TIRED);
        decision.setDecisionType("RECOVERY_SHIFT");
        decision.setDecisionScope("NEXT_7_DAYS");
        decision.setConfidence(new java.math.BigDecimal("1.000"));
        decision.setReasonCodes(List.of("READINESS_HIGH_RISK"));
        decision.setBeforeState(Map.of("week", plan.getCurrentWeekIndex()));
        decision.setAfterState(Map.of("week", plan.getCurrentWeekIndex()));
        decision.setAffectedFromDate(from);
        decision.setAffectedToDate(to);
        decision.setDecisionSummary(summary);
        decision.setChangedWorkoutIds(changedWorkoutIds);
        decision.setCreatedAt(OffsetDateTime.now(ZoneId.of("UTC")));
        return decision;
    }
}
