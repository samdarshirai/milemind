-- Baseline migration placeholder for MVP skeleton.
-- Business tables will be added in slice-based migrations.
CREATE TABLE IF NOT EXISTS flyway_bootstrap_marker (
    id INT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO flyway_bootstrap_marker (id)
VALUES (1)
    ON CONFLICT (id) DO NOTHING;
