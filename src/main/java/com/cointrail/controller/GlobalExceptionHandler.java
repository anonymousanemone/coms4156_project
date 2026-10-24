package com.cointrail.controller;

import com.cointrail.config.UnauthorizedException;
import com.cointrail.service.InvalidFilterException;
import com.cointrail.service.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Turns exceptions into JSON {@link ErrorResponse}s with the right status.
 */
@RestControllerAdvice
public final class GlobalExceptionHandler {

    /**
     * Handles a filter value that does not exist with 400 Bad Request,
     * listing the values that would have been accepted.
     *
     * @param ex the InvalidFilterException
     * @return status 400 with the message and accepted values
     */
    @ExceptionHandler(InvalidFilterException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFilterException(
        final InvalidFilterException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getMessage(), ex.getAcceptedValues()));
    }

    /**
     * Handles any other bad input with 400 Bad Request.
     *
     * @param ex the IllegalArgumentException
     * @return status 400 with the message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        final IllegalArgumentException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(ex.getMessage()));
    }

    /**
     * Handles a query or path parameter of the wrong type, such as
     * {@code yearFrom=abc}, with 400 Bad Request.
     *
     * @param ex the exception Spring threw while reading the parameter
     * @return status 400 naming the parameter
     */
    @ExceptionHandler({
        BindException.class, MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleBadParameter(
        final Exception ex
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of("Invalid parameter value"));
    }

    /**
     * Handles a missing or unknown API key with 401 Unauthorized.
     *
     * @param ex the UnauthorizedException
     * @return status 401 with the message
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
        final UnauthorizedException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse.of(ex.getMessage()));
    }

    /**
     * Handles a record that does not exist with 404 Not Found.
     *
     * @param ex the NotFoundException
     * @return status 404 with the message
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
        final NotFoundException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of(ex.getMessage()));
    }
}
