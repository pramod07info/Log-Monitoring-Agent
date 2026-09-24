package com.apiguardians.logmonitoring.controller;

import com.apiguardians.logmonitoring.agent.LogMonitoringAgent;
import com.apiguardians.logmonitoring.model.EvidenceReport;
import com.apiguardians.logmonitoring.model.InvestigationRequest;
import com.apiguardians.logmonitoring.model.LogEvent;
import com.apiguardians.logmonitoring.tool.LogInvestigationTools;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class LogInvestigationController {
    private final LogMonitoringAgent agent;
    private final LogInvestigationTools tools;

    public LogInvestigationController(LogMonitoringAgent agent, LogInvestigationTools tools) {
        this.agent = agent;
        this.tools = tools;
    }

    @PostMapping("/investigations")
    public EvidenceReport investigate(@Valid @RequestBody InvestigationRequest request) {
        return agent.investigate(request);
    }

    @GetMapping("/logs/recent")
    public List<LogEvent> recent(@RequestParam String service,
                                 @RequestParam(defaultValue = "30") int timeWindowMinutes) {
        return tools.getRecentLogs(service, timeWindowMinutes);
    }

    @GetMapping("/logs/errors")
    public List<LogEvent> errors(@RequestParam String service,
                                 @RequestParam(defaultValue = "30") int timeWindowMinutes) {
        return tools.filterErrors(service, timeWindowMinutes);
    }

    @GetMapping("/logs/correlated")
    public List<LogEvent> correlated(@RequestParam(required = false) String traceId,
                                     @RequestParam(required = false) String requestId,
                                     @RequestParam(defaultValue = "30") int timeWindowMinutes) {
        return tools.correlate(traceId, requestId, timeWindowMinutes);
    }
}
