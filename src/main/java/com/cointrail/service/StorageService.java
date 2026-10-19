/** This file was written by Claude Code, Sonnet 5.5 */

package com.cointrail.service;

import com.cointrail.model.Acquisition;
import com.cointrail.model.AcquisitionType;
import com.cointrail.model.Coin;
import com.cointrail.model.Donor;
import com.cointrail.model.Museum;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/**
 * Reads and writes the catalog tables in PostgreSQL.
 *
 */
@Service
public final class StorageService {

    /** The most coins {@link #findCoins} returns in one page. */
    public static final int MAX_PAGE_SIZE = 1000;

    /** How many rows go into one JDBC batch. */
    private static final int BATCH_SIZE = 1000;

    /**
     * Selects a coin with its acquisition joined in, so a coin is read
     * in one query. The acquisition columns are null if it has none.
     */
    private static final String COIN_SELECT =
        "SELECT c.id, c.museum_id AS coin_museum_id, c.identifier_code,"
            + " c.catalogue_link, c.category, c.material, c.period_start,"
            + " c.period_end, c.period_inferred, c.credit_line,"
            + " c.accessed_at, c.acquisition_id, a.museum_id, a.donor_id,"
            + " a.year, a.type AS acquisition_type, a.lot_identifier,"
            + " a.acquired_on_text,"
            + " a.additional_info AS acquisition_info"
            + " FROM coins c"
            + " LEFT JOIN acquisitions a ON a.id = c.acquisition_id";

    /** Inserts a museum, or updates its name if the code exists. */
    private static final String UPSERT_MUSEUM =
        "INSERT INTO museums (museum_id, name) VALUES (?, ?)"
            + " ON CONFLICT (museum_id) DO UPDATE SET name = EXCLUDED.name";

    /** Inserts a donor, or updates it if the ID exists. */
    private static final String UPSERT_DONOR =
        "INSERT INTO donors (id, name, name_variants, additional_info)"
            + " VALUES (?, ?, ?, ?)"
            + " ON CONFLICT (id) DO UPDATE SET"
            + " name = EXCLUDED.name,"
            + " name_variants = EXCLUDED.name_variants,"
            + " additional_info = EXCLUDED.additional_info";

    /** Inserts an acquisition, or updates it if the ID exists. */
    private static final String UPSERT_ACQUISITION =
        "INSERT INTO acquisitions (id, museum_id, donor_id, year, type,"
            + " lot_identifier, acquired_on_text, additional_info)"
            + " VALUES (?, ?, ?, ?, ?::acquisition_type, ?, ?, ?)"
            + " ON CONFLICT (id) DO UPDATE SET"
            + " museum_id = EXCLUDED.museum_id,"
            + " donor_id = EXCLUDED.donor_id,"
            + " year = EXCLUDED.year,"
            + " type = EXCLUDED.type,"
            + " lot_identifier = EXCLUDED.lot_identifier,"
            + " acquired_on_text = EXCLUDED.acquired_on_text,"
            + " additional_info = EXCLUDED.additional_info";

    /** Inserts a coin, or updates it if the ID exists. */
    private static final String UPSERT_COIN =
        "INSERT INTO coins (id, museum_id, identifier_code, catalogue_link,"
            + " category, material, period_start, period_end,"
            + " period_inferred, credit_line, acquisition_id, accessed_at)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            + " ON CONFLICT (id) DO UPDATE SET"
            + " museum_id = EXCLUDED.museum_id,"
            + " identifier_code = EXCLUDED.identifier_code,"
            + " catalogue_link = EXCLUDED.catalogue_link,"
            + " category = EXCLUDED.category,"
            + " material = EXCLUDED.material,"
            + " period_start = EXCLUDED.period_start,"
            + " period_end = EXCLUDED.period_end,"
            + " period_inferred = EXCLUDED.period_inferred,"
            + " credit_line = EXCLUDED.credit_line,"
            + " acquisition_id = EXCLUDED.acquisition_id,"
            + " accessed_at = EXCLUDED.accessed_at";

    /** The JDBC template. */
    private final JdbcTemplate jdbc;

    /** Runs each bulk write as a single transaction. */
    private final TransactionTemplate transaction;

    /**
     * Constructs the service.
     *
     * @param jdbc the JDBC template
     * @param transaction the transaction template
     */
    public StorageService(
        final JdbcTemplate jdbc,
        final TransactionTemplate transaction
    ) {
        this.jdbc = jdbc;
        this.transaction = transaction;
    }

    /**
     * Gets a coin, with its acquisition, by ID.
     *
     * @param id the coin ID
     * @return the coin, or null if there is none
     * @throws IllegalArgumentException if the ID is not a UUID
     */
    public Coin findCoin(final String id) {
        List<Coin> coins = jdbc.query(
            COIN_SELECT + " WHERE c.id = ?", this::mapCoin, uuid(id)
        );
        return coins.isEmpty() ? null : coins.get(0);
    }

    /**
     * Gets one page of coins, in ID order, optionally filtered. To get
     * the next page, pass the last ID of this page as {@code afterId}.
     * This stays fast on late pages, unlike an offset.
     *
     * @param category only coins in this category, or null for any
     * @param material only coins of this material, or null for any
     * @param afterId only coins with an ID after this one, or null to
     *      start at the beginning
     * @param limit the page size, from 1 to {@link #MAX_PAGE_SIZE}
     * @return the coins, at most {@code limit} of them
     * @throws IllegalArgumentException if the limit is out of range or
     *      {@code afterId} is not a UUID
     */
    public List<Coin> findCoins(
        final String category,
        final String material,
        final String afterId,
        final int limit
    ) {
        if (limit < 1 || limit > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                "Limit must be from 1 to " + MAX_PAGE_SIZE);
        }

        StringBuilder sql = new StringBuilder(COIN_SELECT)
            .append(" WHERE TRUE");
        List<Object> args = new ArrayList<>();
        if (category != null) {
            sql.append(" AND c.category = ?");
            args.add(category);
        }
        if (material != null) {
            sql.append(" AND c.material = ?");
            args.add(material);
        }
        if (afterId != null) {
            sql.append(" AND c.id > ?");
            args.add(uuid(afterId));
        }
        sql.append(" ORDER BY c.id LIMIT ?");
        args.add(limit);

        return jdbc.query(sql.toString(), this::mapCoin, args.toArray());
    }

    /**
     * Writes coins, replacing any with the same ID. Each coin's museum
     * must already exist, and so must its acquisition if it has one;
     * only the acquisition's ID is stored. All coins are written in one
     * transaction.
     *
     * @param coins the coins
     */
    public void upsertCoins(final List<Coin> coins) {
        batch(UPSERT_COIN, coins, (ps, coin) -> {
            ps.setObject(1, uuid(coin.getId()));
            ps.setString(2, coin.getMuseumId());
            ps.setString(3, coin.getIdentifierCode());
            ps.setString(4, coin.getCatalogueLink());
            ps.setString(5, coin.getCategory());
            ps.setString(6, coin.getMaterial());
            ps.setObject(7, coin.getPeriodStart());
            ps.setObject(8, coin.getPeriodEnd());
            ps.setBoolean(9, coin.isPeriodInferred());
            ps.setString(10, coin.getCreditLine());
            ps.setObject(11, coin.getAcquisition() == null
                ? null
                : uuid(coin.getAcquisition().getId()));
            ps.setObject(12, coin.getAccessedAt() == null
                ? null
                : OffsetDateTime.ofInstant(
                    coin.getAccessedAt(), ZoneOffset.UTC));
        });
    }

    /**
     * Gets all museums. There are few, so this reads them all.
     *
     * @return the museums, in code order
     */
    public List<Museum> findMuseums() {
        return jdbc.query(
            "SELECT museum_id, name FROM museums ORDER BY museum_id",
            (rs, rowNum) -> new Museum(
                rs.getString("name"), rs.getString("museum_id"))
        );
    }

    /**
     * Writes museums, replacing the name of any with the same code, in
     * one transaction.
     *
     * @param museums the museums
     */
    public void upsertMuseums(final List<Museum> museums) {
        batch(UPSERT_MUSEUM, museums, (ps, museum) -> {
            ps.setString(1, museum.getId());
            ps.setString(2, museum.getName());
        });
    }

    /**
     * Gets a donor by ID.
     *
     * @param id the donor ID
     * @return the donor, or null if there is none
     * @throws IllegalArgumentException if the ID is not a UUID
     */
    public Donor findDonor(final String id) {
        List<Donor> donors = jdbc.query(
            "SELECT id, name, name_variants, additional_info"
                + " FROM donors WHERE id = ?",
            this::mapDonor, uuid(id)
        );
        return donors.isEmpty() ? null : donors.get(0);
    }

    /**
     * Writes donors, replacing any with the same ID, in one
     * transaction.
     *
     * @param donors the donors
     */
    public void upsertDonors(final List<Donor> donors) {
        batch(UPSERT_DONOR, donors, (ps, donor) -> {
            ps.setObject(1, uuid(donor.getId()));
            ps.setString(2, donor.getName());
            ps.setArray(3, ps.getConnection().createArrayOf(
                "text", donor.getNameVariants().toArray()));
            ps.setString(4, donor.getAdditionalInfo());
        });
    }

    /**
     * Writes acquisitions, replacing any with the same ID, in one
     * transaction. Each acquisition's museum and donor must already
     * exist.
     *
     * @param acquisitions the acquisitions
     */
    public void upsertAcquisitions(final List<Acquisition> acquisitions) {
        batch(UPSERT_ACQUISITION, acquisitions, (ps, acquisition) -> {
            ps.setObject(1, uuid(acquisition.getId()));
            ps.setString(2, acquisition.getMuseumId());
            ps.setObject(3, uuid(acquisition.getDonorId()));
            ps.setObject(4, acquisition.getYear());
            ps.setString(5, acquisition.getType().name());
            ps.setString(6, acquisition.getLotIdentifier());
            ps.setString(7, acquisition.getAcquiredOnText());
            ps.setString(8, acquisition.getAdditionalInfo());
        });
    }

    /**
     * Runs an upsert for every item, in batches, inside one transaction
     * so that a failure part-way leaves nothing half-written.
     *
     * @param <T> the item type
     * @param sql the upsert statement
     * @param items the items
     * @param setter fills the statement's parameters from an item
     */
    private <T> void batch(
        final String sql,
        final List<T> items,
        final ParameterizedPreparedStatementSetter<T> setter
    ) {
        transaction.executeWithoutResult(status ->
            jdbc.batchUpdate(sql, items, BATCH_SIZE, setter));
    }

    /**
     * Converts an ID to a UUID.
     *
     * @param id the ID, or null
     * @return the UUID, or null if the ID is null
     * @throws IllegalArgumentException if the ID is not a UUID
     */
    private static UUID uuid(final String id) {
        return id == null ? null : UUID.fromString(id);
    }

    /**
     * Builds a coin, and its acquisition if it has one, from a row of
     * {@link #COIN_SELECT}.
     *
     * @param rs the result set, at the row to read
     * @param rowNum the row number
     * @return the coin
     * @throws SQLException if a column cannot be read
     */
    private Coin mapCoin(final ResultSet rs, final int rowNum)
        throws SQLException {
        Coin coin = new Coin(
            rs.getString("id"),
            rs.getString("identifier_code"),
            rs.getString("catalogue_link"),
            rs.getString("credit_line")
        );
        coin.setMuseumId(rs.getString("coin_museum_id"));
        coin.setCategory(rs.getString("category"));
        coin.setMaterial(rs.getString("material"));
        coin.setPeriodStart(rs.getObject("period_start", Integer.class));
        coin.setPeriodEnd(rs.getObject("period_end", Integer.class));
        coin.setPeriodInferred(rs.getBoolean("period_inferred"));
        OffsetDateTime accessedAt =
            rs.getObject("accessed_at", OffsetDateTime.class);
        if (accessedAt != null) {
            coin.setAccessedAt(accessedAt.toInstant());
        }

        String acquisitionId = rs.getString("acquisition_id");
        if (acquisitionId != null) {
            Acquisition acquisition = new Acquisition(
                acquisitionId,
                rs.getString("museum_id"),
                rs.getString("donor_id"),
                rs.getObject("year", Integer.class),
                AcquisitionType.valueOf(rs.getString("acquisition_type"))
            );
            acquisition.setLotIdentifier(rs.getString("lot_identifier"));
            acquisition.setAcquiredOnText(
                rs.getString("acquired_on_text"));
            acquisition.setAdditionalInfo(rs.getString("acquisition_info"));
            coin.setAcquisition(acquisition);
        }
        return coin;
    }

    /**
     * Builds a donor from a row of the donors table.
     *
     * @param rs the result set, at the row to read
     * @param rowNum the row number
     * @return the donor
     * @throws SQLException if a column cannot be read
     */
    private Donor mapDonor(final ResultSet rs, final int rowNum)
        throws SQLException {
        String[] variants = (String[]) rs.getArray("name_variants")
            .getArray();
        Donor donor = new Donor(
            rs.getString("id"),
            rs.getString("name"),
            new LinkedHashSet<>(Arrays.asList(variants))
        );
        donor.setAdditionalInfo(rs.getString("additional_info"));
        return donor;
    }
}
