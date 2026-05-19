# 14 Test Plan

## Backend Unit Tests

- Plan template selection by race distance and experience level.
- Weekly mileage progression cap enforcement.
- Long-run duration and distance cap enforcement.
- Recovery week insertion logic.
- Taper logic.
- Completion scoring.
- Fatigue risk calculation.
- Pain severity branching.
- Match confidence scoring for Strava activity linking.
- AI output validator.

## Backend Integration Tests

- Register, login, and refresh token flow.
- Onboarding creates profile, goal, plan, weeks, and workouts.
- Profile update triggers expected behavior.
- Skip workout endpoint creates adaptation decision and increments plan version.
- Pain feedback endpoint strips intensity from next 7 days.
- Strava callback persists encrypted tokens.
- Webhook ingest persists idempotent event and enqueues job.

## Android Unit Tests

- ViewModel state transitions for Today, Plan, Workout Detail, Check-Ins, and Coach.
- Repository mapping from DTO to UI model.
- Error mapping from API error codes.
- Session restore and refresh behavior.

## Compose UI Tests

- Onboarding validation messages.
- Today screen rest-day empty state.
- Workout detail action buttons.
- Completion form validation.
- Fatigue and pain check-in submissions.
- "What changed?" chip visibility after adaptation.
- Strava connection status UI.
- Coach explanation fallback text display.

## API Contract Tests

- Request and response schema validation for auth, onboarding, plan, completion, check-in, Strava, and coach endpoints.
- Error response format consistency.
- Cursor pagination format for chat and Strava activities.
- `409 CONFLICT` behavior for stale plan version mutations.

## Strava Integration Tests

- OAuth state verification.
- Token refresh token rotation handling.
- Duplicate webhook delivery idempotency.
- Activity create, update, and delete event processing.
- Athlete deauthorization handling.
- Low-confidence match manual-review path.

## Training Engine Tests

- Half marathon beginner plan shape.
- Marathon beginner plan shape.
- Marathon intermediate taper.
- No more than 10 percent weekly increase.
- No adjacent quality and long-run days for beginner and intermediate plans.
- Long run never exceeds allowed share of weekly distance.

## Adaptation Engine Tests

- Missed easy run does not create catch-up volume.
- Missed quality run reschedules only when spacing is safe.
- Missed long run does not create unsafe following week.
- Partial completion reduces future load only after repeated signals.
- Overdone workout inserts recovery adjustment.
- Orange readiness removes one quality stressor.
- Red readiness converts to recovery structure.

## AI Validation Tests

- Reject invalid JSON.
- Reject missing reason codes.
- Reject contradiction with adaptation decision.
- Reject medical diagnosis language.
- Reject unsafe training escalation language.
- Accept safe explanation output.
- Fallback deterministic text returned when validation fails.

## Manual QA Checklist

- Sign up and sign in work on fresh install.
- Onboarding can be completed end to end.
- Invalid race dates show clear errors.
- Plan renders correctly for both half and marathon users.
- Manual completion updates plan and Today screen.
- Fatigue check-in can downshift next workout.
- High pain check-in removes intensity and shows safety banner.
- Strava connect opens browser and returns to app cleanly.
- Imported Strava activity appears in history.
- AI explanation appears when available and deterministic fallback appears when unavailable.
- Disconnecting Strava updates status immediately.

## Recommended MVP Test Priorities

Priority 1:
- Training engine
- Adaptation engine
- Auth
- Onboarding

Priority 2:
- Workout completion
- Strava ingest
- AI validation

Priority 3:
- Progress summaries
- UI polish states
