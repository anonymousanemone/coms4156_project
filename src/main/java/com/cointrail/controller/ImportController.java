package com.cointrail.controller;

import com.cointrail.service.ImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for importing coin records (operation 1).
 */
@RestController
@RequestMapping("/v1/objects")
public final class ImportController {

    /** The import service. */
    private final ImportService importService;

    /**
     * Constructs the controller.
     *
     * @param importService the import service
     */
    public ImportController(final ImportService importService) {
        this.importService = importService;
    }

    /**
     * Imports a single object sent as JSON.
     *
     * @return 501 until implemented
     */
    @PostMapping
    public ResponseEntity<Object> importObject() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Imports a batch of objects from one museum sent as CSV.
     *
     * @return 501 until implemented
     */
    @PostMapping("/batch")
    public ResponseEntity<Object> importBatch() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
