package com.cointrail;

import com.cointrail.model.SupportedResponse;
import com.cointrail.service.CointrailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest
@Import(TestConfig.class)
class CointrailServiceUnitTests {

    @Autowired
    private CointrailService service;

    @Autowired
    private LocalStorageService localStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        Files.writeString(tempDir.resolve("clients.json"),
            "[{\"id\":\"client-1\",\"name\":\"Alice\",\"apiKey\":\"valid-key\"}]");
        Files.writeString(tempDir.resolve("items.json"),
            "[{\"id\":\"item-1\",\"name\":\"Laptop\",\"category\":\"electronics\",\"basePrice\":999.99}]");
        Files.writeString(tempDir.resolve("taxrates.json"),
            "[{\"state\":\"CA\",\"category\":\"electronics\",\"rate\":0.0725},"
            + "{\"state\":\"NY\",\"category\":\"clothing\",\"rate\":0.04}]");

        localStorageService.setDirectory(tempDir);
    }

    // The `service` field above is the autowired bean under test.

    @Test
    void shouldReturnSeededStatesAndCategorieWhenFileIsValid() throws Exception {
        SupportedResponse response = service.getSupported();

        assertTrue(response.getStates().contains("CA"));
        assertTrue(response.getCategories().contains("electronics"));
    }

    @Test
    void shouldReturnIOExceptionWhenFileIsMissing() throws Exception {
        Files.deleteIfExists(tempDir.resolve("taxrates.json"));

        assertThrows(IOException.class, () -> service.getSupported());
    }

    @Test
    void shouldReturnEmptyStatesAndCategoriesWhenFilesAreEmpty() throws Exception {
        Files.writeString(tempDir.resolve("taxrates.json"), "[]");

        SupportedResponse response = service.getSupported();

        assertTrue(response.getStates().isEmpty());
        assertTrue(response.getCategories().isEmpty());
    }

    // test calculate tax with valid item id, with valid price and category 
    // - should return the tax of item, ignore price and category
    @Test
    void shouldReturnItemTaxWhenItemIDExists() throws Exception {
        String state = "CA";
        double price = 500.0;
        String category = "clothing";

        var request = new com.cointrail.model.TaxQuoteRequest(state, "item-1", price, category);
        var response = service.calculateTax(request);

        double actualPriceForItem = 999.99;
        assertTrue(response.getPrice() == actualPriceForItem);
        assertTrue(response.getTaxRate() == 0.0725);
        assertTrue(response.getTaxAmount() == actualPriceForItem * 0.0725);
        assertTrue(response.getTotal() == actualPriceForItem * 1.0725);
    }

    @Test 
    void shouldReturnTaxAmountForValidPriceAndCategoryandNullItemID() throws Exception {
        String state = "CA";
        double price = 100.0;
        String category = "electronics";

        var request = new com.cointrail.model.TaxQuoteRequest(state, null, price, category);
        var response = service.calculateTax(request);

        assertTrue(response.getPrice() == price);
        assertTrue(response.getTaxRate() == 0.0725);
        assertTrue(response.getTaxAmount() == price * 0.0725);
        assertTrue(response.getTotal() == price + (price * 0.0725));
    }

    @Test 
    void shouldReturnNullForValidPriceAndCategoryandInvalidItemID() throws Exception {
        String state = "NY";
        double price = 100.0;
        String category = "clothing";

        var request = new com.cointrail.model.TaxQuoteRequest(state, "invalid-item-id", price, category);
        var response = service.calculateTax(request);

        assertNull(response);
    }

    @Test 
    void shouldThrowExceptionForNullPriceAndCategoryandNullItemID() throws Exception {
        var request = new com.cointrail.model.TaxQuoteRequest("CA", null, null, null);

        assertThrows(IllegalArgumentException.class, () -> service.calculateTax(request));
    }

    @Test 
    void shouldReturnIllegalArgumentwhenStateisNull() throws Exception {
        double price = 100.0;
        String category = "clothing";

        var request = new com.cointrail.model.TaxQuoteRequest(null, "item-id", price, category);

        assertThrows(IllegalArgumentException.class, () -> service.calculateTax(request));
    }
   
        // try null id, with null price and category - should return null
        // try null id, with invalid price and valid category - should return null
        @Test
        void shouldThrowExceptionForNullItemIdWithInvalidPriceAndValidCategory() throws Exception {
            String state = "CA";
            double price = -50.0;
            String category = "electronics";

            var request = new com.cointrail.model.TaxQuoteRequest(state, null, price, category);

            assertThrows(IllegalArgumentException.class, () -> service.calculateTax(request));
        }
        
        @Test
        void shouldReturnNullForCalculateTaxWhenTaxRatesFileIsEmpty() throws Exception {
            Files.writeString(tempDir.resolve("taxrates.json"), "[]");

            String state = "CA";
            double price = 100.0;
            String category = "electronics";

            var request = new com.cointrail.model.TaxQuoteRequest(state, null, price, category);
            var response = service.calculateTax(request);

            assertNull(response);
        }
        
        @Test
        void shouldReturnNullForCalculateTaxWhenItemsFileIsEmptyAndItemIdProvided() throws Exception {
            Files.writeString(tempDir.resolve("items.json"), "[]");

            String state = "CA";

            var request = new com.cointrail.model.TaxQuoteRequest(state, "item-1", null, null);
            var response = service.calculateTax(request);

            assertNull(response);
        }

        @Test 
        void shouldCreateClientWithValidName() throws Exception {
            String name = "Bob";

            var client = service.createClient(name);

            assertTrue(client.getName().equals(name));
        }

        @Test 
        void shouldNotCreateClientWithExistingName() throws Exception {
            String name = "Alice";

            var client = service.createClient(name);

            assertNull(client);
        }

        @Test 
        void shouldThrowIllegalArgumentExceptionWithEmptyName() throws Exception {
            String name = "";

            assertThrows(IllegalArgumentException.class, () -> service.createClient(name));
        }

         @Test 
        void shouldThrowIllegalArgumentExceptionWithNullName() throws Exception {
            String name = null;

            assertThrows(IllegalArgumentException.class, () -> service.createClient(name));
        }

        
        @Test
        void shouldReturnTrueForValidApiKey() throws Exception {
            var client = service.createClient("Brandnew");

            assertTrue(service.validateApiKey(client.getApiKey()));
        }

        @Test
        void shouldReturnFalseForInvalidKey() throws Exception {
            assertFalse(service.validateApiKey("invalid-key"));
        }

        @Test
        void shouldReturnFalseForNullApiKey() throws Exception {
            assertFalse(service.validateApiKey(null));
        }

        @Test
        void shouldReturnItemWhenIdExists() throws Exception {
            var item = service.getItemById("item-1");

            assertTrue(item.getCategory().equals("electronics"));
            assertTrue(item.getBasePrice() == 999.99);
            assertTrue(item.getName().equals("Laptop"));
        }

        @Test
        void shouldReturnNullWhenItemIdDoesNotExist() throws Exception {
            var item = service.getItemById("invalid-id");

            assertNull(item);
        }

        @Test
        void shouldDeleteExistingItemAndReturnTrue() throws Exception {
            boolean deleted = service.deleteItem("item-1");

            assertTrue(deleted);
            assertNull(service.getItemById("item-1"));
        }

        @Test
        void shouldReturnFalseWhenDeletingNonexistentItem() throws Exception {
            boolean deleted = service.deleteItem("invalid-id");

            assertFalse(deleted);
            assertNotNull(service.getItemById("item-1"));
        }
        
        @Test
        void shouldCreateItemAndReturnWithGeneratedId() throws Exception {
            var item = service.createItem("Desk", "furniture", 250.0);

            assertNotNull(item.getId());
            assertTrue(item.getName().equals("Desk"));
            assertTrue(item.getCategory().equals("furniture"));
            assertTrue(item.getBasePrice() == 250.0);
        }

        @Test
        void shouldIncludeNewlyCreatedItemInGetItems() throws Exception {
            service.createItem("Desk", "furniture", 250.0);

            var items = service.getItems();

            assertTrue(items.size() == 2);
        }

        @Test
        void shouldReturnSeededItemsFromGetItems() throws Exception {
            var items = service.getItems();

            assertTrue(items.size() == 1);
            assertTrue(items.get(0).getId().equals("item-1"));
        }

        @Test 
        void shouldReturnMatchedCategoryItemsFromGetItems() throws Exception {
            var items = service.getItems("electronics", null);

            assertTrue(items.size() == 1);
            assertTrue(items.get(0).getId().equals("item-1"));
        }

        @Test 
        void shouldReturnMatchedNameItemsFromGetItems() throws Exception {
            var items = service.getItems(null, "PTO");

            assertTrue(items.size() == 1);
            assertTrue(items.get(0).getId().equals("item-1"));
        }

        @Test 
        void shouldReturnEmptyListForNonexistentCategoryAndNameFromGetItems() throws Exception {
            var items = service.getItems("none", "none");

            assert(items.isEmpty());
        }

        @Test 
        void shouldReturnEmptyListForNonexistentCategoryAndValidNameFromGetItems() throws Exception {
            var items = service.getItems("none", "PTO");

            assert(items.isEmpty());
        }

        @Test 
        void shouldReturnAllItemsForValidNameFromGetItems() throws Exception {
            service.createItem("Desktop", "furniture", 250.0);
            var items = service.getItems(null, "TOP");

            assertTrue(items.size() == 2);
        }

        @Test 
        void shouldReturnEditedItemFromPatchItem() throws Exception {
            var editedItem = service.patchItem("item-1", 666.66);

            assertTrue(editedItem.getBasePrice() == 666.66);
        }

        @Test 
        void shouldReturnIllegalArgumentFromPatchItem() throws Exception {
            assertThrows(IllegalArgumentException.class, () -> {
                service.patchItem("item-1", -100.0);
            });
        }

        @Test 
        void shouldReturnNullWithInvalidIdFromPatchItem() throws Exception {
            var editedItem = service.patchItem("invalid-id", 666.66);

            assertNull(editedItem);
        }

        @Test 
        void shouldReturnIllegalArgumentForBlankItemName() throws Exception {
            assertThrows(IllegalArgumentException.class, () -> {
                service.createItem("", "furniture", 250.0);
            });
        }

        @Test 
        void shouldReturnIllegalArgumentForBlankItemCategory() throws Exception {
            assertThrows(IllegalArgumentException.class, () -> {
                service.createItem("Desk", "", 250.0);
            });
        }

        @Test 
        void shouldReturnIllegalArgumentForNegativeItemPrice() throws Exception {
            assertThrows(IllegalArgumentException.class, () -> {
                service.createItem("Desk", "furniture", -250.0);
            });
        }

        @Test 
        void shouldReturnIllegalArgumentForNullItemName() throws Exception {
            assertThrows(IllegalArgumentException.class, () -> {
                service.createItem(null, "furniture", 250.0);
            });
        }
}
