package com.beepollen.controller;

import com.beepollen.dto.PlantDTO;
import com.beepollen.dto.PlantRequest;
import com.beepollen.service.PlantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API controller for Plant CRUD operations.
 * Provides JSON endpoints under /api/plants.
 */
@RestController
@RequestMapping("/api/plants")
@RequiredArgsConstructor
@Slf4j
public class PlantController {

    private final PlantService plantService;

    /**
     * GET /api/plants
     * Retrieves all plants, or searches by keyword if the "keyword" parameter is provided.
     *
     * @param keyword optional search keyword
     * @return list of plants
     */
    @GetMapping
    public ResponseEntity<List<PlantDTO>> getAllPlants(
            @RequestParam(value = "keyword", required = false) String keyword) {
        List<PlantDTO> plants;
        if (keyword != null && !keyword.isBlank()) {
            log.debug("REST: Searching plants with keyword: {}", keyword);
            plants = plantService.searchPlants(keyword, org.springframework.data.domain.Pageable.unpaged()).getContent();
        } else {
            log.debug("REST: Fetching all plants");
            plants = plantService.getAllPlants(org.springframework.data.domain.Pageable.unpaged()).getContent();
        }
        return ResponseEntity.ok(plants);
    }

    /**
     * GET /api/plants/{id}
     * Retrieves a single plant by its ID.
     *
     * @param id the plant ID
     * @return the plant
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlantDTO> getPlantById(@PathVariable Long id) {
        log.debug("REST: Fetching plant with id: {}", id);
        PlantDTO plant = plantService.getPlantById(id);
        return ResponseEntity.ok(plant);
    }

    /**
     * POST /api/plants
     * Creates a new plant.
     *
     * @param request the plant creation request
     * @return the created plant with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<PlantDTO> createPlant(@Valid @RequestBody PlantRequest request) {
        log.debug("REST: Creating plant with scientific name: {}", request.getScientificName());
        PlantDTO createdPlant = plantService.createPlant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlant);
    }

    /**
     * PUT /api/plants/{id}
     * Updates an existing plant.
     *
     * @param id      the plant ID
     * @param request the plant update request
     * @return the updated plant
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlantDTO> updatePlant(
            @PathVariable Long id,
            @Valid @RequestBody PlantRequest request) {
        log.debug("REST: Updating plant with id: {}", id);
        PlantDTO updatedPlant = plantService.updatePlant(id, request);
        return ResponseEntity.ok(updatedPlant);
    }

    /**
     * DELETE /api/plants/{id}
     * Deletes a plant by its ID.
     *
     * @param id the plant ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlant(@PathVariable Long id) {
        log.debug("REST: Deleting plant with id: {}", id);
        plantService.deletePlant(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/plants/by-pollen/{pollenId}
     * Retrieves all plants associated with a specific pollen ID.
     *
     * @param pollenId the pollen ID
     * @return list of plants associated with the pollen
     */
    @GetMapping("/by-pollen/{pollenId}")
    public ResponseEntity<List<PlantDTO>> getPlantsByPollenId(@PathVariable Long pollenId) {
        log.debug("REST: Fetching plants by pollen id: {}", pollenId);
        List<PlantDTO> plants = plantService.getPlantsByPollenId(pollenId);
        return ResponseEntity.ok(plants);
    }
}
