package com.cointrail;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cointrail.controller.AcquisitionController;
import com.cointrail.controller.DonorController;
import com.cointrail.controller.EditController;
import com.cointrail.controller.ImportController;
import com.cointrail.controller.StatisticsController;
import com.cointrail.service.AcquisitionService;
import com.cointrail.service.DonorService;
import com.cointrail.service.EditService;
import com.cointrail.service.ImportService;
import com.cointrail.service.StatisticsService;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Checks that every planned route exists. Each owner replaces the 501
 * expectation for their routes with real tests as they implement them,
 * and deletes the row here.
 */
class StubRoutesTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            new ImportController(mock(ImportService.class)),
            new AcquisitionController(mock(AcquisitionService.class)),
            new DonorController(mock(DonorService.class)),
            new StatisticsController(mock(StatisticsService.class)),
            new EditController(mock(EditService.class))
        ).build();
    }

    static Stream<Arguments> stubRoutes() {
        return Stream.of(
            Arguments.of(post("/v1/objects")),
            Arguments.of(post("/v1/objects/batch")),
            Arguments.of(get("/v1/acquisitions")),
            Arguments.of(get("/v1/acquisitions/a1")),
            Arguments.of(get("/v1/donors")),
            Arguments.of(get("/v1/donors/d1")),
            Arguments.of(get("/v1/statistics")),
            Arguments.of(get("/v1/statistics/museums/MMA")),
            Arguments.of(get("/v1/statistics/donors/d1")),
            Arguments.of(patch("/v1/objects/o1")),
            Arguments.of(delete("/v1/objects/o1")),
            Arguments.of(patch("/v1/acquisitions/a1")),
            Arguments.of(patch("/v1/donors/d1")),
            Arguments.of(patch("/v1/museums/MMA"))
        );
    }

    @ParameterizedTest
    @MethodSource("stubRoutes")
    void stubRouteReturns501(final RequestBuilder request) throws Exception {
        mockMvc.perform(request).andExpect(status().isNotImplemented());
    }

    @Test
    void unknownRouteReturns404() throws Exception {
        mockMvc.perform(get("/v1/unknown")).andExpect(status().isNotFound());
    }
}
