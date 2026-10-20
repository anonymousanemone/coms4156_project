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
import com.cointrail.model.Coin;
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
}
