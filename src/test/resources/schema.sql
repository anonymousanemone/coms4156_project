-- This file was written by Claude Code, Sonnet 5.5. 
-- TEST ONLY. A copy of the live Supabase tables (public schema), created in
-- the throwaway Postgres that the tests start. The application itself never
-- runs a schema file, so starting it cannot change a real database.
-- Row-level security and the unused "imports" table are left out.

CREATE TYPE acquisition_type AS ENUM (
    'GIFT', 'PURCHASE', 'EXCHANGE', 'TRANSFER', 'EXCAVATION', 'LOAN',
    'UNKNOWN', 'BEQUEST', 'OTHER'
);

CREATE TABLE museums (
    id             UUID        NOT NULL DEFAULT gen_random_uuid(),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    museum_id      VARCHAR     NOT NULL UNIQUE,
    name           TEXT        NOT NULL UNIQUE,
    catalogue_link TEXT,
    PRIMARY KEY (id, museum_id)
);

CREATE TABLE donors (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    name            TEXT        NOT NULL,
    name_variants   TEXT[]      NOT NULL DEFAULT '{}',
    additional_info TEXT
);

CREATE TABLE acquisitions (
    id               UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at       TIMESTAMPTZ      NOT NULL DEFAULT now(),
    museum_id        VARCHAR          NOT NULL REFERENCES museums (museum_id),
    donor_id         UUID             REFERENCES donors (id),
    year             INTEGER,
    type             acquisition_type NOT NULL,
    lot_identifier   TEXT,
    acquired_on_text TEXT,
    additional_info  TEXT
);

CREATE TABLE coins (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    museum_id       VARCHAR     NOT NULL REFERENCES museums (museum_id),
    identifier_code TEXT        NOT NULL,
    catalogue_link  TEXT,
    category        TEXT,
    material        TEXT        NOT NULL,
    period_start    INTEGER,
    period_end      INTEGER,
    period_inferred BOOLEAN     NOT NULL,
    credit_line     TEXT,
    acquisition_id  UUID        REFERENCES acquisitions (id),
    accessed_at     TIMESTAMPTZ NOT NULL
);
