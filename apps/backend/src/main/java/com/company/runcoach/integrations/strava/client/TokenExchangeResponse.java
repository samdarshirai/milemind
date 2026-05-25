package com.company.runcoach.integrations.strava.client;

public record TokenExchangeResponse(
    String accessToken,
    String refreshToken,
    long expiresAt,
    String scope,
    Athlete athlete
) {
    public record Athlete(Long id, String username, String firstname, String lastname) {
    }
}
