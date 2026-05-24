CREATE TABLE adaptation_decision (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    training_plan_id UUID NOT NULL REFERENCES training_plan(id) ON DELETE CASCADE,
    plan_version_before INT NOT NULL,
    plan_version_after INT NOT NULL,
    trigger_type VARCHAR(30) NOT NULL,
    trigger_workout_id UUID REFERENCES planned_workout(id),
    reason VARCHAR(40) NOT NULL,
    affected_from_date DATE NOT NULL,
    affected_to_date DATE NOT NULL,
    decision_summary TEXT NOT NULL,
    changed_workout_ids JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_adaptation_plan_version_before_positive CHECK (plan_version_before > 0),
    CONSTRAINT chk_adaptation_plan_version_after_positive CHECK (plan_version_after > 0),
    CONSTRAINT chk_adaptation_affected_dates CHECK (affected_from_date <= affected_to_date)
);

CREATE INDEX idx_adaptation_decision_user_created_at ON adaptation_decision(user_id, created_at DESC);
CREATE INDEX idx_adaptation_decision_training_plan ON adaptation_decision(training_plan_id, created_at DESC);
