# coms4156_project

## Running the tests

Docker is required. The database tests (`StorageServiceTest`) start a
throwaway PostgreSQL 16 container with Testcontainers and fill it from
`src/test/resources/test_data`. Make sure Docker is running before you run:

```
./mvnw test
```

Without Docker those tests are skipped, not failed, so a run with no Docker
can look green without having tested the database.

## Writing a database test

Import `TestDatabase` (the shared container), mark the class
`@Testcontainers(disabledWithoutDocker = true)`, and call
`TestData.load(dataSource)` in a `@BeforeEach` to rebuild the tables and fill
them from the CSVs. Copy `StorageServiceTest` as a starting point.

## Keeping the test schema in sync

The tests do not run against the real Supabase database. They run against a
copy of its tables in `src/test/resources/schema.sql`, filled from the CSV
exports in `src/test/resources/test_data`. Nothing checks that the copy still
matches the real tables, so a change to the real database that is not copied
there leaves the tests passing while the app breaks.

Whenever you change the real database (a new column, a changed type, a new
enum value, a new table), make the same change to
`src/test/resources/schema.sql` in the same pull request. If the change adds
or renames a column, update the CSVs and `TestData` as well.
