package com.company.runcoach.identity.service;

import com.company.runcoach.common.api.ApiErrorDetail;
import com.company.runcoach.common.api.ApiException;
import com.company.runcoach.identity.api.AuthResponse;
import com.company.runcoach.identity.api.LoginRequest;
import com.company.runcoach.identity.api.RegisterRequest;
import com.company.runcoach.identity.api.TokenRefreshResponse;
import com.company.runcoach.identity.domain.AppUser;
import com.company.runcoach.identity.domain.RefreshToken;
import com.company.runcoach.identity.domain.UserStatus;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.identity.repo.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenHasher refreshTokenHasher;

    public AuthService(
        AppUserRepository appUserRepository,
        RefreshTokenRepository refreshTokenRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        RefreshTokenHasher refreshTokenHasher
    ) {
        this.appUserRepository = appUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenHasher = refreshTokenHasher;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ApiException("CONFLICT", "Email already registered.", HttpStatus.CONFLICT,
                List.of(new ApiErrorDetail("email", "already_exists")));
        }
        validateTimezone(request.timezone());

        OffsetDateTime now = OffsetDateTime.now();
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        user.setLocale("en-US");
        user.setTimezone(request.timezone());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        appUserRepository.save(user);

        TokenPair tokens = createSession(user, now);
        return new AuthResponse(user.getId(), tokens.accessToken(), tokens.refreshToken(), true);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmailIgnoreCase(request.email().trim())
            .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        TokenPair tokens = createSession(user, OffsetDateTime.now());
        return new AuthResponse(user.getId(), tokens.accessToken(), tokens.refreshToken(), false);
    }

    @Transactional
    public TokenRefreshResponse refresh(String refreshTokenValue) {
        parseRefreshTokenClaims(refreshTokenValue);

        String tokenHash = refreshTokenHasher.hash(refreshTokenValue);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(this::invalidRefreshToken);

        OffsetDateTime now = OffsetDateTime.now();
        if (!existing.isUsableAt(now)) {
            throw invalidRefreshToken();
        }

        existing.setRevokedAt(now);
        TokenPair newTokens = createSession(existing.getUser(), now);
        RefreshToken next = refreshTokenRepository.findByTokenHash(refreshTokenHasher.hash(newTokens.refreshToken()))
            .orElseThrow();
        existing.setRotatedToTokenId(next.getId());
        refreshTokenRepository.save(existing);

        return new TokenRefreshResponse(newTokens.accessToken(), newTokens.refreshToken());
    }

    @Transactional
    public void logout(UUID userId, String refreshTokenValue) {
        Claims claims = parseRefreshTokenClaims(refreshTokenValue);
        UUID tokenSubjectUserId;
        try {
            tokenSubjectUserId = UUID.fromString(claims.getSubject());
        } catch (IllegalArgumentException ex) {
            throw invalidRefreshToken();
        }
        if (!userId.equals(tokenSubjectUserId)) {
            throw invalidRefreshToken();
        }

        String tokenHash = refreshTokenHasher.hash(refreshTokenValue);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(this::invalidRefreshToken);
        if (!userId.equals(token.getUser().getId())) {
            throw invalidRefreshToken();
        }

        if (token.getRevokedAt() == null) {
            token.setRevokedAt(OffsetDateTime.now());
            refreshTokenRepository.save(token);
        }
    }

    private TokenPair createSession(AppUser user, OffsetDateTime now) {
        UUID refreshTokenId = UUID.randomUUID();
        String accessToken = jwtService.issueAccessToken(user.getId());
        String refreshTokenValue = jwtService.issueRefreshToken(user.getId(), refreshTokenId);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(refreshTokenId);
        refreshToken.setUser(user);
        refreshToken.setTokenHash(refreshTokenHasher.hash(refreshTokenValue));
        refreshToken.setCreatedAt(now);
        refreshToken.setExpiresAt(claimsExpiry(refreshTokenValue));
        refreshTokenRepository.save(refreshToken);

        return new TokenPair(accessToken, refreshTokenValue);
    }

    private OffsetDateTime claimsExpiry(String token) {
        Claims claims = jwtService.parse(token);
        return OffsetDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.of("UTC"));
    }

    private void validateTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
        } catch (Exception ex) {
            throw new ApiException("VALIDATION_ERROR", "Unsupported timezone.", HttpStatus.BAD_REQUEST,
                List.of(new ApiErrorDetail("timezone", "unsupported")));
        }
    }

    private ApiException invalidCredentials() {
        return new ApiException("UNAUTHORIZED", "Invalid email or password.", HttpStatus.UNAUTHORIZED,
            List.of(new ApiErrorDetail("credentials", "invalid")));
    }

    private Claims parseRefreshTokenClaims(String refreshTokenValue) {
        Claims claims;
        try {
            claims = jwtService.parse(refreshTokenValue);
        } catch (JwtException | IllegalArgumentException ex) {
            throw invalidRefreshToken();
        }
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw invalidRefreshToken();
        }
        return claims;
    }

    private ApiException invalidRefreshToken() {
        return new ApiException("UNAUTHORIZED", "Invalid refresh token.", HttpStatus.UNAUTHORIZED,
            List.of(new ApiErrorDetail("refreshToken", "invalid_or_expired")));
    }
}
