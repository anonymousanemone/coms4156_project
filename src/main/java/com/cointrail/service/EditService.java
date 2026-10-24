package com.cointrail.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Edits and deletes records and re-derives anything affected
 * (operation 5).
 */
@Service
public final class EditService {

    /** The JDBC template. */
    private final JdbcTemplate jdbc;

    /**
     * Constructs the service.
     *
     * @param jdbc the JDBC template
     */
    public EditService(final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
}
