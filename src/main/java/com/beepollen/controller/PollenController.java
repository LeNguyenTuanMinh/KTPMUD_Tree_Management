package com.beepollen.controller;

import com.beepollen.dto.PollenDTO;
import com.beepollen.dto.PollenRequest;
import com.beepollen.service.PollenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Pollen CRUD operations.
 * Provides JSON API endpoints under /api/pollens.
 */
@RestController
@RequestMapping("/api/pollens")
@RequiredArgsConstructor
public class PollenController {

    private final PollenService pollenService;

    /**
     * GET /api/pollens
     * Lists all pollens, optionally filtered by a search keyword.
     *
     * @param keyword optional search keyword
     * @return list of pollens
     */
    @GetMapping
    public ResponseEntity<List<PollenDTO>> getAllPollens(
            @RequestParam(required = false) String keyword) {
        List<PollenDTO> pollens;
        if (keyword != null && !keyword.isBlank()) {
            pollens = pollenService.searchPollens(keyword, org.springframework.data.domain.Pageable.unpaged()).getContent();
        } else {
            pollens = pollenService.getAllPollens(org.springframework.data.domain.Pageable.unpaged()).getContent();
        }
        return ResponseEntity.ok(pollens);
    }

    /**
     * GET /api/pollens/{id}
     * Retrieves a single pollen by ID.
     *
     * @param id the pollen ID
     * @return the pollen DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<PollenDTO> getPollenById(@PathVariable Long id) {
        return ResponseEntity.ok(pollenService.getPollenById(id));
    }

    /**
     * POST /api/pollens
     * Creates a new pollen.
     *
     * @param request the validated pollen request body
     * @return the created pollen DTO with 201 status
     */
    @PostMapping
    public ResponseEntity<PollenDTO> createPollen(@Valid @RequestBody PollenRequest request) {
        PollenDTO created = pollenService.createPollen(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/pollens/{id}
     * Updates an existing pollen.
     *
     * @param id      the pollen ID
     * @param request the validated pollen request body
     * @return the updated pollen DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<PollenDTO> updatePollen(
            @PathVariable Long id,
            @Valid @RequestBody PollenRequest request) {
        return ResponseEntity.ok(pollenService.updatePollen(id, request));
    }

    /**
     * DELETE /api/pollens/{id}
     * Deletes a pollen by ID.
     *
     * @param id the pollen ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePollen(@PathVariable Long id) {
        pollenService.deletePollen(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/pollens/by-plant/{plantId}
     * Retrieves all pollens associated with a specific plant.
     *
     * @param plantId the plant ID
     * @return list of pollens associated with the plant
     */
    @GetMapping("/by-plant/{plantId}")
    public ResponseEntity<List<PollenDTO>> getPollensByPlantId(@PathVariable Long plantId) {
        return ResponseEntity.ok(pollenService.getPollensByPlantId(plantId));
    }
}
