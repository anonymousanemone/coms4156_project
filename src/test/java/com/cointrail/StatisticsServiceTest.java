package com.cointrail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cointrail.service.NotFoundException;
import com.cointrail.service.QueryFilter;
import com.cointrail.service.StatisticsService;
import com.cointrail.service.StatisticsService.Bucket;
import com.cointrail.service.StatisticsService.Statistics;
import com.cointrail.service.StatisticsService.Summary;
import com.cointrail.service.StorageService;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Runs {@link StatisticsService} against a throwaway PostgreSQL in Docker,
 * filled from the CSV exports in src/test/resources/test_data. The
 * expected numbers were counted straight from those CSVs.
 *
 * <p>The test data has 1,405 MMA coins: 678 EXCAVATION, 496 GIFT,
 * 210 PURCHASE and 21 of unknown type (2 UNKNOWN plus 19 coins with no
 * acquisition). 20 coins have no acquisition year (1 plus those 19).</p>
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestDatabase.class, StorageService.class, StatisticsService.class})
@Testcontainers(disabledWithoutDocker = true)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class StatisticsServiceTest {

    /** Every coin in the test data. */
    private static final long ALL_COINS = 1405;

    /** Edmund Kerper, Esq., who gave 4 coins in one 1952 gift. */
    private static final String KERPER =
        "f7863357-6dd5-41a0-ac9e-7c6653ca767f";

    /** The largest acquisition: a 1939 excavation of 525 coins. */
    private static final String LARGEST_ACQUISITION =
        "589d2038-b9ed-4bbc-af31-df6ac20544c5";

    /** The service under test. */
    @Autowired
    private StatisticsService statistics;

    /** The test database, for reloading the CSVs. */
    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void reloadTestData() throws Exception {
        TestData.load(dataSource);
    }

    /**
     * Builds a filter from the fields the tests use most.
     *
     * @param types acquisition types, or null
     * @param yearFrom the earliest year, or null
     * @param yearTo the latest year, or null
     * @return the filter
     */
    private static QueryFilter filter(
        final List<String> types,
        final Integer yearFrom,
        final Integer yearTo
    ) {
        return new QueryFilter(null, types, null, yearFrom, yearTo, null);
    }

    /**
     * Finds the bucket with the given group values.
     *
     * @param result the statistics
     * @param groups the group values to look for
     * @return the bucket's value
     */
    private static long valueOf(
        final Statistics result,
        final Map<String, Object> groups
    ) {
        return result.buckets().stream()
            .filter(b -> b.groups().equals(groups))
            .findFirst()
            .orElseThrow()
            .value();
    }

    @Nested
    class Compute {

        @Test
        void noGroupByCountsEveryCoin() {
            Statistics result =
                statistics.compute("objects", null, QueryFilter.NONE);
            assertEquals("objects", result.metric());
            assertTrue(result.buckets().isEmpty());
            assertEquals(ALL_COINS, result.total());
            assertEquals(0, result.excludedMissingYear());
            assertEquals(0, result.excludedUnknownType());
        }

        @Test
        void groupByTypeLeavesOutUnknownType() {
            Statistics result = statistics.compute(
                "objects", List.of("type"), QueryFilter.NONE);
            assertEquals(1384, result.total());
            assertEquals(21, result.excludedUnknownType());
            assertEquals(0, result.excludedMissingYear());
            assertEquals(3, result.buckets().size());
            assertEquals(496, valueOf(result, Map.of("type", "GIFT")));
            assertEquals(678, valueOf(result, Map.of("type", "EXCAVATION")));
        }

        @Test
        void groupByDecadeAndTypeLeavesOutBoth() {
            Statistics result = statistics.compute(
                "objects", List.of("decade", "type"), QueryFilter.NONE);
            assertEquals(List.of("decade", "type"), result.groupBy());
            assertEquals(25, result.buckets().size());
            assertEquals(1383, result.total());
            assertEquals(20, result.excludedMissingYear());
            assertEquals(21, result.excludedUnknownType());
            assertEquals(113,
                valueOf(result, Map.of("decade", 1890, "type", "GIFT")));
            assertEquals(39,
                valueOf(result, Map.of("decade", 1900, "type", "PURCHASE")));
        }

        @Test
        void bucketsAreOrderedByGroupValue() {
            Statistics result = statistics.compute(
                "objects", List.of("decade", "type"), QueryFilter.NONE);
            Bucket first = result.buckets().get(0);
            assertEquals(Map.of("decade", 1870, "type", "PURCHASE"),
                first.groups());
            assertEquals(4, first.value());
        }

        @Test
        void bucketValuesAddUpToTotal() {
            Statistics result = statistics.compute(
                "objects", List.of("category"), QueryFilter.NONE);
            long sum = result.buckets().stream()
                .mapToLong(Bucket::value).sum();
            assertEquals(result.total(), sum);
        }

        @Test
        void acquisitionMetricCountsDistinctAcquisitions() {
            Statistics result = statistics.compute(
                "acquisitions", List.of("type"), QueryFilter.NONE);
            assertEquals(80, valueOf(result, Map.of("type", "GIFT")));
            assertEquals(11, valueOf(result, Map.of("type", "PURCHASE")));
            assertEquals(2, result.excludedUnknownType());
        }

        @Test
        void donorMetricCountsDistinctDonors() {
            Statistics result =
                statistics.compute("donors", null, QueryFilter.NONE);
            assertEquals(65, result.total());
        }

        @Test
        void typeAndYearFiltersNarrowTheCount() {
            Statistics result = statistics.compute(
                "objects", null, filter(List.of("gift"), 1900, 1919));
            assertEquals(152, result.total());
        }

        @Test
        void minObjectsKeepsOnlyLargeAcquisitions() {
            Statistics result = statistics.compute("objects", null,
                new QueryFilter(null, null, null, null, null, 10));
            assertEquals(1200, result.total());
        }

        @Test
        void periodGroupsByCentury() {
            Statistics result = statistics.compute(
                "objects", List.of("period"), QueryFilter.NONE);
            // 332 BC rounds down to the century starting 400 BC.
            assertEquals(185, valueOf(result, Map.of("period", -400)));
        }

        @Test
        void donorGroupKeepsCoinsWithNoDonorAsNull() {
            Statistics result = statistics.compute(
                "objects", List.of("donor"), QueryFilter.NONE);
            Bucket last = result.buckets().get(result.buckets().size() - 1);
            assertNull(last.groups().get("donor"));
            assertEquals(909, last.value());
        }

        @Test
        void filterMatchingNothingGivesZeroes() {
            Statistics result = statistics.compute(
                "objects", List.of("type"), filter(null, 3000, null));
            assertTrue(result.buckets().isEmpty());
            assertEquals(0, result.total());
        }
    }

    @Nested
    class MuseumSummary {

        @Test
        void summarizesTheMuseum() {
            Summary summary = statistics.summarizeMuseum("MMA");
            assertEquals("The Metropolitan Museum of Art", summary.name());
            assertEquals(ALL_COINS, summary.totalObjects());
            assertEquals(95, summary.totalAcquisitions());
            assertEquals(65, summary.distinctDonors());
            assertEquals(1, summary.distinctMuseums());
            assertEquals(1874, summary.firstYear());
            assertEquals(2026, summary.lastYear());
            assertEquals(
                Map.of("EXCAVATION", 678L, "GIFT", 496L,
                    "PURCHASE", 210L, "UNKNOWN", 21L),
                summary.objectsByType());
            assertEquals(Map.of("MMA", ALL_COINS), summary.objectsByMuseum());
        }

        @Test
        void listsLargestAcquisitionsFirst() {
            Summary summary = statistics.summarizeMuseum("MMA");
            assertEquals(StatisticsService.LARGEST_ACQUISITIONS,
                summary.largestAcquisitions().size());
            assertEquals(LARGEST_ACQUISITION,
                summary.largestAcquisitions().get(0).id());
            assertEquals(525, summary.largestAcquisitions().get(0).objects());
            assertEquals(153, summary.largestAcquisitions().get(1).objects());
        }

        @Test
        void unknownMuseumIsNotFound() {
            assertThrows(NotFoundException.class,
                () -> statistics.summarizeMuseum("LOUVRE"));
        }
    }

    @Nested
    class DonorSummary {

        @Test
        void summarizesTheDonor() {
            Summary summary = statistics.summarizeDonor(KERPER);
            assertEquals("Edmund Kerper, Esq.", summary.name());
            assertEquals(4, summary.totalObjects());
            assertEquals(1, summary.totalAcquisitions());
            assertEquals(1, summary.distinctMuseums());
            assertEquals(1952, summary.firstYear());
            assertEquals(1952, summary.lastYear());
            assertEquals(Map.of("GIFT", 4L), summary.objectsByType());
            assertEquals("Edmund Kerper, Esq.",
                summary.largestAcquisitions().get(0).donorName());
        }

        @Test
        void unknownDonorIsNotFound() {
            assertThrows(NotFoundException.class,
                () -> statistics.summarizeDonor(
                    "00000000-0000-0000-0000-000000000000"));
        }

        @Test
        void malformedDonorIdIsRejected() {
            assertThrows(IllegalArgumentException.class,
                () -> statistics.summarizeDonor("not-an-id"));
        }
    }
}
