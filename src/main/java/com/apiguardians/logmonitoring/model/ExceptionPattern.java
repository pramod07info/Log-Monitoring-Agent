package com.apiguardians.logmonitoring.model;

import java.time.Instant;
import java.util.List;

public record ExceptionPattern(
        String exceptionType,
        String normalizedMessage,
        long occurrences,
        Instant firstSeen,
        Instant lastSeen,
        List<String> evidenceEventIds) {
}
