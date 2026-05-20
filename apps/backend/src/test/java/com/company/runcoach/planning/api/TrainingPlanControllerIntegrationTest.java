package com.company.runcoach.planning.api;

import com.company.runcoach.app.RunCoachApplication;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.identity.repo.RefreshTokenRepository;
import com.company.runcoach.goals.repo.RaceGoalRepository;
import com.company.runcoach.planning.repo.PlannedWorkoutRepository;
import com.company.runcoach.planning.repo.TrainingPlanRepository;
import com.company.runcoach.planning.repo.TrainingPlanWeekRepository;
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
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RunCoachApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TrainingPlanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrainingPlanRepository trainingPlanRepository;

    @Autowired
    private TrainingPlanWeekRepository trainingPlanWeekRepository;

    @Autowired
    private PlannedWorkoutRepository plannedWorkoutRepository;

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
        plannedWorkoutRepository.deleteAll();
        trainingPlanWeekRepository.deleteAll();
        trainingPlanRepository.deleteAll();
        raceGoalRepository.deleteAll();
        runnerProfileRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void generatePlanPersistsAndCanFetchById() throws Exception {
        String token = registerAndGetAccessToken("plan-owner@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);

        String response = mockMvc.perform(post("/v1/plans/generate")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "raceGoalId": "%s",
                      "startDate": "%s"
                    }
                    """.formatted(raceGoalId, LocalDate.now(ZoneId.of("UTC")).toString())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trainingPlanId").isString())
            .andExpect(jsonPath("$.planVersion").isNumber())
            .andReturn().getResponse().getContentAsString();

        String planId = objectMapper.readTree(response).get("trainingPlanId").asText();

        mockMvc.perform(get("/v1/plans/{planId}", planId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.planId").value(planId))
            .andExpect(jsonPath("$.weeks[0].workouts").isArray())
            .andExpect(jsonPath("$.weeks[0].workouts[0].intensityZone").isString());
    }

    @Test
    void duplicateGenerateReturnsExistingPlanWithoutCreatingNewOne() throws Exception {
        String token = registerAndGetAccessToken("duplicate-plan@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);

        String payload = """
            {
              "raceGoalId": "%s"
            }
            """.formatted(raceGoalId);

        String first = mockMvc.perform(post("/v1/plans/generate")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String second = mockMvc.perform(post("/v1/plans/generate")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode firstNode = objectMapper.readTree(first);
        JsonNode secondNode = objectMapper.readTree(second);

        org.junit.jupiter.api.Assertions.assertEquals(firstNode.get("trainingPlanId").asText(), secondNode.get("trainingPlanId").asText());
    }

    @Test
    void userCannotAccessAnotherUsersPlan() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-plan@example.com");
        onboard(ownerToken);
        String raceGoalId = createRaceGoal(ownerToken);

        String response = mockMvc.perform(post("/v1/plans/generate")
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "raceGoalId": "%s"
                    }
                    """.formatted(raceGoalId)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String planId = objectMapper.readTree(response).get("trainingPlanId").asText();

        String otherToken = registerAndGetAccessToken("other-plan@example.com");
        onboard(otherToken);

        mockMvc.perform(get("/v1/plans/{planId}", planId)
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void currentPlanAndPlannedWorkoutEndpointsRespectContractAndIsolation() throws Exception {
        String token = registerAndGetAccessToken("current-plan@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);

        String generate = mockMvc.perform(post("/v1/plans/generate")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "raceGoalId": "%s"
                    }
                    """.formatted(raceGoalId)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String trainingPlanId = objectMapper.readTree(generate).get("trainingPlanId").asText();

        String current = mockMvc.perform(get("/v1/plans/current")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trainingPlanId").value(trainingPlanId))
            .andExpect(jsonPath("$.planVersion").isNumber())
            .andExpect(jsonPath("$.methodologyCode").isString())
            .andExpect(jsonPath("$.weeks[0].workouts[0].plannedWorkoutId").isString())
            .andReturn().getResponse().getContentAsString();

        String plannedWorkoutId = objectMapper.readTree(current).at("/weeks/0/workouts/0/plannedWorkoutId").asText();

        mockMvc.perform(get("/v1/planned-workouts/{plannedWorkoutId}", plannedWorkoutId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.plannedWorkoutId").value(plannedWorkoutId))
            .andExpect(jsonPath("$.structure").isArray())
            .andExpect(jsonPath("$.whyThisWorkout").isString());

        String otherToken = registerAndGetAccessToken("current-other@example.com");
        onboard(otherToken);
        mockMvc.perform(get("/v1/planned-workouts/{plannedWorkoutId}", plannedWorkoutId)
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isNotFound());
    }

    @Test
    void forceRegenerateArchivesPriorPlanAndReturnsNewPlanVersion() throws Exception {
        String token = registerAndGetAccessToken("force-regenerate@example.com");
        onboard(token);
        String raceGoalId = createRaceGoal(token);

        String first = mockMvc.perform(post("/v1/plans/generate")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "raceGoalId": "%s"
                    }
                    """.formatted(raceGoalId)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String second = mockMvc.perform(post("/v1/plans/generate")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "raceGoalId": "%s",
                      "forceRegenerate": true
                    }
                    """.formatted(raceGoalId)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode firstNode = objectMapper.readTree(first);
        JsonNode secondNode = objectMapper.readTree(second);

        org.junit.jupiter.api.Assertions.assertNotEquals(
            firstNode.get("trainingPlanId").asText(),
            secondNode.get("trainingPlanId").asText()
        );
        org.junit.jupiter.api.Assertions.assertTrue(
            secondNode.get("planVersion").asInt() > firstNode.get("planVersion").asInt()
        );
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
}
