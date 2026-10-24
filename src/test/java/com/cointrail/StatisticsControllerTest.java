package com.cointrail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cointrail.controller.GlobalExceptionHandler;
import com.cointrail.controller.StatisticsController;
import com.cointrail.service.InvalidFilterException;
import com.cointrail.service.NotFoundException;
import com.cointrail.service.QueryFilter;
import com.cointrail.service.StatisticsService;
import com.cointrail.service.StatisticsService.AcquisitionSize;
import com.cointrail.service.StatisticsService.Bucket;
import com.cointrail.service.StatisticsService.Statistics;
import com.cointrail.service.StatisticsService.Summary;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Checks how the statistics endpoints read requests and write responses.
 * The service is mocked.
 */
class StatisticsControllerTest {

    /** A mock service that the tests program per case. */
    private StatisticsService service;

    /** Sends requests to the controller. */
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(StatisticsService.class);
        mockMvc = MockMvcBuilders
            .standaloneSetup(new StatisticsController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void returnsBucketsAndTotals() throws Exception {
        when(service.compute(any(), any(), any())).thenReturn(new Statistics(
            "objects", List.of("decade", "type"),
            List.of(new Bucket(Map.of("decade", 1890, "type", "GIFT"), 113)),
            1383, 20, 21));

        mockMvc.perform(get("/v1/statistics")
                .param("groupBy", "decade", "type"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metric").value("objects"))
            .andExpect(jsonPath("$.buckets[0].groups.decade").value(1890))
            .andExpect(jsonPath("$.buckets[0].value").value(113))
            .andExpect(jsonPath("$.total").value(1383))
            .andExpect(jsonPath("$.excludedMissingYear").value(20))
            .andExpect(jsonPath("$.excludedUnknownType").value(21));
    }

    @Test
    void metricDefaultsToObjects() throws Exception {
        when(service.compute(any(), any(), any())).thenReturn(
            new Statistics("objects", List.of(), List.of(), 0, 0, 0));
        mockMvc.perform(get("/v1/statistics")).andExpect(status().isOk());
        verify(service).compute(eq("objects"), isNull(), any());
    }

    @Test
    void passesMetricGroupByAndFilterToService() throws Exception {
        when(service.compute(any(), any(), any())).thenReturn(
            new Statistics("donors", List.of(), List.of(), 0, 0, 0));
        mockMvc.perform(get("/v1/statistics")
                .param("metric", "donors")
                .param("groupBy", "museum")
                .param("types", "gift")
                .param("yearFrom", "1900"))
            .andExpect(status().isOk());
        verify(service).compute("donors", List.of("museum"),
            new QueryFilter(null, List.of("gift"), null, 1900, null, null));
    }

    @Test
    void invalidValueReturns400WithAcceptedValues() throws Exception {
        when(service.compute(any(), any(), any())).thenThrow(
            new InvalidFilterException("Unknown metric: coins",
                List.of("objects", "acquisitions", "donors")));
        mockMvc.perform(get("/v1/statistics").param("metric", "coins"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Unknown metric: coins"))
            .andExpect(jsonPath("$.acceptedValues[2]").value("donors"));
    }

    @Test
    void museumSummaryReturnsSummary() throws Exception {
        when(service.summarizeMuseum("MMA")).thenReturn(new Summary(
            "MMA", "The Met", 1405, 95, 65, 1, 1874, 2026,
            Map.of("GIFT", 496L), Map.of("MMA", 1405L),
            List.of(new AcquisitionSize(
                "a1", "MMA", null, null, 1939, "EXCAVATION", 525))));

        mockMvc.perform(get("/v1/statistics/museums/MMA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalObjects").value(1405))
            .andExpect(jsonPath("$.objectsByType.GIFT").value(496))
            .andExpect(jsonPath("$.largestAcquisitions[0].objects")
                .value(525));
    }

    @Test
    void unknownMuseumReturns404() throws Exception {
        when(service.summarizeMuseum("LOUVRE")).thenThrow(
            new NotFoundException("No museum with code LOUVRE"));
        mockMvc.perform(get("/v1/statistics/museums/LOUVRE"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                .value("No museum with code LOUVRE"));
    }

    @Test
    void malformedDonorIdReturns400() throws Exception {
        when(service.summarizeDonor("bad")).thenThrow(
            new IllegalArgumentException("Invalid ID: bad"));
        mockMvc.perform(get("/v1/statistics/donors/bad"))
            .andExpect(status().isBadRequest());
    }
}
