package com.company.runcoach.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "runcoach.security.jwt")
public record JwtProperties(String issuer, long accessTokenTtlMinutes, long refreshTokenTtlDays, String secret) {
}
