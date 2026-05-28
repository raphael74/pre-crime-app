ALTER TABLE vision
    ADD COLUMN first_name VARCHAR NOT NULL DEFAULT 'Unknown';
ALTER TABLE pre_arrest
    ADD COLUMN first_name VARCHAR NOT NULL DEFAULT 'Unknown';
ALTER TABLE pre_apology
    ADD COLUMN first_name VARCHAR NOT NULL DEFAULT 'Unknown';
