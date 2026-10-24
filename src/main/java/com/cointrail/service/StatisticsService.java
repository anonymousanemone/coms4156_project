package com.cointrail.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Aggregates counts across the whole datastore (operation 4).
 */
@Service
public final class StatisticsService {

    /** The JDBC template. */
    private final JdbcTemplate jdbc;

    /**
     * Constructs the service.
     *
     * @param jdbc the JDBC template
     */
    public StatisticsService(final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
}
