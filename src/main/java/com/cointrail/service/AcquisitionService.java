package com.cointrail.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Looks up acquisitions (lots) with optional filters (operation 2).
 */
@Service
public final class AcquisitionService {

    /** The JDBC template. */
    private final JdbcTemplate jdbc;

    /**
     * Constructs the service.
     *
     * @param jdbc the JDBC template
     */
    public AcquisitionService(final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
}
