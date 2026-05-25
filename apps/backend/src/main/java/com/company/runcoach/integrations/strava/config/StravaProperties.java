package com.company.runcoach.integrations.strava.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "runcoach.integrations.strava")
public record StravaProperties(
    String clientId,
    String clientSecret,
    String redirectUri,
    String authorizationUrl,
    String tokenUrl,
    String deauthorizeUrl,
    Duration oauthStateTtl,
    String approvalPrompt,
    String scope,
    Duration tokenRefreshSkew,
    String tokenEncryptionSecret
) {
}
