package com.company.runcoach.integrations.strava.api;

import com.company.runcoach.RunCoachApplication;
import com.company.runcoach.identity.domain.AppUser;
import com.company.runcoach.identity.domain.UserStatus;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.identity.repo.RefreshTokenRepository;
import com.company.runcoach.integrations.strava.domain.StravaConnection;
import com.company.runcoach.integrations.strava.domain.StravaOauthSession;
import com.company.runcoach.integrations.strava.service.StateHasher;
import com.company.runcoach.integrations.strava.repo.StravaConnectionRepository;
import com.company.runcoach.integrations.strava.repo.StravaOauthSessionRepository;
import com.company.runcoach.integrations.strava.client.StravaClient;
import com.company.runcoach.integrations.strava.client.StravaClientException;
import com.company.runcoach.integrations.strava.client.TokenExchangeResponse;
import com.company.runcoach.integrations.strava.client.TokenRefreshResponse;
import com.company.runcoach.profile.repo.RunnerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(
    classes = {RunCoachApplication.class, StravaControllerIntegrationTest.StravaTestConfig.class},
    properties = "spring.main.allow-bean-definition-overriding=true"
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StravaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private RunnerProfileRepository runnerProfileRepository;
    @Autowired
    private StravaConnectionRepository stravaConnectionRepository;
    @Autowired
    private StravaOauthSessionRepository stravaOauthSessionRepository;
    @Autowired
    private StateHasher stateHasher;
    @Autowired
    private StravaClient stravaClient;

    @BeforeEach
    void setUp() {
        stravaOauthSessionRepository.deleteAll();
        stravaConnectionRepository.deleteAll();
        runnerProfileRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        appUserRepository.deleteAll();
        ((StubStravaClient) stravaClient).reset();
    }

    @Test
    void connectSessionRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/v1/integrations/strava/connect-session"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void connectSessionReturnsAuthorizationUrlAndState() throws Exception {
        String token = registerAndGetAccessToken("strava-connect@example.com");

        mockMvc.perform(post("/v1/integrations/strava/connect-session").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authorizationUrl").exists())
            .andExpect(jsonPath("$.state").isNotEmpty());
    }

    @Test
    void statusFalseWhenDisconnected() throws Exception {
        String token = registerAndGetAccessToken("strava-status-off@example.com");

        mockMvc.perform(get("/v1/integrations/strava/status").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.connected").value(false))
            .andExpect(jsonPath("$.connectionStatus").value("DISCONNECTED"))
            .andExpect(jsonPath("$.grantedScopes").isArray());
    }

    @Test
    void statusRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/v1/integrations/strava/status"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void statusTrueWhenConnected() throws Exception {
        String token = registerAndGetAccessToken("strava-status-on@example.com");
        AppUser user = appUserRepository.findByEmailIgnoreCase("strava-status-on@example.com").orElseThrow();

        StravaConnection connection = new StravaConnection();
        connection.setId(UUID.randomUUID());
        connection.setUser(user);
        connection.setStravaAthleteId(123456L);
        connection.setAthleteFirstName("Test");
        connection.setAthleteLastName("Runner");
        connection.setAccessTokenEncrypted("x");
        connection.setRefreshTokenEncrypted("y");
        connection.setTokenExpiresAt(OffsetDateTime.now().plusHours(1));
        connection.setConnectionStatus("ACTIVE");
        connection.setLastSyncAt(OffsetDateTime.parse("2026-05-21T10:15:30Z"));
        connection.setScopes("read,activity:read");
        connection.setConnectedAt(OffsetDateTime.now().minusMinutes(1));
        connection.setCreatedAt(OffsetDateTime.now().minusMinutes(1));
        connection.setUpdatedAt(OffsetDateTime.now().minusMinutes(1));
        stravaConnectionRepository.save(connection);

        mockMvc.perform(get("/v1/integrations/strava/status").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.connected").value(true))
            .andExpect(jsonPath("$.connectionStatus").value("ACTIVE"))
            .andExpect(jsonPath("$.grantedScopes[0]").value("read"))
            .andExpect(jsonPath("$.grantedScopes[1]").value("activity:read"))
            .andExpect(jsonPath("$.lastSyncAt").value("2026-05-21T10:15:30Z"));
    }

    @Test
    void deleteConnectionIsIdempotent() throws Exception {
        String token = registerAndGetAccessToken("strava-delete@example.com");

        mockMvc.perform(delete("/v1/integrations/strava/connection").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.connected").value(false))
            .andExpect(jsonPath("$.connectionStatus").value("DISCONNECTED"));

        mockMvc.perform(delete("/v1/integrations/strava/connection").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.connected").value(false))
            .andExpect(jsonPath("$.connectionStatus").value("DISCONNECTED"));
    }

    @Test
    void deleteConnectionRequiresAuthentication() throws Exception {
        mockMvc.perform(delete("/v1/integrations/strava/connection"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void callbackWithMissingStateRedirectsInvalidState() throws Exception {
        mockMvc.perform(get("/v1/integrations/strava/callback").param("code", "oauth-code"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", containsString("result=error")))
            .andExpect(header().string("Location", containsString("reason=invalid_state")));
    }

    @Test
    void callbackWithAccessDeniedRedirectsCancelled() throws Exception {
        AppUser user = createUser("strava-callback-denied@example.com");
        createOauthSession(user, "denied-state");

        mockMvc.perform(get("/v1/integrations/strava/callback")
                .param("state", "denied-state")
                .param("error", "access_denied"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", containsString("result=cancelled")))
            .andExpect(header().string("Location", containsString("reason=authorization_denied")));
    }

    @Test
    void callbackSuccessRedirectsSuccess() throws Exception {
        AppUser user = createUser("strava-callback-success@example.com");
        createOauthSession(user, "ok-state");

        mockMvc.perform(get("/v1/integrations/strava/callback")
                .param("state", "ok-state")
                .param("code", "oauth-code"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", containsString("result=success")));
    }

    private String registerAndGetAccessToken(String email) throws Exception {
        String response = mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s","password":"StrongPassword123!","timezone":"Europe/Berlin"}
                    """.formatted(email)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        int start = response.indexOf("\"accessToken\":\"") + 15;
        int end = response.indexOf('"', start);
        return response.substring(start, end);
    }

    private AppUser createUser(String email) {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("pw");
        user.setStatus(UserStatus.ACTIVE);
        user.setLocale("en-US");
        user.setTimezone("Europe/Berlin");
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return appUserRepository.save(user);
    }

    private void createOauthSession(AppUser user, String rawState) {
        StravaOauthSession session = new StravaOauthSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setStateHash(stateHasher.hash(rawState));
        session.setCreatedAt(OffsetDateTime.now());
        session.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        session.setConsumed(false);
        stravaOauthSessionRepository.save(session);
    }

    @TestConfiguration
    static class StravaTestConfig {
        @Bean
        @Primary
        StravaClient stravaClient() {
            return new StubStravaClient();
        }
    }

    static class StubStravaClient implements StravaClient {
        void reset() {
            // No-op for now; kept for parity with other Strava integration tests.
        }

        @Override
        public TokenExchangeResponse exchangeCode(String code) {
            return new TokenExchangeResponse(
                "oauth-access",
                "oauth-refresh",
                OffsetDateTime.now().plusHours(1).toEpochSecond(),
                "read,activity:read",
                new TokenExchangeResponse.Athlete(88L, "runner", "Test", "Athlete")
            );
        }

        @Override
        public TokenRefreshResponse refreshToken(String refreshToken) {
            return new TokenRefreshResponse(
                "rotated-access",
                "rotated-refresh",
                OffsetDateTime.now().plusHours(1).toEpochSecond()
            );
        }

        @Override
        public void deauthorize(String accessToken) {
            throw new StravaClientException("Not used in this test.", false);
        }
    }
}
