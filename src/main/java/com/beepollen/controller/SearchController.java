package com.beepollen.controller;

import com.beepollen.dto.BeeColonyDTO;
import com.beepollen.dto.PlantDTO;
import com.beepollen.dto.PollenDTO;
import com.beepollen.service.BeeColonyService;
import com.beepollen.service.PlantService;
import com.beepollen.service.PollenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API controller providing cross-entity search functionality.
 * Delegates keyword-based searches to the respective service layers.
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final PlantService plantService;
    private final PollenService pollenService;
    private final BeeColonyService beeColonyService;

    /**
     * Searches plants by keyword.
     *
     * @param keyword the search keyword
     * @return list of matching plant DTOs
     */
    @GetMapping("/plants")
    public ResponseEntity<List<PlantDTO>> searchPlants(
            @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        List<PlantDTO> results = plantService.searchPlants(keyword, org.springframework.data.domain.Pageable.unpaged()).getContent();
        return ResponseEntity.ok(results);
    }

    /**
     * Searches pollens by keyword.
     *
     * @param keyword the search keyword
     * @return list of matching pollen DTOs
     */
    @GetMapping("/pollens")
    public ResponseEntity<List<PollenDTO>> searchPollens(
            @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        List<PollenDTO> results = pollenService.searchPollens(keyword, org.springframework.data.domain.Pageable.unpaged()).getContent();
        return ResponseEntity.ok(results);
    }

    /**
     * Searches bee colonies by keyword.
     *
     * @param keyword the search keyword
     * @return list of matching colony DTOs
     */
    @GetMapping("/colonies")
    public ResponseEntity<List<BeeColonyDTO>> searchColonies(
            @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        List<BeeColonyDTO> results = beeColonyService.searchColonies(keyword, org.springframework.data.domain.Pageable.unpaged()).getContent();
        return ResponseEntity.ok(results);
    }
}
