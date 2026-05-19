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
  "profileId": "13d2ebee-fcd5-4e55-b7e8-6cf9173659fa",
  "raceGoalId": "5bf19182-62d9-4127-b349-2fab89a80f6d",
  "trainingPlanId": "06cfb0f4-2f0e-4f22-b6c0-60ab7ea1b0cb",
  "planVersion": 1
}
```

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
  "timezone": "Europe/Berlin"
}
```

Behavior:
- Changes affecting future workouts may trigger plan regeneration.

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

### `POST /v1/planned-workouts/{plannedWorkoutId}/skip`

Request:

```json
{
  "reason": "TOO_BUSY",
  "notes": "Work trip"
}
```

Response:

```json
{
  "plannedWorkoutId": "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd",
  "status": "SKIPPED",
  "adaptationTriggered": true,
  "decisionId": "1f8b89eb-926e-48ef-89d0-c0cbec3678b3"
}
```

### `POST /v1/planned-workouts/{plannedWorkoutId}/reschedule`

Request:

```json
{
  "newDate": "2026-06-16",
  "planVersion": 2
}
```

Validation:
- Only one-day forward or backward move in MVP.
- Reject if spacing would place two quality sessions adjacently.

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
  "source": "MANUAL",
  "completedAt": "2026-06-15T06:40:00Z",
  "actualDistanceKm": 14.2,
  "actualDurationMin": 82,
  "avgPaceSecPerKm": 346,
  "avgHr": 154,
  "rpe": 7,
  "feltVsTarget": "HARDER",
  "notes": "Felt controlled until last rep, left calf slightly tight."
}
```

Response:

```json
{
  "completionId": "7d6f0d3e-4f3c-43ee-a98e-3d1b9b9fd6b1",
  "plannedWorkoutId": "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd",
  "completionScore": 0.86,
  "qualityScore": 0.73,
  "adaptationTriggered": false
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
  "readinessState": "ORANGE",
  "adaptationTriggered": true,
  "decisionId": "5904545d-7f74-41c0-b48c-b91a27504df1"
}
```

### `POST /v1/injury-feedback`

Request:

```json
{
  "reportedAt": "2026-06-15T08:00:00Z",
  "bodyRegion": "LEFT_CALF",
  "painType": "SHARP",
  "severity": 7,
  "onsetContext": "DURING_RUN",
  "canRun": false,
  "freeText": "Sharp pain during final interval."
}
```

Response:

```json
{
  "injuryFeedbackId": "b6dd69f6-7b05-4973-b66a-f17973a7fa19",
  "readinessState": "RED",
  "safetyAction": "REMOVE_INTENSITY_7_DAYS",
  "adaptationTriggered": true,
  "decisionId": "035964d4-ca68-4b0c-997a-f6693d52f02d"
}
```

### `GET /v1/insights/today`

Response:

```json
{
  "date": "2026-06-15",
  "readinessState": "ORANGE",
  "todayWorkout": {
    "plannedWorkoutId": "5c5a3b24-d8f2-448d-a6de-a3c0f6a825dd",
    "workoutType": "EASY",
    "headline": "45 min easy run"
  },
  "latestAdaptation": {
    "decisionId": "5904545d-7f74-41c0-b48c-b91a27504df1",
    "headline": "This week has been lightened to protect recovery."
  }
}
```

### `GET /v1/progress/summary`

Response:

```json
{
  "weeklyCompletionRate": 0.82,
  "weeklyDistanceKm": 41.5,
  "longRunTrend": [
    {
      "weekIndex": 3,
      "distanceKm": 22.0
    },
    {
      "weekIndex": 4,
      "distanceKm": 24.0
    }
  ],
  "readinessTrend": [
    {
      "date": "2026-06-13",
      "state": "GREEN"
    },
    {
      "date": "2026-06-14",
      "state": "YELLOW"
    }
  ],
  "recentAdaptationCount": 1
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
- Reject duplicate same-day fatigue signal from same source.
- Reject injury feedback with severity outside 0 to 10.
- Reject AI chat questions that attempt plan creation or medical diagnosis.

## Conflict Rules

- Mutating workout endpoints must accept or infer current `planVersion`.
- Return `409 CONFLICT` if client acts on a stale plan version.

## Auth Requirements Summary

- Public: register, login, refresh, Strava webhook verification, Strava webhook event receiver
- Authenticated: all profile, goal, plan, workout, check-in, status, activity, and coach endpoints
