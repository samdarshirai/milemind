ALTER TABLE adaptation_decision ADD COLUMN IF NOT EXISTS decision_type VARCHAR(50);
ALTER TABLE adaptation_decision ADD COLUMN IF NOT EXISTS decision_scope VARCHAR(20);
ALTER TABLE adaptation_decision ADD COLUMN IF NOT EXISTS confidence NUMERIC(4,3);
ALTER TABLE adaptation_decision ADD COLUMN IF NOT EXISTS reason_codes JSONB;
ALTER TABLE adaptation_decision ADD COLUMN IF NOT EXISTS before_state_json JSONB;
ALTER TABLE adaptation_decision ADD COLUMN IF NOT EXISTS after_state_json JSONB;

UPDATE adaptation_decision
SET
    decision_type = COALESCE(decision_type, 'NEAR_TERM_REGENERATION'),
    decision_scope = COALESCE(decision_scope, 'WEEK'),
    confidence = COALESCE(confidence, 0.850),
    reason_codes = COALESCE(reason_codes, ${adaptation_reason_codes_backfill_expr}),
    before_state_json = COALESCE(before_state_json, '{"workouts":[]}'),
    after_state_json = COALESCE(after_state_json, '{"workouts":[]}');

ALTER TABLE adaptation_decision ALTER COLUMN decision_type SET NOT NULL;
ALTER TABLE adaptation_decision ALTER COLUMN decision_scope SET NOT NULL;
ALTER TABLE adaptation_decision ALTER COLUMN confidence SET NOT NULL;
ALTER TABLE adaptation_decision ALTER COLUMN reason_codes SET NOT NULL;
ALTER TABLE adaptation_decision ALTER COLUMN before_state_json SET NOT NULL;
ALTER TABLE adaptation_decision ALTER COLUMN after_state_json SET NOT NULL;

ALTER TABLE adaptation_decision DROP CONSTRAINT IF EXISTS chk_adaptation_plan_version_transition;
ALTER TABLE adaptation_decision
    ADD CONSTRAINT chk_adaptation_plan_version_transition CHECK (plan_version_after > plan_version_before);
