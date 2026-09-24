package com.apiguardians.logmonitoring.source;

import com.apiguardians.logmonitoring.model.LogLevel;
import java.time.Instant;

public record LogQuery(String service, Instant from, Instant to, LogLevel minimumLevel, int limit) {
}
