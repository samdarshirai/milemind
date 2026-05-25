package com.company.runcoach.integrations.strava.client;

import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.util.Map;
import java.nio.charset.StandardCharsets;

public class DefaultStravaClient implements StravaClient {

    private final RestClient restClient;
    private final String tokenUrl;
    private final String deauthorizeUrl;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public DefaultStravaClient(
        RestClient restClient,
        String tokenUrl,
        String deauthorizeUrl,
        String clientId,
        String clientSecret,
        String redirectUri
    ) {
        this.restClient = restClient;
        this.tokenUrl = tokenUrl;
        this.deauthorizeUrl = deauthorizeUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @Override
    public TokenExchangeResponse exchangeCode(String code) {
        try {
            Map<?, ?> body = restClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&code=" + UriUtils.encode(code, StandardCharsets.UTF_8) +
                    "&grant_type=authorization_code" +
                    "&redirect_uri=" + UriUtils.encode(redirectUri, StandardCharsets.UTF_8))
                .retrieve()
                .body(Map.class);
            return toExchange(body);
        } catch (HttpStatusCodeException ex) {
            throw new StravaClientException("Strava token exchange failed.", ex.getStatusCode().value() == 401);
        }
    }

    @Override
    public TokenRefreshResponse refreshToken(String refreshToken) {
        try {
            Map<?, ?> body = restClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&grant_type=refresh_token" +
                    "&refresh_token=" + UriUtils.encode(refreshToken, StandardCharsets.UTF_8))
                .retrieve()
                .body(Map.class);
            return toRefresh(body);
        } catch (HttpStatusCodeException ex) {
            throw new StravaClientException("Strava token refresh failed.", ex.getStatusCode().value() == 401);
        }
    }

    @Override
    public void deauthorize(String accessToken) {
        try {
            restClient.post()
                .uri(deauthorizeUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("access_token=" + UriUtils.encode(accessToken, StandardCharsets.UTF_8))
                .retrieve()
                .toBodilessEntity();
        } catch (HttpStatusCodeException ex) {
            throw new StravaClientException("Strava deauthorize failed.", ex.getStatusCode().value() == 401);
        }
    }

    @SuppressWarnings("unchecked")
    private TokenExchangeResponse toExchange(Map<?, ?> body) {
        Map<String, Object> athlete = (Map<String, Object>) body.get("athlete");
        TokenExchangeResponse.Athlete tokenAthlete = new TokenExchangeResponse.Athlete(
            athlete.get("id") == null ? null : Long.parseLong(athlete.get("id").toString()),
            (String) athlete.get("username"),
            (String) athlete.get("firstname"),
            (String) athlete.get("lastname")
        );
        return new TokenExchangeResponse(
            String.valueOf(body.get("access_token")),
            String.valueOf(body.get("refresh_token")),
            Long.parseLong(String.valueOf(body.get("expires_at"))),
            body.get("scope") == null ? "" : String.valueOf(body.get("scope")),
            tokenAthlete
        );
    }

    private TokenRefreshResponse toRefresh(Map<?, ?> body) {
        return new TokenRefreshResponse(
            String.valueOf(body.get("access_token")),
            String.valueOf(body.get("refresh_token")),
            Long.parseLong(String.valueOf(body.get("expires_at"))));
    }
}
