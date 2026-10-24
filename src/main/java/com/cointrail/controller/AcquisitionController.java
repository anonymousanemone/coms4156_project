package com.cointrail.controller;

import com.cointrail.service.AcquisitionService;
import com.cointrail.service.QueryFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for looking up acquisitions (operation 2).
 */
@RestController
@RequestMapping("/v1/acquisitions")
public final class AcquisitionController {

    /** The acquisition service. */
    private final AcquisitionService acquisitionService;

    /**
     * Constructs the controller.
     *
     * @param acquisitionService the acquisition service
     */
    public AcquisitionController(
        final AcquisitionService acquisitionService
    ) {
        this.acquisitionService = acquisitionService;
    }

    /**
     * Lists acquisitions matching optional filters.
     *
     * @param filter the filters, from query parameters
     * @return 501 until implemented
     */
    @GetMapping
    public ResponseEntity<Object> getAcquisitions(final QueryFilter filter) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Gets one acquisition with all of its objects.
     *
     * @param id the acquisition ID
     * @return 501 until implemented
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getAcquisition(
        @PathVariable final String id
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
