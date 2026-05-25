# 07 API Contracts

## API Conventions

- Base path: `/v1`
- Auth: JWT bearer token for all user APIs unless noted otherwise
- Content type: `application/json`
- IDs: UUID strings except Strava external IDs
- Time format: ISO 8601 UTC timestamps
- Date format: `YYYY-MM-DD`

## Error Response Format

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Race date must be at least 12 weeks away for marathon plans.",
    "details": [
      {
        "field": "raceDate",
        "issue": "too_soon"
      }
    ],
    "correlationId": "fdab0f88-ec5e-47f8-92c0-6642c0c1cce8"
  }
}
```

## Pagination Convention

Use cursor pagination for chat history and Strava activity history.

Query params:
- `limit`
- `cursor`

Response format:

```json
{
  "items": [],
  "nextCursor": "opaque-cursor"
}
```

## Auth Endpoints

### `POST /v1/auth/register`

Request:

```json
{
  "email": "runner@example.com",
  "password": "StrongPassword123!",
  "timezone": "Europe/Berlin"
}
```

Response:

```json
{
  "userId": "a0fc10b5-4b7e-49b7-985e-d4b29cb499dd",
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "onboardingRequired": true
}
```

Validation:
- Valid email.
- Password minimum 10 characters.
- Supported timezone required.

### `POST /v1/auth/login`

Request:

```json
{
  "email": "runner@example.com",
  "password": "StrongPassword123!"
}
```

Response:

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "onboardingRequired": false
}
```

### `POST /v1/auth/refresh`

Request:

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

Response:

```json
{
  "accessToken": "new-access-token",
  "refreshToken": "new-refresh-token"
}
```

### `POST /v1/auth/logout`

Headers:
- `Authorization: Bearer <access-token>`

Request:

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

Response:
- `204 No Content`

## Onboarding And Profile Endpoints

### `POST /v1/users/onboarding`

Creates profile, goal, and initial plan in one transaction boundary plus async follow-up events.

Request:

```json
{
  "profile": {
    "birthYear": 1992,
    "sex": "FEMALE",
    "experienceLevel": "BEGINNER",
    "typicalWeeklyDistanceKm": 24.0,
    "longestRecentRunKm": 10.0,
    "preferredRunDays": ["TUESDAY", "THURSDAY", "SATURDAY", "SUNDAY"],
    "preferredLongRunDay": "SUNDAY",
    "goalStyle": "FINISH",
    "strengthDaysPerWeek": 1,
    "units": "KM",
    "timezone": "Europe/Berlin",
    "injuryHistory": {
      "hadRunningInjuryLast12Months": false
    }
  },
  "raceGoal": {
    "raceName": "Berlin Half",
    "raceDistanceType": "HALF_MARATHON",
    "raceDate": "2026-10-04",
    "targetTimeSeconds": null
  }
}
```

Response:

```json
{
  "userId": "a0fc10b5-4b7e-49b7-985e-d4b29cb499dd",
  "profileId": "13d2ebee-fcd5-4e55-b7e8-6cf9173659fa"
}
```

Note:
- Slice 2 onboarding currently persists the runner profile only.
- Race goal and training plan identifiers are returned by their dedicated endpoints when those slices are enabled.

Validation:
- Adult only.
- Half marathon must be at least 8 weeks away.
- Marathon must be at least 12 weeks away.
- Preferred run days minimum 3 for half marathon, minimum 4 for marathon.
- Longest recent run must be positive.

### `GET /v1/profile`

Response:

```json
{
  "userId": "a0fc10b5-4b7e-49b7-985e-d4b29cb499dd",
  "email": "runner@example.com",
  "timezone": "Europe/Berlin",
  "profile": {
    "experienceLevel": "BEGINNER",
    "typicalWeeklyDistanceKm": 24.0,
    "longestRecentRunKm": 10.0,
    "preferredRunDays": ["TUESDAY", "THURSDAY", "SATURDAY", "SUNDAY"],
    "preferredLongRunDay": "SUNDAY",
    "goalStyle": "FINISH",
    "strengthDaysPerWeek": 1,
    "units": "KM"
  }
}
```

### `PUT /v1/profile`

Request:

```json
{
  "preferredRunDays": ["MONDAY", "WEDNESDAY", "FRIDAY", "SUNDAY"],
  "preferredLongRunDay": "SUNDAY",
  "strengthDaysPerWeek": 1,
  "units": "KM",
  "timezone": "Europe/Berlin",
  "injuryHistory": {
    "hadRunningInjuryLast12Months": false,
    "summary": "No current injury, mild right knee pain last winter."
  }
}
```

Behavior:
- Changes affecting future workouts may trigger plan regeneration.
- `typicalWeeklyDistanceKm` and `longestRecentRunKm` are onboarding fields and are not mutable via profile update.

## Race Goal Endpoints

### `POST /v1/race-goals`

Request:

```json
{
  "raceName": "Munich Marathon",
  "raceDistanceType": "MARATHON",
  "raceDate": "2026-10-11",
  "goalStyle": "IMPROVE",
  "targetTimeSeconds": 13680
}
```

Response:

```json
{
  "raceGoalId": "de885ad0-cbc8-499d-a99e-9b9112ae3330",
  "status": "ACTIVE"
}
```

Rule:
- Reject if another active goal exists.

### `GET /v1/race-goals/current`

Response:

```json
{
  "raceGoalId": "de885ad0-cbc8-499d-a99e-9b9112ae3330",
  "raceDistanceType": "MARATHON",
  "raceDate": "2026-10-11",
  "goalStyle": "IMPROVE",
  "targetTimeSeconds": 13680,
  "status": "ACTIVE"
}
```

## Plan Endpoints

### `POST /v1/plans/generate`

Used only when plan needs explicit regeneration after goal creation or profile changes.

Request:

```json
{
  "raceGoalId": "de885ad0-cbc8-499d-a99e-9b9112ae3330",
  "reason": "USER_REQUESTED_REBUILD"
}
```

Response:

```json
{
  "trainingPlanId": "06cfb0f4-2f0e-4f22-b6c0-60ab7ea1b0cb",
  "planVersion": 2,
  "status": "ACTIVE"
}
```

### `GET /v1/plans/current`

Response:

```json
{
  "trainingPlanId": "06cfb0f4-2f0e-4f22-b6c0-60ab7ea1b0cb",
  "planVersion": 2,
  "methodologyCode": "ROAD_MARATHON_V1",
  "raceGoal": {
    "raceDistanceType": "MARATHON",
    "raceDate": "2026-10-11"
  },
  "currentWeekIndex": 5,
  "weeks": [
    {
      "weekIndex": 5,
      "phase": "BUILD",
      "recoveryWeek": false,
      "targetDistanceKm": 42.0,
      "workouts": [
        {
          "plannedWorkoutId": "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd",
          "scheduledDate": "2026-06-15",
          "workoutType": "THRESHOLD",
          "status": "PLANNED",
          "plannedDistanceKm": 12.0,
          "plannedDurationMin": 68,
          "intensityZone": "Z3",
          "changeReasonCodes": []
        }
      ]
    }
  ]
}
```

### `GET /v1/plans/{planId}`

Response:

```json
{
  "planId": "06cfb0f4-2f0e-4f22-b6c0-60ab7ea1b0cb",
  "status": "ACTIVE",
  "startDate": "2026-05-20",
  "endDate": "2026-09-20",
  "raceGoalId": "de885ad0-cbc8-499d-a99e-9b9112ae3330",
  "weeks": [
    {
      "weekNumber": 1,
      "startDate": "2026-05-20",
      "endDate": "2026-05-26",
      "totalPlannedDistanceKm": 24.0,
      "workouts": []
    }
  ]
}
```

### `POST /v1/planned-workouts/{plannedWorkoutId}/skip`

Validation:
- `expectedPlanVersion` is required.
- Return `409 CONFLICT` with `error.code = "STALE_PLAN_VERSION"` when `expectedPlanVersion` is stale.
- Reject when workout status is not mutable for skip.

Request:

```json
{
  "reason": "NO_TIME",
  "expectedPlanVersion": 2
}
```

Response:

```json
{
  "planVersion": 3,
  "adaptation": {
    "id": "1f8b89eb-926e-48ef-89d0-c0cbec3678b3",
    "summary": "Your next 14 days were adjusted after skipping a workout.",
    "affectedFromDate": "2026-06-15",
    "affectedToDate": "2026-06-29",
    "changedWorkoutIds": [
      "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd"
    ]
  }
}
```

### `POST /v1/planned-workouts/{plannedWorkoutId}/reschedule`

Request:

```json
{
  "targetDate": "2026-06-16",
  "expectedPlanVersion": 2
}
```

Validation:
- `expectedPlanVersion` is required.
- `targetDate` is required.
- Only one-day forward or backward move in MVP (`move_window_exceeded` when violated).
- Reject if spacing would place two quality sessions adjacently.
- Reject long-run moves when target day is not free or long-run spacing is unsafe.
- Reject when workout status is not mutable for reschedule.
- Return `409 CONFLICT` with `error.code = "STALE_PLAN_VERSION"` when `expectedPlanVersion` is stale.

### `GET /v1/planned-workouts/{plannedWorkoutId}`

Response:

```json
{
  "plannedWorkoutId": "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd",
  "scheduledDate": "2026-06-15",
  "workoutType": "THRESHOLD",
  "workoutSubtype": "CRUISE_INTERVALS",
  "plannedDistanceKm": 12.0,
  "plannedDurationMin": 68,
  "intensityZone": "Z3",
  "structure": [
    {
      "segmentType": "WARM_UP",
      "durationMin": 15,
      "cue": "Easy running"
    },
    {
      "segmentType": "MAIN_SET",
      "repetitions": 4,
      "durationMin": 6,
      "recoveryMin": 2,
      "cue": "Comfortably hard"
    }
  ],
  "whyThisWorkout": "Build sustainable speed without excessive fatigue.",
  "changeReasonCodes": []
}
```

## Completion And Check-In Endpoints

### `POST /v1/workout-completions`

Request:

```json
{
  "plannedWorkoutId": "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd",
  "actualDistanceKm": 14.2,
  "actualDurationMin": 82
}
```

Response:

```json
{
  "completionId": "7d6f0d3e-4f3c-43ee-a98e-3d1b9b9fd6b1",
  "plannedWorkoutId": "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd",
  "planVersion": 3,
  "adaptationTriggered": true,
  "adaptation": {
    "id": "1f8b89eb-926e-48ef-89d0-c0cbec3678b3",
    "summary": "Your near-term plan was softened after an under-completed workout.",
    "affectedFromDate": "2026-06-15",
    "affectedToDate": "2026-06-22",
    "changedWorkoutIds": [
      "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd"
    ]
  }
}
```

### `POST /v1/fatigue-signals`

Request:

```json
{
  "signalDate": "2026-06-15",
  "sleepScore": 2,
  "stressScore": 4,
  "sorenessScore": 4,
  "motivationScore": 2,
  "illnessFlag": false,
  "tooBusyFlag": false,
  "travellingFlag": false
}
```

Response:

```json
{
  "fatigueSignalId": "4f5e76eb-e590-4cd1-9f95-dc70a8d30930",
  "readinessState": "CAUTION"
}
```

### `POST /v1/injury-feedback`

Request:

```json
{
  "reportedAt": "2026-06-15T08:00:00Z",
  "hasPain": true,
  "bodyRegion": "LEFT_CALF",
  "painType": "SHARP",
  "severity": 7,
  "onsetContext": "DURING_RUN",
  "canRun": false,
  "freeText": "Sharp pain during final interval."
}
```

No-pain request (supported):

```json
{
  "reportedAt": "2026-06-15T08:00:00Z",
  "hasPain": false,
  "freeText": "No pain today."
}
```

Behavior:
- Canonical request: include `hasPain` explicitly.
- If `hasPain=false`, pain detail fields must be omitted.
- If `hasPain=true`, `bodyRegion`, `painType`, `severity`, and `onsetContext` are required.
- Backward compatibility: if `hasPain` is omitted and pain detail fields are all omitted, backend treats it as a no-pain check-in.
- If `hasPain` is omitted and only risk fields are provided (`canRun` and/or `redFlag`) without pain details, backend rejects the request with `VALIDATION_ERROR`.
- If any of those pain fields are provided, all pain fields are required and `severity` must be 0 to 10.
- Allowed `bodyRegion`: `LEFT_CALF`, `RIGHT_CALF`, `KNEE`, `ANKLE`, `HIP`, `LOWER_BACK`.
- Allowed `painType`: `SHARP`, `DULL`, `ACHING`, `TIGHTNESS`.
- Allowed `onsetContext`: `DURING_RUN`, `AFTER_RUN`, `ALL_DAY`, `OTHER`.

Response:

```json
{
  "injuryFeedbackId": "b6dd69f6-7b05-4973-b66a-f17973a7fa19",
  "readinessState": "HIGH_RISK"
}
```

### `GET /v1/insights/today`

Behavior:
- `date` and `hasCheckInToday` are evaluated using the runner's profile timezone calendar-day boundaries.
- `readinessState` is computed from check-ins submitted within that same local calendar day only.
- If no check-in exists for the runner's local day, readiness defaults to `READY`.
- `latestAdaptation` is only returned when it is relevant to today's context:
  - affected date range includes today, or
  - changed workout IDs include today's planned workout.

Response:

```json
{
  "date": "2026-06-15",
  "planId": "06cfb0f4-2f0e-4f22-b6c0-60ab7ea1b0cb",
  "planVersion": 2,
  "todaysPlannedWorkout": {
    "plannedWorkoutId": "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd",
    "workoutType": "EASY_RUN",
    "status": "PLANNED",
    "plannedDistanceKm": 8.0,
    "plannedDurationMin": 45,
    "intensityZone": "EASY"
  },
  "readinessState": "CAUTION",
  "readinessLabel": "Caution",
  "readinessMessage": "Some readiness signals suggest a conservative effort today.",
  "latestFatigueSignal": {
    "signalDate": "2026-06-15",
    "sleepScore": 2,
    "stressScore": 4,
    "sorenessScore": 4,
    "motivationScore": 2,
    "illnessFlag": false,
    "tooBusyFlag": false,
    "travellingFlag": false,
    "notes": "Hard week"
  },
  "latestInjuryFeedback": null,
  "hasCheckInToday": true,
  "recommendedTone": "supportive",
  "latestAdaptation": {
    "adaptationDecisionId": "9df7fb80-2b70-4bcd-b6e7-58b6f58427bd",
    "summary": "Shifted quality session to recovery day.",
    "affectedFromDate": "2026-06-15",
    "affectedToDate": "2026-06-21",
    "changedWorkoutIds": [
      "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd"
    ]
  },
  "insightMessages": [
    "Today's planned workout is EASY_RUN.",
    "Some readiness signals suggest a conservative effort today."
  ],
  "warnings": [
    "Readiness signals indicate reduced training tolerance today."
  ]
}
```

Readiness states:
- `READY`
- `CAUTION`
- `HIGH_RISK`

### `GET /v1/progress/summary`

Response:

```json
{
  "planId": "06cfb0f4-2f0e-4f22-b6c0-60ab7ea1b0cb",
  "planVersion": 2,
  "currentTrainingWeek": 5,
  "summary": {
    "plannedWorkouts": 32,
    "completedWorkouts": 20,
    "skippedWorkouts": 3,
    "rescheduledWorkouts": 2,
    "adherencePercentage": 63
  },
  "weeklyCompletion": [
    {
      "weekNumber": 5,
      "planned": 5,
      "completed": 4,
      "skipped": 1,
      "completionPercentage": 80
    }
  ],
  "longRunProgression": [
    {
      "weekNumber": 3,
      "plannedDistanceKm": 22.0,
      "actualDistanceKm": 21.5,
      "status": "COMPLETED"
    },
    {
      "weekNumber": 4,
      "plannedDistanceKm": 24.0,
      "actualDistanceKm": null,
      "status": "PLANNED"
    }
  ],
  "readinessTrend": [
    {
      "date": "2026-06-13",
      "readinessState": "READY",
      "fatigueLevel": 2,
      "painSeverity": null
    },
    {
      "date": "2026-06-14",
      "readinessState": "CAUTION",
      "fatigueLevel": 3,
      "painSeverity": 4
    }
  ],
  "recentStatusDistribution": {
    "planned": 2,
    "completed": 3,
    "skipped": 1,
    "rescheduled": 1
  },
  "emptyState": false,
  "message": "You completed 63% of your planned workouts so far."
}
```

## Strava Endpoints

### `POST /v1/integrations/strava/connect-session`

Creates backend state and returns authorization URL.

Response:

```json
{
  "authorizationUrl": "https://www.strava.com/oauth/authorize?...",
  "state": "opaque-state-id"
}
```

### `GET /v1/integrations/strava/callback`

Notes:
- Browser redirect target from Strava.
- No Android auth token returned in query string.
- Backend completes exchange and redirects to app link.

### `GET /v1/integrations/strava/status`

Response:

```json
{
  "connected": true,
  "connectionStatus": "ACTIVE",
  "grantedScopes": ["read", "activity:read"],
  "lastSyncAt": "2026-06-15T07:10:00Z"
}
```

### `POST /v1/integrations/strava/webhook`

Auth:
- None.

Behavior:
- Verify request type.
- Persist idempotency record.
- Enqueue async job.
- Return 200 quickly.

### `GET /v1/integrations/strava/activities`

Query params:
- `status`
- `limit`
- `cursor`

Response:

```json
{
  "items": [
    {
      "stravaActivityId": 1234567890,
      "startTime": "2026-06-15T06:00:00Z",
      "distanceM": 14200.0,
      "movingTimeSec": 4840,
      "matchStatus": "MATCHED",
      "plannedWorkoutId": "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd"
    }
  ],
  "nextCursor": null
}
```

### `POST /v1/workout-completions/{completionId}/confirm-match`

Request:

```json
{
  "plannedWorkoutId": "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd"
}
```

Response:

```json
{
  "completionId": "7d6f0d3e-4f3c-43ee-a98e-3d1b9b9fd6b1",
  "plannedWorkoutId": "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd",
  "matchStatus": "CONFIRMED"
}
```

### `DELETE /v1/integrations/strava/connection`

Response:

```json
{
  "connected": false,
  "connectionStatus": "DISCONNECTED"
}
```

## AI Endpoints

### `POST /v1/coach/explain-workout`

Request:

```json
{
  "plannedWorkoutId": "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd"
}
```

Response:

```json
{
  "headline": "This workout builds sustainable speed.",
  "summary": "The intervals help you improve threshold effort without the stress of an all-out session.",
  "confidence": 0.92,
  "reasonCodes": ["THRESHOLD_DEVELOPMENT", "SAFE_INTENSITY_DISTRIBUTION"],
  "source": "AI_VALIDATED"
}
```

### `POST /v1/coach/explain-adaptation`

Request:

```json
{
  "decisionId": "5904545d-7f74-41c0-b48c-b91a27504df1"
}
```

Response:

```json
{
  "headline": "Your week has been dialed back to protect recovery.",
  "summary": "We reduced intensity because your fatigue signals are elevated and recent training completion has dropped.",
  "confidence": 0.91,
  "reasonCodes": ["HIGH_FATIGUE_SCORE", "LOW_COMPLETION_RECENT_KEY_WORKOUTS"],
  "source": "AI_VALIDATED"
}
```

### `POST /v1/coach/chat`

Request:

```json
{
  "message": "Why did my threshold run change?",
  "contextType": "ADAPTATION_EXPLANATION"
}
```

Response:

```json
{
  "messageId": "db8d1221-5b0b-4fdf-8ae5-40eb50c3c855",
  "messageType": "SAFE_EXPLANATION",
  "reply": "Your threshold run was replaced because your recent fatigue signals were elevated and the engine is protecting recovery before your long run.",
  "confidence": 0.89,
  "reasonCodes": ["HIGH_FATIGUE_SCORE", "PROTECT_LONG_RUN"]
}
```

### `GET /v1/coach/chat/history`

Response:

```json
{
  "items": [
    {
      "messageId": "db8d1221-5b0b-4fdf-8ae5-40eb50c3c855",
      "role": "ASSISTANT",
      "messageType": "SAFE_EXPLANATION",
      "text": "Your threshold run was replaced because your recent fatigue signals were elevated and the engine is protecting recovery before your long run.",
      "createdAt": "2026-06-15T08:30:00Z"
    }
  ],
  "nextCursor": null
}
```

## Validation Rules

- Reject onboarding if age under 18.
- Reject unsupported race distances.
- Reject race date too soon for minimum training block.
- Reject plan generation if no active goal exists.
- Reject reschedule if it creates unsafe spacing.
- Same-day fatigue signal submissions from the same source are idempotent and update the existing record.
- Reject injury feedback with severity outside 0 to 10.
- Reject partial pain payloads for injury feedback when pain is being reported.
- Reject AI chat questions that attempt plan creation or medical diagnosis.

## Conflict Rules

- Mutating workout endpoints must accept or infer current `planVersion`.
- Return `409 CONFLICT` if client acts on a stale plan version.

## Auth Requirements Summary

- Public: register, login, refresh, Strava webhook verification, Strava webhook event receiver
- Authenticated: all profile, goal, plan, workout, check-in, status, activity, and coach endpoints
