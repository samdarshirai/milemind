# 02 MVP Scope

## Exact MVP Scope

### In Scope

- Account creation and sign-in.
- Onboarding flow.
- Runner profile creation and editing.
- Single active race goal setup.
- Deterministic half marathon plan generation.
- Deterministic marathon plan generation.
- Workout calendar with day and week views.
- Workout detail screen.
- Manual workout completion.
- Fatigue check-ins.
- Pain check-ins.
- Basic adaptation after completed, missed, partial, and overdone workouts.
- Read-only Strava connection and activity import.
- DeepSeek-powered workout and adaptation explanations.
- Safe coaching chat limited to explanation and support.

### Explicit Exclusions

- Social feed and clubs.
- Route planning and map-based workout design.
- Nutrition, fueling, or body-composition tracking.
- Strength video library.
- Garmin, Apple Health, Health Connect, COROS, Polar, Suunto, or smartwatch apps.
- Direct workout export back to Strava.
- Ultra, trail, triathlon, or track plans.
- Multiple overlapping race goals.
- Community challenges.
- Coach marketplace.
- Payment and subscriptions.
- Voice coaching.
- AI-created plans.
- AI-edited training calendar.
- Medical triage, diagnosis, or rehab programming.

## User Journeys

### Journey 1: New User Creates First Plan

1. User creates account.
2. User completes onboarding with running history, available days, current weekly volume, recent longest run, and race goal.
3. Backend validates inputs and generates a deterministic plan.
4. User lands on Today and Plan screens.
5. User can inspect the first week and the first workout detail.

### Journey 2: User Completes a Workout Manually

1. User opens today's workout.
2. User completes workout and submits distance, duration, RPE, and notes.
3. User optionally answers soreness and pain questions.
4. Backend records completion and evaluates adaptation rules.
5. User sees updated workout state and any adaptation notice.

### Journey 3: User Misses a Workout

1. User opens a planned workout after its scheduled day.
2. User marks it skipped or reschedules once.
3. Backend applies missed-workout rules.
4. Plan may remain unchanged, shift one workout, or downshift the next week.
5. User sees a "What changed?" explanation.

### Journey 4: User Reports Fatigue or Pain

1. User submits readiness check-in or pain report.
2. Backend calculates fatigue state and safety flags.
3. Adaptation engine reduces or removes intensity if thresholds are crossed.
4. User sees updated calendar and safety-focused explanation.

### Journey 5: User Connects Strava

1. User taps Connect Strava in Profile.
2. Android opens OAuth flow.
3. Backend callback exchanges token and links athlete.
4. Webhook or manual sync imports activity.
5. Backend tries to match the activity to a planned workout.

## MVP Readiness Acceptance Criteria

### Functional Readiness

- A new user can complete onboarding in under 5 minutes.
- A valid half marathon goal creates a plan with appropriate phase structure.
- A valid marathon goal creates a plan with conservative long-run progression.
- The app shows today's workout and current week without requiring Strava.
- The user can mark a workout complete, skipped, or rescheduled.
- Fatigue and pain check-ins trigger deterministic adaptation where required.
- The user can connect and disconnect Strava.
- Imported Strava activities appear in the app and can be matched or reviewed.
- AI explanations are visible only for validated outputs.

### Safety Readiness

- No generated week exceeds configured progression caps.
- No high-pain state leaves intensity unchanged.
- No missed-workout flow instructs a user to "make up" all lost volume.
- No AI output can alter plan structure or prescribe unsafe increases.

### Operational Readiness

- Backend modules, DB migrations, and API contracts are stable.
- Basic observability exists for auth, planning, adaptation, Strava, and AI.
- Supportable audit data exists for plan generation and adaptation decisions.

## Recommended MVP Default

- If the PRD suggests broader adaptation or advanced race logic, defer it.
- If a rule is ambiguous, choose the more conservative training load change.
