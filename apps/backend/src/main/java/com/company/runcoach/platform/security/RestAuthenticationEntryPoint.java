package com.company.runcoach.platform.security;

import com.company.runcoach.common.api.ApiError;
import com.company.runcoach.common.api.ApiErrorDetail;
import com.company.runcoach.common.api.ApiErrorEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorEnvelope envelope = new ApiErrorEnvelope(new ApiError(
            "UNAUTHORIZED",
            "Authentication required.",
            List.of(new ApiErrorDetail("authorization", "missing_or_invalid")),
            UUID.randomUUID().toString()
        ));
        objectMapper.writeValue(response.getOutputStream(), envelope);
    }
}
