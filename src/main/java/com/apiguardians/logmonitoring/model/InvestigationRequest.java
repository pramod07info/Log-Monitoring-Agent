package com.apiguardians.logmonitoring.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record InvestigationRequest(
        @NotBlank String service,
        @Min(1) @Max(1440) int timeWindowMinutes,
        String traceId,
        String requestId) {
}
