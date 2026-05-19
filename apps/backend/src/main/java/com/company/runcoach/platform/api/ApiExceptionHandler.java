package com.company.runcoach.platform.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(
        NoResourceFoundException ex,
        HttpServletRequest request
    ) {
        String correlationId = UUID.randomUUID().toString();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", Map.of(
                "code", "NOT_FOUND",
                "message", ex.getMessage(),
                "details", List.of(Map.of(
                    "field", "path",
                    "issue", "not_found",
                    "value", request.getRequestURI()
                )),
                "correlationId", correlationId
            )
        ));
    }
}
