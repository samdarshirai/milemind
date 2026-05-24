INSERT INTO app_user (id, email, password_hash, status, locale, timezone, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'migration-check@example.com',
    'hashed',
    'ACTIVE',
    'en-US',
    'Europe/Berlin',
    TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00',
    TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00'
);

INSERT INTO runner_profile (
    id,
    user_id,
    birth_year,
    sex,
    experience_level,
    typical_weekly_distance_km,
    longest_recent_run_km,
    preferred_run_days,
    preferred_long_run_day,
    goal_style,
    injury_history,
    strength_days_per_week,
    units,
    created_at,
    updated_at
)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000001',
    1990,
    'FEMALE',
    'BEGINNER',
    24.0,
    10.0,
    '["TUESDAY","THURSDAY","SATURDAY","SUNDAY"]',
    'SUNDAY',
    'FINISH',
    '{"hadRunningInjuryLast12Months":false}',
    1,
    'KM',
    TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00',
    TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00'
);

INSERT INTO race_goal (
    id,
    user_id,
    race_name,
    race_distance_type,
    race_date,
    target_time_seconds,
    goal_style,
    status,
    created_at,
    updated_at
)
VALUES (
    '00000000-0000-0000-0000-000000000003',
    '00000000-0000-0000-0000-000000000001',
    'Berlin Half',
    'HALF_MARATHON',
    DATE '2026-10-10',
    NULL,
    'FINISH',
    'ACTIVE',
    TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00',
    TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00'
);

INSERT INTO training_plan (
    id,
    user_id,
    runner_profile_id,
    race_goal_id,
    plan_status,
    plan_version,
    methodology_code,
    start_date,
    end_date,
    current_week_index,
    last_regenerated_at,
    created_at,
    updated_at
)
VALUES (
    '00000000-0000-0000-0000-000000000004',
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000003',
    'ACTIVE',
    1,
    'HM_BEGINNER_V1',
    DATE '2026-01-01',
    DATE '2026-04-01',
    1,
    NULL,
    TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00',
    TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00'
);

INSERT INTO training_plan_week (
    id,
    training_plan_id,
    week_index,
    phase,
    target_distance_km,
    target_time_min,
    recovery_week,
    start_date,
    end_date
)
VALUES (
    '00000000-0000-0000-0000-000000000005',
    '00000000-0000-0000-0000-000000000004',
    1,
    'BASE',
    20.0,
    150,
    FALSE,
    DATE '2026-01-01',
    DATE '2026-01-07'
);

INSERT INTO planned_workout (
    id,
    training_plan_id,
    training_week_id,
    user_id,
    scheduled_date,
    workout_type,
    workout_subtype,
    planned_distance_km,
    planned_duration_min,
    intensity_zone,
    structure_json,
    rationale_json,
    adapted_from_workout_id,
    plan_version,
    status
)
VALUES (
    '00000000-0000-0000-0000-000000000006',
    '00000000-0000-0000-0000-000000000004',
    '00000000-0000-0000-0000-000000000005',
    '00000000-0000-0000-0000-000000000001',
    DATE '2026-01-02',
    'EASY_RUN',
    'Easy',
    6.0,
    45,
    'EASY',
    '{"segments":[]}',
    '{"why":"base"}',
    NULL,
    1,
    'PLANNED'
);

INSERT INTO adaptation_decision (
    id,
    user_id,
    training_plan_id,
    plan_version_before,
    plan_version_after,
    trigger_type,
    trigger_workout_id,
    reason,
    affected_from_date,
    affected_to_date,
    decision_summary,
    changed_workout_ids,
    created_at
)
VALUES (
    '00000000-0000-0000-0000-000000000007',
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000004',
    1,
    2,
    'SKIP',
    '00000000-0000-0000-0000-000000000006',
    'NO_TIME',
    DATE '2026-01-02',
    DATE '2026-01-09',
    'Seed adaptation for V11 regression check.',
    '["00000000-0000-0000-0000-000000000006"]',
    TIMESTAMP WITH TIME ZONE '2026-01-02 00:00:00+00'
);
