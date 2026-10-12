package com.cointrail;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Import(TestConfig.class)
class ApiControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private LocalStorageService localStorageService;

    private MockMvc mockMvc;

    @TempDir
    Path tempDir;

    protected static final String VALID_KEY = "valid-key";

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        Files.writeString(tempDir.resolve("clients.json"),
            "[{\"id\":\"client-1\",\"name\":\"Alice\",\"apiKey\":\"valid-key\"}]");
        Files.writeString(tempDir.resolve("items.json"),
            "[{\"id\":\"item-1\",\"name\":\"Laptop\",\"category\":\"electronics\",\"basePrice\":999.99}]");
        Files.writeString(tempDir.resolve("taxrates.json"),
            "[{\"state\":\"CA\",\"category\":\"electronics\",\"rate\":0.0725},"
            + "{\"state\":\"NY\",\"category\":\"clothing\",\"rate\":0.04}]");

        localStorageService.setDirectory(tempDir);
    }

    // via mockMvc.perform(...). Aim for >= 55% JaCoCo coverage overall.

    @Test
    void shouldCreateClient() throws Exception {
        String json = "{\"name\":\"Bob\"}";

        mockMvc.perform(post("/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.name").value("Bob")
            );
    }

    @Test
    void shouldReturn400ForInvalidBody() throws Exception {
        String json = "{\"no-name\":\"Bob\"}";

        mockMvc.perform(post("/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpectAll(
                status().isBadRequest()
            );
    }

    @Test
    void shouldReturn400ForNoBody() throws Exception {
        mockMvc.perform(post("/v1/clients")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpectAll(
                status().isBadRequest()
            );
    }
    
    @Test
    void shouldCreateClientReturn409WhenNameAlreadyExists() throws Exception {
        String json = "{\"name\":\"Alice\"}";

        mockMvc.perform(post("/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpectAll(
                status().isConflict(),
                jsonPath("$.error").value("A client with that name already exists")
            );
    }

    @Test
    void shouldCreateItemWithValidApiKey() throws Exception {
        String json = "{\"name\":\"Desk\",\"category\":\"furniture\",\"basePrice\":250.0}";

        mockMvc.perform(post("/v1/items")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.name").value("Desk")
            );
    }

    @Test
    void shouldGetItemsWithValidApiKey() throws Exception {
        mockMvc.perform(get("/v1/items")
                .header("X-API-Key", VALID_KEY))
            .andExpectAll(
                status().isOk(),
                jsonPath("$[0].id").value("item-1")
            );
    }

    @Test
    void shouldGetItemsWithCategory() throws Exception {
        mockMvc.perform(get("/v1/items?category=electronics")
                .header("X-API-Key", VALID_KEY))
            .andExpectAll(
                status().isOk(),
                jsonPath("$[0].id").value("item-1")
            );
    }

    @Test
    void shouldGetItemsWithName() throws Exception {
        mockMvc.perform(get("/v1/items?q=PT")
                .header("X-API-Key", VALID_KEY))
            .andExpectAll(
                status().isOk(),
                jsonPath("$[0].id").value("item-1")
            );
    }

    @Test
    void shouldGetItemByIdWithValidApiKey() throws Exception {
        mockMvc.perform(get("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.category").value("electronics")
            );
    }

    @Test
    void shouldGetItemByIdReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get("/v1/items/invalid-id")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteItemReturns204WhenFound() throws Exception {
        mockMvc.perform(delete("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldDeleteItemReturn404WhenNotFound() throws Exception {
        mockMvc.perform(delete("/v1/items/invalid-id")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCalculateTaxReturns200ForValidRequest() throws Exception {
        String json = "{\"state\":\"CA\",\"price\":100.0,\"category\":\"electronics\"}";

        mockMvc.perform(post("/v1/tax/quote")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.taxRate").value(0.0725)
            );
    }

    @Test
    void shouldCalculateTaxReturn400WhenServiceReturnsNull() throws Exception {
        String json = "{\"state\":\"NY\",\"price\":100.0,\"category\":\"electronics\"}";

        mockMvc.perform(post("/v1/tax/quote")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetSupportedWithValidApiKey() throws Exception {
        mockMvc.perform(get("/v1/supported")
                .header("X-API-Key", VALID_KEY))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.states", hasItem("CA"))
            );
    }

    @Test
    void shouldReturn401WhenApiKeyIsInvalid() throws Exception {
        mockMvc.perform(get("/v1/items")
                .header("X-API-Key", "invalid-key"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldPatchItemReturn401WhenApiKeyIsInvalid() throws Exception {
        mockMvc.perform(patch("/v1/items/item-1")
                .header("X-API-Key", "invalid-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("666.66"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldPatchItemWhenApiKeyValid() throws Exception {
        mockMvc.perform(patch("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("666.66"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenPatchingItemWithInvalidPrice() throws Exception {
        mockMvc.perform(patch("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("xx"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenPatchingItemWithEmptyPrice() throws Exception {
        mockMvc.perform(patch("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
            .andExpect(status().isBadRequest());
    }
    
    @Test 
    void shouldReturn401WithInvalidApiKeyForFilteredItems() throws Exception {
        mockMvc.perform(get("/v1/items?q=PT")
                .header("X-API-Key", "invalid-key"))
            .andExpect(status().isUnauthorized());
    }

    @Test 
    void shouldReturn401WithInvalidApiKeyForDeleteItem() throws Exception {
        mockMvc.perform(delete("/v1/items/item-1")
                .header("X-API-Key", "invalid-key"))
            .andExpect(status().isUnauthorized());
    }

    @Test 
    void shouldReturn401WithInvalidApiKeyForCreateItem() throws Exception {
        String json = "{\"name\":\"Desk\",\"category\":\"furniture\",\"basePrice\":250.0}";

        mockMvc.perform(post("/v1/items")
                .header("X-API-Key", "invalid-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isUnauthorized());
    }

    @Test 
    void shouldReturn401WithInvalidApiKeyForGetItem() throws Exception {
        mockMvc.perform(get("/v1/items/item-1")
                .header("X-API-Key", "invalid-key"))
            .andExpect(status().isUnauthorized());
    }
}