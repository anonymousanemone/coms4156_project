package com.cointrail.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Looks up donors and aggregates their acquisitions across museums
 * (operation 3).
 */
@Service
public final class DonorService {

    /** The JDBC template. */
    private final JdbcTemplate jdbc;

    /**
     * Constructs the service.
     *
     * @param jdbc the JDBC template
     */
    public DonorService(final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
}
