package com.cointrail.service;

import com.cointrail.model.Donor;
import com.cointrail.model.Museum;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Aggregates counts across the whole datastore (operation 4).
 *
 * <p>Every query starts from the coins table, joined to each coin's
 * acquisition and donor. A coin with no acquisition has no year and an
 * unknown type.</p>
 */
@Service
public final class StatisticsService {

    /** The most group-by dimensions one request may use. */
    public static final int MAX_GROUP_BY = 2;

    /** How many acquisitions a summary lists as the largest. */
    public static final int LARGEST_ACQUISITIONS = 5;

    /** Coins joined to their acquisition and donor. */
    private static final String FROM =
        " FROM coins c"
            + " LEFT JOIN acquisitions a ON a.id = c.acquisition_id"
            + " LEFT JOIN donors d ON d.id = a.donor_id";

    /** True for a coin whose acquisition year is missing. */
    private static final String MISSING_YEAR = "a.year IS NULL";

    /** True for a coin whose acquisition type is unknown. */
    private static final String UNKNOWN_TYPE =
        "(a.type IS NULL OR a.type = 'UNKNOWN')";

    /** What to count. */
    public enum Metric {

        /** Number of coins. */
        OBJECTS("COUNT(c.id)"),

        /** Number of distinct acquisitions. */
        ACQUISITIONS("COUNT(DISTINCT a.id)"),

        /** Number of distinct donors. */
        DONORS("COUNT(DISTINCT a.donor_id)");

        /** The SQL aggregate that computes this metric. */
        private final String sql;

        Metric(final String sql) {
            this.sql = sql;
        }
    }

    /** What to group by. */
    public enum Dimension {

        /** Museum code. */
        MUSEUM("c.museum_id"),

        /** Acquisition year. */
        YEAR("a.year"),

        /** Acquisition decade, as its first year (1910 for 1910-1919). */
        DECADE("(a.year / 10) * 10"),

        /** Acquisition type. */
        TYPE("COALESCE(a.type::text, 'UNKNOWN')"),

        /** Donor ID. */
        DONOR("a.donor_id::text"),

        /** Coin category, such as "Byzantine". */
        CATEGORY("c.category"),

        /** Coin material, such as "Silver". */
        MATERIAL("c.material"),

        /**
         * The century the coin was made in, as its first year
         * (-400 for 400-301 BC, 1700 for 1700-1799).
         */
        PERIOD("(FLOOR(c.period_start / 100.0) * 100)::int");

        /** The SQL expression for this dimension's value. */
        private final String sql;

        Dimension(final String sql) {
            this.sql = sql;
        }

        /**
         * Tells whether this dimension needs an acquisition year.
         *
         * @return true for YEAR and DECADE
         */
        boolean needsYear() {
            return this == YEAR || this == DECADE;
        }
    }

    /**
     * One group of results.
     *
     * @param groups the value of each group-by dimension, in request order
     * @param value the metric for this group
     */
    public record Bucket(Map<String, Object> groups, long value) {
    }

    /**
     * The result of a statistics request.
     *
     * @param metric what was counted
     * @param groupBy the dimensions grouped by
     * @param buckets one entry per group, empty if nothing was grouped by
     * @param total the metric over every included record
     * @param excludedMissingYear records left out because grouping by
     *     year or decade needs a year they lack
     * @param excludedUnknownType records left out because grouping by
     *     type needs a known type
     */
    public record Statistics(
        String metric,
        List<String> groupBy,
        List<Bucket> buckets,
        long total,
        long excludedMissingYear,
        long excludedUnknownType
    ) {
    }

    /**
     * One acquisition and how many coins it holds.
     *
     * @param id the acquisition ID
     * @param museumId the museum code
     * @param donorId the donor ID, or null
     * @param donorName the donor's name, or null
     * @param year the acquisition year, or null
     * @param type the acquisition type
     * @param objects the number of coins
     */
    public record AcquisitionSize(
        String id,
        String museumId,
        String donorId,
        String donorName,
        Integer year,
        String type,
        long objects
    ) {
    }

    /**
     * A summary of one museum's or one donor's acquisitions.
     *
     * @param id the museum code or donor ID
     * @param name the museum or donor name
     * @param totalObjects the number of coins
     * @param totalAcquisitions the number of acquisitions
     * @param distinctDonors the number of different donors
     * @param distinctMuseums the number of different museums
     * @param firstYear the earliest acquisition year, or null
     * @param lastYear the latest acquisition year, or null
     * @param objectsByType coin counts per acquisition type
     * @param objectsByMuseum coin counts per museum
     * @param largestAcquisitions the acquisitions with the most coins
     */
    public record Summary(
        String id,
        String name,
        long totalObjects,
        long totalAcquisitions,
        long distinctDonors,
        long distinctMuseums,
        Integer firstYear,
        Integer lastYear,
        Map<String, Long> objectsByType,
        Map<String, Long> objectsByMuseum,
        List<AcquisitionSize> largestAcquisitions
    ) {
    }

    /** The JDBC template. */
    private final JdbcTemplate jdbc;

    /** Reads museums and donors. */
    private final StorageService storage;

    /**
     * Constructs the service.
     *
     * @param jdbc the JDBC template
     * @param storage reads museums and donors
     */
    public StatisticsService(
        final JdbcTemplate jdbc,
        final StorageService storage
    ) {
        this.jdbc = jdbc;
        this.storage = storage;
    }

    /**
     * Computes a metric over every coin that matches the filter, grouped
     * by up to two dimensions.
     *
     * @param metricName what to count: objects, acquisitions or donors
     * @param groupByNames up to two dimensions, or null for none
     * @param filter the acquisition filters
     * @return the buckets, the total and the excluded counts
     * @throws InvalidFilterException if a metric, dimension, museum or
     *     type does not exist
     * @throws IllegalArgumentException if more than two dimensions are
     *     given, one is repeated, or the filter is invalid
     */
    public Statistics compute(
        final String metricName,
        final List<String> groupByNames,
        final QueryFilter filter
    ) {
        Metric metric = parse(Metric.class, metricName, "metric");
        List<Dimension> dimensions = parseDimensions(groupByNames);
        QueryFilter checked = filter.validate(museumCodes());

        StringBuilder where = new StringBuilder(" WHERE TRUE");
        List<Object> args = new ArrayList<>();
        checked.appendConditions(where, args);
        if (checked.minObjects() != null) {
            where.append(" AND a.id IN (SELECT acquisition_id FROM coins")
                .append(" GROUP BY acquisition_id HAVING COUNT(*) >= ?)");
            args.add(checked.minObjects());
        }

        // A group-by on year or type leaves out records that lack one.
        boolean dropMissingYear =
            dimensions.stream().anyMatch(Dimension::needsYear);
        boolean dropUnknownType = dimensions.contains(Dimension.TYPE);
        String missingYear = dropMissingYear ? MISSING_YEAR : "FALSE";
        String unknownType = dropUnknownType ? UNKNOWN_TYPE : "FALSE";
        String included =
            "NOT (" + missingYear + ") AND NOT " + unknownType;

        Map<String, Object> totals = jdbc.queryForMap(
            "SELECT " + metric.sql + " FILTER (WHERE " + included
                + ") AS total, "
                + metric.sql + " FILTER (WHERE " + missingYear
                + ") AS missing_year, "
                + metric.sql + " FILTER (WHERE " + unknownType
                + ") AS unknown_type"
                + FROM + where,
            args.toArray()
        );

        List<Bucket> buckets = dimensions.isEmpty()
            ? List.of()
            : buckets(metric, dimensions, where + " AND " + included, args);

        return new Statistics(
            name(metric),
            dimensions.stream().map(StatisticsService::name).toList(),
            buckets,
            count(totals.get("total")),
            count(totals.get("missing_year")),
            count(totals.get("unknown_type"))
        );
    }

    /**
     * Summarizes one museum's acquisitions.
     *
     * @param museumId the museum code
     * @return the summary
     * @throws NotFoundException if there is no such museum
     */
    public Summary summarizeMuseum(final String museumId) {
        Museum museum = storage.findMuseums().stream()
            .filter(m -> m.getId().equals(museumId))
            .findFirst()
            .orElseThrow(() ->
                new NotFoundException("No museum with code " + museumId));
        return summarize(
            museumId, museum.getName(), "c.museum_id = ?", museumId
        );
    }

    /**
     * Summarizes one donor's acquisitions across every museum.
     *
     * @param donorId the donor ID
     * @return the summary
     * @throws NotFoundException if there is no such donor
     * @throws IllegalArgumentException if the ID is not a valid ID
     */
    public Summary summarizeDonor(final String donorId) {
        Donor donor = storage.findDonor(donorId);
        if (donor == null) {
            throw new NotFoundException("No donor with ID " + donorId);
        }
        return summarize(
            donorId, donor.getName(), "a.donor_id = ?",
            UUID.fromString(donorId)
        );
    }

    /**
     * Runs the grouped query and turns each row into a bucket.
     *
     * @param metric what to count
     * @param dimensions what to group by
     * @param where the WHERE clause, including exclusions
     * @param args the WHERE clause's parameter values
     * @return the buckets, ordered by group values
     */
    private List<Bucket> buckets(
        final Metric metric,
        final List<Dimension> dimensions,
        final String where,
        final List<Object> args
    ) {
        // e.g. SELECT <dim0> AS g0, <dim1> AS g1, <metric> AS value
        //      ... GROUP BY 1, 2 ORDER BY 1 NULLS LAST, 2 NULLS LAST
        List<String> columns = new ArrayList<>();
        List<String> positions = new ArrayList<>();
        List<String> orders = new ArrayList<>();
        for (int i = 0; i < dimensions.size(); i++) {
            columns.add(dimensions.get(i).sql + " AS g" + i);
            positions.add(String.valueOf(i + 1));
            orders.add((i + 1) + " NULLS LAST");
        }
        String sql = "SELECT " + String.join(", ", columns) + ", "
            + metric.sql + " AS value" + FROM + where
            + " GROUP BY " + String.join(", ", positions)
            + " ORDER BY " + String.join(", ", orders);

        return jdbc.query(sql, (rs, rowNum) -> {
            Map<String, Object> groups = new LinkedHashMap<>();
            for (int i = 0; i < dimensions.size(); i++) {
                groups.put(name(dimensions.get(i)), rs.getObject("g" + i));
            }
            return new Bucket(groups, rs.getLong("value"));
        }, args.toArray());
    }

    /**
     * Builds a summary of the coins matching one condition.
     *
     * @param id the museum code or donor ID
     * @param name the museum or donor name
     * @param condition a SQL condition with one parameter
     * @param arg the condition's parameter value
     * @return the summary
     */
    private Summary summarize(
        final String id,
        final String name,
        final String condition,
        final Object arg
    ) {
        String where = " WHERE " + condition;
        Map<String, Object> totals = jdbc.queryForMap(
            "SELECT COUNT(c.id) AS objects,"
                + " COUNT(DISTINCT a.id) AS acquisitions,"
                + " COUNT(DISTINCT a.donor_id) AS donors,"
                + " COUNT(DISTINCT c.museum_id) AS museums,"
                + " MIN(a.year) AS first_year, MAX(a.year) AS last_year"
                + FROM + where,
            arg
        );
        List<AcquisitionSize> largest = jdbc.query(
            "SELECT a.id, a.museum_id, a.donor_id, d.name, a.year,"
                + " a.type, COUNT(c.id) AS objects"
                + FROM + where + " AND a.id IS NOT NULL"
                + " GROUP BY a.id, d.name"
                + " ORDER BY objects DESC, a.id LIMIT ?",
            StatisticsService::mapAcquisitionSize,
            arg, LARGEST_ACQUISITIONS
        );

        return new Summary(
            id,
            name,
            count(totals.get("objects")),
            count(totals.get("acquisitions")),
            count(totals.get("donors")),
            count(totals.get("museums")),
            (Integer) totals.get("first_year"),
            (Integer) totals.get("last_year"),
            countBy(Dimension.TYPE.sql, where, arg),
            countBy("c.museum_id", where, arg),
            largest
        );
    }

    /**
     * Counts coins per value of one expression.
     *
     * @param expression the SQL expression to group by
     * @param where the WHERE clause, with one parameter
     * @param arg the WHERE clause's parameter value
     * @return the count for each value, ordered by value
     */
    private Map<String, Long> countBy(
        final String expression,
        final String where,
        final Object arg
    ) {
        Map<String, Long> counts = new LinkedHashMap<>();
        jdbc.query(
            "SELECT " + expression + " AS k, COUNT(c.id) AS n" + FROM + where
                + " GROUP BY 1 ORDER BY 1",
            rs -> {
                counts.put(rs.getString("k"), rs.getLong("n"));
            },
            arg
        );
        return counts;
    }

    /**
     * Parses the group-by names into dimensions.
     *
     * @param names the names, or null
     * @return the dimensions, in request order
     */
    private static List<Dimension> parseDimensions(final List<String> names) {
        if (names == null || names.isEmpty()) {
            return List.of();
        }
        if (names.size() > MAX_GROUP_BY) {
            throw new IllegalArgumentException(
                "At most " + MAX_GROUP_BY + " groupBy dimensions are allowed");
        }
        List<Dimension> dimensions = names.stream()
            .map(name -> parse(Dimension.class, name, "groupBy dimension"))
            .toList();
        if (new LinkedHashSet<>(dimensions).size() != dimensions.size()) {
            throw new IllegalArgumentException(
                "A groupBy dimension is repeated");
        }
        return dimensions;
    }

    /**
     * Parses a name into an enum constant, ignoring case.
     *
     * @param type the enum class
     * @param value the name
     * @param label what the value is, for the error message
     * @param <E> the enum type
     * @return the constant
     * @throws InvalidFilterException if no constant has that name
     */
    private static <E extends Enum<E>> E parse(
        final Class<E> type,
        final String value,
        final String label
    ) {
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(value)) {
                return constant;
            }
        }
        throw new InvalidFilterException(
            "Unknown " + label + ": " + value,
            Arrays.stream(type.getEnumConstants())
                .map(StatisticsService::name)
                .toList()
        );
    }

    /**
     * Gets the codes of every museum, for validating filters.
     *
     * @return the museum codes
     */
    private List<String> museumCodes() {
        return storage.findMuseums().stream().map(Museum::getId).toList();
    }

    /**
     * Gets the lower-case name an enum constant has in the API.
     *
     * @param constant the constant
     * @return its API name
     */
    private static String name(final Enum<?> constant) {
        return constant.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Reads a count that may be null when no rows matched.
     *
     * @param value the database value
     * @return the count, or 0
     */
    private static long count(final Object value) {
        return value == null ? 0 : ((Number) value).longValue();
    }

    /**
     * Maps a row of the largest-acquisitions query.
     *
     * @param rs the result set
     * @param rowNum the row number
     * @return the acquisition and its size
     * @throws SQLException if a column cannot be read
     */
    private static AcquisitionSize mapAcquisitionSize(
        final ResultSet rs,
        final int rowNum
    ) throws SQLException {
        return new AcquisitionSize(
            rs.getString("id"),
            rs.getString("museum_id"),
            rs.getString("donor_id"),
            rs.getString("name"),
            (Integer) rs.getObject("year"),
            rs.getString("type"),
            rs.getLong("objects")
        );
    }
}
