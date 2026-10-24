package com.cointrail.controller;

import com.cointrail.service.EditService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for editing and deleting records (operation 5).
 */
@RestController
@RequestMapping("/v1")
public final class EditController {

    /** The edit service. */
    private final EditService editService;

    /**
     * Constructs the controller.
     *
     * @param editService the edit service
     */
    public EditController(final EditService editService) {
        this.editService = editService;
    }

    /**
     * Edits an object, moving it to a new acquisition if needed.
     *
     * @param id the object ID
     * @return 501 until implemented
     */
    @PatchMapping("/objects/{id}")
    public ResponseEntity<Object> editObject(@PathVariable final String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Deletes an object and anything left without objects.
     *
     * @param id the object ID
     * @return 501 until implemented
     */
    @DeleteMapping("/objects/{id}")
    public ResponseEntity<Object> deleteObject(
        @PathVariable final String id
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Edits an acquisition and all of its objects.
     *
     * @param id the acquisition ID
     * @return 501 until implemented
     */
    @PatchMapping("/acquisitions/{id}")
    public ResponseEntity<Object> editAcquisition(
        @PathVariable final String id
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Renames a donor, merging it into an existing donor of that name.
     *
     * @param id the donor ID
     * @return 501 until implemented
     */
    @PatchMapping("/donors/{id}")
    public ResponseEntity<Object> editDonor(@PathVariable final String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Renames a museum, merging it into an existing museum of that name.
     *
     * @param id the museum code
     * @return 501 until implemented
     */
    @PatchMapping("/museums/{id}")
    public ResponseEntity<Object> editMuseum(@PathVariable final String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
