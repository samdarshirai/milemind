package com.company.runcoach.integrations.strava.service;

import com.company.runcoach.RunCoachApplication;
import com.company.runcoach.identity.domain.AppUser;
import com.company.runcoach.identity.domain.UserStatus;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.integrations.strava.client.StravaClient;
import com.company.runcoach.integrations.strava.client.StravaClientException;
import com.company.runcoach.integrations.strava.client.TokenExchangeResponse;
import com.company.runcoach.integrations.strava.client.TokenRefreshResponse;
import com.company.runcoach.integrations.strava.domain.StravaConnection;
import com.company.runcoach.integrations.strava.domain.StravaOauthSession;
import com.company.runcoach.integrations.strava.repo.StravaConnectionRepository;
import com.company.runcoach.integrations.strava.repo.StravaOauthSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    classes = {RunCoachApplication.class, StravaServiceIntegrationTest.StravaTestConfig.class},
    properties = "spring.main.allow-bean-definition-overriding=true"
)
@ActiveProfiles("test")
class StravaServiceIntegrationTest {

    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private StravaOauthSessionRepository oauthSessionRepository;
    @Autowired
    private StravaConnectionRepository connectionRepository;
    @Autowired
    private StravaConnectionService connectionService;
    @Autowired
    private StravaTokenService tokenService;
    @Autowired
    private TokenCryptoService tokenCryptoService;
    @Autowired
    private StravaClient stravaClient;

    @BeforeEach
    void setUp() {
        oauthSessionRepository.deleteAll();
        connectionRepository.deleteAll();
        appUserRepository.deleteAll();
        ((StubStravaClient) stravaClient).reset();
    }

    @Test
    void callbackWithExpiredStateIsRejected() {
        AppUser user = createUser("expired-state@example.com");
        StravaOauthSession session = new StravaOauthSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setStateHash(new StateHasher().hash("expired-state"));
        session.setCreatedAt(OffsetDateTime.now().minusMinutes(20));
        session.setExpiresAt(OffsetDateTime.now().minusMinutes(1));
        session.setConsumed(false);
        oauthSessionRepository.save(session);

        String redirect = connectionService.handleCallback("expired-state", "oauth-code", null);

        assertTrue(redirect.contains("result=error"));
        assertEquals(0, connectionRepository.count());
    }

    @Test
    void callbackWithUnknownStateIsRejected() {
        createUser("unknown-state@example.com");

        String redirect = connectionService.handleCallback("does-not-exist", "oauth-code", null);

        assertTrue(redirect.contains("result=error"));
        assertTrue(redirect.contains("reason=invalid_state"));
        assertEquals(0, connectionRepository.count());
    }

    @Test
    void callbackWithConsumedStateIsRejected() {
        AppUser user = createUser("consumed-state@example.com");
        StravaOauthSession session = new StravaOauthSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setStateHash(new StateHasher().hash("consumed-state"));
        session.setCreatedAt(OffsetDateTime.now().minusMinutes(1));
        session.setExpiresAt(OffsetDateTime.now().plusMinutes(9));
        session.setConsumed(true);
        session.setConsumedAt(OffsetDateTime.now().minusSeconds(10));
        oauthSessionRepository.save(session);

        String redirect = connectionService.handleCallback("consumed-state", "oauth-code", null);

        assertTrue(redirect.contains("result=error"));
        assertTrue(redirect.contains("reason=invalid_state"));
        assertEquals(0, connectionRepository.count());
    }

    @Test
    void callbackSuccessStoresEncryptedTokensAndConsumesState() {
        AppUser user = createUser("callback-success@example.com");
        StravaOauthSession session = new StravaOauthSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setStateHash(new StateHasher().hash("ok-state"));
        session.setCreatedAt(OffsetDateTime.now());
        session.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        session.setConsumed(false);
        oauthSessionRepository.save(session);

        String redirect = connectionService.handleCallback("ok-state", "oauth-code", null);

        assertTrue(redirect.contains("result=success"));
        StravaConnection connection = connectionRepository.findFirstByUser_IdAndDisconnectedAtIsNull(user.getId()).orElseThrow();
        assertEquals("oauth-access", tokenCryptoService.decrypt(connection.getAccessTokenEncrypted()));
        assertEquals("oauth-refresh", tokenCryptoService.decrypt(connection.getRefreshTokenEncrypted()));

        StravaOauthSession savedSession = oauthSessionRepository.findById(session.getId()).orElseThrow();
        assertTrue(savedSession.isConsumed());
    }

    @Test
    void refreshRotationStoresNewRefreshToken() {
        AppUser user = createUser("rotation@example.com");

        StravaConnection connection = new StravaConnection();
        connection.setId(UUID.randomUUID());
        connection.setUser(user);
        connection.setStravaAthleteId(1001L);
        connection.setAthleteFirstName("Rot");
        connection.setAthleteLastName("Ation");
        connection.setAccessTokenEncrypted(tokenCryptoService.encrypt("old-access"));
        connection.setRefreshTokenEncrypted(tokenCryptoService.encrypt("old-refresh"));
        connection.setConnectionStatus("ACTIVE");
        connection.setTokenExpiresAt(OffsetDateTime.now().plusSeconds(10));
        connection.setScopes("read,activity:read");
        connection.setConnectedAt(OffsetDateTime.now());
        connection.setCreatedAt(OffsetDateTime.now());
        connection.setUpdatedAt(OffsetDateTime.now());
        connectionRepository.save(connection);

        String token = tokenService.getValidAccessToken(user.getId());

        assertEquals("rotated-access", token);
        StravaConnection updated = connectionRepository.findById(connection.getId()).orElseThrow();
        assertEquals("rotated-refresh", tokenCryptoService.decrypt(updated.getRefreshTokenEncrypted()));
    }

    @Test
    void callbackStateIsSingleUseAfterSuccess() {
        AppUser user = createUser("single-use@example.com");
        StravaOauthSession session = new StravaOauthSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setStateHash(new StateHasher().hash("single-state"));
        session.setCreatedAt(OffsetDateTime.now());
        session.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        session.setConsumed(false);
        oauthSessionRepository.save(session);

        String first = connectionService.handleCallback("single-state", "oauth-code", null);
        String second = connectionService.handleCallback("single-state", "oauth-code", null);

        assertTrue(first.contains("result=success"));
        assertTrue(second.contains("reason=invalid_state"));
    }

    @Test
    void callbackMissingCodeConsumesState() {
        AppUser user = createUser("missing-code@example.com");
        StravaOauthSession session = new StravaOauthSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setStateHash(new StateHasher().hash("missing-code-state"));
        session.setCreatedAt(OffsetDateTime.now());
        session.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        session.setConsumed(false);
        oauthSessionRepository.save(session);

        String redirect = connectionService.handleCallback("missing-code-state", null, null);
        StravaOauthSession savedSession = oauthSessionRepository.findById(session.getId()).orElseThrow();

        assertTrue(redirect.contains("reason=missing_code"));
        assertTrue(savedSession.isConsumed());
    }

    @Test
    void callbackAuthorizationDeniedReturnsCancelled() {
        AppUser user = createUser("denied@example.com");
        StravaOauthSession session = new StravaOauthSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setStateHash(new StateHasher().hash("denied-state"));
        session.setCreatedAt(OffsetDateTime.now());
        session.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        session.setConsumed(false);
        oauthSessionRepository.save(session);

        String redirect = connectionService.handleCallback("denied-state", null, "access_denied");
        StravaOauthSession savedSession = oauthSessionRepository.findById(session.getId()).orElseThrow();

        assertTrue(redirect.contains("result=cancelled"));
        assertTrue(redirect.contains("reason=authorization_denied"));
        assertTrue(savedSession.isConsumed());
    }

    @Test
    void callbackTokenExchangeFailureConsumesState() {
        AppUser user = createUser("exchange-failure@example.com");
        StravaOauthSession session = new StravaOauthSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setStateHash(new StateHasher().hash("exchange-failure-state"));
        session.setCreatedAt(OffsetDateTime.now());
        session.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        session.setConsumed(false);
        oauthSessionRepository.save(session);
        ((StubStravaClient) stravaClient).failExchange = true;

        String redirect = connectionService.handleCallback("exchange-failure-state", "oauth-code", null);
        StravaOauthSession savedSession = oauthSessionRepository.findById(session.getId()).orElseThrow();

        assertTrue(redirect.contains("reason=token_exchange_failed"));
        assertTrue(savedSession.isConsumed());
    }

    @Test
    void callbackWithMissingAthleteIdIsRejected() {
        AppUser user = createUser("invalid-athlete@example.com");
        StravaOauthSession session = new StravaOauthSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setStateHash(new StateHasher().hash("invalid-athlete-state"));
        session.setCreatedAt(OffsetDateTime.now());
        session.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        session.setConsumed(false);
        oauthSessionRepository.save(session);
        ((StubStravaClient) stravaClient).nullAthleteId = true;

        String redirect = connectionService.handleCallback("invalid-athlete-state", "oauth-code", null);

        assertTrue(redirect.contains("result=error"));
        assertTrue(redirect.contains("reason=invalid_athlete_payload"));
        assertEquals(0, connectionRepository.count());
    }

    @Test
    void disconnectStillCleansUpWhenRemoteDeauthorizeFails() {
        AppUser user = createUser("disconnect-cleanup@example.com");
        ((StubStravaClient) stravaClient).failDeauthorize = true;

        StravaConnection connection = new StravaConnection();
        connection.setId(UUID.randomUUID());
        connection.setUser(user);
        connection.setStravaAthleteId(1002L);
        connection.setAthleteFirstName("Dis");
        connection.setAthleteLastName("Connect");
        connection.setAccessTokenEncrypted(tokenCryptoService.encrypt("old-access"));
        connection.setRefreshTokenEncrypted(tokenCryptoService.encrypt("old-refresh"));
        connection.setConnectionStatus("ACTIVE");
        connection.setTokenExpiresAt(OffsetDateTime.now().plusHours(1));
        connection.setScopes("read,activity:read");
        connection.setConnectedAt(OffsetDateTime.now());
        connection.setCreatedAt(OffsetDateTime.now());
        connection.setUpdatedAt(OffsetDateTime.now());
        connectionRepository.save(connection);

        connectionService.disconnect(user.getId());

        StravaConnection updated = connectionRepository.findById(connection.getId()).orElseThrow();
        assertFalse(updated.isActive());
        assertEquals("DISCONNECTED", updated.getConnectionStatus());
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

    @TestConfiguration
    static class StravaTestConfig {
        @Bean
        @Primary
        StravaClient stravaClient() {
            return new StubStravaClient();
        }
    }

    static class StubStravaClient implements StravaClient {
        boolean failExchange;
        boolean failDeauthorize;
        boolean nullAthleteId;

        void reset() {
            failExchange = false;
            failDeauthorize = false;
            nullAthleteId = false;
        }

        @Override
        public TokenExchangeResponse exchangeCode(String code) {
            if (failExchange) {
                throw new StravaClientException("Strava token exchange failed.", false);
            }
            return new TokenExchangeResponse(
                "oauth-access",
                "oauth-refresh",
                OffsetDateTime.now().plusHours(1).toEpochSecond(),
                "read,activity:read",
                new TokenExchangeResponse.Athlete(nullAthleteId ? null : 88L, "runner", "Test", "Athlete")
            );
        }

        @Override
        public TokenRefreshResponse refreshToken(String refreshToken) {
            return new TokenRefreshResponse("rotated-access", "rotated-refresh", OffsetDateTime.now().plusHours(1).toEpochSecond());
        }

        @Override
        public void deauthorize(String accessToken) {
            if (failDeauthorize) {
                throw new StravaClientException("Strava deauthorize failed.", false);
            }
        }
    }
}
