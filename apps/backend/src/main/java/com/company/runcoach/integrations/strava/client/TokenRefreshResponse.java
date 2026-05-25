package com.company.runcoach.integrations.strava.client;

public record TokenRefreshResponse(String accessToken, String refreshToken, long expiresAt) {
}
