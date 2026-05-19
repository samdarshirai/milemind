package com.company.runcoach.common.api;

import java.util.List;

public record ApiError(String code, String message, List<ApiErrorDetail> details, String correlationId) {
}
