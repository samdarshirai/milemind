package com.company.runcoach.integrations.strava.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StravaStatusResponse(
    boolean connected,
    String connectionStatus,
    List<String> grantedScopes,
    OffsetDateTime lastSyncAt
) {
    public static StravaStatusResponse disconnected() {
        return new StravaStatusResponse(false, "DISCONNECTED", List.of(), null);
    }
}
