# 04 Screen Navigation

## Full Screen List

- Splash
- Sign In
- Sign Up
- Onboarding Intro
- Onboarding Running History
- Onboarding Availability
- Onboarding Race Goal
- Onboarding Review
- Today
- Plan Calendar
- Workout Detail
- Complete Workout
- Fatigue Check-In
- Pain Check-In
- Progress
- Coach
- Coach Chat History
- Profile
- Edit Profile
- Strava Connection
- Strava Import Review

## Navigation Graph

```text
Splash
 -> Sign In
 -> Sign Up
 -> Onboarding Intro
 -> Onboarding Running History
 -> Onboarding Availability
 -> Onboarding Race Goal
 -> Onboarding Review
 -> Today

Today
 -> Workout Detail
 -> Complete Workout
 -> Fatigue Check-In
 -> Pain Check-In
 -> Coach

Plan Calendar
 -> Workout Detail
 -> Complete Workout
 -> Fatigue Check-In
 -> Pain Check-In

Progress
 -> Workout Detail

Coach
 -> Coach Chat History

Profile
 -> Edit Profile
 -> Strava Connection
 -> Strava Import Review
```

## Main User Flows

### Flow 1: Onboarding To Plan

- Sign up.
- Complete runner history and goal setup.
- Backend generates plan.
- Open Today screen with first workout.

### Flow 2: Calendar To Completion

- Open Plan Calendar.
- Tap a workout.
- Review details.
- Complete manually.
- Return to Today or Plan with updated status.

### Flow 3: Safety Check-In To Adaptation

- Open fatigue or pain check-in.
- Submit response.
- Backend adapts plan if needed.
- Return to Today or Plan with visible "What changed?" markers.

### Flow 4: Strava Connect To Import

- Open Profile.
- Tap Connect Strava.
- Complete OAuth in Custom Tab.
- Return to app.
- View connection status and imported activity state.

## Screen Requirements

### Splash

Purpose:
- Restore session.
- Route to auth, onboarding, or main app.

Backend data:
- Refresh session if refresh token exists.
- Fetch minimal profile status.

States:
- Loading.
- Error with retry.
- Success routing.

### Sign In

Purpose:
- Authenticate existing user.

Backend data:
- `POST /v1/auth/login`

States:
- Idle.
- Submitting.
- Field error.
- General error.
- Success.

### Sign Up

Purpose:
- Create account.

Backend data:
- `POST /v1/auth/register`

States:
- Idle.
- Submitting.
- Validation error.
- Success.

### Onboarding Intro

Purpose:
- Set expectations.

Requirements:
- Explain deterministic coaching and safety-first positioning.
- Explain that AI provides explanations only.

### Onboarding Running History

Purpose:
- Capture current training baseline.

Required fields:
- Birth year.
- Sex.
- Experience level.
- Weekly running distance.
- Longest recent run.
- Injury history summary.

Backend data:
- Local form until review submit.

UI states:
- Idle.
- Validation error.

### Onboarding Availability

Purpose:
- Capture schedule constraints.

Required fields:
- Available run days.
- Preferred long-run day.
- Optional strength availability.
- Units and timezone.

### Onboarding Race Goal

Purpose:
- Capture one active goal.

Required fields:
- Half marathon or marathon.
- Race name optional.
- Race date.
- Goal style.
- Target time optional.

Validation:
- Race date must be at least 8 weeks away for half marathon.
- Race date must be at least 12 weeks away for marathon.

Recommended MVP default:
- If date is too close, reject and ask user to choose a later race.

### Onboarding Review

Purpose:
- Confirm inputs and create profile plus goal plus plan.

Backend data:
- `POST /v1/users/onboarding`

States:
- Review.
- Submitting.
- Success.
- Failure.

### Today

Purpose:
- Make the next action obvious.
- Current implementation may use a temporary shell landing screen while full Today content is completed in a later slice.

Required backend data:
- Current planned workout for today.
- Current readiness state.
- Latest adaptation summary.
- Pending AI explanation if available.

UI sections:
- Workout card.
- Reason chip.
- Check-in card.
- Safety banner when needed.

States:
- Loading.
- Empty with rest-day message.
- Error with retry.
- Success.

### Plan Calendar

Purpose:
- Show full current block clearly.

Required backend data:
- Current plan metadata.
- Current week workouts.
- Adjacent weeks summary.
- Adaptation markers.

Requirements:
- Day and week view toggle.
- Phase label.
- Recovery week label.
- "What changed?" chip on modified workouts.

States:
- Loading.
- Empty if no active plan.
- Error.
- Success.

### Workout Detail

Purpose:
- Remove ambiguity about the planned session.

Required backend data:
- Workout structure.
- Pace or effort guidance.
- Rationale.
- Safety notices.
- Reschedule and skip permissions.

Actions:
- Mark complete (disabled in current Slice 5 implementation).
- Mark skipped (disabled in current Slice 5 implementation).
- Reschedule once (deferred).
- Ask coach for explanation.

States:
- Loading.
- Error.
- Success.

### Complete Workout

Purpose:
- Capture completion data in under 20 seconds.

Current branch status:
- Deferred after Slice 5 follow-up remediation. Workout detail remains read-only in this branch.

Required inputs:
- Distance.
- Duration.
- RPE.
- Notes optional.
- Felt easier than target, on target, or harder than target.

Backend data:
- `POST /v1/workout-completions`

States:
- Idle.
- Submitting.
- Success.
- Error.

### Fatigue Check-In

Purpose:
- Capture daily readiness.

Required inputs:
- Sleep.
- Stress.
- Soreness.
- Motivation.
- Sick, travelling, or too busy flags.

Backend data:
- `POST /v1/fatigue-signals`

States:
- Idle.
- Success.
- Error.

### Pain Check-In

Purpose:
- Capture risk signals without diagnosing injury.

Required inputs:
- Body region.
- Severity.
- Sharp or diffuse.
- During run or after run.
- Can continue running yes or no.

Backend data:
- `POST /v1/injury-feedback`

States:
- Idle.
- Success.
- Error.

### Progress

Purpose:
- Show adherence and plan trends.

Required backend data:
- Weekly completion rate.
- Weekly distance trend.
- Long-run progression.
- Readiness trend.
- Recent adaptation count.

States:
- Loading.
- Empty for new users.
- Error.
- Success.

### Coach

Purpose:
- Explain workouts and adaptations.

Required backend data:
- Suggested prompts.
- Recent explanations.
- Chat availability.

Constraints:
- No free-form plan editing.
- No unsafe advice.

States:
- Loading.
- Error.
- Success.

### Coach Chat History

Purpose:
- Show safe explanation history.

Required backend data:
- Paginated chat items.

### Profile

Purpose:
- Manage account, preferences, and integrations.

Required backend data:
- Runner profile.
- Goal summary.
- Strava connection status.

### Edit Profile

Purpose:
- Update mutable profile fields.

Backend data:
- `PUT /v1/profile`

### Strava Connection

Purpose:
- Start or remove Strava connection.

Required backend data:
- Current connection state.
- Last sync time.
- Scope summary.

### Strava Import Review

Purpose:
- Resolve low-confidence activity matches.

Required backend data:
- List of unmatched or low-confidence activities.
- Candidate planned workouts.

## UI State Rules

- Every primary screen must support loading, error, and success.
- Any screen with content that may legitimately be absent must also support empty.
- AI explanation load failures must degrade to deterministic fallback text, not a blank space.
- High pain state must override standard success UI with a safety banner and reduced CTA set.
