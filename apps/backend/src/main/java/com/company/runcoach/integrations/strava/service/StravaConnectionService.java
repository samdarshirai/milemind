package com.company.runcoach.integrations.strava.service;

import com.company.runcoach.common.api.ApiErrorDetail;
import com.company.runcoach.common.api.ApiException;
import com.company.runcoach.identity.domain.AppUser;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.integrations.strava.api.StravaConnectSessionResponse;
import com.company.runcoach.integrations.strava.api.StravaStatusResponse;
import com.company.runcoach.integrations.strava.client.StravaClient;
import com.company.runcoach.integrations.strava.client.StravaClientException;
import com.company.runcoach.integrations.strava.client.TokenExchangeResponse;
import com.company.runcoach.integrations.strava.config.AppProperties;
import com.company.runcoach.integrations.strava.config.StravaProperties;
import com.company.runcoach.integrations.strava.domain.StravaConnection;
import com.company.runcoach.integrations.strava.domain.StravaOauthSession;
import com.company.runcoach.integrations.strava.repo.StravaConnectionRepository;
import com.company.runcoach.integrations.strava.repo.StravaOauthSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class StravaConnectionService {
    private static final Logger log = LoggerFactory.getLogger(StravaConnectionService.class);
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DISCONNECTED = "DISCONNECTED";

    private final AppUserRepository appUserRepository;
    private final StravaOauthSessionRepository oauthSessionRepository;
    private final StravaConnectionRepository connectionRepository;
    private final StateHasher stateHasher;
    private final StravaProperties stravaProperties;
    private final AppProperties appProperties;
    private final StravaClient stravaClient;
    private final TokenCryptoService tokenCryptoService;
    private final StravaTokenService stravaTokenService;
    private final SecureRandom secureRandom = new SecureRandom();

    public StravaConnectionService(
        AppUserRepository appUserRepository,
        StravaOauthSessionRepository oauthSessionRepository,
        StravaConnectionRepository connectionRepository,
        StateHasher stateHasher,
        StravaProperties stravaProperties,
        AppProperties appProperties,
        StravaClient stravaClient,
        TokenCryptoService tokenCryptoService,
        StravaTokenService stravaTokenService
    ) {
        this.appUserRepository = appUserRepository;
        this.oauthSessionRepository = oauthSessionRepository;
        this.connectionRepository = connectionRepository;
        this.stateHasher = stateHasher;
        this.stravaProperties = stravaProperties;
        this.appProperties = appProperties;
        this.stravaClient = stravaClient;
        this.tokenCryptoService = tokenCryptoService;
        this.stravaTokenService = stravaTokenService;
    }

    @Transactional
    public StravaConnectSessionResponse createConnectSession(UUID userId) {
        AppUser user = appUserRepository.findById(userId)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "User not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("userId", "not_found"))));

        String state = generateState();
        OffsetDateTime now = OffsetDateTime.now();

        StravaOauthSession session = new StravaOauthSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setStateHash(stateHasher.hash(state));
        session.setCreatedAt(now);
        session.setExpiresAt(now.plus(stravaProperties.oauthStateTtl()));
        session.setConsumed(false);
        oauthSessionRepository.save(session);

        String authorizationUrl = UriComponentsBuilder
            .fromUriString(stravaProperties.authorizationUrl())
            .queryParam("client_id", stravaProperties.clientId())
            .queryParam("redirect_uri", stravaProperties.redirectUri())
            .queryParam("response_type", "code")
            .queryParam("approval_prompt", stravaProperties.approvalPrompt())
            .queryParam("scope", stravaProperties.scope())
            .queryParam("state", state)
            .build(true)
            .toUriString();

        return new StravaConnectSessionResponse(authorizationUrl, state);
    }

    @Transactional
    public String handleCallback(String state, String code, String error) {
        String base = appProperties.androidStravaReturnDeeplink();
        if (state == null || state.isBlank()) {
            return redirect(base, "error", "invalid_state");
        }

        StravaOauthSession session = oauthSessionRepository.findByStateHash(stateHasher.hash(state)).orElse(null);
        if (session == null || session.isConsumed() || session.getExpiresAt().isBefore(OffsetDateTime.now())) {
            return redirect(base, "error", "invalid_state");
        }

        if (error != null && !error.isBlank()) {
            consumeSession(session);
            if (isAuthorizationDenied(error)) {
                return redirect(base, "cancelled", "authorization_denied");
            }
            return redirect(base, "error", "authorization_failed");
        }

        if (code == null || code.isBlank()) {
            consumeSession(session);
            return redirect(base, "error", "missing_code");
        }

        try {
            TokenExchangeResponse token = stravaClient.exchangeCode(code);
            if (token.athlete() == null || token.athlete().id() == null || token.athlete().id() <= 0L) {
                consumeSession(session);
                return redirect(base, "error", "invalid_athlete_payload");
            }
            upsertConnection(session.getUser(), token);
            consumeSession(session);
            return redirect(base, "success", null);
        } catch (StravaClientException ex) {
            consumeSession(session);
            return redirect(base, "error", "token_exchange_failed");
        }
    }

    @Transactional(readOnly = true)
    public StravaStatusResponse status(UUID userId) {
        StravaConnection connection = connectionRepository.findFirstByUser_IdAndDisconnectedAtIsNull(userId).orElse(null);
        if (connection == null) {
            return StravaStatusResponse.disconnected();
        }

        return new StravaStatusResponse(
            true,
            normalizedStatus(connection),
            splitScopes(connection.getScopes()),
            connection.getLastSyncAt()
        );
    }

    @Transactional
    public StravaStatusResponse disconnect(UUID userId) {
        StravaConnection connection = connectionRepository.findFirstByUser_IdAndDisconnectedAtIsNull(userId).orElse(null);
        if (connection == null) {
            return StravaStatusResponse.disconnected();
        }

        try {
            String token = stravaTokenService.getValidAccessToken(userId);
            if (token != null) {
                stravaClient.deauthorize(token);
            }
        } catch (StravaClientException ex) {
            if (!ex.isUnauthorized()) {
                log.warn("Strava deauthorize failed; proceeding with local disconnect. userId={}", userId, ex);
            }
        }

        stravaTokenService.disconnectLocally(userId);
        return StravaStatusResponse.disconnected();
    }

    private void upsertConnection(AppUser user, TokenExchangeResponse token) {
        StravaConnection connection = connectionRepository.findFirstByUser_IdAndDisconnectedAtIsNull(user.getId())
            .orElseGet(StravaConnection::new);
        OffsetDateTime now = OffsetDateTime.now();

        if (connection.getId() == null) {
            connection.setId(UUID.randomUUID());
            connection.setCreatedAt(now);
            connection.setConnectedAt(now);
            connection.setUser(user);
        }

        connection.setDisconnectedAt(null);
        connection.setStravaAthleteId(token.athlete().id());
        connection.setAthleteUsername(token.athlete().username());
        connection.setAthleteFirstName(token.athlete().firstname());
        connection.setAthleteLastName(token.athlete().lastname());
        connection.setAccessTokenEncrypted(tokenCryptoService.encrypt(token.accessToken()));
        connection.setRefreshTokenEncrypted(tokenCryptoService.encrypt(token.refreshToken()));
        connection.setTokenExpiresAt(OffsetDateTime.ofInstant(Instant.ofEpochSecond(token.expiresAt()), ZoneOffset.UTC));
        connection.setConnectionStatus(STATUS_ACTIVE);
        connection.setScopes(token.scope() == null || token.scope().isBlank() ? stravaProperties.scope() : token.scope());
        connection.setUpdatedAt(now);
        connectionRepository.save(connection);
    }

    private String redirect(String base, String result, String reason) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(base).queryParam("result", result);
        if (reason != null) {
            builder.queryParam("reason", reason);
        }
        return builder.build(true).toUriString();
    }

    private boolean isAuthorizationDenied(String error) {
        return "access_denied".equalsIgnoreCase(error.trim());
    }

    private String generateState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void consumeSession(StravaOauthSession session) {
        session.setConsumed(true);
        session.setConsumedAt(OffsetDateTime.now());
        oauthSessionRepository.save(session);
    }

    private List<String> splitScopes(String scopes) {
        if (scopes == null || scopes.isBlank()) {
            return List.of();
        }
        return Stream.of(scopes.split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .toList();
    }

    private String normalizedStatus(StravaConnection connection) {
        String persistedStatus = connection.getConnectionStatus();
        if (persistedStatus != null && !persistedStatus.isBlank()) {
            return persistedStatus;
        }
        return connection.getDisconnectedAt() == null ? STATUS_ACTIVE : STATUS_DISCONNECTED;
    }
}
