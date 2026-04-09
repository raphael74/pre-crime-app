CREATE TABLE audit_log
(
    id          UUID PRIMARY KEY,
    event_type  TEXT NOT NULL,
    payload     JSON NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
