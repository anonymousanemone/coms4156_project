package com.cointrail.service;

import com.cointrail.model.AcquisitionType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * The acquisition filters shared by Get acquisitions, Get donors and Get
 * statistics. Spring fills it from query parameters of the same names,
 * e.g. {@code ?museums=MMA&museums=ANS&types=gift&yearFrom=1900}.
 * Every field is optional; null means "no filter".
 *
 * @param museums museum codes to include
 * @param types acquisition types to include, in any case
 * @param donorId the donor ID to include
 * @param yearFrom the earliest acquisition year, inclusive
 * @param yearTo the latest acquisition year, inclusive
 * @param minObjects the fewest objects an acquisition may have
 */
public record QueryFilter(
    List<String> museums,
    List<String> types,
    String donorId,
    Integer yearFrom,
    Integer yearTo,
    Integer minObjects
) {

    /** A filter that matches everything. */
    public static final QueryFilter NONE =
        new QueryFilter(null, null, null, null, null, null);

    /**
     * Checks every filter value and returns a copy with the types in
     * upper case, ready for {@link #appendConditions}.
     *
     * @param knownMuseums the codes of the museums that exist
     * @return the checked filter
     * @throws InvalidFilterException if a museum or type does not exist
     * @throws IllegalArgumentException if the donor ID is not a UUID, the
     *     year range is backwards, or {@code minObjects} is negative
     */
    public QueryFilter validate(final Collection<String> knownMuseums) {
        if (museums != null) {
            for (String museum : museums) {
                if (!knownMuseums.contains(museum)) {
                    throw new InvalidFilterException(
                        "Unknown museum: " + museum,
                        knownMuseums.stream().sorted().toList()
                    );
                }
            }
        }

        List<String> upperTypes = null;
        if (types != null) {
            upperTypes = types.stream()
                .map(type -> type.toUpperCase(Locale.ROOT))
                .toList();
            for (int i = 0; i < types.size(); i++) {
                if (!isAcquisitionType(upperTypes.get(i))) {
                    throw new InvalidFilterException(
                        "Unknown acquisition type: " + types.get(i),
                        Arrays.stream(AcquisitionType.values())
                            .map(Enum::name)
                            .toList()
                    );
                }
            }
        }

        if (donorId != null) {
            try {
                UUID.fromString(donorId);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "Donor ID is not a valid ID: " + donorId);
            }
        }
        if (yearFrom != null && yearTo != null && yearFrom > yearTo) {
            throw new IllegalArgumentException(
                "yearFrom must not be after yearTo");
        }
        if (minObjects != null && minObjects < 0) {
            throw new IllegalArgumentException(
                "minObjects must not be negative");
        }

        return new QueryFilter(
            museums, upperTypes, donorId, yearFrom, yearTo, minObjects
        );
    }

    /**
     * Appends {@code AND ...} conditions for the museum, type, donor and
     * year filters to a query whose acquisitions table is aliased
     * {@code a}, and adds their values to {@code args}. The
     * {@code minObjects} filter is left to each caller, since it applies
     * after counting.
     *
     * @param sql the query so far, ending in a WHERE clause
     * @param args the query's parameter values so far
     */
    public void appendConditions(
        final StringBuilder sql,
        final List<Object> args
    ) {
        if (museums != null && !museums.isEmpty()) {
            sql.append(" AND a.museum_id IN (")
                .append(placeholders(museums.size()))
                .append(')');
            args.addAll(museums);
        }
        if (types != null && !types.isEmpty()) {
            sql.append(" AND a.type::text IN (")
                .append(placeholders(types.size()))
                .append(')');
            args.addAll(types);
        }
        if (donorId != null) {
            sql.append(" AND a.donor_id = ?");
            args.add(UUID.fromString(donorId));
        }
        if (yearFrom != null) {
            sql.append(" AND a.year >= ?");
            args.add(yearFrom);
        }
        if (yearTo != null) {
            sql.append(" AND a.year <= ?");
            args.add(yearTo);
        }
    }

    private static boolean isAcquisitionType(final String name) {
        return Arrays.stream(AcquisitionType.values())
            .anyMatch(type -> type.name().equals(name));
    }

    private static String placeholders(final int count) {
        return String.join(", ", Collections.nCopies(count, "?"));
    }
}
