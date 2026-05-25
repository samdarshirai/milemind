package com.company.runcoach.integrations.strava.service;

import com.company.runcoach.integrations.strava.client.StravaClient;
import com.company.runcoach.integrations.strava.client.TokenRefreshResponse;
import com.company.runcoach.integrations.strava.config.StravaProperties;
import com.company.runcoach.integrations.strava.domain.StravaConnection;
import com.company.runcoach.integrations.strava.repo.StravaConnectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class StravaTokenService {
    private static final String STATUS_DISCONNECTED = "DISCONNECTED";

    private final StravaConnectionRepository connectionRepository;
    private final TokenCryptoService tokenCryptoService;
    private final StravaClient stravaClient;
    private final StravaProperties properties;

    public StravaTokenService(
        StravaConnectionRepository connectionRepository,
        TokenCryptoService tokenCryptoService,
        StravaClient stravaClient,
        StravaProperties properties
    ) {
        this.connectionRepository = connectionRepository;
        this.tokenCryptoService = tokenCryptoService;
        this.stravaClient = stravaClient;
        this.properties = properties;
    }

    @Transactional
    public String getValidAccessToken(UUID userId) {
        StravaConnection connection = connectionRepository.findFirstByUser_IdAndDisconnectedAtIsNull(userId)
            .orElse(null);
        if (connection == null) {
            return null;
        }

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime refreshThreshold = now.plus(properties.tokenRefreshSkew());
        if (connection.getTokenExpiresAt().isAfter(refreshThreshold)) {
            return tokenCryptoService.decrypt(connection.getAccessTokenEncrypted());
        }

        String refreshToken = tokenCryptoService.decrypt(connection.getRefreshTokenEncrypted());
        TokenRefreshResponse refreshed = stravaClient.refreshToken(refreshToken);
        connection.setAccessTokenEncrypted(tokenCryptoService.encrypt(refreshed.accessToken()));
        connection.setRefreshTokenEncrypted(tokenCryptoService.encrypt(refreshed.refreshToken()));
        connection.setTokenExpiresAt(OffsetDateTime.ofInstant(Instant.ofEpochSecond(refreshed.expiresAt()), ZoneOffset.UTC));
        connection.setUpdatedAt(now);
        connectionRepository.save(connection);
        return refreshed.accessToken();
    }

    @Transactional
    public void disconnectLocally(UUID userId) {
        StravaConnection connection = connectionRepository.findFirstByUser_IdAndDisconnectedAtIsNull(userId).orElse(null);
        if (connection == null) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        connection.setDisconnectedAt(now);
        connection.setAccessTokenEncrypted(tokenCryptoService.encrypt("revoked"));
        connection.setRefreshTokenEncrypted(tokenCryptoService.encrypt("revoked"));
        connection.setConnectionStatus(STATUS_DISCONNECTED);
        connection.setTokenExpiresAt(now);
        connection.setUpdatedAt(now);
        connectionRepository.save(connection);
    }
}
