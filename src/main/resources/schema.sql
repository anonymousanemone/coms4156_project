-- Entire file by claude code.
-- Cointrail schema. Safe to run on every startup (all IF NOT EXISTS).
-- Ids are text because they come from the sources (e.g. "MET-12.34").

CREATE TABLE IF NOT EXISTS museums (
    id   TEXT PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS donors (
    id              TEXT PRIMARY KEY,
    name            TEXT NOT NULL,
    name_variants   TEXT[] NOT NULL DEFAULT '{}',
    additional_info TEXT
);

-- type holds the AcquisitionType name (e.g. 'GIFT'). It is plain text, not a
-- Postgres enum or CHECK, so the Java enum can change without a migration.
CREATE TABLE IF NOT EXISTS acquisitions (
    id               TEXT PRIMARY KEY,
    museum_id        TEXT NOT NULL REFERENCES museums (id),
    donor_id         TEXT REFERENCES donors (id),
    year             INTEGER,
    type             TEXT NOT NULL,
    lot_identifier   TEXT,
    acquired_on_text TEXT,
    additional_info  TEXT
);

CREATE TABLE IF NOT EXISTS coins (
    id              TEXT PRIMARY KEY,
    identifier_code TEXT,
    catalogue_link  TEXT,
    category        TEXT,
    material        TEXT,
    period_start    INTEGER,
    period_end      INTEGER,
    period_inferred BOOLEAN NOT NULL DEFAULT FALSE,
    credit_line     TEXT,
    acquisition_id  TEXT REFERENCES acquisitions (id),
    accessed_at     TIMESTAMPTZ
);

-- Postgres does not index foreign keys on its own; these make the coin ->
-- acquisition join and "everything from this donor/museum" lookups cheap.
CREATE INDEX IF NOT EXISTS acquisitions_donor_idx
    ON acquisitions (donor_id);
CREATE INDEX IF NOT EXISTS acquisitions_museum_idx
    ON acquisitions (museum_id);
CREATE INDEX IF NOT EXISTS coins_acquisition_idx
    ON coins (acquisition_id);

-- Filter columns come first and id second, so "category = ? AND id > ?
-- ORDER BY id LIMIT n" is answered by walking one index range, no sort.
CREATE INDEX IF NOT EXISTS coins_category_idx
    ON coins (category, id);
CREATE INDEX IF NOT EXISTS coins_material_idx
    ON coins (material, id);
