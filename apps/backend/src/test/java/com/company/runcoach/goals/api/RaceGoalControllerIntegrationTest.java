package com.company.runcoach.goals.api;

import com.company.runcoach.app.RunCoachApplication;
import com.company.runcoach.goals.domain.RaceGoalStatus;
import com.company.runcoach.goals.repo.RaceGoalRepository;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.identity.repo.RefreshTokenRepository;
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
class RaceGoalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RunnerProfileRepository runnerProfileRepository;

    @Autowired
    private RaceGoalRepository raceGoalRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        raceGoalRepository.deleteAll();
        runnerProfileRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void authenticatedUserCanCreateHalfMarathonGoal() throws Exception {
        String accessToken = registerAndGetAccessToken("half-goal@example.com");
        onboard(accessToken);

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("HALF_MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(8), "FINISH", null)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.raceGoalId").isString())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void authenticatedUserCanCreateMarathonGoal() throws Exception {
        String accessToken = registerAndGetAccessToken("marathon-goal@example.com");
        onboard(accessToken);

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(12), "IMPROVE", 13680)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.raceGoalId").isString())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void unauthenticatedRequestRejected() throws Exception {
        mockMvc.perform(post("/v1/race-goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("HALF_MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(8), "FINISH", null)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
            .andExpect(jsonPath("$.error.correlationId").isString());
    }

    @Test
    void raceGoalCreationRequiresExistingRunnerProfile() throws Exception {
        String accessToken = registerAndGetAccessToken("no-profile@example.com");

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("HALF_MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(8), "FINISH", null)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("profile"))
            .andExpect(jsonPath("$.error.details[0].issue").value("missing"));
    }

    @Test
    void unsupportedRaceDistanceRejected() throws Exception {
        String accessToken = registerAndGetAccessToken("unsupported-distance@example.com");
        onboard(accessToken);

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("TEN_K", LocalDate.now(ZoneId.of("UTC")).plusWeeks(8), "FINISH", null)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("raceDistanceType"))
            .andExpect(jsonPath("$.error.details[0].issue").value("unsupported"));
    }

    @Test
    void halfMarathonDateLessThanEightWeeksRejected() throws Exception {
        String accessToken = registerAndGetAccessToken("half-too-soon@example.com");
        onboard(accessToken);

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("HALF_MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(7), "FINISH", null)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("raceDate"))
            .andExpect(jsonPath("$.error.details[0].issue").value("too_soon"));
    }

    @Test
    void marathonDateLessThanTwelveWeeksRejected() throws Exception {
        String accessToken = registerAndGetAccessToken("marathon-too-soon@example.com");
        onboard(accessToken);

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(11), "PB", null)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("raceDate"))
            .andExpect(jsonPath("$.error.details[0].issue").value("too_soon"));
    }

    @Test
    void duplicateActiveGoalRejected() throws Exception {
        String accessToken = registerAndGetAccessToken("duplicate-goal@example.com");
        onboard(accessToken);

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("HALF_MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(8), "FINISH", 7000)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(12), "PB", 14000)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code").value("CONFLICT"))
            .andExpect(jsonPath("$.error.details[0].field").value("raceGoal"))
            .andExpect(jsonPath("$.error.details[0].issue").value("active_goal_exists"));
    }

    @Test
    void halfMarathonDateExactlyEightWeeksAccepted() throws Exception {
        String accessToken = registerAndGetAccessToken("half-boundary@example.com");
        onboard(accessToken);

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("HALF_MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(8), "FINISH", null)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.raceGoalId").isString())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void marathonDateExactlyTwelveWeeksAccepted() throws Exception {
        String accessToken = registerAndGetAccessToken("marathon-boundary@example.com");
        onboard(accessToken);

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(12), "IMPROVE", 14500)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.raceGoalId").isString())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void userCanCreateNewActiveGoalAfterPreviousGoalIsArchived() throws Exception {
        String accessToken = registerAndGetAccessToken("archived-goal@example.com");
        onboard(accessToken);

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("HALF_MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(8), "FINISH", 7000)))
            .andExpect(status().isOk());

        var existingGoal = raceGoalRepository.findAll().stream().findFirst().orElseThrow();
        existingGoal.setStatus(RaceGoalStatus.ARCHIVED);
        raceGoalRepository.saveAndFlush(existingGoal);

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(12), "IMPROVE", 15000)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.raceGoalId").isString())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void positiveTargetTimeAccepted() throws Exception {
        String accessToken = registerAndGetAccessToken("positive-target@example.com");
        onboard(accessToken);

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(12), "IMPROVE", 13680)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.raceGoalId").isString());
    }

    @Test
    void negativeOrZeroTargetTimeRejected() throws Exception {
        String accessToken = registerAndGetAccessToken("invalid-target@example.com");
        onboard(accessToken);

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(12), "IMPROVE", 0)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("targetTimeSeconds"));

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(12), "IMPROVE", -1)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("targetTimeSeconds"));
    }

    @Test
    void getCurrentGoalReturnsActiveGoal() throws Exception {
        String accessToken = registerAndGetAccessToken("current-goal@example.com");
        onboard(accessToken);
        LocalDate raceDate = LocalDate.now(ZoneId.of("UTC")).plusWeeks(8);

        String createResponse = mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("HALF_MARATHON", raceDate, "FINISH", 7000)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String raceGoalId = objectMapper.readTree(createResponse).get("raceGoalId").asText();

        mockMvc.perform(get("/v1/race-goals/current")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.raceGoalId").value(raceGoalId))
            .andExpect(jsonPath("$.raceDistanceType").value("HALF_MARATHON"))
            .andExpect(jsonPath("$.raceDate").value(raceDate.toString()))
            .andExpect(jsonPath("$.goalStyle").value("FINISH"))
            .andExpect(jsonPath("$.targetTimeSeconds").value(7000))
            .andExpect(jsonPath("$.raceName").doesNotExist())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void unauthenticatedGetCurrentGoalRejected() throws Exception {
        mockMvc.perform(get("/v1/race-goals/current"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
            .andExpect(jsonPath("$.error.message").isString())
            .andExpect(jsonPath("$.error.correlationId").isString());
    }

    @Test
    void getCurrentGoalDoesNotReturnAnotherUsersGoal() throws Exception {
        String userOneToken = registerAndGetAccessToken("owner-goal@example.com");
        onboard(userOneToken);
        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + userOneToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("HALF_MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(8), "FINISH", null)))
            .andExpect(status().isOk());

        String userTwoToken = registerAndGetAccessToken("other-user@example.com");
        onboard(userTwoToken);

        mockMvc.perform(get("/v1/race-goals/current")
                .header("Authorization", "Bearer " + userTwoToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void invalidGoalStyleRejectedAndErrorEnvelopeIsConsistent() throws Exception {
        String accessToken = registerAndGetAccessToken("invalid-style@example.com");
        onboard(accessToken);

        mockMvc.perform(post("/v1/race-goals")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(raceGoalPayload("HALF_MARATHON", LocalDate.now(ZoneId.of("UTC")).plusWeeks(8), "RACE", null)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.message").isString())
            .andExpect(jsonPath("$.error.details[0].field").value("goalStyle"))
            .andExpect(jsonPath("$.error.details[0].issue").value("invalid"))
            .andExpect(jsonPath("$.error.correlationId").isString());
    }

    private String registerAndGetAccessToken(String email) throws Exception {
        String response = mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"email\":\"" + email + "\"," +
                    "\"password\":\"StrongPassword123!\"," +
                    "\"timezone\":\"Europe/Berlin\"" +
                    "}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        return node.get("accessToken").asText();
    }

    private void onboard(String accessToken) throws Exception {
        mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "profile": {
                        "birthYear": 1990,
                        "sex": "MALE",
                        "experienceLevel": "BEGINNER",
                        "typicalWeeklyDistanceKm": 25.0,
                        "longestRecentRunKm": 10.0,
                        "preferredRunDays": ["TUESDAY", "THURSDAY", "SATURDAY"],
                        "preferredLongRunDay": "SATURDAY",
                        "goalStyle": "FINISH",
                        "injuryHistory": {
                          "hadRunningInjuryLast12Months": false
                        },
                        "strengthDaysPerWeek": 1,
                        "units": "KM",
                        "timezone": "Europe/Berlin"
                      }
                    }
                    """))
            .andExpect(status().isOk());
    }

    private String raceGoalPayload(String raceDistanceType, LocalDate raceDate, String goalStyle, Integer targetTimeSeconds) {
        String targetTime = targetTimeSeconds == null ? "null" : targetTimeSeconds.toString();
        return "{" +
            "\"raceName\":\"Berlin Goal\"," +
            "\"raceDistanceType\":\"" + raceDistanceType + "\"," +
            "\"raceDate\":\"" + raceDate + "\"," +
            "\"goalStyle\":\"" + goalStyle + "\"," +
            "\"targetTimeSeconds\":" + targetTime +
            "}";
    }
}
