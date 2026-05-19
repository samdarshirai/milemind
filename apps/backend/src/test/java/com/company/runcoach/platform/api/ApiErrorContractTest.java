package com.company.runcoach.platform.api;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiErrorContractTest {

    @Test
    void noResourceExceptionShouldMapToBaselineErrorShape() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        HttpServletRequest request = new MockHttpServletRequest("GET", "/v1/unknown-route");

        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/v1/unknown-route");
        ResponseEntity<Map<String, Object>> response = handler.handleNoResourceFound(ex, request);

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));

        Map<String, Object> error = castMap(response.getBody().get("error"));
        assertEquals("NOT_FOUND", error.get("code"));
        assertEquals(ex.getMessage(), error.get("message"));
        assertTrue(error.get("details") instanceof List<?>);
        assertTrue(error.get("correlationId") instanceof String);
        assertFalse(((String) error.get("correlationId")).isBlank());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }
}
