# Slice 005 - Plan Calendar + Workout Detail

Status: Completed (remediated)

Implemented scope:
- Android `PlanOverviewScreen` backed by `GET /v1/plans/current`
- Android week navigation (previous/next) with current week selected by default
- Android day/week calendar toggle where day mode filters workouts to selected day inside the selected week
- Android workout list/cards with normalized status rendering and today highlight
- Android selected week metadata rendering: phase label and recovery-week chip
- Android `WorkoutDetailScreen` backed by `GET /v1/planned-workouts/{plannedWorkoutId}`
- Full-screen loading, empty, and error states with retry
- Navigation from plan workout card to workout detail route
- Disabled mark-completed and mark-skipped buttons because no backend mutation endpoint is available in this codebase for Slice 5

Status source behavior:
- Preferred source is backend detail response status (`GET /v1/planned-workouts/{plannedWorkoutId}`) when provided.
- Temporary fallback source is the status query argument passed from plan card navigation.
- If neither source is available, status renders as `Unknown`.
- Normalized status display labels used in plan and workout detail: `Planned`, `Completed`, `Missed`, `Skipped`, `Rest day`, and `Unknown`.

APIs used:
- `GET /v1/plans/current`
- `GET /v1/planned-workouts/{plannedWorkoutId}`

Backend changes:
- None. Existing Slice 4 read endpoints already satisfy Slice 5 read requirements.

Out-of-scope intentionally preserved:
- No Strava sync
- No AI adaptation UI
- No live workout tracking
- No GPS recording
- No local plan-generation logic on Android
- No fake completion/skip mutation

Test additions/hardening:
- Plan ViewModel tests now use `StandardTestDispatcher` with `Dispatchers.setMain`/`resetMain` and `advanceUntilIdle` for deterministic coroutine execution.
- Plan ViewModel tests cover default week mode, day/week toggle transitions, day filtering, and phase/recovery mapping.
- Workout detail ViewModel tests now use the same deterministic dispatcher pattern.
- Workout detail ViewModel tests cover backend status mapping, navigation fallback status mapping, and unknown-status fallback.
- Workout navigation tests verify route construction and encoding for `plannedWorkoutId` and optional `status` query arg.
- Compose tests cover loading/empty/error/success states, day mode filtering, toggle visibility, phase label, and recovery chip visibility.

Notes:
- The repository currently has no `PATCH /v1/workouts/{id}/status` or `POST /v1/workout-completions` Android integration in this slice, so completion/skip controls are visible but disabled.
- Plan empty-state CTA text is `Try again` because the action only retries fetch.
- Build/test execution in this environment may still be blocked by missing Android SDK path (`local.properties` / `ANDROID_HOME`).

Remediation updates (post-review blockers):
- Auth success and race-goal completion now route to a minimal `Today` landing shell (`TodayRoutes.Home`) to keep Today-first product intent while full Today content remains in a later slice.
- App-wide cleartext traffic was removed from the main manifest; cleartext is now debug-only via `app/src/debug/AndroidManifest.xml`.
