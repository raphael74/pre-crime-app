CREATE TABLE precog_division
(
    id                     UUID PRIMARY KEY,
    version                BIGINT NOT NULL DEFAULT 0,
    total_crimes_prevented INT             DEFAULT 0
);

CREATE TABLE vision
(
    id                 UUID PRIMARY KEY,
    precog_division_id UUID      NOT NULL REFERENCES precog_division (id),
    perpetrator        TEXT      NOT NULL,
    crime_type         TEXT      NOT NULL,
    foreseen_at        TIMESTAMP NOT NULL
);

CREATE TABLE law_enforcement_unit
(
    id        UUID PRIMARY KEY,
    version   BIGINT NOT NULL DEFAULT 0,
    unit_name TEXT   NOT NULL
);

CREATE TABLE pre_arrest
(
    id                  UUID PRIMARY KEY,
    enforcement_unit_id UUID NOT NULL REFERENCES law_enforcement_unit (id),
    vision_id           UUID NOT NULL,
    perpetrator         TEXT NOT NULL,
    status              TEXT NOT NULL
);

CREATE TABLE audit_log
(
    id          UUID PRIMARY KEY,
    event_type  TEXT                     NOT NULL,
    payload     JSON                     NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE outbox
(
    id           UUID PRIMARY KEY,
    event_class  TEXT                     NOT NULL,
    event        JSON                     NOT NULL,
    status       TEXT                     NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE inbox
(
    id             UUID                     NOT NULL,
    consumer_group TEXT                     NOT NULL,
    processed_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id, consumer_group)
);

-- Seed singletons for the demo
INSERT INTO precog_division (id, total_crimes_prevented)
VALUES ('00000000-0000-0000-0000-000000000001', 0);
INSERT INTO law_enforcement_unit (id, unit_name)
VALUES ('00000000-0000-0000-0000-000000000002', 'Pre-Crime Team Alpha');
