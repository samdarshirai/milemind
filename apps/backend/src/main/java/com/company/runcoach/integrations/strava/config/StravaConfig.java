package com.company.runcoach.integrations.strava.config;

import com.company.runcoach.integrations.strava.client.DefaultStravaClient;
import com.company.runcoach.integrations.strava.client.StravaClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({StravaProperties.class, AppProperties.class})
public class StravaConfig {

    @Bean
    StravaClient stravaClient(StravaProperties properties) {
        return new DefaultStravaClient(
            RestClient.builder().build(),
            properties.tokenUrl(),
            properties.deauthorizeUrl(),
            properties.clientId(),
            properties.clientSecret(),
            properties.redirectUri()
        );
    }
}
