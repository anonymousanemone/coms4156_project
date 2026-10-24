package com.cointrail.service;

import java.util.List;

/**
 * Thrown when a filter names a value that does not exist, such as an
 * unknown museum or acquisition type.
 */
public final class InvalidFilterException extends RuntimeException {

    /** The values that would have been accepted. */
    private final List<String> acceptedValues;

    /**
     * Constructs the exception.
     *
     * @param message which value was rejected
     * @param acceptedValues the values that would have been accepted
     */
    public InvalidFilterException(
        final String message,
        final List<String> acceptedValues
    ) {
        super(message);
        this.acceptedValues = List.copyOf(acceptedValues);
    }

    /**
     * Gets the values that would have been accepted.
     *
     * @return the accepted values
     */
    public List<String> getAcceptedValues() {
        return acceptedValues;
    }
}
