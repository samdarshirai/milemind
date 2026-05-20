ALTER TABLE runner_profile
    ADD CONSTRAINT chk_runner_profile_preferred_run_days_not_empty
    CHECK (
        preferred_run_days <> '[]'
    );
