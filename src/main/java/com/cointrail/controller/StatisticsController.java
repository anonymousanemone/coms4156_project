package com.cointrail.controller;

import com.cointrail.service.StatisticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for cross-museum statistics (operation 4).
 */
@RestController
@RequestMapping("/v1/statistics")
public final class StatisticsController {

    /** The statistics service. */
    private final StatisticsService statisticsService;

    /**
     * Constructs the controller.
     *
     * @param statisticsService the statistics service
     */
    public StatisticsController(final StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * Computes a metric grouped by up to two dimensions.
     *
     * @return 501 until implemented
     */
    @GetMapping
    public ResponseEntity<Object> getStatistics() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Summarizes one museum's acquisitions.
     *
     * @param museumId the museum code
     * @return 501 until implemented
     */
    @GetMapping("/museums/{museumId}")
    public ResponseEntity<Object> getMuseumSummary(
        @PathVariable final String museumId
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Summarizes one donor's acquisitions.
     *
     * @param donorId the donor ID
     * @return 501 until implemented
     */
    @GetMapping("/donors/{donorId}")
    public ResponseEntity<Object> getDonorSummary(
        @PathVariable final String donorId
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
