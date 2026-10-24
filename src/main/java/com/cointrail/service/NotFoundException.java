package com.cointrail.service;

/**
 * Thrown when a record asked for by ID does not exist.
 */
public final class NotFoundException extends RuntimeException {

    /**
     * Constructs the exception.
     *
     * @param message which record was not found
     */
    public NotFoundException(final String message) {
        super(message);
    }
}
