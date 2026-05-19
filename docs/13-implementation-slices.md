# 13 Implementation Slices

## Slice 1: Auth And Session Foundation

Goal:
- Create secure account and session management.

Backend tasks:
- Implement register, login, refresh token, logout.
- Create `app_user` and `refresh_token`.
- Add JWT issuance and refresh rotation.

Android tasks:
- Build splash, sign in, sign up, session restore.
- Add token storage and auth interceptor.

DB changes:
- `app_user`
- `refresh_token`

APIs:
- `POST /v1/auth/register`
- `POST /v1/auth/login`
- `POST /v1/auth/refresh`
- `POST /v1/auth/logout`

Tests:
- Password hashing.
- Refresh token rotation.
- Invalid login.

Acceptance criteria:
- User can sign up, sign in, and restore session.

Dependencies:
- None.

## Slice 2: Onboarding And Profile

Goal:
- Capture runner profile and preferences.

Backend tasks:
- Implement onboarding validation.
- Implement profile read and update.

Android tasks:
- Build onboarding screens and profile edit screen.

DB changes:
- `runner_profile`

APIs:
- `POST /v1/users/onboarding`
- `GET /v1/profile`
- `PUT /v1/profile`

Tests:
- Adult-only validation.
- Preferred day validation.
- Timezone persistence.

Acceptance criteria:
- User can save profile and return to it.

Dependencies:
- Slice 1.

## Slice 3: Race Goal Setup

Goal:
- Support one active half or marathon goal.

Backend tasks:
- Implement race goal validation and lifecycle.
- Enforce one active goal constraint.

Android tasks:
- Build race goal onboarding step and current goal display.

DB changes:
- `race_goal`

APIs:
- `POST /v1/race-goals`
- `GET /v1/race-goals/current`

Tests:
- Date too soon rejection.
- One active goal per user.

Acceptance criteria:
- User can create a valid goal and cannot create a second active one.

Dependencies:
- Slice 2.

## Slice 4: Deterministic Plan Generation V1

Goal:
- Generate initial half and marathon plans.

Backend tasks:
- Implement templates, phase builder, mileage policy, long-run policy, taper policy.
- Persist plan, weeks, and workouts.

Android tasks:
- Build Today shell and Plan Calendar read-only views.

DB changes:
- `training_plan`
- `training_week`
- `planned_workout`

APIs:
- `POST /v1/plans/generate`
- `GET /v1/plans/current`
- `GET /v1/planned-workouts/{plannedWorkoutId}`

Tests:
- Half and marathon plan generation.
- Recovery week insertion.
- Safety cap enforcement.

Acceptance criteria:
- User gets a plan matching availability, distance, and date constraints.

Dependencies:
- Slice 3.

## Slice 5: Workout Detail And Manual Completion

Goal:
- Let users inspect and complete workouts manually.

Backend tasks:
- Implement workout detail query.
- Implement completion scoring and completion persistence.

Android tasks:
- Build workout detail and complete workout flow.

DB changes:
- `workout_completion`

APIs:
- `POST /v1/workout-completions`

Tests:
- Duplicate completion handling.
- Completion scoring.

Acceptance criteria:
- Completed workout appears as completed and updates plan state.

Dependencies:
- Slice 4.

## Slice 6: Fatigue And Pain Check-Ins

Goal:
- Capture subjective readiness and pain signals.

Backend tasks:
- Implement fatigue signal and injury feedback endpoints.
- Compute readiness state.

Android tasks:
- Build fatigue and pain check-in screens.
- Surface readiness banners on Today.

DB changes:
- `fatigue_signal`
- `injury_feedback`

APIs:
- `POST /v1/fatigue-signals`
- `POST /v1/injury-feedback`
- `GET /v1/insights/today`

Tests:
- Readiness mapping.
- Pain severity threshold handling.

Acceptance criteria:
- Check-ins persist and return readiness state.

Dependencies:
- Slice 5.

## Slice 7: Adaptation Engine V1

Goal:
- Adapt next 7 to 14 days after missed, partial, overdone, fatigue, and pain events.

Backend tasks:
- Implement adaptation decision service.
- Implement plan version increment and near-term regeneration.
- Persist adaptation audit.

Android tasks:
- Show "What changed?" markers.
- Add skip and reschedule flows.
- Show latest adaptation summary.

DB changes:
- `adaptation_decision`
- `adapted_from_workout_id` on `planned_workout`

APIs:
- `POST /v1/planned-workouts/{plannedWorkoutId}/skip`
- `POST /v1/planned-workouts/{plannedWorkoutId}/reschedule`

Tests:
- No unsafe long-run spikes.
- No catch-up stacking.
- Stale plan version conflict.

Acceptance criteria:
- Plan changes are visible, deterministic, and auditable.

Dependencies:
- Slice 6.

## Slice 8: Progress And Insights

Goal:
- Surface adherence and trend summaries.

Backend tasks:
- Implement progress queries and today insight summaries.

Android tasks:
- Build Progress screen.
- Improve Today screen.

DB changes:
- None required beyond prior slices.

APIs:
- `GET /v1/insights/today`
- `GET /v1/progress/summary`

Tests:
- Empty-state progress for new users.
- Trend aggregation.

Acceptance criteria:
- User can see long-run progression, weekly completion, and readiness trend.

Dependencies:
- Slice 7.

## Slice 9: Strava OAuth And Connection Status

Goal:
- Connect Strava safely.

Backend tasks:
- Implement connect session endpoint.
- Implement callback flow.
- Encrypt and store tokens.

Android tasks:
- Build Strava connect screen and deep-link return handling.

DB changes:
- `strava_connection`

APIs:
- `POST /v1/integrations/strava/connect-session`
- `GET /v1/integrations/strava/status`
- `DELETE /v1/integrations/strava/connection`

Tests:
- State validation.
- Token refresh and rotation.
- Disconnect cleanup.

Acceptance criteria:
- User can connect and disconnect Strava, and status updates correctly.

Dependencies:
- Slice 1.

## Slice 10: Strava Webhook Import

Goal:
- Import Strava activities asynchronously.

Backend tasks:
- Implement webhook verification and ingest.
- Add raw webhook event persistence.
- Add activity import worker.

Android tasks:
- Show last sync state and import history.

DB changes:
- `strava_activity`
- `strava_webhook_event`
- `outbox_event`

APIs:
- `POST /v1/integrations/strava/webhook`
- `GET /v1/integrations/strava/activities`

Tests:
- Duplicate webhook retry.
- Import idempotency.
- Deauthorization event.

Acceptance criteria:
- New Strava activities appear in the app after import.

Dependencies:
- Slice 9.

## Slice 11: Strava Match To Planned Workouts

Goal:
- Link imported activities to workouts.

Backend tasks:
- Implement match engine and confidence scoring.
- Add manual review state.
- Link completion to Strava activity.

Android tasks:
- Build import review UI.

DB changes:
- Link fields in `workout_completion`

APIs:
- `POST /v1/workout-completions/{completionId}/confirm-match`

Tests:
- Same-day multiple run edge case.
- Low-confidence review flow.

Acceptance criteria:
- Common single-run cases auto-match correctly.

Dependencies:
- Slice 10.

## Slice 12: DeepSeek Explanations

Goal:
- Add validated AI explanations without allowing AI plan control.

Backend tasks:
- Implement prompt builder.
- Implement DeepSeek client abstraction.
- Implement schema validator and deterministic fallback.

Android tasks:
- Show workout and adaptation explanation cards.
- Build Coach screen with safe prompts.

DB changes:
- `coaching_insight`
- `coach_chat_message`

APIs:
- `POST /v1/coach/explain-workout`
- `POST /v1/coach/explain-adaptation`
- `POST /v1/coach/chat`
- `GET /v1/coach/chat/history`

Tests:
- Schema rejection.
- Prohibited medical language.
- Fallback path.

Acceptance criteria:
- AI explanations display only when validated and never alter plans.

Dependencies:
- Slice 7.

## Slice 13: Hardening And Observability

Goal:
- Make MVP supportable in staging and production.

Backend tasks:
- Add metrics, tracing, structured logs, correlation IDs, feature flags.
- Add audit persistence for adaptation and AI.

Android tasks:
- Improve error surfaces and retry behavior.

DB changes:
- None required if audit tables already exist.

APIs:
- No new user APIs required.

Tests:
- API contract tests.
- Observability smoke checks.

Acceptance criteria:
- Failures are diagnosable across auth, planning, adaptation, Strava, and AI.

Dependencies:
- Slice 12.
