package com.company.runcoach.adaptation.api;

import com.company.runcoach.RunCoachApplication;
import com.company.runcoach.adaptation.domain.InjuryFeedback;
import com.company.runcoach.adaptation.repo.FatigueSignalRepository;
import com.company.runcoach.adaptation.repo.InjuryFeedbackRepository;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.identity.repo.RefreshTokenRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RunCoachApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CheckInControllerIntegrationTest {

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

    @BeforeEach
    void setUp() {
        injuryFeedbackRepository.deleteAll();
        fatigueSignalRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void creatingFatigueSignalPersistsAndUpsertsPerDate() throws Exception {
        String token = registerAndGetAccessToken("fatigue@example.com");
        LocalDate today = LocalDate.now();

        mockMvc.perform(post("/v1/fatigue-signals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "signalDate": "%s",
                      "sleepScore": 3,
                      "stressScore": 3,
                      "sorenessScore": 2,
                      "motivationScore": 3,
                      "illnessFlag": false,
                      "tooBusyFlag": false,
                      "travellingFlag": false,
                      "notes": "first"
                    }
                    """.formatted(today)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fatigueSignalId").isString())
            .andExpect(jsonPath("$.readinessState").value("CAUTION"));

        mockMvc.perform(post("/v1/fatigue-signals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "signalDate": "%s",
                      "sleepScore": 4,
                      "stressScore": 2,
                      "sorenessScore": 2,
                      "motivationScore": 4,
                      "illnessFlag": false,
                      "tooBusyFlag": false,
                      "travellingFlag": false,
                      "notes": "updated"
                    }
                    """.formatted(today)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.readinessState").value("READY"));

        assertEquals(1, fatigueSignalRepository.count());
        assertEquals("updated", fatigueSignalRepository.findAll().getFirst().getNotes());
    }

    @Test
    void creatingInjuryFeedbackPersists() throws Exception {
        String token = registerAndGetAccessToken("injury@example.com");

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
                      "canRun": false,
                      "redFlag": false,
                      "freeText": "Sharp pain"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.injuryFeedbackId").isString())
            .andExpect(jsonPath("$.readinessState").value("HIGH_RISK"));

        assertEquals(1, injuryFeedbackRepository.count());
    }

    @Test
    void sharpPainDuringRunEscalatesToHighRiskEvenWhenSeverityIsModerate() throws Exception {
        String token = registerAndGetAccessToken("sharp-moderate@example.com");

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-15T08:00:00Z",
                      "bodyRegion": "LEFT_CALF",
                      "painType": "SHARP",
                      "severity": 5,
                      "onsetContext": "DURING_RUN",
                      "canRun": true,
                      "redFlag": false
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.readinessState").value("HIGH_RISK"));
    }

    @Test
    void creatingInjuryFeedbackWithoutPainDetailsUsesNoPainDefaults() throws Exception {
        String token = registerAndGetAccessToken("no-pain@example.com");

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-15T08:00:00Z",
                      "hasPain": false,
                      "freeText": "No pain today."
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.injuryFeedbackId").isString())
            .andExpect(jsonPath("$.readinessState").value("READY"));

        assertEquals(1, injuryFeedbackRepository.count());
        InjuryFeedback saved = injuryFeedbackRepository.findAll().getFirst();
        assertEquals(false, saved.isHasPain());
        assertNull(saved.getSeverity());
    }

    @Test
    void creatingInjuryFeedbackWithoutHasPainRemainsBackwardCompatible() throws Exception {
        String token = registerAndGetAccessToken("no-pain-legacy@example.com");

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-15T08:00:00Z",
                      "freeText": "No pain today."
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.injuryFeedbackId").isString())
            .andExpect(jsonPath("$.readinessState").value("READY"));

        InjuryFeedback saved = injuryFeedbackRepository.findAll().getFirst();
        assertEquals(false, saved.isHasPain());
        assertNull(saved.getSeverity());
    }

    @Test
    void validationFailuresAreReturned() throws Exception {
        String token = registerAndGetAccessToken("validation@example.com");

        mockMvc.perform(post("/v1/fatigue-signals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "signalDate": "2026-06-15",
                      "sleepScore": 6,
                      "stressScore": 1,
                      "sorenessScore": 1,
                      "motivationScore": 1
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-15T08:00:00Z",
                      "bodyRegion": "LEFT_CALF"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[?(@.field=='painType')]").exists())
            .andExpect(jsonPath("$.error.details[?(@.field=='severity')]").exists())
            .andExpect(jsonPath("$.error.details[?(@.field=='onsetContext')]").exists())
            .andExpect(jsonPath("$.error.details[?(@.field=='canRun')]").exists());

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-15T08:00:00Z",
                      "bodyRegion": "LEFT_CALF",
                      "painType": "SHARP",
                      "severity": 11,
                      "onsetContext": "DURING_RUN"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[?(@.field=='severity')]").exists())
            .andExpect(jsonPath("$.error.details[?(@.field=='canRun')]").exists());
    }

    @Test
    void unauthorizedRequestsRejected() throws Exception {
        mockMvc.perform(post("/v1/fatigue-signals")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "signalDate": "2026-06-15",
                      "sleepScore": 3,
                      "stressScore": 3,
                      "sorenessScore": 3,
                      "motivationScore": 3
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mockMvc.perform(post("/v1/injury-feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-15T08:00:00Z",
                      "bodyRegion": "LEFT_CALF",
                      "painType": "SHARP",
                      "severity": 4,
                      "onsetContext": "DURING_RUN"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void injuryFeedbackRejectsInvalidCategoricalValues() throws Exception {
        String token = registerAndGetAccessToken("invalid-categorical@example.com");

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-15T08:00:00Z",
                      "bodyRegion": "FOOT_ARCH",
                      "painType": "BURNING",
                      "severity": 5,
                      "onsetContext": "UNKNOWN_CONTEXT"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[?(@.field=='bodyRegion')]").exists())
            .andExpect(jsonPath("$.error.details[?(@.field=='painType')]").exists())
            .andExpect(jsonPath("$.error.details[?(@.field=='onsetContext')]").exists());
    }

    @Test
    void injuryFeedbackRejectsPainFieldsWhenExplicitNoPain() throws Exception {
        String token = registerAndGetAccessToken("no-pain-conflict@example.com");

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-15T08:00:00Z",
                      "hasPain": false,
                      "severity": 4
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("hasPain"));
    }

    @Test
    void injuryFeedbackRejectsCanRunWithoutHasPain() throws Exception {
        String token = registerAndGetAccessToken("missing-haspain-canrun@example.com");

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-15T08:00:00Z",
                      "canRun": false
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("hasPain"));
    }

    @Test
    void injuryFeedbackRejectsRedFlagWithoutHasPain() throws Exception {
        String token = registerAndGetAccessToken("missing-haspain-redflag@example.com");

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-15T08:00:00Z",
                      "redFlag": true
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details[0].field").value("hasPain"));
    }

    @Test
    void injuryFeedbackSameDaySubmissionsAreAppendOnly() throws Exception {
        String token = registerAndGetAccessToken("append-only@example.com");

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-15T08:00:00Z",
                      "bodyRegion": "LEFT_CALF",
                      "painType": "SHARP",
                      "severity": 6,
                      "onsetContext": "DURING_RUN",
                      "canRun": false
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-15T12:00:00Z",
                      "bodyRegion": "KNEE",
                      "painType": "DULL",
                      "severity": 4,
                      "onsetContext": "AFTER_RUN",
                      "canRun": true
                    }
                    """))
            .andExpect(status().isOk());

        assertEquals(2, injuryFeedbackRepository.count());
    }

    @Test
    void fatigueReadinessIncludesSameLocalDayInjuryForNonUtcTimezone() throws Exception {
        String token = registerAndGetAccessToken("fatigue-timezone@example.com", "America/Los_Angeles");

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-16T06:30:00Z",
                      "bodyRegion": "LEFT_CALF",
                      "painType": "SHARP",
                      "severity": 8,
                      "onsetContext": "DURING_RUN",
                      "canRun": false
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.readinessState").value("HIGH_RISK"));

        mockMvc.perform(post("/v1/fatigue-signals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "signalDate": "2026-06-15",
                      "sleepScore": 4,
                      "stressScore": 2,
                      "sorenessScore": 2,
                      "motivationScore": 4,
                      "illnessFlag": false,
                      "tooBusyFlag": false,
                      "travellingFlag": false
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.readinessState").value("HIGH_RISK"));
    }

    @Test
    void injuryReadinessUsesRunnerLocalDateForFatigueLookup() throws Exception {
        String token = registerAndGetAccessToken("injury-timezone@example.com", "Asia/Tokyo");

        mockMvc.perform(post("/v1/fatigue-signals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "signalDate": "2026-06-15",
                      "sleepScore": 1,
                      "stressScore": 4,
                      "sorenessScore": 4,
                      "motivationScore": 1,
                      "illnessFlag": false,
                      "tooBusyFlag": false,
                      "travellingFlag": false
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.readinessState").value("HIGH_RISK"));

        mockMvc.perform(post("/v1/injury-feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportedAt": "2026-06-14T16:30:00Z",
                      "hasPain": false
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.readinessState").value("HIGH_RISK"));
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
}
