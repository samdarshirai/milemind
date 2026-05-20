CREATE TABLE race_goal (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    race_name VARCHAR(255),
    race_distance_type VARCHAR(30) NOT NULL,
    race_date DATE NOT NULL,
    target_time_seconds INT,
    goal_style VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_race_goal_distance CHECK (race_distance_type IN ('HALF_MARATHON', 'MARATHON')),
    CONSTRAINT chk_race_goal_goal_style CHECK (goal_style IN ('FINISH', 'IMPROVE', 'PB')),
    CONSTRAINT chk_race_goal_status CHECK (status IN ('ACTIVE', 'ARCHIVED', 'CANCELLED')),
    CONSTRAINT chk_race_goal_target_time_seconds_positive CHECK (
        target_time_seconds IS NULL OR target_time_seconds > 0
    )
);

CREATE UNIQUE INDEX uq_race_goal_user_active
    ON race_goal(${race_goal_active_unique_columns})
    ${race_goal_active_unique_predicate};

CREATE INDEX idx_race_goal_user_status ON race_goal(user_id, status);
