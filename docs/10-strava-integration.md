# 10 Strava Integration

## Scope

MVP Strava integration is read-only.

Included:
- Connect account
- Store backend tokens
- Receive webhook events
- Import activities
- Match activities to planned workouts

Excluded:
- Writing workouts to Strava
- Editing Strava data
- Multi-provider abstraction

## Android Strava Connect Flow

### Flow

1. User taps Connect Strava.
2. Android calls `POST /v1/integrations/strava/connect-session`.
3. Backend creates signed `state` record and returns authorization URL.
4. Android opens the URL in a Custom Tab.
5. Strava redirects to backend callback URL.
6. Backend exchanges code for tokens and links athlete.
7. Backend redirects to Android app link with connection result only.
8. Android refreshes Strava status from backend.

### Android Responsibilities

- Start the browser flow.
- Handle deep-link result.
- Never store Strava access or refresh tokens.
- Show clear connected, syncing, and failed states.

## Backend OAuth Callback Flow

### Callback Responsibilities

- Validate `state`.
- Exchange authorization code using Strava client secret.
- Encrypt and store access token and refresh token.
- Persist athlete ID and granted scopes.
- Mark connection active.
- Redirect to app link with success or error result.

### Recommended MVP Default

- Use backend-generated short-lived signed `state` with user ID and nonce.
- Expire state after 10 minutes.
- Single use only.

## Token Storage And Refresh

Rules:
- Store tokens encrypted at rest.
- Always replace stored refresh token atomically on refresh.
- Refresh on demand before API calls when token expiry is near.
- Persist last successful refresh time and failure count.

Failure handling:
- If refresh fails with revocation-like error, mark connection `REAUTH_REQUIRED`.
- Do not keep retrying indefinitely.

## Required Scopes

Recommended MVP default:
- Request only minimal scopes needed for read-only activity import.

Scope policy:
- Use `read` for athlete context if required by current Strava flow.
- Use `activity:read` for normal read-only activity access and webhook eligibility.
- Avoid `activity:read_all` unless product explicitly decides to support "Only You" visibility cases and legal review approves it.

## Webhook Handling

### Verification

- Support Strava GET verification challenge.
- Return challenge JSON within required response window.

### Event Ingest

- Accept POST event payload.
- Validate shape.
- Persist raw event with idempotency key.
- Enqueue worker job.
- Return 200 immediately.

### Supported Event Types

- Activity create
- Activity update
- Activity delete
- Athlete deauthorization

## Activity Import

### Import Strategy

- Webhook-first.
- Fetch activity detail from Strava only when worker needs it.
- Upsert `strava_activity`.
- Trigger match attempt.

### Imported Fields

- Strava activity ID
- Athlete ID
- Sport type
- Start time
- Elapsed time
- Moving time
- Distance
- External ID
- Activity name
- Raw payload subset required for matching and audit

## Activity Matching To Planned Workouts

### Matching Inputs

- User ID from linked athlete
- Activity date and time
- Sport type
- Planned workout date
- Duration tolerance
- Distance tolerance
- Existing linked completion

### Matching Rules

- Only activities with run-compatible sport types are eligible.
- Prefer same calendar day match.
- Prefer planned workout within configured time and distance tolerance.
- Reject auto-match when two candidate workouts are equally plausible.
- Create manual review item for low confidence.

### Recommended MVP Default Tolerances

- Easy and recovery runs: plus or minus 30 percent duration or distance
- Quality sessions: plus or minus 20 percent with type-sensitive logic
- Long runs: plus or minus 20 percent distance, stricter on same-day placement

## Idempotency Rules

- Unique webhook constraint on `(subscription_id, object_type, object_id, aspect_type, event_time)`.
- Worker import upserts by `strava_activity_id`.
- Completion creation must be idempotent on `(planned_workout_id, strava_activity_id)`.
- Adaptation trigger must be guarded by plan version and event source fingerprint.

## Delete And Deauthorization Handling

### Activity Delete

- If linked completion came only from Strava and is not user-locked, unlink and mark source unavailable.
- If legal policy requires deletion, delete retained raw payload and keep minimal audit marker.

### Athlete Deauthorization

- Mark connection disconnected.
- Delete or anonymize stored Strava personal data according to legal policy.
- Remove tokens immediately.
- Stop sync jobs.

## Edge Cases

- User completes workout manually before Strava sync arrives.
- Two Strava runs on same day.
- Treadmill or unsupported sport type.
- Activity visibility change appears as delete or create.
- Token refresh rotates refresh token mid-import.
- User disconnects Strava while jobs are queued.
- Old webhook arrives after newer plan version exists.
- Marathon plan includes two runs in one day in future roadmap. Not supported in MVP. Matching may assume at most one candidate per date slot.

## Security Rules

- Verify callback state.
- Encrypt tokens.
- Redact tokens and raw payloads from logs.
- Restrict webhook endpoint exposure to required path only.

## Operational Rules

- Expose connection status endpoint for Android polling after OAuth return.
- Capture sync failure metrics.
- Capture match confidence metrics.
- Provide admin-safe traceability for support without exposing raw Strava secrets.
