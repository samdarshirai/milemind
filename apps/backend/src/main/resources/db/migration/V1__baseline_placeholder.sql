-- Baseline migration placeholder for MVP skeleton.
-- Business tables will be added in slice-based migrations.
CREATE TABLE IF NOT EXISTS flyway_bootstrap_marker (
    id INT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO flyway_bootstrap_marker (id)
SELECT 1
WHERE NOT EXISTS (
    SELECT 1 FROM flyway_bootstrap_marker WHERE id = 1
);
