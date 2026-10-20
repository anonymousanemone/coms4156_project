/** This file was written by Claude Code, Sonnet 5.5.  */
package com.cointrail;

import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Rebuilds the test database from src/test/resources/schema.sql, then
 * fills it from the CSV exports in src/test/resources/test_data using
 * Postgres COPY. Anything already in those tables is dropped.
 */
final class TestData {

    private TestData() {
    }

    static void load(final DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(
                "DROP TABLE IF EXISTS coins, acquisitions, donors, museums"
                    + " CASCADE");
            statement.execute("DROP TYPE IF EXISTS acquisition_type");
            ScriptUtils.executeSqlScript(
                connection, new ClassPathResource("schema.sql"));

            CopyManager copy =
                connection.unwrap(PGConnection.class).getCopyAPI();

            copy(copy, "museums",
                "id, created_at, museum_id, name, catalogue_link");

            // The export writes name_variants as a JSON array, which COPY
            // cannot read into a text[], so go through a staging table.
            statement.execute("CREATE TEMP TABLE donors_stage ("
                + "id uuid, created_at timestamptz, name text,"
                + " name_variants text, additional_info text)");
            copy(copy, "donors_stage",
                "id, created_at, name, name_variants, additional_info");
            statement.execute("INSERT INTO donors"
                + " (id, created_at, name, name_variants, additional_info)"
                + " SELECT id, created_at, name,"
                + " ARRAY(SELECT jsonb_array_elements_text("
                + "name_variants::jsonb)), additional_info"
                + " FROM donors_stage");
            statement.execute("DROP TABLE donors_stage");

            copy(copy, "acquisitions",
                "id, created_at, museum_id, donor_id, year, type,"
                    + " lot_identifier, acquired_on_text, additional_info");
            copy(copy, "coins",
                "id, created_at, museum_id, identifier_code,"
                    + " catalogue_link, category, material, period_start,"
                    + " period_end, period_inferred, credit_line,"
                    + " acquisition_id, accessed_at");
        }
    }

    private static void copy(
        final CopyManager copy,
        final String table,
        final String columns
    ) throws Exception {
        String file = "/test_data/"
            + table.replace("_stage", "") + "_rows.csv";
        try (InputStream in = TestData.class.getResourceAsStream(file);
             Reader reader =
                 new InputStreamReader(in, StandardCharsets.UTF_8)) {
            copy.copyIn("COPY " + table + " (" + columns + ")"
                + " FROM STDIN WITH (FORMAT csv, HEADER true)", reader);
        }
    }
}
