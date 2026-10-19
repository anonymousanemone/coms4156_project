/** This file was written by Claude Code, Sonnet 5.5. */

package com.cointrail;

import com.cointrail.model.Acquisition;
import com.cointrail.model.AcquisitionType;
import com.cointrail.model.Coin;
import com.cointrail.model.Donor;
import com.cointrail.model.Museum;
import com.cointrail.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs {@link StorageService} against a real PostgreSQL in Docker. */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(StorageService.class)
@Testcontainers(disabledWithoutDocker = true)
// No wrapping test transaction: commits and rollbacks must really happen.
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class StorageServiceTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
        new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private StorageService storage;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void emptyTables() {
        jdbc.execute("TRUNCATE coins, acquisitions, donors, museums CASCADE");
    }

    private void seedMuseumDonorAcquisition() {
        storage.upsertMuseums(List.of(new Museum("The Met", "MET")));
        storage.upsertDonors(List.of(
            new Donor("d1", "J. Doe", Set.of("Doe, J.", "John Doe"))));
        Acquisition acquisition = new Acquisition(
            "a1", "MET", "d1", 1920, AcquisitionType.GIFT);
        acquisition.setLotIdentifier("lot 7");
        storage.upsertAcquisitions(List.of(acquisition));
    }

    private Coin coin(String id, String category, String acquisitionId) {
        Coin coin = new Coin(id, "acc-" + id, "http://x/" + id, "credit");
        coin.setCategory(category);
        coin.setMaterial("silver");
        coin.setPeriodStart(-300);
        coin.setPeriodEnd(-200);
        coin.setAccessedAt(Instant.parse("2026-10-12T10:15:30Z"));
        if (acquisitionId != null) {
            Acquisition a = new Acquisition();
            a.setId(acquisitionId);
            coin.setAcquisition(a);
        }
        return coin;
    }

    @Test
    void coinComesBackWithItsAcquisitionAndDonor() {
        seedMuseumDonorAcquisition();
        storage.upsertCoins(List.of(coin("MET-1", "Greek", "a1")));

        Coin found = storage.findCoin("MET-1");

        assertEquals("acc-MET-1", found.getIdentifierCode());
        assertEquals(-300, found.getPeriodStart());
        assertEquals(Instant.parse("2026-10-12T10:15:30Z"),
            found.getAccessedAt());
        assertEquals("MET", found.getAcquisition().getMuseumId());
        assertEquals(1920, found.getAcquisition().getYear());
        assertEquals(AcquisitionType.GIFT, found.getAcquisition().getType());
        assertEquals("lot 7", found.getAcquisition().getLotIdentifier());
        assertEquals(Set.of("Doe, J.", "John Doe"),
            storage.findDonor("d1").getNameVariants());
        assertEquals("The Met", storage.findMuseums().get(0).getName());
    }

    @Test
    void coinWithoutAcquisitionAndMissingIdsAreHandled() {
        storage.upsertCoins(List.of(coin("X-1", "Roman", null)));

        assertNull(storage.findCoin("X-1").getAcquisition());
        assertNull(storage.findCoin("nope"));
        assertNull(storage.findDonor("nope"));
    }

    @Test
    void upsertingTwiceUpdatesInsteadOfDuplicating() {
        storage.upsertCoins(List.of(coin("X-1", "Roman", null)));
        storage.upsertCoins(List.of(coin("X-1", "Greek", null)));

        assertEquals(1, jdbc.queryForObject(
            "SELECT COUNT(*) FROM coins", Integer.class));
        assertEquals("Greek", storage.findCoin("X-1").getCategory());
    }

    @Test
    void coinsArePagedAndFilteredInIdOrder() {
        storage.upsertCoins(List.of(
            coin("C", "Greek", null), coin("A", "Greek", null),
            coin("B", "Roman", null), coin("D", "Greek", null)));

        List<Coin> page1 = storage.findCoins("Greek", null, null, 2);
        List<Coin> page2 = storage.findCoins(
            "Greek", null, page1.get(1).getId(), 2);

        assertEquals(List.of("A", "C"),
            page1.stream().map(Coin::getId).toList());
        assertEquals(List.of("D"),
            page2.stream().map(Coin::getId).toList());
        assertEquals(0, storage.findCoins(null, "gold", null, 10).size());
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
    void failedBatchWritesNothing() {
        // The second coin names an acquisition that does not exist.
        List<Coin> coins = List.of(
            coin("OK-1", "Greek", null), coin("BAD-1", "Greek", "ghost"));

        assertThrows(DataIntegrityViolationException.class,
            () -> storage.upsertCoins(coins));

        assertNull(storage.findCoin("OK-1"));
    }

    @Test
    void writesMoreThanOneBatch() {
        List<Coin> coins = new java.util.ArrayList<>();
        for (int i = 0; i < 2500; i++) {
            coins.add(coin(String.format("C-%05d", i), "Greek", null));
        }

        storage.upsertCoins(coins);

        assertEquals(2500, jdbc.queryForObject(
            "SELECT COUNT(*) FROM coins", Integer.class));
        assertNotNull(storage.findCoin("C-02499"));
        assertTrue(storage.findCoins(null, null, null, 1000).size() == 1000);
    }
}
