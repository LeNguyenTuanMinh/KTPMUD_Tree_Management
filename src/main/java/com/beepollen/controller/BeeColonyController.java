package com.beepollen.controller;

import com.beepollen.dto.BeeColonyDTO;
import com.beepollen.dto.BeeColonyRequest;
import com.beepollen.service.BeeColonyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for BeeColony CRUD operations.
 * Provides JSON endpoints for programmatic access to colony data.
 */
@RestController
@RequestMapping("/api/colonies")
@RequiredArgsConstructor
public class BeeColonyController {

    private final BeeColonyService beeColonyService;

    /**
     * Retrieves all colonies, optionally filtered by keyword.
     *
     * @param keyword optional search keyword
     * @return list of colonies
     */
    @GetMapping
    public ResponseEntity<List<BeeColonyDTO>> getAllColonies(
            @RequestParam(value = "keyword", required = false) String keyword) {
        List<BeeColonyDTO> colonies;
        if (keyword != null && !keyword.isBlank()) {
            colonies = beeColonyService.searchColonies(keyword, org.springframework.data.domain.Pageable.unpaged()).getContent();
        } else {
            colonies = beeColonyService.getAllColonies(org.springframework.data.domain.Pageable.unpaged()).getContent();
        }
        return ResponseEntity.ok(colonies);
    }

    /**
     * Retrieves a single colony by ID.
     *
     * @param id the colony ID
     * @return the colony DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<BeeColonyDTO> getColonyById(@PathVariable Long id) {
        BeeColonyDTO colony = beeColonyService.getColonyById(id);
        return ResponseEntity.ok(colony);
    }

    /**
     * Creates a new colony.
     *
     * @param request the colony creation request
     * @return the created colony DTO with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<BeeColonyDTO> createColony(@Valid @RequestBody BeeColonyRequest request) {
        BeeColonyDTO createdColony = beeColonyService.createColony(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdColony);
    }

    /**
     * Updates an existing colony.
     *
     * @param id      the colony ID to update
     * @param request the colony update request
     * @return the updated colony DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<BeeColonyDTO> updateColony(
            @PathVariable Long id,
            @Valid @RequestBody BeeColonyRequest request) {
        BeeColonyDTO updatedColony = beeColonyService.updateColony(id, request);
        return ResponseEntity.ok(updatedColony);
    }

    /**
     * Deletes a colony by ID.
     *
     * @param id the colony ID to delete
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteColony(@PathVariable Long id) {
        beeColonyService.deleteColony(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves colonies filtered by health status.
     *
     * @param status the health status string (HEALTHY, WEAK, CRITICAL, DEAD)
     * @return list of colonies with the specified health status
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<BeeColonyDTO>> getColoniesByHealthStatus(@PathVariable String status) {
        List<BeeColonyDTO> colonies = beeColonyService.getColoniesByHealthStatus(status);
        return ResponseEntity.ok(colonies);
    }
}
