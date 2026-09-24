package com.apiguardians.logmonitoring.source;

import com.apiguardians.logmonitoring.model.LogEvent;
import com.apiguardians.logmonitoring.model.LogLevel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "log-agent.source", havingValue = "mock", matchIfMissing = true)
public class MockLogSource implements LogSource {
    private final Clock clock;

    public MockLogSource(Clock clock) { this.clock = clock; }

    @Override
    public List<LogEvent> query(LogQuery query) {
        return events().stream()
                .filter(e -> e.service().equalsIgnoreCase(query.service()))
                .filter(e -> !e.timestamp().isBefore(query.from()) && !e.timestamp().isAfter(query.to()))
                .filter(e -> query.minimumLevel() == null || e.level().ordinal() >= query.minimumLevel().ordinal())
                .sorted(Comparator.comparing(LogEvent::timestamp).reversed())
                .limit(query.limit())
                .toList();
    }

    @Override
    public List<LogEvent> findByCorrelation(String traceId, String requestId, Instant from, Instant to) {
        return events().stream()
                .filter(e -> !e.timestamp().isBefore(from) && !e.timestamp().isAfter(to))
                .filter(e -> (traceId != null && traceId.equals(e.traceId()))
                        || (requestId != null && requestId.equals(e.requestId())))
                .sorted(Comparator.comparing(LogEvent::timestamp))
                .toList();
    }

    @Override public String sourceName() { return "mock"; }

    private List<LogEvent> events() {
        Instant now = clock.instant();
        return List.of(
                event("evt-1", now.minusSeconds(840), "payment-service", LogLevel.INFO,
                        "Payment request accepted", null, "trace-pay-42", "req-42"),
                event("evt-2", now.minusSeconds(810), "inventory-service", LogLevel.WARN,
                        "Inventory reservation is slow", null, "trace-pay-42", "req-42"),
                event("evt-3", now.minusSeconds(780), "payment-service", LogLevel.ERROR,
                        "Database connection timeout after 3000ms", "SQLTransientConnectionException", "trace-pay-42", "req-42"),
                event("evt-4", now.minusSeconds(600), "payment-service", LogLevel.ERROR,
                        "Database connection timeout after 3000ms", "SQLTransientConnectionException", "trace-pay-43", "req-43"),
                event("evt-5", now.minusSeconds(420), "payment-service", LogLevel.ERROR,
                        "Database connection timeout after 5000ms", "SQLTransientConnectionException", "trace-pay-44", "req-44"),
                event("evt-6", now.minusSeconds(240), "payment-service", LogLevel.WARN,
                        "Connection pool utilization above 90%", null, "trace-pay-45", "req-45"),
                event("evt-7", now.minusSeconds(120), "payment-service", LogLevel.INFO,
                        "Health check completed", null, null, null));
    }

    private LogEvent event(String id, Instant timestamp, String service, LogLevel level, String message,
                           String exception, String traceId, String requestId) {
        return new LogEvent(id, timestamp, service, level, message, exception, traceId, requestId,
                Map.of("environment", "production", "source", "mock"));
    }
}
