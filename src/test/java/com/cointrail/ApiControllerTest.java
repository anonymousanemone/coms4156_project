package com.cointrail;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cointrail.controller.ApiController;
import com.cointrail.service.CointrailService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApiControllerTest {

    private MockMvc mockMvc;

    protected static final String VALID_KEY = "valid-key";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new ApiController(mock(CointrailService.class)))
            .build();
    }

    @Test
    void shouldReturn404ForUnknownRoute() throws Exception {
        mockMvc.perform(get("/v1/unknown"))
            .andExpect(status().isNotFound());
    }
}
