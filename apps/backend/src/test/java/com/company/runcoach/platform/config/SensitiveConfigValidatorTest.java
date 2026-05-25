package com.company.runcoach.platform.config;

import com.company.runcoach.identity.config.JwtProperties;
import com.company.runcoach.integrations.strava.config.StravaProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SensitiveConfigValidatorTest {

    @Test
    void allowsLocalProfilePlaceholders() {
        SensitiveConfigValidator validator = new SensitiveConfigValidator(
            stravaProps("change-me", "change-me-secret"),
            new JwtProperties("issuer", 15, 30, "change-me-jwt"),
            new MockEnvironment().withProperty("spring.profiles.active", "local")
        );

        assertDoesNotThrow(validator::validate);
    }

    @Test
    void rejectsPlaceholderSecretsOutsideLocalAndTest() {
        SensitiveConfigValidator validator = new SensitiveConfigValidator(
            stravaProps("change-me", "change-me-secret"),
            new JwtProperties("issuer", 15, 30, "change-me-jwt"),
            new MockEnvironment().withProperty("spring.profiles.active", "prod")
        );

        assertThrows(IllegalStateException.class, validator::validate);
    }

    @Test
    void allowsNonPlaceholderSecretsOutsideLocalAndTest() {
        SensitiveConfigValidator validator = new SensitiveConfigValidator(
            stravaProps("s3cr3t-client", "s3cr3t-token-enc"),
            new JwtProperties("issuer", 15, 30, "s3cr3t-jwt-key"),
            new MockEnvironment().withProperty("spring.profiles.active", "prod")
        );

        assertDoesNotThrow(validator::validate);
    }

    private static StravaProperties stravaProps(String clientSecret, String tokenEncryptionSecret) {
        return new StravaProperties(
            "client-id",
            clientSecret,
            "http://localhost/callback",
            "https://www.strava.com/oauth/authorize",
            "https://www.strava.com/oauth/token",
            "https://www.strava.com/oauth/deauthorize",
            Duration.ofMinutes(10),
            "auto",
            "read,activity:read",
            Duration.ofMinutes(2),
            tokenEncryptionSecret
        );
    }

}
