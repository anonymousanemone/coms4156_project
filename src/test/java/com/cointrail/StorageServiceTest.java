/** This file was written by Claude Code, Sonnet 5.5. */

package com.cointrail;

import com.cointrail.model.Acquisition;
import com.cointrail.model.AcquisitionType;
import com.cointrail.model.Coin;
import com.cointrail.model.Museum;
import com.cointrail.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs {@link StorageService} against a throwaway PostgreSQL in Docker,
 * filled from the CSV exports in src/test/resources/test_data.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestDatabase.class, StorageService.class})
@Testcontainers(disabledWithoutDocker = true)
// No wrapping test transaction: commits and rollbacks must really happen.
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class StorageServiceTest {

    /** A GIFT from Edmund Kerper, Esq., of a Greek silver coin. */
    private static final String GREEK_COIN =
        "0148f5cc-c369-46ad-85c7-f6f32adfba63";

    /** A coin from an EXCAVATION acquisition that has no donor. */
    private static final String EXCAVATED_COIN =
        "0094df25-cebd-491f-aabf-7dab4058654d";

    /** A donor with name variants in the test data. */
    private static final String KERPER =
        "f7863357-6dd5-41a0-ac9e-7c6653ca767f";

    /** A second donor in the test data. */
    private static final String ICKELHEIMER =
        "ee164e0d-d9d9-4e45-8af2-78fa366b0971";

    /** The service under test. */
    @Autowired
    private StorageService storage;

    /** Runs SQL to check what the service wrote. */
    @Autowired
    private JdbcTemplate jdbc;

    /** The test database, for reloading the CSVs. */
    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void reloadTestData() throws Exception {
        TestData.load(dataSource);
    }

    private int count(final String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table,
            Integer.class);
    }

    private Coin newCoin(final String acquisitionId) {
        Coin coin = new Coin(UUID.randomUUID().toString(), "test-1",
            "http://example.com", "credit");
        coin.setMuseumId("MMA");
        coin.setMaterial("Silver");
        coin.setAccessedAt(Instant.parse("2026-10-12T10:15:30Z"));
        if (acquisitionId != null) {
            Acquisition a = new Acquisition();
            a.setId(acquisitionId);
            coin.setAcquisition(a);
        }
        return coin;
    }

    /**
     * Reads every matching coin, a page at a time.
     *
     * @param category the category filter, or null
     * @param material the material filter, or null
     * @param page how many coins to read per page
     * @return every matching coin, in ID order
     */
    private List<Coin> allCoins(
        final String category,
        final String material,
        final int page
    ) {
        List<Coin> all = new ArrayList<>();
        String after = null;
        List<Coin> batch;
        do {
            batch = storage.findCoins(category, material, after, page);
            all.addAll(batch);
            if (!batch.isEmpty()) {
                after = batch.get(batch.size() - 1).getId();
            }
        } while (batch.size() == page);
        return all;
    }

    @Test
    void testDataIsLoaded() {
        assertEquals(1405, count("coins"));
        assertEquals(95, count("acquisitions"));
        assertEquals(65, count("donors"));
        assertEquals(1, count("museums"));
        assertEquals("The Metropolitan Museum of Art",
            storage.findMuseums().get(0).getName());
        assertEquals("MMA", storage.findMuseums().get(0).getId());
    }

    @Test
    void coinComesBackWithItsAcquisitionAndDonor() {
        Coin coin = storage.findCoin(GREEK_COIN);

        assertEquals("MMA", coin.getMuseumId());
        assertEquals("52.127.6", coin.getIdentifierCode());
        assertEquals("Greek", coin.getCategory());
        assertEquals("Silver", coin.getMaterial());
        assertEquals(-450, coin.getPeriodStart());
        assertEquals(-420, coin.getPeriodEnd());
        Acquisition acquisition = coin.getAcquisition();
        assertEquals("c646c394-40c5-4634-9202-de0fa2d228da",
            acquisition.getId());
        assertEquals(1952, acquisition.getYear());
        assertEquals(AcquisitionType.GIFT, acquisition.getType());
        assertEquals("MMA", acquisition.getMuseumId());
        assertEquals(KERPER, acquisition.getDonorId());
        assertEquals("Edmund Kerper, Esq.",
            storage.findDonor(KERPER).getName());
    }

    @Test
    void coinWithAnAcquisitionButNoDonor() {
        Coin coin = storage.findCoin(EXCAVATED_COIN);

        assertEquals("Islamic – Iran & Central Asia", coin.getCategory());
        assertEquals(AcquisitionType.EXCAVATION,
            coin.getAcquisition().getType());
        assertEquals(1939, coin.getAcquisition().getYear());
        assertNull(coin.getAcquisition().getDonorId());
        assertEquals(Instant.parse("2026-10-13T17:00:57Z"),
            coin.getAccessedAt());
    }

    @Test
    void coinWithoutAnAcquisitionHasNone() {
        String id = jdbc.queryForObject("SELECT id::text FROM coins"
            + " WHERE acquisition_id IS NULL LIMIT 1", String.class);

        assertNull(storage.findCoin(id).getAcquisition());
    }

    @Test
    void donorKeepsAllNameVariants() {
        assertEquals(Set.of("H.R. Ickelheimer", "H. R. Ickelheimer"),
            storage.findDonor(ICKELHEIMER).getNameVariants());
    }

    @Test
    void missingIdsAreNullAndMalformedIdsAreRejected() {
        String unused = UUID.randomUUID().toString();

        assertNull(storage.findCoin(unused));
        assertNull(storage.findDonor(unused));
        assertThrows(IllegalArgumentException.class,
            () -> storage.findCoin("not-a-uuid"));
        assertThrows(IllegalArgumentException.class,
            () -> storage.findCoins(null, null, "not-a-uuid", 10));
    }

    @Test
    void pagingVisitsEveryCoinOnceInIdOrder() {
        List<String> expected = jdbc.queryForList(
            "SELECT id::text FROM coins ORDER BY id", String.class);

        List<String> actual = allCoins(null, null, 100).stream()
            .map(Coin::getId).toList();

        assertEquals(expected, actual);
    }

    @Test
    void filtersMatchTheDatabaseCounts() {
        int ptolemaic = jdbc.queryForObject(
            "SELECT COUNT(*) FROM coins WHERE category = 'Ptolemaic'",
            Integer.class);
        int ptolemaicBronze = jdbc.queryForObject(
            "SELECT COUNT(*) FROM coins WHERE category = 'Ptolemaic'"
                + " AND material = 'Bronze & Copper'", Integer.class);
        assertTrue(ptolemaic > 100 && ptolemaicBronze > 0);

        List<Coin> category = allCoins("Ptolemaic", null, 50);
        List<Coin> both = allCoins("Ptolemaic", "Bronze & Copper", 50);

        assertEquals(ptolemaic, category.size());
        assertTrue(category.stream()
            .allMatch(c -> "Ptolemaic".equals(c.getCategory())));
        assertEquals(ptolemaicBronze, both.size());
        assertEquals(0, allCoins("No such category", null, 50).size());
    }

    @Test
    void pageSizeMustBeInRange() {
        assertThrows(IllegalArgumentException.class,
            () -> storage.findCoins(null, null, null, 0));
        assertThrows(IllegalArgumentException.class,
            () -> storage.findCoins(null, null, null,
                StorageService.MAX_PAGE_SIZE + 1));
    }

    @Test
    void upsertingAnExistingCoinUpdatesItInPlace() {
        Coin coin = storage.findCoin(GREEK_COIN);
        coin.setCategory("Changed");

        storage.upsertCoins(List.of(coin));

        assertEquals(1405, count("coins"));
        Coin updated = storage.findCoin(GREEK_COIN);
        assertEquals("Changed", updated.getCategory());
        assertEquals(coin.getAcquisition().getId(),
            updated.getAcquisition().getId());
    }

    @Test
    void bequestAndOtherAcquisitionTypesAreStored() {
        Acquisition bequest = new Acquisition(UUID.randomUUID().toString(),
            "MMA", null, 1900, AcquisitionType.BEQUEST);
        Acquisition other = new Acquisition(UUID.randomUUID().toString(),
            "MMA", KERPER, null, AcquisitionType.OTHER);
        storage.upsertAcquisitions(List.of(bequest, other));
        Coin first = newCoin(bequest.getId());
        Coin second = newCoin(other.getId());
        storage.upsertCoins(List.of(first, second));

        assertEquals(AcquisitionType.BEQUEST,
            storage.findCoin(first.getId()).getAcquisition().getType());
        assertEquals(AcquisitionType.OTHER,
            storage.findCoin(second.getId()).getAcquisition().getType());
    }

    @Test
    void upsertingMuseumsRenamesByCode() {
        storage.upsertMuseums(List.of(
            new Museum("The Met", "MMA"), new Museum("Another", "XYZ")));

        List<Museum> museums = storage.findMuseums();

        assertEquals(2, museums.size());
        assertEquals("The Met", museums.get(0).getName());
        assertEquals("XYZ", museums.get(1).getId());
    }

    @Test
    void failedBatchWritesNothing() {
        Coin good = newCoin(null);
        // The second coin names an acquisition that does not exist.
        Coin bad = newCoin(UUID.randomUUID().toString());

        assertThrows(DataIntegrityViolationException.class,
            () -> storage.upsertCoins(List.of(good, bad)));

        assertNull(storage.findCoin(good.getId()));
        assertEquals(1405, count("coins"));
    }

    @Test
    void writesMoreThanOneBatch() {
        List<Coin> coins = new ArrayList<>();
        for (int i = 0; i < 2500; i++) {
            coins.add(newCoin(null));
        }

        storage.upsertCoins(coins);

        assertEquals(1405 + 2500, count("coins"));
        assertNotNull(storage.findCoin(coins.get(2499).getId()));
    }
}
