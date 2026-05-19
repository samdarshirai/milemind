package com.company.runcoach.identity.api;

import com.company.runcoach.app.RunCoachApplication;
import com.company.runcoach.identity.domain.AppUser;
import com.company.runcoach.identity.domain.RefreshToken;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.identity.repo.RefreshTokenRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RunCoachApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void registerSuccessAndPasswordHashed() throws Exception {
        String response = mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"runner@example.com","password":"StrongPassword123!","timezone":"Europe/Berlin"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").isString())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.refreshToken").isString())
            .andExpect(jsonPath("$.onboardingRequired").value(true))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        AppUser user = appUserRepository.findById(UUID.fromString(node.get("userId").asText())).orElseThrow();
        assertNotEquals("StrongPassword123!", user.getPasswordHash());
        assertFalse(user.getPasswordHash().isBlank());
    }

    @Test
    void duplicateEmailRejected() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"runner@example.com","password":"StrongPassword123!","timezone":"Europe/Berlin"}
                """));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"runner@example.com","password":"StrongPassword123!","timezone":"Europe/Berlin"}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code").value("CONFLICT"));
    }

    @Test
    void loginSuccess() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"runner@example.com","password":"StrongPassword123!","timezone":"Europe/Berlin"}
                """));

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"runner@example.com","password":"StrongPassword123!"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.refreshToken").isString())
            .andExpect(jsonPath("$.onboardingRequired").value(false));
    }

    @Test
    void invalidLoginRejected() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"runner@example.com","password":"StrongPassword123!","timezone":"Europe/Berlin"}
                """));

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"runner@example.com","password":"WrongPassword!"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void refreshSuccessAndRotationAndReuseRejected() throws Exception {
        String registerResponse = mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"runner@example.com","password":"StrongPassword123!","timezone":"Europe/Berlin"}
                    """))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String oldRefresh = objectMapper.readTree(registerResponse).get("refreshToken").asText();

        String refreshResponse = mockMvc.perform(post("/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" + "\"refreshToken\":\"" + oldRefresh + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.refreshToken").isString())
            .andReturn().getResponse().getContentAsString();

        String newRefresh = objectMapper.readTree(refreshResponse).get("refreshToken").asText();
        assertNotEquals(oldRefresh, newRefresh);

        mockMvc.perform(post("/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" + "\"refreshToken\":\"" + oldRefresh + "\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        List<RefreshToken> tokens = refreshTokenRepository.findAll().stream()
            .sorted(Comparator.comparing(RefreshToken::getCreatedAt))
            .toList();
        assertNotNull(tokens.get(0).getRevokedAt());
        assertNotNull(tokens.get(0).getRotatedToTokenId());
        assertNull(tokens.get(1).getRevokedAt());
    }

    @Test
    void revokedRefreshTokenRejected() throws Exception {
        String registerResponse = mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"runner2@example.com","password":"StrongPassword123!","timezone":"Europe/Berlin"}
                    """))
            .andReturn().getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(registerResponse).get("refreshToken").asText();

        RefreshToken stored = refreshTokenRepository.findAll().getFirst();
        stored.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepository.save(stored);

        mockMvc.perform(post("/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" + "\"refreshToken\":\"" + refreshToken + "\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void logoutRevokesRefreshTokenAndPreventsFutureRefresh() throws Exception {
        String registerResponse = mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"runner3@example.com","password":"StrongPassword123!","timezone":"Europe/Berlin"}
                    """))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode authNode = objectMapper.readTree(registerResponse);
        String accessToken = authNode.get("accessToken").asText();
        String refreshToken = authNode.get("refreshToken").asText();

        mockMvc.perform(post("/v1/auth/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" + "\"refreshToken\":\"" + refreshToken + "\"}"))
            .andExpect(status().isNoContent());

        mockMvc.perform(post("/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" + "\"refreshToken\":\"" + refreshToken + "\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        RefreshToken stored = refreshTokenRepository.findAll().getFirst();
        assertNotNull(stored.getRevokedAt());
    }

    @Test
    void logoutWithoutAccessTokenRejected() throws Exception {
        mockMvc.perform(post("/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"refreshToken":"abc"}
                    """))
            .andExpect(status().isUnauthorized());
    }
}
