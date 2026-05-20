CREATE TABLE runner_profile (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES app_user(id),
    birth_year INT NOT NULL,
    sex VARCHAR(20),
    experience_level VARCHAR(20) NOT NULL,
    typical_weekly_distance_km NUMERIC(6,2) NOT NULL,
    longest_recent_run_km NUMERIC(6,2) NOT NULL,
    preferred_run_days JSONB NOT NULL,
    preferred_long_run_day VARCHAR(12) NOT NULL,
    goal_style VARCHAR(20) NOT NULL,
    injury_history JSONB,
    strength_days_per_week INT NOT NULL DEFAULT 0,
    units VARCHAR(10) NOT NULL DEFAULT 'KM',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_runner_profile_strength_days_per_week CHECK (strength_days_per_week BETWEEN 0 AND 2),
    CONSTRAINT chk_runner_profile_experience_level CHECK (experience_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT chk_runner_profile_goal_style CHECK (goal_style IN ('FINISH', 'IMPROVE', 'PB')),
    CONSTRAINT chk_runner_profile_units CHECK (units IN ('KM', 'MILES')),
    CONSTRAINT chk_runner_profile_distances_positive CHECK (
        typical_weekly_distance_km > 0 AND longest_recent_run_km > 0
    ),
    CONSTRAINT chk_runner_profile_preferred_long_run_day CHECK (
        preferred_long_run_day IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')
    )
);
