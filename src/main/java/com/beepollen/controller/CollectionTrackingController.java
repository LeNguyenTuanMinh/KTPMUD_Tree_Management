package com.beepollen.controller;

import com.beepollen.dto.CollectionTrackingDTO;
import com.beepollen.dto.CollectionTrackingRequest;
import com.beepollen.service.CollectionTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API controller for Collection Tracking CRUD operations.
 */
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class CollectionTrackingController {

    private final CollectionTrackingService collectionTrackingService;

    /**
     * Lists all collection tracking records.
     *
     * @return list of all tracking DTOs
     */
    @GetMapping
    public ResponseEntity<List<CollectionTrackingDTO>> getAllTrackings() {
        List<CollectionTrackingDTO> trackings = collectionTrackingService.getAllTrackings(org.springframework.data.domain.Pageable.unpaged()).getContent();
        return ResponseEntity.ok(trackings);
    }

    /**
     * Retrieves a single tracking record by ID.
     *
     * @param id the tracking record ID
     * @return the tracking DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<CollectionTrackingDTO> getTrackingById(@PathVariable Long id) {
        CollectionTrackingDTO tracking = collectionTrackingService.getTrackingById(id);
        return ResponseEntity.ok(tracking);
    }

    /**
     * Creates a new collection tracking record.
     *
     * @param request the creation request payload
     * @return the created tracking DTO with HTTP 201
     */
    @PostMapping
    public ResponseEntity<CollectionTrackingDTO> createTracking(@Valid @RequestBody CollectionTrackingRequest request) {
        CollectionTrackingDTO created = collectionTrackingService.createTracking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing tracking record.
     *
     * @param id      the tracking record ID
     * @param request the update request payload
     * @return the updated tracking DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<CollectionTrackingDTO> updateTracking(
            @PathVariable Long id,
            @Valid @RequestBody CollectionTrackingRequest request) {
        CollectionTrackingDTO updated = collectionTrackingService.updateTracking(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a tracking record by ID.
     *
     * @param id the tracking record ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTracking(@PathVariable Long id) {
        collectionTrackingService.deleteTracking(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists tracking records filtered by colony ID.
     *
     * @param colonyId the colony ID
     * @return list of tracking DTOs for the specified colony
     */
    @GetMapping("/by-colony/{colonyId}")
    public ResponseEntity<List<CollectionTrackingDTO>> getTrackingsByColony(@PathVariable Long colonyId) {
        List<CollectionTrackingDTO> trackings = collectionTrackingService.getTrackingsByColonyId(colonyId);
        return ResponseEntity.ok(trackings);
    }

    /**
     * Lists tracking records filtered by pollen ID.
     *
     * @param pollenId the pollen ID
     * @return list of tracking DTOs for the specified pollen type
     */
    @GetMapping("/by-pollen/{pollenId}")
    public ResponseEntity<List<CollectionTrackingDTO>> getTrackingsByPollen(@PathVariable Long pollenId) {
        List<CollectionTrackingDTO> trackings = collectionTrackingService.getTrackingsByPollenId(pollenId);
        return ResponseEntity.ok(trackings);
    }
}
