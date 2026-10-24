package com.cointrail.controller;

import com.cointrail.service.DonorService;
import com.cointrail.service.QueryFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for looking up donors (operation 3).
 */
@RestController
@RequestMapping("/v1/donors")
public final class DonorController {

    /** The donor service. */
    private final DonorService donorService;

    /**
     * Constructs the controller.
     *
     * @param donorService the donor service
     */
    public DonorController(final DonorService donorService) {
        this.donorService = donorService;
    }

    /**
     * Lists donors matching optional filters.
     *
     * @param name part of the donor's name, matched without case
     * @param filter the acquisition filters, from query parameters
     * @return 501 until implemented
     */
    @GetMapping
    public ResponseEntity<Object> getDonors(
        @RequestParam(required = false) final String name,
        final QueryFilter filter
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Gets one donor with name variants and breakdowns.
     *
     * @param id the donor ID
     * @return 501 until implemented
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getDonor(@PathVariable final String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
