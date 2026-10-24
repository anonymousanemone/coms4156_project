package com.cointrail.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Imports coin records and derives their museums, donors and acquisitions
 * (operation 1).
 */
@Service
public final class ImportService {

    /** The JDBC template. */
    private final JdbcTemplate jdbc;

    /**
     * Constructs the service.
     *
     * @param jdbc the JDBC template
     */
    public ImportService(final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
}
