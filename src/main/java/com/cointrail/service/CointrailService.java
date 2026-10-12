package com.cointrail.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cointrail.model.Client;
import com.cointrail.model.Item;
import com.cointrail.model.SupportedResponse;
import com.cointrail.model.TaxQuoteRequest;
import com.cointrail.model.TaxQuoteResponse;
import com.cointrail.model.TaxRate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Service for Cointrail API business logic.
 */
@Service
public final class CointrailService {

    /** The object mapper. */
    private final ObjectMapper objectMapper =
        new ObjectMapper();

    /** The storage service. */
    private final StorageService storageService;

    /**
     * Constructs the service.
     *
     * @param storageService the storage service
     */
    public CointrailService(
        final StorageService storageService
    ) {
        this.storageService = storageService;
    }

    /**
     * Reads a list from a JSON file.
     *
     * @param <T> the element type
     * @param fileName the file name
     * @param typeRef the type reference
     * @return the list
     * @throws IOException if an I/O error occurs
     */
    private <T> List<T> readList(
        final String fileName,
        final TypeReference<List<T>> typeRef
    ) throws IOException {
        String json =
            storageService.readFile(fileName);
        return objectMapper.readValue(json, typeRef);
    }

    /**
     * Writes a list to a JSON file.
     *
     * @param <T> the element type
     * @param fileName the file name
     * @param list the list to write
     * @throws IOException if an I/O error occurs
     */
    private <T> void writeList(
        final String fileName,
        final List<T> list
    ) throws IOException {
        String json = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(list);
        storageService.writeFile(fileName, json);
    }

    /**
     * Creates a new client.
     *
     * @param name the client name
     * @return the created client, or null if a client with that
     *      name already exists
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the name is null or empty
     */
    public Client createClient(
        final String name
    ) throws IOException, IllegalArgumentException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Client name cannot be null or empty");
        }

        List<Client> clients = readList(
            "clients.json",
            new TypeReference<>() { }
        );

        boolean nameExists = clients.stream()
            .anyMatch(c ->
                c.getName().equalsIgnoreCase(name)
            );
        if (nameExists) {
            return null;
        }

        Client newClient = new Client(
            UUID.randomUUID().toString(),
            name,
            UUID.randomUUID().toString()
        );

        clients.add(newClient);
        writeList("clients.json", clients);

        return newClient;
    }

    /**
     * Validates an API key.
     *
     * @param apiKey the API key
     * @return true if valid
     * @throws IOException if an I/O error occurs
     */
    public boolean validateApiKey(
        final String apiKey
    ) throws IOException {
        if (apiKey == null) {
            return false;
        }
        List<Client> clients = readList(
            "clients.json",
            new TypeReference<>() { }
        );

        return clients.stream()
            .anyMatch(client ->
                client.getApiKey().equals(apiKey)
            );
    }

    /**
     * Creates a new item.
     *
     * @param name the item name
     * @param category the item category
     * @param basePrice the base price
     * @return the created item
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the name or category is null/empty
     *      or basePrice is negative
     */
    public Item createItem(
        final String name,
        final String category,
        final double basePrice
    ) throws IOException, IllegalArgumentException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Item name cannot be null or empty");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Item category cannot be null or empty");
        }
        if (basePrice < 0) {
            throw new IllegalArgumentException(
                "Base price cannot be negative");
        }
        List<Item> items = readList(
            "items.json",
            new TypeReference<>() { }
        );

        Item newItem = new Item(
            UUID.randomUUID().toString(),
            name,
            category,
            basePrice
        );

        items.add(newItem);
        writeList("items.json", items);

        return newItem;
    }

    /**
     * Gets all items.
     *
     * @return all items
     * @throws IOException if an I/O error occurs
     */
    public List<Item> getItems() throws IOException {
        return readList(
            "items.json",
            new TypeReference<>() { }
        );
    }

    /**
     * Gets all items matching category or name substring.
     *
     * @param category the category of items to retrieve
     * @param q the name query
     * @return items
     * @throws IOException if an I/O error occurs
     */
    public List<Item> getItems(
        final String category,
        final String q
    ) throws IOException {
        List<Item> items = readList(
            "items.json",
            new TypeReference<>() { }
        );

        return items.stream()
            .filter(item ->
                category == null 
                || item.getCategory().equalsIgnoreCase(category) 
            ).filter(item ->
                q == null 
                || item.getName().toLowerCase().contains(q.toLowerCase())
            )
            .toList();
    }

    /**
     * Gets an item by ID.
     *
     * @param id the item ID
     * @return the item or null
     * @throws IOException if an I/O error occurs
     */
    public Item getItemById(
        final String id
    ) throws IOException {
        List<Item> items = readList(
            "items.json",
            new TypeReference<>() { }
        );

        return items.stream()
            .filter(item ->
                item.getId().equals(id)
            )
            .findFirst()
            .orElse(null);
    }

    /**
     * Patches an existing item.
     *
     * @param id the item ID
     * @param newPrice the new price
     * @return the updated item or null if not found
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the new price is negative
     */
    public Item patchItem(
        final String id,
        final double newPrice
    ) throws IOException, IllegalArgumentException {
        if (newPrice < 0) {
            throw new IllegalArgumentException(
                "New price cannot be negative");
        }

        List<Item> items = readList(
            "items.json",
            new TypeReference<>() { }
        );
        
        Item editedItem = items.stream()
            .filter(item ->
                item.getId().equals(id)
            )
            .findFirst()
            .orElse(null);
        if (editedItem == null) {
            return null;
        }

        editedItem.setBasePrice(newPrice);
        writeList("items.json", items);
        
        return editedItem;
    }

    /**
     * Deletes an item by ID.
     *
     * @param id the item ID
     * @return true if deleted
     * @throws IOException if an I/O error occurs
     */
    public boolean deleteItem(
        final String id
    ) throws IOException {
        List<Item> items = readList(
            "items.json",
            new TypeReference<>() { }
        );

        boolean removed = items.removeIf(item ->
            item.getId().equals(id)
        );

        if (removed) {
            writeList("items.json", items);
        }

        return removed;
    }

    /**
     * Calculates tax for a request.
     *
     * @param request the tax quote request
     * @return the tax quote response, or null if the item ID does not
     *      exist or no tax rate matches the state/category
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the state is null/empty, the
     *      category is empty, the price is negative, or neither an item
     *      ID nor both price and category are provided
     */
    public TaxQuoteResponse calculateTax(
        final TaxQuoteRequest request
    ) throws IOException, IllegalArgumentException {
        double price;
        String category;

        String itemId = request.getItemId();
        String state = request.getState();
        if (state == null || state.trim().isEmpty()) {
            throw new IllegalArgumentException("State cannot be null or empty");
        }

        if (itemId != null && !itemId.trim().isEmpty()) {
            Item item =
                getItemById(itemId);
            if (item == null) {
                return null;
            }
            price = item.getBasePrice();
            category = item.getCategory();

        } else {
            if (request.getPrice() == null || request.getCategory() == null) {
                throw new IllegalArgumentException(
                "Price and category cannot be null when item ID is not provided"
                );
            }
            price = request.getPrice();
            category = request.getCategory();
        }

        if (category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }

        if (price < 0) {
            throw new IllegalArgumentException(
                "Price cannot be negative"
            );
        }

        List<TaxRate> taxRates = readList(
            "taxrates.json",
            new TypeReference<>() { }
        );

        TaxRate taxRate = taxRates.stream()
            .filter(tr ->
                tr.getState().equalsIgnoreCase(
                    request.getState()
                )
            )
            .filter(tr ->
                tr.getCategory()
                    .equalsIgnoreCase(category)
            )
            .findFirst()
            .orElse(null);

        if (taxRate == null) {
            return null;
        }

        double rate = taxRate.getRate();
        double taxAmount = price * rate;
        double total = price + taxAmount;

        return new TaxQuoteResponse(
            price, rate, taxAmount, total
        );
    }

    /**
     * Gets supported states and categories.
     *
     * @return the supported response
     * @throws IOException if an I/O error occurs
     */
    public SupportedResponse getSupported()
        throws IOException {
        List<TaxRate> taxRates = readList(
            "taxrates.json",
            new TypeReference<>() { }
        );

        List<String> states = taxRates.stream()
            .map(TaxRate::getState)
            .distinct()
            .toList();

        List<String> categories = taxRates.stream()
            .map(TaxRate::getCategory)
            .distinct()
            .toList();

        return new SupportedResponse(
            states, categories
        );
    }
}
