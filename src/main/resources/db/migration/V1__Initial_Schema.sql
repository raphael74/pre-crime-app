CREATE TABLE precog_division
(
    id                     UUID PRIMARY KEY,
    total_crimes_prevented INT DEFAULT 0
);

CREATE TABLE law_enforcement_unit
(
    id        UUID PRIMARY KEY,
    unit_name TEXT NOT NULL
);

CREATE TABLE pre_arrest
(
    id                  UUID PRIMARY KEY,
    enforcement_unit_id UUID NOT NULL REFERENCES law_enforcement_unit (id),
    vision_id           UUID NOT NULL,
    perpetrator         TEXT NOT NULL,
    status              TEXT NOT NULL
);

CREATE TABLE outbox
(
    id           UUID PRIMARY KEY,
    event_type   TEXT NOT NULL,
    event_key    TEXT NOT NULL,
    topic        TEXT NOT NULL,
    payload      JSON NOT NULL,
    status       TEXT NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE
);

-- Seed singletons for the demo
INSERT INTO precog_division (id, total_crimes_prevented)
VALUES ('00000000-0000-0000-0000-000000000001', 0);
INSERT INTO law_enforcement_unit (id, unit_name)
VALUES ('00000000-0000-0000-0000-000000000002', 'Pre-Crime Team Alpha');
