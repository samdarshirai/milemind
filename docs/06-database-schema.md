# 06 Database Schema

## Schema Principles

- PostgreSQL is the source of truth for users, profiles, plans, workouts, check-ins, adaptations, and integrations.
- Store deterministic plan state explicitly, not as derived-only projections.
- Keep Strava raw payload retention minimal.
- Persist enough audit detail to explain why a workout changed.

## Tables

### `app_user`

Purpose:
- Account identity.

Columns:
- `id uuid primary key`
- `email varchar(255) not null unique`
- `password_hash varchar(255) not null`
- `status varchar(30) not null`
- `locale varchar(20) not null default 'en-US'`
- `timezone varchar(64) not null`
- `created_at timestamptz not null`
- `updated_at timestamptz not null`

Constraints:
- Unique email.
- Status enum in application layer and DB check constraint.

### `refresh_token`

Purpose:
- Server-side refresh token rotation.

Columns:
- `id uuid primary key`
- `user_id uuid not null references app_user(id)`
- `token_hash varchar(255) not null unique`
- `expires_at timestamptz not null`
- `revoked_at timestamptz`
- `created_at timestamptz not null`

### `runner_profile`

Purpose:
- Runner baseline and preferences.

Columns:
- `id uuid primary key`
- `user_id uuid not null unique references app_user(id)`
- `birth_year int not null`
- `sex varchar(20)`
- `experience_level varchar(20) not null`
- `typical_weekly_distance_km numeric(6,2) not null`
- `longest_recent_run_km numeric(6,2) not null`
- `preferred_run_days jsonb not null`
- `preferred_long_run_day varchar(12) not null`
- `goal_style varchar(20) not null`
- `injury_history jsonb`
- `strength_days_per_week int not null default 0`
- `units varchar(10) not null default 'km'`
- `created_at timestamptz not null`
- `updated_at timestamptz not null`

Constraints:
- One profile per user.
- Run days array cannot be empty.
- Strength days limited to 0 to 2 in MVP.

### `race_goal`

Purpose:
- Single active race goal.

Columns:
- `id uuid primary key`
- `user_id uuid not null references app_user(id)`
- `race_name varchar(255)`
- `race_distance_type varchar(30) not null`
- `race_date date not null`
- `target_time_seconds int`
- `goal_style varchar(20) not null`
- `status varchar(20) not null`
- `created_at timestamptz not null`
- `updated_at timestamptz not null`

Constraints:
- Partial unique index on one active goal per user.
- Allowed distances: `HALF_MARATHON`, `MARATHON`.

### `training_plan`

Purpose:
- Top-level plan aggregate.

Columns:
- `id uuid primary key`
- `user_id uuid not null references app_user(id)`
- `race_goal_id uuid not null references race_goal(id)`
- `plan_status varchar(20) not null`
- `plan_version int not null`
- `methodology_code varchar(50) not null`
- `start_date date not null`
- `end_date date not null`
- `current_week_index int not null`
- `last_regenerated_at timestamptz`
- `created_at timestamptz not null`
- `updated_at timestamptz not null`

Constraints:
- Unique `(user_id, race_goal_id, plan_version)`.

### `training_week`

Purpose:
- Week-level plan metadata.

Columns:
- `id uuid primary key`
- `training_plan_id uuid not null references training_plan(id)`
- `week_index int not null`
- `phase varchar(30) not null`
- `target_distance_km numeric(6,2)`
- `target_time_min int`
- `recovery_week boolean not null default false`
- `created_at timestamptz not null`
- `updated_at timestamptz not null`

Constraints:
- Unique `(training_plan_id, week_index)`.

### `planned_workout`

Purpose:
- Workout calendar entries.

Columns:
- `id uuid primary key`
- `training_week_id uuid not null references training_week(id)`
- `training_plan_id uuid not null references training_plan(id)`
- `user_id uuid not null references app_user(id)`
- `scheduled_date date not null`
- `workout_type varchar(30) not null`
- `workout_subtype varchar(50)`
- `status varchar(20) not null`
- `planned_distance_km numeric(6,2)`
- `planned_duration_min int`
- `intensity_zone varchar(20)`
- `structure_json jsonb not null`
- `rationale_json jsonb not null`
- `adapted_from_workout_id uuid`
- `plan_version int not null`
- `created_at timestamptz not null`
- `updated_at timestamptz not null`

Constraints:
- `planned_distance_km` or `planned_duration_min` must be present.
- `plan_version` required for optimistic conflict detection.

### `workout_completion`

Purpose:
- Completed workout record.

Columns:
- `id uuid primary key`
- `planned_workout_id uuid references planned_workout(id)`
- `user_id uuid not null references app_user(id)`
- `completion_source varchar(20) not null`
- `completed_at timestamptz not null`
- `actual_distance_km numeric(6,2)`
- `actual_duration_min int`
- `avg_pace_sec_per_km int`
- `avg_hr int`
- `rpe int not null`
- `felt_vs_target varchar(20) not null`
- `completion_score numeric(4,3) not null`
- `quality_score numeric(4,3)`
- `notes text`
- `strava_activity_id uuid references strava_activity(id)`
- `match_locked boolean not null default false`
- `created_at timestamptz not null`

Constraints:
- One confirmed completion per planned workout unless replacement flow is added later.

### `fatigue_signal`

Purpose:
- Daily readiness check-in.

Columns:
- `id uuid primary key`
- `user_id uuid not null references app_user(id)`
- `signal_date date not null`
- `sleep_score int not null`
- `stress_score int not null`
- `soreness_score int not null`
- `motivation_score int not null`
- `illness_flag boolean not null default false`
- `too_busy_flag boolean not null default false`
- `travelling_flag boolean not null default false`
- `source varchar(20) not null`
- `created_at timestamptz not null`

Constraints:
- Unique `(user_id, signal_date, source)`.

### `injury_feedback`

Purpose:
- Pain and risk check-in.

Columns:
- `id uuid primary key`
- `user_id uuid not null references app_user(id)`
- `reported_at timestamptz not null`
- `has_pain boolean not null default true`
- `body_region varchar(50)`
- `pain_type varchar(20)`
- `severity int`
- `onset_context varchar(20)`
- `can_run boolean`
- `red_flag boolean not null default false`
- `free_text text`
- `created_at timestamptz not null`

Constraints:
- Severity limited to 0 to 10 when present.
- API normalization rule: no-pain defaulting applies only when `hasPain` and all pain fields are omitted; risk-only payloads (`canRun`/`redFlag`) without `hasPain` are rejected.

### `adaptation_decision`

Purpose:
- Audit of deterministic plan changes.

Columns:
- `id uuid primary key`
- `user_id uuid not null references app_user(id)`
- `training_plan_id uuid not null references training_plan(id)`
- `plan_version_before int not null`
- `plan_version_after int not null`
- `decision_type varchar(50) not null`
- `decision_scope varchar(20) not null`
- `confidence numeric(4,3) not null`
- `reason_codes jsonb not null`
- `before_state_json jsonb not null`
- `after_state_json jsonb not null`
- `created_at timestamptz not null`

Constraints:
- `plan_version_after > plan_version_before`.

### `strava_connection`

Purpose:
- Linked Strava athlete and tokens.

Columns:
- `id uuid primary key`
- `user_id uuid not null unique references app_user(id)`
- `strava_athlete_id bigint not null unique`
- `granted_scopes text not null`
- `access_token_encrypted text not null`
- `refresh_token_encrypted text not null`
- `token_expires_at timestamptz not null`
- `connection_status varchar(20) not null`
- `last_sync_at timestamptz`
- `created_at timestamptz not null`
- `updated_at timestamptz not null`

### `strava_activity`

Purpose:
- Imported Strava activity metadata.

Columns:
- `id uuid primary key`
- `user_id uuid not null references app_user(id)`
- `strava_activity_id bigint not null unique`
- `sport_type varchar(40) not null`
- `status varchar(20) not null`
- `start_time timestamptz not null`
- `elapsed_time_sec int`
- `moving_time_sec int`
- `distance_m numeric(10,2)`
- `external_id varchar(255)`
- `activity_name text`
- `payload_json jsonb not null`
- `last_seen_at timestamptz not null`
- `created_at timestamptz not null`
- `updated_at timestamptz not null`

### `strava_webhook_event`

Purpose:
- Raw idempotent ingest record.

Columns:
- `id uuid primary key`
- `subscription_id bigint not null`
- `object_type varchar(30) not null`
- `object_id bigint not null`
- `aspect_type varchar(30) not null`
- `event_time bigint not null`
- `owner_id bigint`
- `payload_json jsonb not null`
- `received_at timestamptz not null`
- `processed_at timestamptz`

Constraints:
- Unique `(subscription_id, object_type, object_id, aspect_type, event_time)`.

### `coach_chat_message`

Purpose:
- Safe coaching chat history.

Columns:
- `id uuid primary key`
- `user_id uuid not null references app_user(id)`
- `session_id uuid not null`
- `role varchar(20) not null`
- `message_text text not null`
- `message_type varchar(30) not null`
- `reason_codes jsonb`
- `confidence numeric(4,3)`
- `created_at timestamptz not null`

### `coaching_insight`

Purpose:
- Persisted AI or deterministic explanations.

Columns:
- `id uuid primary key`
- `user_id uuid not null references app_user(id)`
- `insight_date date not null`
- `insight_type varchar(50) not null`
- `headline varchar(255) not null`
- `body text not null`
- `confidence numeric(4,3) not null`
- `source_refs jsonb not null`
- `created_by varchar(20) not null`
- `created_at timestamptz not null`

### `outbox_event`

Purpose:
- Reliable async dispatch.

Columns:
- `id uuid primary key`
- `aggregate_type varchar(50) not null`
- `aggregate_id uuid not null`
- `event_type varchar(50) not null`
- `payload_json jsonb not null`
- `status varchar(20) not null`
- `available_at timestamptz not null`
- `created_at timestamptz not null`
- `processed_at timestamptz`

## Relationships

- `app_user` 1:1 `runner_profile`
- `app_user` 1:N `race_goal`
- `race_goal` 1:N `training_plan`
- `training_plan` 1:N `training_week`
- `training_week` 1:N `planned_workout`
- `planned_workout` 0:1 `workout_completion`
- `app_user` 1:N `fatigue_signal`
- `app_user` 1:N `injury_feedback`
- `training_plan` 1:N `adaptation_decision`
- `app_user` 0:1 `strava_connection`
- `app_user` 1:N `strava_activity`
- `strava_activity` 0:1 `workout_completion`

## Indexes

- `idx_runner_profile_user_id`
- `idx_race_goal_user_status`
- `ux_race_goal_one_active_per_user`
- `idx_training_plan_user_status`
- `idx_training_week_plan_week_index`
- `idx_planned_workout_user_date`
- `idx_planned_workout_plan_version`
- `idx_workout_completion_user_completed_at`
- `idx_fatigue_signal_user_date`
- `idx_injury_feedback_user_reported_at`
- `idx_adaptation_decision_plan_created_at`
- `idx_strava_activity_user_start_time`
- `idx_strava_connection_athlete_id`
- `ux_strava_webhook_event_idempotency`
- `idx_outbox_event_status_available_at`

## Constraints And Checks

- One active race goal per user.
- One active Strava connection per user.
- Only half marathon and marathon enum values allowed in MVP.
- Planned workout status restricted to `PLANNED`, `COMPLETED`, `SKIPPED`, `RESCHEDULED`, `REPLACED`.
- Completion source restricted to `MANUAL`, `STRAVA`.
- Pain severity restricted to 0 through 10.
- Fatigue scores restricted to 1 through 5.

## Flyway Migration Order

1. `V001__create_app_user_and_refresh_token.sql`
2. `V002__create_runner_profile_and_race_goal.sql`
3. `V003__create_training_plan_week_workout.sql`
4. `V004__create_workout_completion_and_checkins.sql`
5. `V005__create_adaptation_decision.sql`
6. `V006__create_strava_tables.sql`
7. `V007__create_ai_and_insights_tables.sql`
8. `V008__create_outbox_event.sql`
9. `V009__add_indexes_and_check_constraints.sql`
10. `V010__seed_reference_data.sql`

## Seed And Demo Data Suggestions

- One demo user with half marathon goal 14 weeks away.
- One demo user with marathon goal 18 weeks away.
- Example readiness signals for green, yellow, orange, and red states.
- Example missed workout scenario that triggers a downshift.
- Example Strava connection with synthetic activity metadata for local development.

## Recommended MVP Defaults

- Keep enums in code and DB aligned.
- Keep raw Strava payload retention to the minimum period approved by legal review.
- Do not add warehouse-style analytics tables in MVP.
