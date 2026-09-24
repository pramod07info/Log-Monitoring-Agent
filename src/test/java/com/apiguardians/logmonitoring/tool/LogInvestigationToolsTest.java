package com.apiguardians.logmonitoring.tool;

import com.apiguardians.logmonitoring.source.MockLogSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LogInvestigationToolsTest {
    private LogInvestigationTools tools;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-24T10:00:00Z"), ZoneOffset.UTC);
        tools = new LogInvestigationTools(new MockLogSource(clock), clock);
    }

    @Test void filtersErrorsForServiceAndWindow() {
        assertThat(tools.filterErrors("payment-service", 30))
                .hasSize(3)
                .allMatch(event -> event.level().name().equals("ERROR"));
    }

    @Test void correlatesAcrossServices() {
        assertThat(tools.correlate("trace-pay-42", null, 30))
                .extracting(event -> event.service())
                .containsExactly("payment-service", "inventory-service", "payment-service");
    }

    @Test void groupsVariableTimeoutValuesIntoOnePattern() {
        assertThat(tools.identifyRecurringPatterns("payment-service", 30))
                .singleElement()
                .satisfies(pattern -> {
                    assertThat(pattern.exceptionType()).isEqualTo("SQLTransientConnectionException");
                    assertThat(pattern.occurrences()).isEqualTo(3);
                });
    }
}
