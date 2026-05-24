# Local Development Runbook

This guide documents how to:
- start the local database,
- run the backend locally,
- run the Android app against that local backend.

## Prerequisites

- Docker Desktop (or Docker Engine with Compose)
- Java 21
- Maven 3.9+
- Android Studio (latest stable) with Android SDK 35

## 1. Start the local database

From repository root:

```bash
docker compose up -d postgres
```

Check container health:

```bash
docker compose ps
```

Expected service:
- `runcoach-postgres` on port `5432`
- status should become `healthy`

Stop database when done:

```bash
docker compose stop postgres
```

Remove container + data volume (destructive):

```bash
docker compose down -v
```

## 2. Run backend locally

From repository root:

```bash
cd apps/backend
mvn spring-boot:run
```

Notes:
- Spring default profile is `local` (configured in `application.yml`).
- Local datasource is `jdbc:postgresql://localhost:5432/runcoach` with user/password `runcoach`.
- Flyway migrations run automatically on startup.

Verify backend is up:

```bash
curl http://localhost:8080/v1/health
```

Expected response:

```json
{"status":"UP"}
```

## 3. Run Android app against local backend

The app is already configured to use:
- `http://10.0.2.2:8080/`

`10.0.2.2` is the Android emulator alias for your host machine's `localhost`.

### 3.1 Configure Android SDK path (one-time)

Option A (environment variable):

```bash
export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"
```

Option B (`apps/android/local.properties`):

```properties
sdk.dir=/Users/<your-user>/Library/Android/sdk
```

### 3.2 Build and run

From repository root:

```bash
cd apps/android
./gradlew :app:assembleDebug
```

Then open `apps/android` in Android Studio and run the `app` configuration on an emulator.

### 3.3 If running on a physical Android device

`10.0.2.2` works only for the emulator. For a physical device, either:

- use `adb reverse` so device traffic maps to your host:

```bash
adb reverse tcp:8080 tcp:8080
```

and set base URL to `http://127.0.0.1:8080/`, or

- set base URL to your machine LAN IP (example: `http://192.168.1.25:8080/`) and ensure phone + laptop are on the same network.

After changing the base URL, rebuild/reinstall the app.

## 4. Quick full-stack startup checklist

From repository root, in separate terminals:

1. Terminal A (DB):

```bash
docker compose up -d postgres
docker compose ps
```

2. Terminal B (Backend):

```bash
cd apps/backend
mvn spring-boot:run
```

3. Terminal C (Verify backend):

```bash
curl http://localhost:8080/v1/health
```

4. Run Android app from Android Studio on emulator.

## Connectivity troubleshooting

- Backend not reachable from emulator:
  - confirm backend is running on `localhost:8080`,
  - confirm app base URL is `http://10.0.2.2:8080/`,
  - do not use `http://localhost:8080/` inside emulator.
- Database connection errors in backend:
  - verify `runcoach-postgres` is up and healthy,
  - verify port `5432` is free on host,
  - restart with `docker compose restart postgres`.
- Build succeeds but app still calls old host:
  - clean + rebuild app after base URL changes,
  - uninstall/reinstall app on emulator/device.
