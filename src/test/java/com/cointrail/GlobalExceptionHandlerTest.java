package com.cointrail;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cointrail.controller.GlobalExceptionHandler;
import com.cointrail.service.InvalidFilterException;
import com.cointrail.service.NotFoundException;
import com.cointrail.service.QueryFilter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Checks that each exception becomes the right status and JSON body, and
 * that query parameters bind to a {@link QueryFilter}.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    /** A controller whose only job is to throw what it is asked to. */
    @RestController
    static class ThrowingController {

        @GetMapping("/throw")
        String throwIt(@RequestParam final String kind) {
            switch (kind) {
                case "filter":
                    throw new InvalidFilterException(
                        "Unknown museum: X", List.of("ANS", "MMA"));
                case "argument":
                    throw new IllegalArgumentException("Bad input");
                case "missing":
                    throw new NotFoundException("No such donor");
                default:
                    return "ok";
            }
        }

        @GetMapping("/filter")
        QueryFilter echo(final QueryFilter filter) {
            return filter;
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void invalidFilterReturns400WithAcceptedValues() throws Exception {
        mockMvc.perform(get("/throw").param("kind", "filter"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Unknown museum: X"))
            .andExpect(jsonPath("$.acceptedValues[0]").value("ANS"))
            .andExpect(jsonPath("$.acceptedValues[1]").value("MMA"));
    }

    @Test
    void illegalArgumentReturns400() throws Exception {
        mockMvc.perform(get("/throw").param("kind", "argument"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Bad input"))
            .andExpect(jsonPath("$.acceptedValues").isEmpty());
    }

    @Test
    void notFoundReturns404() throws Exception {
        mockMvc.perform(get("/throw").param("kind", "missing"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("No such donor"));
    }

    @Test
    void queryParametersBindToFilter() throws Exception {
        mockMvc.perform(get("/filter")
                .param("museums", "MMA", "ANS")
                .param("types", "gift")
                .param("yearFrom", "1900")
                .param("minObjects", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.museums[1]").value("ANS"))
            .andExpect(jsonPath("$.types[0]").value("gift"))
            .andExpect(jsonPath("$.yearFrom").value(1900))
            .andExpect(jsonPath("$.yearTo").doesNotExist())
            .andExpect(jsonPath("$.minObjects").value(2));
    }

    @Test
    void nonNumericYearReturns400() throws Exception {
        mockMvc.perform(get("/filter").param("yearFrom", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invalid parameter value"));
    }
}
