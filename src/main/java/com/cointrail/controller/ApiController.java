package com.cointrail.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.cointrail.model.Client;
import com.cointrail.model.Item;
import com.cointrail.model.SupportedResponse;
import com.cointrail.model.TaxQuoteRequest;
import com.cointrail.model.TaxQuoteResponse;
import com.cointrail.service.CointrailService;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for the Cointrail API.
 */
@RestController
@RequestMapping("/v1")
public final class ApiController {

    /** The Cointrail API service. */
    private final CointrailService cointrailService;

    /**
     * Constructs an ApiController with the specified CointrailService.
     *
     * @param cointrailService the Cointrail API service
     */
    public ApiController(
        final CointrailService cointrailService
    ) {
        this.cointrailService = cointrailService;
    }

    /**
     * Creates a new client.
     *
     * @param client the client to create
     * @return the created client
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the name is null or empty
     */
    @PostMapping("/clients")
    public ResponseEntity<?> createClient(
        @RequestBody final Client client
    ) throws IOException, IllegalArgumentException {
        Client createdClient =
            cointrailService.createClient(
                client.getName()
            );
        if (createdClient == null) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(java.util.Map.of(
                    "error",
                    "A client with that name "
                        + "already exists"
                ));
        }
        return ResponseEntity.ok(createdClient);
    }

    /**
     * Creates a new item.
     *
     * @param apiKey the API key
     * @param item the item to create
     * @return the created item
     * @throws IOException if an I/O error occurs
     */
    @PostMapping("/items")
    public ResponseEntity<Item> createItem(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @RequestBody final Item item
    ) throws IOException {
        if (!cointrailService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        Item createdItem = cointrailService.createItem(
            item.getName(),
            item.getCategory(),
            item.getBasePrice()
        );
        return ResponseEntity.ok(createdItem);
    }

    /**
     * Gets mapping of all items.
     *
     * @param apiKey the API key
     * @param category the category of items to retrieve
     * @param q the name query
     * @return the list of items
     * @throws IOException if an I/O error occurs
     */
    @GetMapping("/items")
    public ResponseEntity<List<Item>> getItems(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @RequestParam(required = false)
        final String category,
        @RequestParam(required = false)
        final String q
    ) throws IOException {
        if (!cointrailService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        List<Item> items;
        if (q == null && category == null) {
            items = cointrailService.getItems();
        } else {
            items = cointrailService.getItems(category, q);
        }
        
        return ResponseEntity.ok(items);
    }

    /**
     * Gets an item by its ID.
     *
     * @param apiKey the API key
     * @param id the item ID
     * @return the item if found
     * @throws IOException if an I/O error occurs
     */
    @GetMapping("/items/{id}")
    public ResponseEntity<Item> getItemById(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @PathVariable final String id
    ) throws IOException {
        if (!cointrailService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        Item item = cointrailService.getItemById(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    /**
     * Patches an item by its ID.
     *
     * @param apiKey the API key
     * @param id the item ID
     * @param newPrice the new price
     * @return the updated item
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the new price is negative
     */
    @PatchMapping ("/items/{id}")
    public ResponseEntity<Item> patchItem(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @PathVariable final String id,
        @RequestBody final double newPrice
    ) throws IOException, IllegalArgumentException {
        if (!cointrailService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        Item editedItem = cointrailService.patchItem(id, newPrice);
        if (editedItem == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(editedItem);
    }

    /**
     * Deletes an item by its ID.
     *
     * @param apiKey the API key
     * @param id the item ID
     * @return a response indicating the result of the deletion
     * @throws IOException if an I/O error occurs
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @PathVariable final String id
    ) throws IOException {
        if (!cointrailService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        boolean deleted =
            cointrailService.deleteItem(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Calculates tax for a quote request.
     *
     * @param apiKey the API key
     * @param request the tax quote request
     * @return the tax quote response
     * @throws IOException if an I/O error occurs
     */
    @PostMapping("/tax/quote")
    public ResponseEntity<TaxQuoteResponse>
        calculateTax(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @RequestBody
        final TaxQuoteRequest request
    ) throws IOException {
        if (!cointrailService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        TaxQuoteResponse response =
            cointrailService.calculateTax(request);
        if (response == null) {
            return ResponseEntity
                .badRequest().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Gets the supported states and categories.
     *
     * @param apiKey the API key
     * @return the supported states and categories
     * @throws IOException if an I/O error occurs
     */
    @GetMapping("/supported")
    public ResponseEntity<SupportedResponse>
        getSupported(
        @RequestHeader("X-API-Key")
        final String apiKey
    ) throws IOException {
        if (!cointrailService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        SupportedResponse response =
            cointrailService.getSupported();
        return ResponseEntity.ok(response);
    }

}
