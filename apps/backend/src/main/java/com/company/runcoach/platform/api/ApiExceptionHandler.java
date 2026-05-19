package com.company.runcoach.platform.api;

import com.company.runcoach.common.api.ApiError;
import com.company.runcoach.common.api.ApiErrorDetail;
import com.company.runcoach.common.api.ApiErrorEnvelope;
import com.company.runcoach.common.api.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorEnvelope> handleNoResourceFound(
        NoResourceFoundException ex,
        HttpServletRequest request
    ) {
        return response(
            HttpStatus.NOT_FOUND,
            "NOT_FOUND",
            ex.getMessage(),
            List.of(new ApiErrorDetail("path", "not_found"))
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorEnvelope> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
            .map(this::toDetail)
            .toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed.", details);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorEnvelope> handleApiException(ApiException ex) {
        return response(ex.getStatus(), ex.getCode(), ex.getMessage(), ex.getDetails());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorEnvelope> handleGeneric(Exception ex) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error.", List.of());
    }

    private ApiErrorDetail toDetail(FieldError error) {
        String issue = error.getCode() == null ? "invalid" : error.getCode().toLowerCase();
        return new ApiErrorDetail(error.getField(), issue);
    }

    private ResponseEntity<ApiErrorEnvelope> response(
        HttpStatus status,
        String code,
        String message,
        List<ApiErrorDetail> details
    ) {
        ApiErrorEnvelope envelope = new ApiErrorEnvelope(
            new ApiError(code, message, details, UUID.randomUUID().toString())
        );
        return ResponseEntity.status(status).body(envelope);
    }
}
