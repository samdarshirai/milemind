package com.company.runcoach.common.api;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ApiException extends RuntimeException {

    private final String code;
    private final HttpStatus status;
    private final List<ApiErrorDetail> details;

    public ApiException(String code, String message, HttpStatus status, List<ApiErrorDetail> details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<ApiErrorDetail> getDetails() {
        return details;
    }
}
