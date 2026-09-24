package com.apiguardians.logmonitoring.controller;

import com.apiguardians.logmonitoring.agent.LogMonitoringAgent;
import com.apiguardians.logmonitoring.model.EvidenceReport;
import com.apiguardians.logmonitoring.tool.LogInvestigationTools;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogInvestigationController.class)
class LogInvestigationControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean LogMonitoringAgent agent;
    @MockitoBean LogInvestigationTools tools;

    @Test void exposesIndependentInvestigationEndpoint() throws Exception {
        when(agent.investigate(any())).thenReturn(new EvidenceReport("inv-1", "payment-service",
                Instant.EPOCH, Instant.EPOCH, "ERRORS_FOUND", "summary", List.of(), List.of(), List.of(),
                List.of(), List.of("evidence"), List.of("next")));

        mvc.perform(post("/api/v1/investigations")
                        .contentType("application/json")
                        .content("""
                                {"service":"payment-service","timeWindowMinutes":30,"traceId":"trace-pay-42"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.investigationId").value("inv-1"))
                .andExpect(jsonPath("$.status").value("ERRORS_FOUND"));
    }
}
