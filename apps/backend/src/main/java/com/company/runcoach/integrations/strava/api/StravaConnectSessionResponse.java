package com.company.runcoach.integrations.strava.api;

public record StravaConnectSessionResponse(
    String authorizationUrl,
    String state
) {
}
