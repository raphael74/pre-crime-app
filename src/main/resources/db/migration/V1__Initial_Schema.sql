CREATE TABLE statistic
(
    id                     UUID PRIMARY KEY,
    version                BIGINT NOT NULL DEFAULT 0,
    total_crimes_prevented INT             DEFAULT 0
);

CREATE TABLE perpetrator
(
    id         UUID PRIMARY KEY,
    version    BIGINT  NOT NULL DEFAULT 0,
    first_name VARCHAR NOT NULL,
    last_name  VARCHAR NOT NULL
);

CREATE TABLE vision
(
    id             UUID PRIMARY KEY,
    version        BIGINT    NOT NULL DEFAULT 0,
    perpetrator_id UUID      NOT NULL REFERENCES perpetrator (id),
    crime_type     VARCHAR   NOT NULL,
    foreseen_at    TIMESTAMP NOT NULL
);

CREATE TABLE pre_arrest
(
    id             UUID PRIMARY KEY,
    version        BIGINT  NOT NULL DEFAULT 0,
    vision_id      UUID    NOT NULL,
    perpetrator_id UUID    NOT NULL REFERENCES perpetrator (id),
    status         VARCHAR NOT NULL
);

CREATE TABLE audit_log
(
    id          UUID PRIMARY KEY,
    event_type  VARCHAR                  NOT NULL,
    payload     JSON                     NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE outbox
(
    id           UUID PRIMARY KEY,
    event_class  VARCHAR                  NOT NULL,
    event        JSON                     NOT NULL,
    status       VARCHAR                  NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE inbox
(
    id             UUID                     NOT NULL,
    consumer_group VARCHAR                  NOT NULL,
    processed_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id, consumer_group)
);

CREATE TABLE pre_apology
(
    id                     UUID PRIMARY KEY,
    vision_id              UUID                     NOT NULL,
    perpetrator_id         UUID                     NOT NULL REFERENCES perpetrator (id),
    base_amount            DECIMAL(10, 2)           NOT NULL,
    jetpack_fuel_deduction DECIMAL(10, 2)           NOT NULL,
    halo_rental_fee        DECIMAL(10, 2)           NOT NULL,
    net_payout             DECIMAL(10, 2)           NOT NULL,
    apology_text           VARCHAR                  NOT NULL,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Seed singletons for the demo
INSERT INTO statistic (id, total_crimes_prevented)
VALUES ('00000000-0000-0000-0000-000000000001', 0);
