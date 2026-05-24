package com.company.runcoach.insights.api;

import com.company.runcoach.RunCoachApplication;
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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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

    @BeforeEach
    void setUp() {
        injuryFeedbackRepository.deleteAll();
        fatigueSignalRepository.deleteAll();
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

        mockMvc.perform(get("/v1/insights/today")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value(today.toString()))
            .andExpect(jsonPath("$.readinessState").value("HIGH_RISK"))
            .andExpect(jsonPath("$.hasCheckInToday").value(true))
            .andExpect(jsonPath("$.latestFatigueSignal.signalDate").value(today.toString()))
            .andExpect(jsonPath("$.recommendedTone").isString());
    }

    @Test
    void todayInsightDateUsesUserTimezoneCalendarDay() throws Exception {
        String token = registerAndGetAccessToken("la-today@example.com", "America/Los_Angeles");
        LocalDate laToday = LocalDate.now(ZoneId.of("America/Los_Angeles"));

        mockMvc.perform(get("/v1/insights/today")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value(laToday.toString()));
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
