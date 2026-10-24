-- Rebuilds a local database from the test schema and CSVs, so the app
-- and the Postman tests can run without Supabase. Anything already in
-- these tables is dropped. Run from the repository root:
--
--   psql -d cointrail -f scripts/load-test-data.sql
--
-- This does the same as TestData.java does for the JUnit tests.

\set ON_ERROR_STOP on

DROP TABLE IF EXISTS coins, acquisitions, donors, museums CASCADE;
DROP TYPE IF EXISTS acquisition_type;

\i src/test/resources/schema.sql

\copy museums (id, created_at, museum_id, name, catalogue_link) FROM 'src/test/resources/test_data/museums_rows.csv' WITH (FORMAT csv, HEADER true)

-- The export writes name_variants as a JSON array, which COPY cannot
-- read into a text[], so go through a staging table.
CREATE TEMP TABLE donors_stage (
    id uuid, created_at timestamptz, name text,
    name_variants text, additional_info text
);
\copy donors_stage FROM 'src/test/resources/test_data/donors_rows.csv' WITH (FORMAT csv, HEADER true)
INSERT INTO donors (id, created_at, name, name_variants, additional_info)
SELECT id, created_at, name,
       ARRAY(SELECT jsonb_array_elements_text(name_variants::jsonb)),
       additional_info
FROM donors_stage;

\copy acquisitions (id, created_at, museum_id, donor_id, year, type, lot_identifier, acquired_on_text, additional_info) FROM 'src/test/resources/test_data/acquisitions_rows.csv' WITH (FORMAT csv, HEADER true)
\copy coins (id, created_at, museum_id, identifier_code, catalogue_link, category, material, period_start, period_end, period_inferred, credit_line, acquisition_id, accessed_at) FROM 'src/test/resources/test_data/coins_rows.csv' WITH (FORMAT csv, HEADER true)

SELECT (SELECT COUNT(*) FROM museums) AS museums,
       (SELECT COUNT(*) FROM donors) AS donors,
       (SELECT COUNT(*) FROM acquisitions) AS acquisitions,
       (SELECT COUNT(*) FROM coins) AS coins;
