CREATE TABLE pre_apology
(
    id                     UUID PRIMARY KEY,
    vision_id              UUID           NOT NULL,
    perpetrator            VARCHAR        NOT NULL,
    base_amount            DECIMAL(10, 2) NOT NULL,
    jetpack_fuel_deduction DECIMAL(10, 2) NOT NULL,
    halo_rental_fee        DECIMAL(10, 2) NOT NULL,
    net_payout             DECIMAL(10, 2) NOT NULL,
    apology_text           TEXT           NOT NULL
);
