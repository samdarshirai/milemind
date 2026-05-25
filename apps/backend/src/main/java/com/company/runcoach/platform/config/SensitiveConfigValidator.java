package com.company.runcoach.platform.config;

import com.company.runcoach.identity.config.JwtProperties;
import com.company.runcoach.integrations.strava.config.StravaProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
public class SensitiveConfigValidator {

    private final StravaProperties stravaProperties;
    private final JwtProperties jwtProperties;
    private final Environment environment;

    public SensitiveConfigValidator(
        StravaProperties stravaProperties,
        JwtProperties jwtProperties,
        Environment environment
    ) {
        this.stravaProperties = stravaProperties;
        this.jwtProperties = jwtProperties;
        this.environment = environment;
    }

    @PostConstruct
    void validate() {
        if (environment.acceptsProfiles(Profiles.of("local", "test"))) {
            return;
        }
        assertSafe("runcoach.integrations.strava.client-secret", stravaProperties.clientSecret());
        assertSafe("runcoach.integrations.strava.token-encryption-secret", stravaProperties.tokenEncryptionSecret());
        assertSafe("runcoach.security.jwt.secret", jwtProperties.secret());
    }

    private void assertSafe(String key, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required secret configuration: " + key);
        }
        String normalized = value.toLowerCase();
        if (normalized.contains("change-me") || normalized.startsWith("test-") || normalized.startsWith("local-dev-")) {
            throw new IllegalStateException("Unsafe placeholder secret configured for: " + key);
        }
    }
}
