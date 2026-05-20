CREATE TABLE training_plan (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    runner_profile_id UUID NOT NULL REFERENCES runner_profile(id),
    race_goal_id UUID NOT NULL REFERENCES race_goal(id),
    plan_status VARCHAR(20) NOT NULL,
    plan_version INT NOT NULL,
    methodology_code VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    current_week_index INT NOT NULL,
    last_regenerated_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_tp_status CHECK (plan_status IN ('GENERATED', 'ACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_tp_version_positive CHECK (plan_version > 0),
    CONSTRAINT chk_tp_current_week_index_positive CHECK (current_week_index > 0),
    CONSTRAINT chk_tp_dates CHECK (start_date <= end_date)
);

CREATE INDEX idx_training_plan_user_status ON training_plan(user_id, plan_status);
CREATE INDEX idx_training_plan_race_goal_status ON training_plan(race_goal_id, plan_status);
CREATE UNIQUE INDEX uq_training_plan_user_goal_version ON training_plan(user_id, race_goal_id, plan_version);

CREATE TABLE training_plan_week (
    id UUID PRIMARY KEY,
    training_plan_id UUID NOT NULL REFERENCES training_plan(id) ON DELETE CASCADE,
    week_index INT NOT NULL,
    phase VARCHAR(30) NOT NULL,
    target_distance_km NUMERIC(6,2),
    target_time_min INT,
    recovery_week BOOLEAN NOT NULL DEFAULT FALSE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT chk_tpw_week_index_positive CHECK (week_index > 0),
    CONSTRAINT chk_tpw_dates CHECK (start_date <= end_date),
    CONSTRAINT chk_tpw_distance_non_negative CHECK (target_distance_km IS NULL OR target_distance_km >= 0)
);

CREATE UNIQUE INDEX uq_training_plan_week_plan_number ON training_plan_week(training_plan_id, week_index);
CREATE INDEX idx_training_plan_week_plan ON training_plan_week(training_plan_id);

CREATE TABLE planned_workout (
    id UUID PRIMARY KEY,
    training_plan_id UUID NOT NULL REFERENCES training_plan(id) ON DELETE CASCADE,
    training_week_id UUID NOT NULL REFERENCES training_plan_week(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_user(id),
    scheduled_date DATE NOT NULL,
    workout_type VARCHAR(30) NOT NULL,
    workout_subtype VARCHAR(50),
    planned_distance_km NUMERIC(6,2),
    planned_duration_min INT,
    intensity_zone VARCHAR(20),
    structure_json JSONB NOT NULL,
    rationale_json JSONB NOT NULL,
    adapted_from_workout_id UUID,
    plan_version INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT chk_pw_type CHECK (workout_type IN ('EASY_RUN', 'LONG_RUN', 'INTERVALS', 'TEMPO_RUN', 'REST', 'RECOVERY_RUN')),
    CONSTRAINT chk_pw_status CHECK (status IN ('PLANNED', 'COMPLETED', 'MISSED', 'SKIPPED')),
    CONSTRAINT chk_pw_requires_distance_or_duration CHECK (planned_distance_km IS NOT NULL OR planned_duration_min IS NOT NULL),
    CONSTRAINT chk_pw_plan_version_positive CHECK (plan_version > 0),
    CONSTRAINT chk_pw_distance_non_negative CHECK (planned_distance_km IS NULL OR planned_distance_km >= 0),
    CONSTRAINT chk_pw_duration_non_negative CHECK (planned_duration_min IS NULL OR planned_duration_min >= 0)
);

CREATE INDEX idx_planned_workout_plan_date ON planned_workout(training_plan_id, scheduled_date);
CREATE INDEX idx_planned_workout_week ON planned_workout(training_week_id);
CREATE INDEX idx_planned_workout_user ON planned_workout(user_id);
