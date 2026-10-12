package com.cointrail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Cointrail API.
 */
@SpringBootApplication
public final class CointrailApplication {

    /** Private constructor to prevent instantiation. */
    private CointrailApplication() {

    }

    /**
     * Application entry point.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(
            CointrailApplication.class, args
        );
    }
}
