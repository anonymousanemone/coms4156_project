package com.cointrail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.cointrail.service.CointrailService;
import com.cointrail.service.StorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CointrailServiceUnitTests {

    private StorageService storageService;

    private CointrailService service;

    @BeforeEach
    void setUp() {
        storageService = mock(StorageService.class);
        service = new CointrailService(storageService);
    }

    @Test
    void shouldConstructWithStorageService() {
        assertNotNull(service);
    }
}
