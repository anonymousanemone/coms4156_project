package com.cointrail.controller;

import com.cointrail.config.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the Cointrail API.
 */
@RestControllerAdvice
public final class GlobalExceptionHandler {
    /**
     * Handles IllegalArgumentException and returns a 400 Bad Request response.
     *
     * @param ex the IllegalArgumentException
     * @return a ResponseEntity with status 400 and the exception message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(
        final IllegalArgumentException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ex.getMessage());
    }

    /**
     * Handles a missing or unknown API key with 401 Unauthorized.
     *
     * @param ex the UnauthorizedException
     * @return a ResponseEntity with status 401 and the exception message
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(
        final UnauthorizedException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ex.getMessage());
    }
}
