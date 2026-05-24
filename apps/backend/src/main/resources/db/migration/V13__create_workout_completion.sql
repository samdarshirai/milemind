CREATE TABLE workout_completion (
    id UUID PRIMARY KEY,
    planned_workout_id UUID NOT NULL REFERENCES planned_workout(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_user(id),
    completion_source VARCHAR(20) NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    actual_distance_km NUMERIC(6,2),
    actual_duration_min INT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_workout_completion_planned_workout UNIQUE (planned_workout_id),
    CONSTRAINT chk_workout_completion_source CHECK (completion_source IN ('MANUAL')),
    CONSTRAINT chk_workout_completion_distance_non_negative CHECK (
        actual_distance_km IS NULL OR actual_distance_km >= 0
    ),
    CONSTRAINT chk_workout_completion_duration_non_negative CHECK (
        actual_duration_min IS NULL OR actual_duration_min >= 0
    )
);

CREATE INDEX idx_workout_completion_user_completed_at ON workout_completion(user_id, completed_at DESC);
