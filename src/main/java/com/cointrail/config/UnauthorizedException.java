package com.cointrail.config;

/**
 * Thrown when a request has a missing or unknown API key.
 */
public final class UnauthorizedException extends RuntimeException {

    /**
     * Constructs the exception.
     *
     * @param message why the request was rejected
     */
    public UnauthorizedException(final String message) {
        super(message);
    }
}
