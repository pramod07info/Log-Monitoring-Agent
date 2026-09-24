package com.apiguardians.logmonitoring.model;

import java.time.Instant;
import java.util.Map;

public record LogEvent(
        String eventId,
        Instant timestamp,
        String service,
        LogLevel level,
        String message,
        String exceptionType,
        String traceId,
        String requestId,
        Map<String, String> attributes) {
}
