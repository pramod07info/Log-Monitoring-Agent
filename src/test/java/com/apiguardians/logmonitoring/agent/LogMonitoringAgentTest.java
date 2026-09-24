package com.apiguardians.logmonitoring.agent;

import com.apiguardians.logmonitoring.model.InvestigationRequest;
import com.apiguardians.logmonitoring.source.MockLogSource;
import com.apiguardians.logmonitoring.tool.LogInvestigationTools;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LogMonitoringAgentTest {
    @Test void returnsLeadAgentReadyEvidenceReport() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-24T10:00:00Z"), ZoneOffset.UTC);
        var agent = new LogMonitoringAgent(new LogInvestigationTools(new MockLogSource(clock), clock), clock);

        var report = agent.investigate(new InvestigationRequest("payment-service", 30, "trace-pay-42", null));

        assertThat(report.status()).isEqualTo("RECURRING_FAILURE_DETECTED");
        assertThat(report.errors()).hasSize(3);
        assertThat(report.correlatedEvents()).hasSize(3);
        assertThat(report.recurringPatterns()).hasSize(1);
        assertThat(report.evidence()).isNotEmpty();
    }
}
