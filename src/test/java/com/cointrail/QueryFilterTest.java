package com.cointrail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cointrail.service.InvalidFilterException;
import com.cointrail.service.QueryFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests the shared acquisition filter: checking values, then turning
 * them into SQL conditions.
 */
class QueryFilterTest {

    /** The museums that exist in these tests. */
    private static final List<String> MUSEUMS = List.of("MMA", "ANS");

    /** A valid donor ID. */
    private static final String DONOR = UUID.randomUUID().toString();

    @Nested
    class Validate {

        @Test
        void emptyFilterIsValid() {
            assertEquals(QueryFilter.NONE, QueryFilter.NONE.validate(MUSEUMS));
        }

        @Test
        void typesAreUpperCased() {
            QueryFilter filter = new QueryFilter(
                null, List.of("gift", "Purchase"), null, null, null, null
            ).validate(MUSEUMS);
            assertEquals(List.of("GIFT", "PURCHASE"), filter.types());
        }

        @Test
        void unknownMuseumListsKnownMuseums() {
            InvalidFilterException ex = assertThrows(
                InvalidFilterException.class,
                () -> new QueryFilter(
                    List.of("LOUVRE"), null, null, null, null, null
                ).validate(MUSEUMS)
            );
            assertEquals("Unknown museum: LOUVRE", ex.getMessage());
            assertEquals(List.of("ANS", "MMA"), ex.getAcceptedValues());
        }

        @Test
        void unknownTypeListsAllTypes() {
            InvalidFilterException ex = assertThrows(
                InvalidFilterException.class,
                () -> new QueryFilter(
                    null, List.of("stolen"), null, null, null, null
                ).validate(MUSEUMS)
            );
            assertEquals("Unknown acquisition type: stolen", ex.getMessage());
            assertTrue(ex.getAcceptedValues().contains("BEQUEST"));
            assertEquals(9, ex.getAcceptedValues().size());
        }

        @Test
        void donorIdMustBeUuid() {
            assertThrows(IllegalArgumentException.class,
                () -> new QueryFilter(
                    null, null, "not-an-id", null, null, null
                ).validate(MUSEUMS));
        }

        @Test
        void backwardsYearRangeIsRejected() {
            assertThrows(IllegalArgumentException.class,
                () -> new QueryFilter(
                    null, null, null, 1950, 1900, null
                ).validate(MUSEUMS));
        }

        @Test
        void sameYearRangeIsAllowed() {
            new QueryFilter(null, null, null, 1900, 1900, null)
                .validate(MUSEUMS);
        }

        @Test
        void negativeMinObjectsIsRejected() {
            assertThrows(IllegalArgumentException.class,
                () -> new QueryFilter(
                    null, null, null, null, null, -1
                ).validate(MUSEUMS));
        }
    }

    @Nested
    class AppendConditions {

        @Test
        void emptyFilterAddsNothing() {
            StringBuilder sql = new StringBuilder("WHERE TRUE");
            List<Object> args = new ArrayList<>();
            QueryFilter.NONE.appendConditions(sql, args);
            assertEquals("WHERE TRUE", sql.toString());
            assertTrue(args.isEmpty());
        }

        @Test
        void emptyListsAddNothing() {
            StringBuilder sql = new StringBuilder("WHERE TRUE");
            List<Object> args = new ArrayList<>();
            new QueryFilter(List.of(), List.of(), null, null, null, null)
                .appendConditions(sql, args);
            assertEquals("WHERE TRUE", sql.toString());
        }

        @Test
        void everyFilterAddsItsCondition() {
            StringBuilder sql = new StringBuilder("WHERE TRUE");
            List<Object> args = new ArrayList<>();
            new QueryFilter(
                MUSEUMS, List.of("GIFT"), DONOR, 1900, 1950, 5
            ).appendConditions(sql, args);
            assertEquals(
                "WHERE TRUE AND a.museum_id IN (?, ?)"
                    + " AND a.type::text IN (?)"
                    + " AND a.donor_id = ?"
                    + " AND a.year >= ? AND a.year <= ?",
                sql.toString()
            );
            assertEquals(
                List.of("MMA", "ANS", "GIFT", UUID.fromString(DONOR),
                    1900, 1950),
                args
            );
        }
    }
}
