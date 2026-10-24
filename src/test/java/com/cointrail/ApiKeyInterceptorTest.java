package com.cointrail;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cointrail.config.ApiKeyInterceptor;
import com.cointrail.controller.GlobalExceptionHandler;
import com.cointrail.controller.StatisticsController;
import com.cointrail.service.StatisticsService;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Checks that /v1 requests need a registered API key.
 */
class ApiKeyInterceptorTest {

    /** The only key the interceptor accepts in these tests. */
    private static final String VALID_KEY = "valid-key";

    /** Sends requests through the interceptor. */
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(
                new StatisticsController(mock(StatisticsService.class))
            )
            .addInterceptors(new ApiKeyInterceptor(Set.of(VALID_KEY)))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void missingKeyReturns401() throws Exception {
        mockMvc.perform(get("/v1/statistics"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Missing X-API-Key header"));
    }

    @Test
    void blankKeyReturns401() throws Exception {
        mockMvc.perform(get("/v1/statistics")
                .header(ApiKeyInterceptor.HEADER, "  "))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownKeyReturns401() throws Exception {
        mockMvc.perform(get("/v1/statistics")
                .header(ApiKeyInterceptor.HEADER, "wrong-key"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unknown API key"));
    }

    @Test
    void validKeyReachesController() throws Exception {
        mockMvc.perform(get("/v1/statistics")
                .header(ApiKeyInterceptor.HEADER, VALID_KEY))
            .andExpect(status().isOk());
    }

    @Test
    void preflightNeedsNoKey() throws Exception {
        mockMvc.perform(options("/v1/statistics"))
            .andExpect(status().isOk());
    }

    @Test
    void loadsKeysFromClientsFile() {
        MockHttpServletRequest request =
            new MockHttpServletRequest("GET", "/v1/statistics");
        // This key is the MMA client's in src/main/resources/data/clients.json.
        request.addHeader(
            ApiKeyInterceptor.HEADER, "540ba996-2ae9-4f72-8b4f-014b463bc4d7"
        );
        assertTrue(new ApiKeyInterceptor().preHandle(
            request, new MockHttpServletResponse(), new Object()
        ));
    }
}
