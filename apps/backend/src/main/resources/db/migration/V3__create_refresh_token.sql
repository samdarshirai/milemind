CREATE TABLE refresh_token (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    rotated_to_token_id UUID REFERENCES refresh_token(id)
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token(user_id);
