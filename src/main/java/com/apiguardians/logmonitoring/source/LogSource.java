package com.apiguardians.logmonitoring.source;

import com.apiguardians.logmonitoring.model.LogEvent;
import java.time.Instant;
import java.util.List;

public interface LogSource {
    List<LogEvent> query(LogQuery query);
    List<LogEvent> findByCorrelation(String traceId, String requestId, Instant from, Instant to);
    String sourceName();
}
