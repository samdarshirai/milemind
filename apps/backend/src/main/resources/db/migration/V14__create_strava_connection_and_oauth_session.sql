CREATE TABLE strava_connection (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    strava_athlete_id BIGINT NOT NULL,
    athlete_username VARCHAR(255),
    athlete_first_name VARCHAR(255),
    athlete_last_name VARCHAR(255),
    encrypted_access_token TEXT NOT NULL,
    encrypted_refresh_token TEXT NOT NULL,
    token_expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    scopes TEXT NOT NULL,
    connected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    disconnected_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uq_strava_connection_active_user
    ON strava_connection(${strava_connection_active_unique_columns})
    ${strava_connection_active_unique_predicate};

CREATE INDEX idx_strava_connection_user ON strava_connection(user_id);
CREATE UNIQUE INDEX uq_strava_connection_athlete ON strava_connection(strava_athlete_id);

CREATE TABLE strava_oauth_session (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    state_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_strava_oauth_session_user ON strava_oauth_session(user_id);
CREATE INDEX idx_strava_oauth_session_expires ON strava_oauth_session(expires_at);
