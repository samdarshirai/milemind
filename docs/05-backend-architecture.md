# 05 Backend Architecture

## Architectural Style

Use a Java Spring Boot modular monolith.

Rationale:
- Planning, adaptation, and workout completion require strong transactional consistency.
- The MVP has one mobile client and one bounded domain.
- Microservices would create avoidable complexity around events, versioning, auth, and debugging.

## Module Boundaries

### identity

Responsibilities:
- Registration.
- Login.
- Refresh token handling.
- Password hashing.
- User lifecycle status.

### profile

Responsibilities:
- Runner profile.
- Preferences.
- Units and timezone.
- Eligibility validation for onboarding and plan generation.

### goals

Responsibilities:
- Single active race goal.
- Goal validation.
- Goal lifecycle.

### planning

Responsibilities:
- Plan template selection.
- Plan generation.
- Week construction.
- Workout construction.
- Plan versioning.

### execution

Responsibilities:
- Manual workout completion.
- Completion scoring.
- Workout state transitions.
- Reschedule and skip actions.

### adaptation

Responsibilities:
- Fatigue signal ingestion.
- Pain and injury feedback ingestion.
- Adaptation decisioning.
- Plan regeneration for near-term horizon.
- Adaptation audit persistence.

### integrations

Responsibilities:
- Strava connect session.
- OAuth callback.
- Token refresh.
- Webhook ingest.
- Activity fetch.
- Activity matching.

### ai

Responsibilities:
- Prompt construction.
- DeepSeek client.
- JSON schema validation.
- Safety filter.
- Deterministic fallback generation.
- Prompt and response audit.

### insights

Responsibilities:
- Today summary.
- Progress statistics.
- Adherence summaries.
- Readiness trend summaries.

### platform

Responsibilities:
- Outbox processing.
- Job scheduling.
- Metrics.
- Structured logging.
- Config and secrets wiring.

## Recommended Package Structure

```text
com.company.runcoach
  app
  common
  identity
    api
    service
    domain
    repo
    config
  profile
    api
    service
    domain
    repo
  goals
    api
    service
    domain
    repo
  planning
    api
    service
    domain
    engine
    repo
  execution
    api
    service
    domain
    repo
  adaptation
    api
    service
    domain
    engine
    repo
  integrations
    strava
      api
      service
      client
      domain
      repo
      jobs
  ai
    api
    service
    prompt
    validation
    client
    repo
  insights
    api
    service
    query
  platform
    outbox
    jobs
    metrics
    security
    config
```

## Service Responsibilities

### Core Domain Services

- `OnboardingService`
- `RaceGoalService`
- `PlanGenerationService`
- `PlanQueryService`
- `WorkoutCompletionService`
- `WorkoutScheduleService`
- `FatigueSignalService`
- `InjuryFeedbackService`
- `AdaptationDecisionService`
- `StravaConnectionService`
- `StravaWebhookService`
- `StravaActivityImportService`
- `StravaWorkoutMatchService`
- `CoachExplanationService`
- `CoachChatService`
- `TodayInsightsService`

### Engine Services

- `PlanTemplateSelector`
- `PlanBuilder`
- `MileageProgressionPolicy`
- `LongRunPolicy`
- `TaperPolicy`
- `WorkoutDistributionPolicy`
- `CompletionScoringPolicy`
- `FatigueRiskCalculator`
- `PainSafetyPolicy`
- `AdaptationRegenerationPolicy`

## API Layer

Rules:
- Controllers remain thin.
- Validation lives at DTO boundary and domain services.
- Mapping between entity and DTO uses dedicated mappers.
- Controllers never call repositories directly.

## Persistence Layer

Rules:
- JPA plus Hibernate for transactional entities is acceptable.
- Use explicit query repositories for calendar and dashboard reads where joins become complex.
- Avoid leaking entities to API responses.

## Async and Event Processing

### Pattern

- Use outbox table plus background worker.
- Publish domain events after successful transaction commit.
- Process asynchronously for Strava ingest, adaptation recalculation, and AI explanation generation.

### Initial Domain Events

- `PlanGenerated`
- `WorkoutCompleted`
- `WorkoutSkipped`
- `WorkoutRescheduled`
- `FatigueSignalAdded`
- `InjuryFeedbackAdded`
- `StravaActivityImported`
- `StravaMatchResolved`
- `AdaptationDecisionCreated`

### Job Rules

- All jobs must be idempotent.
- Adaptation jobs must carry plan version to avoid stale overwrite.
- Strava webhook request thread must only validate, persist, enqueue, and return.

## Security Design

### Auth

Recommended MVP default:
- Email plus password auth.
- Short-lived JWT access tokens.
- Rotating refresh tokens stored server-side.

### Authorization

- User-scoped data only.
- Every user-facing API requires authenticated user context except auth endpoints and Strava webhook verification callback.
- Enforce ownership checks for all plan, workout, profile, and integration resources.

### Sensitive Data

- Encrypt Strava access and refresh tokens at rest.
- Encrypt refresh tokens at rest.
- Do not log secrets, prompt payloads containing personal data, or full Strava raw payloads in application logs.

### Auditability

- Persist plan generation input snapshot.
- Persist adaptation before and after snapshot.
- Persist AI prompt and output references with redaction where needed.

## Observability

### Metrics

- Auth login success and failure count.
- Plan generation latency and failure count.
- Adaptation evaluation latency and decision rate.
- Strava token refresh failures.
- Webhook validation and enqueue success.
- Match confidence distribution.
- AI validation rejection rate.
- API p95 latency by endpoint group.

### Logging

- Structured JSON logs.
- Correlation ID per request.
- Domain event IDs in async logs.
- Redaction for tokens, passwords, and free-text pain notes.

### Tracing

- OpenTelemetry for request to DB to job flow.
- Trace Strava webhook to import to adaptation decision chain.

## Configuration Management

### Profiles

- `local`
- `test`
- `staging`
- `prod`

### Secrets

- Use environment variables or secret manager.
- No secrets in repo.

### Feature Flags

- `feature.ai.explanations.enabled`
- `feature.ai.chat.enabled`
- `feature.strava.enabled`
- `feature.plan.adaptation.enabled`

## Infrastructure Notes

Recommended MVP default:
- Single backend service deployment.
- PostgreSQL primary database.
- Redis optional for ephemeral caching and job coordination, not required on day one if outbox polling is sufficient.
- Object storage optional for exports only, not required for core MVP.

## Architecture Constraints

- Backend is source of truth for all future workouts.
- Android cannot bypass backend adaptation.
- AI outputs must be post-validated before persistence or display.
- Strava data retention must be minimized and revocation-aware.
