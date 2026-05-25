ALTER TABLE strava_connection
    RENAME COLUMN encrypted_access_token TO access_token_encrypted;

ALTER TABLE strava_connection
    RENAME COLUMN encrypted_refresh_token TO refresh_token_encrypted;

ALTER TABLE strava_connection
    ADD COLUMN connection_status VARCHAR(20);

ALTER TABLE strava_connection
    ADD COLUMN last_sync_at TIMESTAMP WITH TIME ZONE;

UPDATE strava_connection
SET connection_status = CASE
    WHEN disconnected_at IS NULL THEN 'ACTIVE'
    ELSE 'DISCONNECTED'
END
WHERE connection_status IS NULL;

ALTER TABLE strava_connection
    ALTER COLUMN connection_status SET NOT NULL;
