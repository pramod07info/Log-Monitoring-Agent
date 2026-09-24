package com.apiguardians.logmonitoring.model;

import java.time.Instant;
import java.util.List;

public record EvidenceReport(
        String investigationId,
        String service,
        Instant windowStart,
        Instant windowEnd,
        String status,
        String summary,
        List<LogEvent> recentLogs,
        List<LogEvent> errors,
        List<LogEvent> correlatedEvents,
        List<ExceptionPattern> recurringPatterns,
        List<String> evidence,
        List<String> recommendedNextSteps) {
}
