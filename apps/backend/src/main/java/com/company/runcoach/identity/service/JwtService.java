package com.company.runcoach.identity.service;

import com.company.runcoach.identity.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

@Service
public class JwtService {

    private final JwtProperties properties;
    private SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueAccessToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(userId.toString())
            .issuer(properties.issuer())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(properties.accessTokenTtlMinutes(), ChronoUnit.MINUTES)))
            .claim("type", "access")
            .signWith(key)
            .compact();
    }

    public String issueRefreshToken(UUID userId, UUID tokenId) {
        Instant now = Instant.now();
        return Jwts.builder()
            .id(tokenId.toString())
            .subject(userId.toString())
            .issuer(properties.issuer())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(properties.refreshTokenTtlDays(), ChronoUnit.DAYS)))
            .claim("type", "refresh")
            .signWith(key)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
