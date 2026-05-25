package com.company.runcoach.integrations.strava.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "runcoach.app")
public record AppProperties(String androidStravaReturnDeeplink) {
}
