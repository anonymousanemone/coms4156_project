package com.cointrail.controller;

import com.cointrail.service.QueryFilter;
import com.cointrail.service.StatisticsService;
import com.cointrail.service.StatisticsService.Statistics;
import com.cointrail.service.StatisticsService.Summary;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
     * Computes a metric grouped by up to two dimensions, e.g.
     * {@code ?metric=objects&groupBy=decade&groupBy=type&museums=MMA}.
     *
     * @param metric what to count: objects, acquisitions or donors
     * @param groupBy up to two dimensions to group by
     * @param filter the acquisition filters, from query parameters
     * @return 200 with the buckets, total and excluded counts
     */
    @GetMapping
    public ResponseEntity<Statistics> getStatistics(
        @RequestParam(defaultValue = "objects") final String metric,
        @RequestParam(required = false) final List<String> groupBy,
        final QueryFilter filter
    ) {
        return ResponseEntity.ok(
            statisticsService.compute(metric, groupBy, filter)
        );
    }

    /**
     * Summarizes one museum's acquisitions.
     *
     * @param museumId the museum code
     * @return 200 with the summary
     */
    @GetMapping("/museums/{museumId}")
    public ResponseEntity<Summary> getMuseumSummary(
        @PathVariable final String museumId
    ) {
        return ResponseEntity.ok(statisticsService.summarizeMuseum(museumId));
    }

    /**
     * Summarizes one donor's acquisitions across every museum.
     *
     * @param donorId the donor ID
     * @return 200 with the summary
     */
    @GetMapping("/donors/{donorId}")
    public ResponseEntity<Summary> getDonorSummary(
        @PathVariable final String donorId
    ) {
        return ResponseEntity.ok(statisticsService.summarizeDonor(donorId));
    }
}
