package com.company.runcoach.integrations.strava.client;

public interface StravaClient {
    TokenExchangeResponse exchangeCode(String code);
    TokenRefreshResponse refreshToken(String refreshToken);
    void deauthorize(String accessToken);
}
