package com.apiguardians.logmonitoring.agent;

import com.apiguardians.logmonitoring.model.EvidenceReport;
import com.apiguardians.logmonitoring.model.ExceptionPattern;
import com.apiguardians.logmonitoring.model.InvestigationRequest;
import com.apiguardians.logmonitoring.model.LogEvent;
import com.apiguardians.logmonitoring.tool.LogInvestigationTools;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LogMonitoringAgent {
    private final LogInvestigationTools tools;
    private final Clock clock;

    public LogMonitoringAgent(LogInvestigationTools tools, Clock clock) {
        this.tools = tools;
        this.clock = clock;
    }

    public EvidenceReport investigate(InvestigationRequest request) {
        Instant end = clock.instant();
        Instant start = end.minusSeconds(request.timeWindowMinutes() * 60L);
        List<LogEvent> recent = tools.getRecentLogs(request.service(), request.timeWindowMinutes());
        List<LogEvent> errors = tools.filterErrors(request.service(), request.timeWindowMinutes());
        List<LogEvent> correlated = hasCorrelation(request)
                ? tools.correlate(request.traceId(), request.requestId(), request.timeWindowMinutes()) : List.of();
        List<ExceptionPattern> patterns = tools.identifyRecurringPatterns(request.service(), request.timeWindowMinutes());

        List<String> evidence = new ArrayList<>();
        evidence.add("Log source: " + tools.sourceName());
        evidence.add(errors.size() + " error event(s) found among " + recent.size() + " service event(s)");
        patterns.forEach(p -> evidence.add("Recurring " + p.exceptionType() + ": " + p.occurrences() + " occurrences"));
        if (!correlated.isEmpty()) evidence.add(correlated.size() + " correlated event(s) found across services");

        String status = errors.isEmpty() ? "NO_ERRORS_FOUND" : patterns.isEmpty() ? "ERRORS_FOUND" : "RECURRING_FAILURE_DETECTED";
        String summary = errors.isEmpty()
                ? "No error-level logs were found in the requested window."
                : "Found %d error(s) and %d recurring exception pattern(s).".formatted(errors.size(), patterns.size());
        List<String> nextSteps = patterns.isEmpty()
                ? List.of("Continue monitoring and widen the time window if symptoms persist.")
                : List.of("Inspect the earliest event in each recurring pattern.",
                          "Validate downstream dependency health and resource saturation.",
                          "Send this evidence report to the Lead Agent for incident-level correlation.");

        return new EvidenceReport(UUID.randomUUID().toString(), request.service(), start, end, status, summary,
                recent, errors, correlated, patterns, evidence, nextSteps);
    }

    private boolean hasCorrelation(InvestigationRequest request) {
        return (request.traceId() != null && !request.traceId().isBlank())
                || (request.requestId() != null && !request.requestId().isBlank());
    }
}
