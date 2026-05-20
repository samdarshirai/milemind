package com.company.runcoach.profile.api;

import com.company.runcoach.app.RunCoachApplication;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.identity.repo.RefreshTokenRepository;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RunCoachApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RunnerProfileRepository runnerProfileRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        runnerProfileRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void authenticatedUserCanCreateAndUpsertOnboardingProfile() throws Exception {
        String accessToken = registerAndGetAccessToken("runner1@example.com");

        String initialResponse = mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validOnboardingPayload(1992, "BEGINNER", "KM", 1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").isString())
            .andExpect(jsonPath("$.profileId").isString())
            .andReturn().getResponse().getContentAsString();

        String firstProfileId = objectMapper.readTree(initialResponse).get("profileId").asText();

        String secondResponse = mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validOnboardingPayload(1992, "ADVANCED", "MILES", 2)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String secondProfileId = objectMapper.readTree(secondResponse).get("profileId").asText();
        Assertions.assertEquals(firstProfileId, secondProfileId);
    }

    @Test
    void onboardingResponseSchema_containsOnlyUserAndProfileIdentifiers() throws Exception {
        String accessToken = registerAndGetAccessToken("runner-schema@example.com");

        String response = mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validOnboardingPayload(1992, "BEGINNER", "KM", 1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").isString())
            .andExpect(jsonPath("$.profileId").isString())
            .andReturn().getResponse().getContentAsString();

        JsonNode payload = objectMapper.readTree(response);
        Assertions.assertEquals(2, payload.size());
        Assertions.assertTrue(payload.has("userId"));
        Assertions.assertTrue(payload.has("profileId"));
    }

    @Test
    void unauthenticatedOnboardingRequestRejected() throws Exception {
        mockMvc.perform(post("/v1/users/onboarding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validOnboardingPayload(1992, "BEGINNER", "KM", 1)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
            .andExpect(jsonPath("$.error.correlationId").isString());
    }

    @Test
    void unauthenticatedProfileUpdateRejected() throws Exception {
        mockMvc.perform(put("/v1/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "preferredRunDays": ["MONDAY", "WEDNESDAY", "SATURDAY"],
                      "preferredLongRunDay": "SATURDAY",
                      "strengthDaysPerWeek": 1,
                      "units": "KM"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
            .andExpect(jsonPath("$.error.correlationId").isString());
    }

    @Test
    void unauthenticatedProfileFetchRejected() throws Exception {
        mockMvc.perform(get("/v1/profile"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
            .andExpect(jsonPath("$.error.correlationId").isString());
    }

    @Test
    void repositoryRejectsEmptyPreferredRunDaysAtDatabaseConstraintLevel() throws Exception {
        String accessToken = registerAndGetAccessToken("runner-empty-days-constraint@example.com");
        mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validOnboardingPayload(1990, "BEGINNER", "KM", 1)))
            .andExpect(status().isOk());

        var saved = runnerProfileRepository.findAll().stream().findFirst().orElseThrow();
        saved.setPreferredRunDays(List.of());

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> runnerProfileRepository.saveAndFlush(saved));
    }

    @Test
    void userCanFetchOwnProfile() throws Exception {
        String accessToken = registerAndGetAccessToken("runner2@example.com");
        mockMvc.perform(post("/v1/users/onboarding")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validOnboardingPayload(1990, "INTERMEDIATE", "KM", 1)));

        mockMvc.perform(get("/v1/profile")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile.experienceLevel").value("INTERMEDIATE"))
            .andExpect(jsonPath("$.profile.units").value("KM"));
    }

    @Test
    void userCanUpdateMutableProfileFields() throws Exception {
        String accessToken = registerAndGetAccessToken("runner3@example.com");
        mockMvc.perform(post("/v1/users/onboarding")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validOnboardingPayload(1990, "INTERMEDIATE", "KM", 1)));

        mockMvc.perform(put("/v1/profile")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "preferredRunDays": ["MONDAY", "WEDNESDAY", "SATURDAY"],
                      "preferredLongRunDay": "SATURDAY",
                      "strengthDaysPerWeek": 2,
                      "units": "MILES",
                      "timezone": "America/New_York",
                      "injuryHistory": {
                        "hadRunningInjuryLast12Months": true,
                        "summary": "Left calf tightness in April."
                      }
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.timezone").value("America/New_York"))
            .andExpect(jsonPath("$.profile.preferredLongRunDay").value("SATURDAY"))
            .andExpect(jsonPath("$.profile.strengthDaysPerWeek").value(2))
            .andExpect(jsonPath("$.profile.units").value("MILES"))
            .andExpect(jsonPath("$.profile.injuryHistory.hadRunningInjuryLast12Months").value(true))
            .andExpect(jsonPath("$.profile.injuryHistory.summary").value("Left calf tightness in April."));
    }

    @Test
    void profileUpdate_allowsExplicitInjuryHistoryClearWithNull() throws Exception {
        String accessToken = registerAndGetAccessToken("runner-clear-injury@example.com");
        mockMvc.perform(post("/v1/users/onboarding")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validOnboardingPayload(1990, "INTERMEDIATE", "KM", 1)));

        mockMvc.perform(put("/v1/profile")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "preferredRunDays": ["MONDAY", "WEDNESDAY", "SATURDAY"],
                      "preferredLongRunDay": "SATURDAY",
                      "strengthDaysPerWeek": 2,
                      "units": "MILES",
                      "injuryHistory": {
                        "hadRunningInjuryLast12Months": true,
                        "summary": "Left calf tightness in April."
                      }
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile.injuryHistory.hadRunningInjuryLast12Months").value(true));

        mockMvc.perform(put("/v1/profile")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "preferredRunDays": ["MONDAY", "WEDNESDAY", "SATURDAY"],
                      "preferredLongRunDay": "SATURDAY",
                      "strengthDaysPerWeek": 2,
                      "units": "MILES",
                      "injuryHistory": null
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile.injuryHistory").doesNotExist());
    }

    @Test
    void profileUpdate_omittedInjuryHistoryLeavesExistingValueUnchanged() throws Exception {
        String accessToken = registerAndGetAccessToken("runner-omit-injury@example.com");
        mockMvc.perform(post("/v1/users/onboarding")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validOnboardingPayload(1990, "INTERMEDIATE", "KM", 1)));

        mockMvc.perform(put("/v1/profile")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "preferredRunDays": ["MONDAY", "WEDNESDAY", "SATURDAY"],
                      "preferredLongRunDay": "SATURDAY",
                      "strengthDaysPerWeek": 2,
                      "units": "MILES",
                      "injuryHistory": {
                        "hadRunningInjuryLast12Months": true,
                        "summary": "Left calf tightness in April."
                      }
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(put("/v1/profile")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "preferredRunDays": ["MONDAY", "THURSDAY", "SUNDAY"],
                      "preferredLongRunDay": "SUNDAY",
                      "strengthDaysPerWeek": 1,
                      "units": "KM"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile.injuryHistory.hadRunningInjuryLast12Months").value(true))
            .andExpect(jsonPath("$.profile.injuryHistory.summary").value("Left calf tightness in April."));
    }

    @Test
    void onboardingRejectsInvalidInjuryHistoryBooleanType() throws Exception {
        String accessToken = registerAndGetAccessToken("runner-injury-onboarding-invalid@example.com");

        mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "profile": {
                        "birthYear": 1992,
                        "sex": "FEMALE",
                        "experienceLevel": "BEGINNER",
                        "typicalWeeklyDistanceKm": 30.0,
                        "longestRecentRunKm": 10.0,
                        "preferredRunDays": ["MONDAY", "WEDNESDAY", "SUNDAY"],
                        "preferredLongRunDay": "SUNDAY",
                        "goalStyle": "FINISH",
                        "injuryHistory": {
                          "hadRunningInjuryLast12Months": "yes"
                        },
                        "strengthDaysPerWeek": 1,
                        "units": "KM"
                      }
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("injuryHistory.hadRunningInjuryLast12Months"))
            .andExpect(jsonPath("$.error.correlationId").isString());
    }

    @Test
    void profileUpdateRejectsInvalidInjuryHistorySummaryType() throws Exception {
        String accessToken = registerAndGetAccessToken("runner-injury-update-invalid@example.com");
        mockMvc.perform(post("/v1/users/onboarding")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validOnboardingPayload(1990, "INTERMEDIATE", "KM", 1)));

        mockMvc.perform(put("/v1/profile")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "preferredRunDays": ["MONDAY", "WEDNESDAY", "SATURDAY"],
                      "preferredLongRunDay": "SATURDAY",
                      "strengthDaysPerWeek": 2,
                      "units": "MILES",
                      "injuryHistory": {
                        "summary": 42
                      }
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("injuryHistory.summary"))
            .andExpect(jsonPath("$.error.correlationId").isString());
    }

    @Test
    void profileUpdateRejectsUnsupportedInjuryHistoryKeys() throws Exception {
        String accessToken = registerAndGetAccessToken("runner-injury-update-invalid-key@example.com");
        mockMvc.perform(post("/v1/users/onboarding")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validOnboardingPayload(1990, "INTERMEDIATE", "KM", 1)));

        mockMvc.perform(put("/v1/profile")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "preferredRunDays": ["MONDAY", "WEDNESDAY", "SATURDAY"],
                      "preferredLongRunDay": "SATURDAY",
                      "strengthDaysPerWeek": 2,
                      "units": "MILES",
                      "injuryHistory": {
                        "summary": "Recovered.",
                        "extraFlag": true
                      }
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("injuryHistory"))
            .andExpect(jsonPath("$.error.details[0].issue").value("invalid_key"));
    }

    @Test
    void profileUpdate_keepsOnboardingFieldsImmutable() throws Exception {
        String accessToken = registerAndGetAccessToken("runner3b@example.com");
        mockMvc.perform(post("/v1/users/onboarding")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validOnboardingPayload(1990, "INTERMEDIATE", "KM", 1)));

        String beforeResponse = mockMvc.perform(get("/v1/profile")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        JsonNode before = objectMapper.readTree(beforeResponse).get("profile");

        String afterResponse = mockMvc.perform(put("/v1/profile")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "preferredRunDays": ["MONDAY", "WEDNESDAY", "SATURDAY"],
                      "preferredLongRunDay": "SATURDAY",
                      "strengthDaysPerWeek": 2,
                      "units": "MILES",
                      "timezone": "America/New_York",
                      "injuryHistory": {
                        "summary": "Updated details."
                      }
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        JsonNode after = objectMapper.readTree(afterResponse).get("profile");

        Assertions.assertEquals(before.get("birthYear").asInt(), after.get("birthYear").asInt());
        Assertions.assertEquals(before.get("experienceLevel").asText(), after.get("experienceLevel").asText());
        Assertions.assertEquals(before.get("typicalWeeklyDistanceKm").asDouble(), after.get("typicalWeeklyDistanceKm").asDouble());
        Assertions.assertEquals(before.get("longestRecentRunKm").asDouble(), after.get("longestRecentRunKm").asDouble());
        Assertions.assertEquals("SATURDAY", after.get("preferredLongRunDay").asText());
        Assertions.assertEquals(2, after.get("strengthDaysPerWeek").asInt());
    }

    @Test
    void under18BirthYearRejected() throws Exception {
        String accessToken = registerAndGetAccessToken("runner4@example.com");

        mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validOnboardingPayload(2012, "BEGINNER", "KM", 1)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("birthYear"));
    }

    @Test
    void emptyPreferredRunDaysRejected() throws Exception {
        String accessToken = registerAndGetAccessToken("runner5@example.com");

        mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "profile": {
                        "birthYear": 1992,
                        "sex": "FEMALE",
                        "experienceLevel": "BEGINNER",
                        "typicalWeeklyDistanceKm": 30.0,
                        "longestRecentRunKm": 10.0,
                        "preferredRunDays": [],
                        "preferredLongRunDay": "SUNDAY",
                        "goalStyle": "FINISH",
                        "strengthDaysPerWeek": 1,
                        "units": "KM"
                      }
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void tooFewPreferredRunDaysRejected() throws Exception {
        String accessToken = registerAndGetAccessToken("runner5b@example.com");

        mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "profile": {
                        "birthYear": 1992,
                        "sex": "FEMALE",
                        "experienceLevel": "BEGINNER",
                        "typicalWeeklyDistanceKm": 30.0,
                        "longestRecentRunKm": 10.0,
                        "preferredRunDays": ["MONDAY", "WEDNESDAY"],
                        "preferredLongRunDay": "SUNDAY",
                        "goalStyle": "FINISH",
                        "strengthDaysPerWeek": 1,
                        "units": "KM"
                      }
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("preferredRunDays"));
    }

    @Test
    void onboardingRejectsLongRunDayOutsidePreferredRunDays() throws Exception {
        String accessToken = registerAndGetAccessToken("runner-longrun-onboarding@example.com");

        mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "profile": {
                        "birthYear": 1992,
                        "sex": "FEMALE",
                        "experienceLevel": "BEGINNER",
                        "typicalWeeklyDistanceKm": 30.0,
                        "longestRecentRunKm": 10.0,
                        "preferredRunDays": ["MONDAY", "WEDNESDAY", "FRIDAY"],
                        "preferredLongRunDay": "SUNDAY",
                        "goalStyle": "FINISH",
                        "strengthDaysPerWeek": 1,
                        "units": "KM"
                      }
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("preferredLongRunDay"))
            .andExpect(jsonPath("$.error.details[0].issue").value("not_in_preferred_run_days"));
    }

    @Test
    void profileUpdateRejectsLongRunDayOutsidePreferredRunDays() throws Exception {
        String accessToken = registerAndGetAccessToken("runner-longrun-update@example.com");
        mockMvc.perform(post("/v1/users/onboarding")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validOnboardingPayload(1990, "INTERMEDIATE", "KM", 1)));

        mockMvc.perform(put("/v1/profile")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "preferredRunDays": ["MONDAY", "WEDNESDAY", "SATURDAY"],
                      "preferredLongRunDay": "SUNDAY",
                      "strengthDaysPerWeek": 1,
                      "units": "KM"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("preferredLongRunDay"))
            .andExpect(jsonPath("$.error.details[0].issue").value("not_in_preferred_run_days"));
    }

    @Test
    void invalidExperienceLevelRejected() throws Exception {
        String accessToken = registerAndGetAccessToken("runner6@example.com");

        mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validOnboardingPayload(1992, "ELITE", "KM", 1)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("experienceLevel"));
    }

    @Test
    void invalidUnitsRejected() throws Exception {
        String accessToken = registerAndGetAccessToken("runner7@example.com");

        mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validOnboardingPayload(1992, "BEGINNER", "YARDS", 1)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("units"));
    }

    @Test
    void invalidStrengthDaysRejected() throws Exception {
        String accessToken = registerAndGetAccessToken("runner8@example.com");

        mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validOnboardingPayload(1992, "BEGINNER", "KM", 4)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("strengthDaysPerWeek"));
    }

    @Test
    void getAndPutReturnNotFoundWhenProfileMissing() throws Exception {
        String accessToken = registerAndGetAccessToken("runner9@example.com");

        mockMvc.perform(get("/v1/profile")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));

        mockMvc.perform(put("/v1/profile")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "preferredRunDays": ["MONDAY", "WEDNESDAY", "SATURDAY"],
                      "preferredLongRunDay": "SATURDAY",
                      "strengthDaysPerWeek": 1,
                      "units": "KM"
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void invalidTimezoneRejectedForOnboardingAndProfileUpdate() throws Exception {
        String onboardingToken = registerAndGetAccessToken("runner10@example.com");

        mockMvc.perform(post("/v1/users/onboarding")
                .header("Authorization", "Bearer " + onboardingToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "profile": {
                        "birthYear": 1992,
                        "sex": "FEMALE",
                        "experienceLevel": "BEGINNER",
                        "typicalWeeklyDistanceKm": 30.0,
                        "longestRecentRunKm": 10.0,
                        "preferredRunDays": ["MONDAY", "WEDNESDAY", "SUNDAY"],
                        "preferredLongRunDay": "SUNDAY",
                        "goalStyle": "FINISH",
                        "strengthDaysPerWeek": 1,
                        "units": "KM",
                        "timezone": "Mars/Olympus_Mons"
                      }
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("timezone"));

        String updateToken = registerAndGetAccessToken("runner11@example.com");
        mockMvc.perform(post("/v1/users/onboarding")
            .header("Authorization", "Bearer " + updateToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validOnboardingPayload(1990, "INTERMEDIATE", "KM", 1)));

        mockMvc.perform(put("/v1/profile")
                .header("Authorization", "Bearer " + updateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "preferredRunDays": ["MONDAY", "WEDNESDAY", "SATURDAY"],
                      "preferredLongRunDay": "SATURDAY",
                      "strengthDaysPerWeek": 1,
                      "units": "KM",
                      "timezone": "Invalid/Zone"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("timezone"));
    }

    @Test
    void missingOwnProfileReturnsNotFoundEvenWhenAnotherUserProfileExists() throws Exception {
        String userAToken = registerAndGetAccessToken("runner12@example.com");
        String userBToken = registerAndGetAccessToken("runner13@example.com");

        mockMvc.perform(post("/v1/users/onboarding")
            .header("Authorization", "Bearer " + userAToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validOnboardingPayload(1991, "BEGINNER", "KM", 1)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/v1/profile")
                .header("Authorization", "Bearer " + userBToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));

        mockMvc.perform(put("/v1/profile")
                .header("Authorization", "Bearer " + userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "preferredRunDays": ["MONDAY", "WEDNESDAY", "SATURDAY"],
                      "preferredLongRunDay": "SATURDAY",
                      "strengthDaysPerWeek": 1,
                      "units": "KM"
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    private String registerAndGetAccessToken(String email) throws Exception {
        String response = mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "password": "StrongPassword123!",
                      "timezone": "Europe/Berlin"
                    }
                    """.formatted(email)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        return node.get("accessToken").asText();
    }

    private String validOnboardingPayload(int birthYear, String experienceLevel, String units, int strengthDays) {
        return """
            {
              "profile": {
                "birthYear": %d,
                "sex": "FEMALE",
                "experienceLevel": "%s",
                "typicalWeeklyDistanceKm": 30.0,
                "longestRecentRunKm": 12.5,
                "preferredRunDays": ["TUESDAY", "THURSDAY", "SUNDAY"],
                "preferredLongRunDay": "SUNDAY",
                "goalStyle": "FINISH",
                "injuryHistory": {
                  "hadRunningInjuryLast12Months": false
                },
                "strengthDaysPerWeek": %d,
                "units": "%s",
                "timezone": "Europe/Berlin"
              }
            }
            """.formatted(birthYear, experienceLevel, strengthDays, units);
    }
}
