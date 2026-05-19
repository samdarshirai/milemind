package com.company.runcoach.identity.api;

public record TokenRefreshResponse(String accessToken, String refreshToken) {
}
