package com.cointrail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cointrail.model.Museum;
import com.cointrail.service.InvalidFilterException;
import com.cointrail.service.NotFoundException;
import com.cointrail.service.QueryFilter;
import com.cointrail.service.StatisticsService;
import com.cointrail.service.StatisticsService.Statistics;
import com.cointrail.service.StorageService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Checks that {@link StatisticsService} rejects bad input before it runs
 * any SQL. The database is mocked; the SQL itself is tested in
 * {@link StatisticsServiceTest}.
 */
class StatisticsServiceUnitTest {

    /** A mock database, so tests can check no SQL ran. */
    private JdbcTemplate jdbc;

    /** A mock that knows one museum and no donors. */
    private StorageService storage;

    /** The service under test. */
    private StatisticsService service;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        storage = mock(StorageService.class);
        when(storage.findMuseums()).thenReturn(
            List.of(new Museum("The Metropolitan Museum of Art", "MMA")));
        service = new StatisticsService(jdbc, storage);
    }

    @Nested
    class Compute {

        @Test
        void unknownMetricListsTheMetrics() {
            InvalidFilterException ex = assertThrows(
                InvalidFilterException.class,
                () -> service.compute("coins", null, QueryFilter.NONE));
            assertEquals("Unknown metric: coins", ex.getMessage());
            assertEquals(List.of("objects", "acquisitions", "donors"),
                ex.getAcceptedValues());
            verifyNoInteractions(jdbc);
        }

        @Test
        void metricIgnoresCaseAndTotalsAreRead() {
            when(jdbc.queryForMap(anyString(), any(Object[].class)))
                .thenReturn(Map.of(
                    "total", 7L, "missing_year", 0L, "unknown_type", 0L));
            Statistics result =
                service.compute("OBJECTS", null, QueryFilter.NONE);
            assertEquals("objects", result.metric());
            assertEquals(7, result.total());
            assertTrue(result.buckets().isEmpty());
        }

        @Test
        void unknownDimensionListsTheDimensions() {
            InvalidFilterException ex = assertThrows(
                InvalidFilterException.class,
                () -> service.compute(
                    "objects", List.of("century"), QueryFilter.NONE));
            assertEquals("Unknown groupBy dimension: century",
                ex.getMessage());
            assertTrue(ex.getAcceptedValues().contains("decade"));
            assertEquals(StatisticsService.Dimension.values().length,
                ex.getAcceptedValues().size());
            verifyNoInteractions(jdbc);
        }

        @Test
        void moreThanTwoDimensionsIsRejected() {
            assertThrows(IllegalArgumentException.class,
                () -> service.compute("objects",
                    List.of("museum", "type", "year"), QueryFilter.NONE));
            verifyNoInteractions(jdbc);
        }

        @Test
        void repeatedDimensionIsRejected() {
            assertThrows(IllegalArgumentException.class,
                () -> service.compute("objects",
                    List.of("type", "TYPE"), QueryFilter.NONE));
            verifyNoInteractions(jdbc);
        }

        @Test
        void unknownMuseumFilterIsRejected() {
            InvalidFilterException ex = assertThrows(
                InvalidFilterException.class,
                () -> service.compute("objects", null, new QueryFilter(
                    List.of("LOUVRE"), null, null, null, null, null)));
            assertEquals(List.of("MMA"), ex.getAcceptedValues());
            verifyNoInteractions(jdbc);
        }
    }

    @Nested
    class Summaries {

        @Test
        void unknownMuseumIsNotFound() {
            assertThrows(NotFoundException.class,
                () -> service.summarizeMuseum("LOUVRE"));
            verifyNoInteractions(jdbc);
        }

        @Test
        void unknownDonorIsNotFound() {
            String id = "00000000-0000-0000-0000-000000000000";
            when(storage.findDonor(id)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.summarizeDonor(id));
            assertEquals("No donor with ID " + id, ex.getMessage());
            verifyNoInteractions(jdbc);
        }
    }
}
