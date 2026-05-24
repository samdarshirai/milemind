# Adaptive Running Coach MVP - Initial Skeleton

This repository now contains an initial, slice-safe skeleton for:
- `apps/backend` (Java 21 + Spring Boot + Maven)
- `apps/android` (Kotlin + Compose + Hilt + Navigation)
- `docs` (implementation documents)

## Why Maven for backend

Maven was selected for the backend skeleton because:
- predictable, conventional Spring Boot project layout;
- lower initial build-script complexity for a clean bootstrap;
- straightforward profile/test/Flyway wiring in one `pom.xml`.

## Project Structure

- `apps/backend`
  - modular package skeleton aligned to `docs/05-backend-architecture.md`
  - `GET /v1/health`
  - `local` and `test` profiles
  - Flyway enabled with baseline placeholder migration
  - health endpoint test using MockMvc
- `apps/android`
  - single-activity Compose app
  - Navigation Compose host with splash placeholder route
  - Hilt application/activity wiring
  - foundational `core` modules
  - Retrofit/OkHttp API client foundation targeting local backend (`10.0.2.2:8080`)
- `docker-compose.yml`
  - PostgreSQL 16 for local backend runtime

## Prerequisites

- Java 21
- Maven 3.9+
- Android Studio (latest stable, with Android SDK 35)
- Docker Desktop

Compatibility note:
- The skeleton targets Java 21. Running under newer JDKs (for example Java 25) is not part of the documented support baseline yet.

## Backend Run

1. Start PostgreSQL:
   - `docker compose up -d`
2. Run backend:
   - `cd apps/backend`
   - `mvn spring-boot:run`
3. Verify health:
   - `curl http://localhost:8080/v1/health`
4. Full backend + DB runbook:
   - `docs/18-local-development-runbook.md`

## Android Run

1. Configure Android SDK path for CLI builds (choose one):
   - Environment variable:
     - `export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"`
   - Or local Gradle property:
     - create `apps/android/local.properties` with:
       - `sdk.dir=/Users/<your-user>/Library/Android/sdk`
2. Verify wrapper commands from Android module root:
   - `cd apps/android`
   - `./gradlew test :app:assembleDebug`
3. Open `apps/android` in Android Studio.
4. Let Gradle sync.
5. Run `app` on emulator/device.

Notes:
- Android emulator reaches local backend using `http://10.0.2.2:8080/`.
- End-to-end local setup/run instructions are documented in `docs/18-local-development-runbook.md`.

## Tests

- Backend:
  - `cd apps/backend`
  - `mvn test`
- Android (from repo root):
  - `./gradlew test`
  - `./gradlew :app:assembleDebug`
- Android (from `apps/android` directly):
  - Ensure SDK path is configured via `ANDROID_SDK_ROOT` or `apps/android/local.properties` first.
  - `./gradlew test`
  - `./gradlew :app:assembleDebug`

## Scope Guardrails Applied

Not implemented in this skeleton:
- Auth/session flows
- Onboarding/profile flows
- Training plan generation
- Strava integration flows
- DeepSeek integration
- Adaptation logic
