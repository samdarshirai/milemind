-- Baseline migration placeholder for MVP skeleton.
-- Business tables will be added in slice-based migrations.
CREATE TABLE IF NOT EXISTS flyway_bootstrap_marker (
    id INT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

MERGE INTO flyway_bootstrap_marker (id) KEY (id) VALUES (1);
