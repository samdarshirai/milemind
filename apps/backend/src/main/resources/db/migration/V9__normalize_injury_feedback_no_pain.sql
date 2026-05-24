ALTER TABLE injury_feedback
    ADD COLUMN has_pain BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE injury_feedback
SET has_pain = FALSE,
    body_region = NULL,
    pain_type = NULL,
    severity = NULL,
    onset_context = NULL,
    can_run = TRUE,
    red_flag = FALSE
WHERE body_region = 'NONE'
  AND pain_type = 'NONE'
  AND severity = 0
  AND onset_context = 'NONE';

ALTER TABLE injury_feedback
    ALTER COLUMN body_region DROP NOT NULL;

ALTER TABLE injury_feedback
    ALTER COLUMN pain_type DROP NOT NULL;

ALTER TABLE injury_feedback
    ALTER COLUMN severity DROP NOT NULL;

ALTER TABLE injury_feedback
    ALTER COLUMN onset_context DROP NOT NULL;
