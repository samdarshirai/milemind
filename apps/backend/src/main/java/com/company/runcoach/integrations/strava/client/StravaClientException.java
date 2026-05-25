package com.company.runcoach.integrations.strava.client;

public class StravaClientException extends RuntimeException {

    private final boolean unauthorized;

    public StravaClientException(String message, boolean unauthorized) {
        super(message);
        this.unauthorized = unauthorized;
    }

    public boolean isUnauthorized() {
        return unauthorized;
    }
}
