package com.cointrail.service;

import com.cointrail.model.Acquisition;
import com.cointrail.model.Client;
import com.cointrail.model.Coin;
import com.cointrail.model.Donor;
import com.cointrail.model.Museum;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Service for Cointrail API business logic.
 */
@Service
public final class CointrailService {

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
}
