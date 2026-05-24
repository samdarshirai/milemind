ALTER TABLE planned_workout
    ADD CONSTRAINT fk_planned_workout_adapted_from
    FOREIGN KEY (adapted_from_workout_id) REFERENCES planned_workout(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_planned_workout_adapted_from_workout_id
    ON planned_workout(adapted_from_workout_id);
