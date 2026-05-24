CREATE TABLE fatigue_signal (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    signal_date DATE NOT NULL,
    sleep_score INT NOT NULL,
    stress_score INT NOT NULL,
    soreness_score INT NOT NULL,
    motivation_score INT NOT NULL,
    illness_flag BOOLEAN NOT NULL DEFAULT FALSE,
    too_busy_flag BOOLEAN NOT NULL DEFAULT FALSE,
    travelling_flag BOOLEAN NOT NULL DEFAULT FALSE,
    source VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_fatigue_sleep_score_range CHECK (sleep_score BETWEEN 1 AND 5),
    CONSTRAINT chk_fatigue_stress_score_range CHECK (stress_score BETWEEN 1 AND 5),
    CONSTRAINT chk_fatigue_soreness_score_range CHECK (soreness_score BETWEEN 1 AND 5),
    CONSTRAINT chk_fatigue_motivation_score_range CHECK (motivation_score BETWEEN 1 AND 5)
);

CREATE UNIQUE INDEX uq_fatigue_signal_user_date_source ON fatigue_signal(user_id, signal_date, source);
CREATE INDEX idx_fatigue_signal_user_date ON fatigue_signal(user_id, signal_date DESC);

CREATE TABLE injury_feedback (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    reported_at TIMESTAMP WITH TIME ZONE NOT NULL,
    body_region VARCHAR(50) NOT NULL,
    pain_type VARCHAR(20) NOT NULL,
    severity INT NOT NULL,
    onset_context VARCHAR(20) NOT NULL,
    can_run BOOLEAN,
    red_flag BOOLEAN NOT NULL DEFAULT FALSE,
    free_text TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_injury_severity_range CHECK (severity BETWEEN 0 AND 10)
);

CREATE INDEX idx_injury_feedback_user_reported_at ON injury_feedback(user_id, reported_at DESC);
