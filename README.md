# API Guardians — Log Monitoring Agent

A standalone Java 21 / Spring Boot 3 service specialized in production-log investigation. It gathers evidence for a Lead Agent while remaining independently testable over REST.

## What it does

- Retrieves recent logs by service and time window.
- Filters error-level events.
- Correlates trace IDs and request IDs across services.
- Detects recurring exception patterns after normalizing variable values.
- Returns a structured, Lead-Agent-ready evidence report.
- Uses a mock source by default and isolates providers behind `LogSource` for a future CloudWatch adapter.
- Exposes the capabilities as Spring AI `@Tool` methods, ready for model-directed tool calling.

## Architecture

```text
controller -> LogMonitoringAgent -> LogInvestigationTools -> LogSource
                                                        |-> MockLogSource (now)
                                                        `-> CloudWatchLogSource (later)
```

Packages:

- `agent`: investigation orchestration and report assembly
- `controller`: independent REST test surface
- `model`: request, log event, pattern, and evidence DTOs
- `tool`: Spring AI tool definitions
- `source`: provider-neutral port and mock adapter
- `config`: application infrastructure

## Run

Requirements: JDK 21 and Maven 3.9+.

```bash
mvn test
mvn spring-boot:run
```

Mock mode does not call an LLM and needs no API key. To prepare OpenAI-backed model use, set variables outside the repository:

```powershell
$env:OPENAI_API_KEY="your-key"
$env:OPENAI_MODEL="gpt-4.1-mini"
$env:SPRING_AI_CHAT_MODEL="openai"
$env:LOG_AGENT_LLM_ENABLED="true"
```

Never commit `.env` files or keys. `.env.example` contains placeholders only.

## Try it

```bash
curl -X POST http://localhost:8080/api/v1/investigations \
  -H "Content-Type: application/json" \
  -d '{"service":"payment-service","timeWindowMinutes":30,"traceId":"trace-pay-42"}'
```

Additional endpoints:

- `GET /api/v1/logs/recent?service=payment-service&timeWindowMinutes=30`
- `GET /api/v1/logs/errors?service=payment-service&timeWindowMinutes=30`
- `GET /api/v1/logs/correlated?traceId=trace-pay-42&timeWindowMinutes=30`

## CloudWatch integration path

Add a `CloudWatchLogSource implements LogSource`, make it conditional on `log-agent.source=cloudwatch`, and add the AWS SDK CloudWatch Logs dependency. Keep AWS credentials in the standard AWS credential chain (workload role, profile, or environment), never in source. The agent and tools need no changes.

Suggested implementation mapping:

- `query`: CloudWatch Logs Insights query scoped to service log groups and timestamps
- `findByCorrelation`: escaped Insights query matching `traceId` or `requestId`
- provider guardrails: query timeout, result limit, allowed log-group prefixes, and redaction

## Lead Agent contract

`POST /api/v1/investigations` returns `EvidenceReport`, including the exact window, status, summary, raw supporting events, correlated events, recurring patterns, concise evidence statements, and next steps. A Lead Agent can consume this JSON without parsing prose.
