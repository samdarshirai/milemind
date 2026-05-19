# 03 Android App Spec

## Android Architecture

### Architectural Style

- Native Android only.
- Kotlin only.
- Jetpack Compose for all screens.
- MVVM per feature.
- Single activity architecture.
- Navigation Compose.
- Repositories as the boundary between UI and backend APIs.
- Room cache for read models only.

### Architectural Principles

- UI state comes from backend-backed view models.
- Android never recreates training or adaptation logic locally.
- View models map backend DTOs into UI models.
- Business rules in Android are limited to presentation, input validation, and interaction orchestration.

## Suggested Module Structure

- `:app`
- `:core:designsystem`
- `:core:model`
- `:core:network`
- `:core:database`
- `:core:datastore`
- `:core:common`
- `:feature:auth`
- `:feature:onboarding`
- `:feature:today`
- `:feature:plan`
- `:feature:workout`
- `:feature:checkin`
- `:feature:progress`
- `:feature:coach`
- `:feature:profile`
- `:feature:strava`

## Kotlin Package Structure

Recommended root package:
- `com.company.runcoach`

Recommended package layout inside each feature module:
- `ui`
- `ui.components`
- `ui.model`
- `ui.navigation`
- `domain`
- `data`
- `data.remote`
- `data.local`
- `data.mapper`

Shared packages:
- `com.company.runcoach.core.model`
- `com.company.runcoach.core.network`
- `com.company.runcoach.core.database`
- `com.company.runcoach.core.designsystem`
- `com.company.runcoach.core.common`

## Jetpack Compose Structure

### Screen Pattern

Each screen should have:
- Route composable.
- Stateless content composable.
- Small reusable section composables.
- Previewable state-specific content where feasible.

Example pattern:
- `TodayRoute`
- `TodayScreen`
- `TodayWorkoutCard`
- `TodayCheckInCard`
- `TodayEmptyState`

### Design System Requirements

- Central typography, color, spacing, icon, and component tokens in `:core:designsystem`.
- Consistent banners for safety warnings, sync errors, and AI confidence disclaimers.
- Shared list, card, chip, button, and dialog components.

## MVVM Structure

### ViewModel Responsibilities

- Load data from repositories.
- Expose a single immutable `UiState`.
- Expose one-off `UiEvent` or `Effect` for navigation, snackbars, and dialogs.
- Handle retries.
- Validate local form input before API submission.

### UI State Pattern

Recommended pattern:
- `isLoading`
- `isRefreshing`
- `error`
- `content`
- `pendingAction`

Do not expose raw Retrofit responses to the UI.

## Repository Layer

### Repository Responsibilities

- Coordinate remote API and local cache.
- Map remote DTOs to domain models.
- Expose flows for cached reads.
- Hide pagination and sync details from view models.

### Recommended Repositories

- `AuthRepository`
- `ProfileRepository`
- `GoalRepository`
- `PlanRepository`
- `WorkoutRepository`
- `CheckInRepository`
- `ProgressRepository`
- `CoachRepository`
- `StravaRepository`

## API Client Layer

### Networking Stack

- Retrofit
- OkHttp
- Kotlinx Serialization or Moshi
- Coroutines

### Client Requirements

- JWT bearer auth interceptor.
- Refresh-token flow handled through a session manager.
- Correlation ID header for observability.
- Request timeout tuned separately for normal APIs and AI explanation APIs.
- Logging interceptor disabled or redacted in production.

## State Management

### Recommended Approach

- Use `StateFlow` from view models.
- Use `collectAsStateWithLifecycle` in Compose.
- Use repository flows for current plan and profile.
- Use saved state handle for navigation arguments only.

### Avoid In MVP

- No Redux-style global store.
- No local rule engine.
- No complicated offline command queue.

## Error Handling

### UI Error Rules

- Show full-screen error only when primary content cannot load.
- Show inline error for failed check-in or completion submission.
- Show non-blocking banner for Strava sync delays.
- Show deterministic fallback text if AI explanation fails.

### Domain Error Categories

- Authentication expired.
- Validation failed.
- Connectivity failure.
- Server unavailable.
- Conflict due to stale plan version.
- Strava connection issue.
- AI explanation unavailable.

## Offline and Local Caching Decision

### Recommended MVP Default

- Cache latest successful reads for profile, current plan, planned workouts, completion history summary, Strava connection status, and current insights.
- Do not support offline submissions for workout completion, check-ins, or Strava actions in MVP.
- If the device is offline, show cached data and disable mutation CTAs with retry guidance.

Rationale:
- This keeps user experience acceptable without introducing replay, merge, or stale-plan conflict complexity.

## Suggested Libraries

- Jetpack Compose
- Navigation Compose
- Hilt
- Retrofit
- OkHttp
- Kotlinx Serialization
- Room
- DataStore
- Coil
- Timber or structured logging adapter
- JUnit 4 or 5
- Turbine
- MockK
- Robolectric only where necessary
- AndroidX Test
- Compose UI Test

## Android-Specific Security Requirements

- Store access and refresh tokens in encrypted storage.
- Never store Strava tokens on device.
- Use app links for OAuth completion back into the app.
- Redact health-like or training-sensitive values from debug logs.

## Deep Link Requirements

- `https://app.example.com/auth/callback`
- `https://app.example.com/strava/connected`

Recommended MVP default:
- Backend handles Strava OAuth callback, then redirects to an Android app link with a short-lived success token or status code only.

## Performance Targets

- Cold start to first meaningful content under 3 seconds on a mid-tier Android device for cached sessions.
- Screen-to-screen navigation under 300 ms perceived latency.
- Plan week render should not block on AI explanations.
