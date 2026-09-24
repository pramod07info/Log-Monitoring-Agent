package com.apiguardians.logmonitoring.tool;

import com.apiguardians.logmonitoring.model.ExceptionPattern;
import com.apiguardians.logmonitoring.model.LogEvent;
import com.apiguardians.logmonitoring.model.LogLevel;
import com.apiguardians.logmonitoring.source.LogQuery;
import com.apiguardians.logmonitoring.source.LogSource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LogInvestigationTools {
    private final LogSource logSource;
    private final Clock clock;

    public LogInvestigationTools(LogSource logSource, Clock clock) {
        this.logSource = logSource;
        this.clock = clock;
    }

    @Tool(description = "Retrieve recent production logs for a service and time window")
    public List<LogEvent> getRecentLogs(String service, int timeWindowMinutes) {
        Instant now = clock.instant();
        return logSource.query(new LogQuery(service, now.minusSeconds(timeWindowMinutes * 60L), now, null, 200));
    }

    @Tool(description = "Filter production error logs for a service and time window")
    public List<LogEvent> filterErrors(String service, int timeWindowMinutes) {
        Instant now = clock.instant();
        return logSource.query(new LogQuery(service, now.minusSeconds(timeWindowMinutes * 60L), now,
                LogLevel.ERROR, 200));
    }

    @Tool(description = "Correlate log events across services by trace ID or request ID")
    public List<LogEvent> correlate(String traceId, String requestId, int timeWindowMinutes) {
        Instant now = clock.instant();
        return logSource.findByCorrelation(blankToNull(traceId), blankToNull(requestId),
                now.minusSeconds(timeWindowMinutes * 60L), now);
    }

    @Tool(description = "Identify recurring exception types and normalized messages in service error logs")
    public List<ExceptionPattern> identifyRecurringPatterns(String service, int timeWindowMinutes) {
        Map<String, List<LogEvent>> groups = filterErrors(service, timeWindowMinutes).stream()
                .collect(Collectors.groupingBy(e -> key(e.exceptionType(), normalize(e.message()))));
        return groups.values().stream()
                .filter(events -> events.size() > 1)
                .map(events -> new ExceptionPattern(
                        events.getFirst().exceptionType(), normalize(events.getFirst().message()), events.size(),
                        events.stream().map(LogEvent::timestamp).min(Instant::compareTo).orElseThrow(),
                        events.stream().map(LogEvent::timestamp).max(Instant::compareTo).orElseThrow(),
                        events.stream().map(LogEvent::eventId).toList()))
                .toList();
    }

    public String sourceName() { return logSource.sourceName(); }

    private String normalize(String message) {
        return message.toLowerCase(Locale.ROOT).replaceAll("\\b\\d+(?:ms|s)?\\b", "{number}");
    }
    private String key(String exception, String normalized) { return String.valueOf(exception) + "|" + normalized; }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
}
