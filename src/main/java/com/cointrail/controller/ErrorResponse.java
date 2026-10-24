package com.cointrail.controller;

import java.util.List;

/**
 * The JSON body of every error response.
 *
 * @param error what went wrong
 * @param acceptedValues the values that would have been accepted, or an
 *     empty list if that does not apply
 */
public record ErrorResponse(String error, List<String> acceptedValues) {

    /**
     * Creates an error with no accepted values.
     *
     * @param error what went wrong
     * @return the error
     */
    public static ErrorResponse of(final String error) {
        return new ErrorResponse(error, List.of());
    }
}
