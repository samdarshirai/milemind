package com.company.runcoach.platform.api;

import com.company.runcoach.common.api.ApiErrorEnvelope;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiErrorContractTest {

    @Test
    void noResourceExceptionShouldMapToBaselineErrorShape() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        HttpServletRequest request = new MockHttpServletRequest("GET", "/v1/unknown-route");

        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/v1/unknown-route");
        ResponseEntity<ApiErrorEnvelope> response = handler.handleNoResourceFound(ex, request);

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("NOT_FOUND", response.getBody().error().code());
        assertEquals(ex.getMessage(), response.getBody().error().message());
        assertNotNull(response.getBody().error().details());
        assertFalse(response.getBody().error().correlationId().isBlank());
    }
}
