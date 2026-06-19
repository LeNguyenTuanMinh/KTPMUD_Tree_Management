package com.beepollen.service;

import com.beepollen.dto.PlantDTO;
import com.beepollen.dto.PlantRequest;
import com.beepollen.dto.PollenDTO;
import com.beepollen.entity.Plant;
import com.beepollen.entity.Pollen;
import com.beepollen.exception.DuplicateResourceException;
import com.beepollen.exception.ResourceNotFoundException;
import com.beepollen.repository.PlantRepository;
import com.beepollen.repository.PollenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service layer for Plant CRUD operations.
 * Handles business logic, entity-DTO mapping, and coordination
 * between PlantRepository and PollenRepository.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PlantService {

    private final PlantRepository plantRepository;
    private final PollenRepository pollenRepository;

    /**
     * Retrieves all plants and converts them to DTOs.
     *
     * @return list of all plants as DTOs
     */
    @Transactional(readOnly = true)
    public Page<PlantDTO> getAllPlants(Pageable pageable) {
        log.debug("Fetching all plants");
        return plantRepository.findAll(pageable).map(this::mapToDTO);
    }

    /**
     * Retrieves a single plant by its ID.
     *
     * @param id the plant ID
     * @return the plant as a DTO including associated pollens
     * @throws ResourceNotFoundException if the plant is not found
     */
    @Transactional(readOnly = true)
    public PlantDTO getPlantById(Long id) {
        log.debug("Fetching plant with id: {}", id);
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant", "id", id));
        return mapToDTO(plant);
    }

    /**
     * Creates a new plant from the given request.
     * Checks for duplicate scientific names before persisting.
     *
     * @param request the plant creation request
     * @return the newly created plant as a DTO
     * @throws DuplicateResourceException if a plant with the same scientific name already exists
     */
    public PlantDTO createPlant(PlantRequest request) {
        log.debug("Creating plant with scientific name: {}", request.getScientificName());

        // Check for duplicate scientific name
        Optional<Plant> existing = plantRepository.findByScientificName(request.getScientificName());
        if (existing.isPresent()) {
            throw new DuplicateResourceException("Plant", "scientificName", request.getScientificName());
        }

        Plant plant = new Plant();
        plant.setCommonName(request.getCommonName());
        plant.setScientificName(request.getScientificName());
        plant.setFamily(request.getFamily());
        plant.setGenus(request.getGenus());
        plant.setFloweringSeason(request.getFloweringSeason());
        plant.setRegion(request.getRegion());
        plant.setDescription(request.getDescription());
        plant.setImageUrl(request.getImageUrl());

        // Associate pollens if IDs are provided
        if (request.getPollenIds() != null && !request.getPollenIds().isEmpty()) {
            Set<Pollen> pollens = resolvePollensByIds(request.getPollenIds());
            plant.setPollens(pollens);
        }

        Plant savedPlant = plantRepository.save(plant);
        log.info("Created plant with id: {}", savedPlant.getId());
        return mapToDTO(savedPlant);
    }

    /**
     * Updates an existing plant with the given request data.
     * Validates that the plant exists and checks for duplicate scientific names
     * if the scientific name has changed.
     *
     * @param id      the ID of the plant to update
     * @param request the plant update request
     * @return the updated plant as a DTO
     * @throws ResourceNotFoundException  if the plant is not found
     * @throws DuplicateResourceException if the new scientific name already exists for another plant
     */
    public PlantDTO updatePlant(Long id, PlantRequest request) {
        log.debug("Updating plant with id: {}", id);

        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant", "id", id));

        // Check for duplicate scientific name if it has changed
        if (!plant.getScientificName().equals(request.getScientificName())) {
            Optional<Plant> existing = plantRepository.findByScientificName(request.getScientificName());
            if (existing.isPresent()) {
                throw new DuplicateResourceException("Plant", "scientificName", request.getScientificName());
            }
        }

        // Update all fields from request
        plant.setCommonName(request.getCommonName());
        plant.setScientificName(request.getScientificName());
        plant.setFamily(request.getFamily());
        plant.setGenus(request.getGenus());
        plant.setFloweringSeason(request.getFloweringSeason());
        plant.setRegion(request.getRegion());
        plant.setDescription(request.getDescription());
        plant.setImageUrl(request.getImageUrl());

        // Clear and re-add pollens if pollenIds are provided
        if (request.getPollenIds() != null) {
            plant.getPollens().clear();
            if (!request.getPollenIds().isEmpty()) {
                Set<Pollen> pollens = resolvePollensByIds(request.getPollenIds());
                plant.getPollens().addAll(pollens);
            }
        }

        Plant savedPlant = plantRepository.save(plant);
        log.info("Updated plant with id: {}", savedPlant.getId());
        return mapToDTO(savedPlant);
    }

    /**
     * Deletes a plant by its ID.
     *
     * @param id the ID of the plant to delete
     * @throws ResourceNotFoundException if the plant is not found
     */
    public void deletePlant(Long id) {
        log.debug("Deleting plant with id: {}", id);
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant", "id", id));
        plantRepository.deleteById(plant.getId());
        log.info("Deleted plant with id: {}", id);
    }

    /**
     * Searches for plants by keyword. Returns all plants if the keyword is blank.
     *
     * @param keyword the search keyword
     * @return list of matching plants as DTOs
     */
    @Transactional(readOnly = true)
    public Page<PlantDTO> searchPlants(String keyword, Pageable pageable) {
        log.debug("Searching plants with keyword: {}", keyword);
        if (keyword == null || keyword.isBlank()) {
            return getAllPlants(pageable);
        }
        return plantRepository.searchByKeyword(keyword, pageable).map(this::mapToDTO);
    }

    /**
     * Retrieves all plants associated with a specific pollen ID.
     *
     * @param pollenId the pollen ID
     * @return list of plants associated with the given pollen as DTOs
     */
    @Transactional(readOnly = true)
    public List<PlantDTO> getPlantsByPollenId(Long pollenId) {
        log.debug("Fetching plants by pollen id: {}", pollenId);
        return plantRepository.findByPollenId(pollenId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    /**
     * Resolves a list of pollen IDs to a set of Pollen entities.
     *
     * @param pollenIds list of pollen IDs
     * @return set of resolved Pollen entities
     * @throws ResourceNotFoundException if any pollen ID is not found
     */
    private Set<Pollen> resolvePollensByIds(List<Long> pollenIds) {
        Set<Pollen> pollens = new HashSet<>();
        for (Long pollenId : pollenIds) {
            Pollen pollen = pollenRepository.findById(pollenId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pollen", "id", pollenId));
            pollens.add(pollen);
        }
        return pollens;
    }

    /**
     * Maps a Plant entity to a PlantDTO, including associated pollens.
     *
     * @param plant the Plant entity
     * @return the mapped PlantDTO
     */
    private PlantDTO mapToDTO(Plant plant) {
        PlantDTO dto = PlantDTO.builder()
                .id(plant.getId())
                .commonName(plant.getCommonName())
                .scientificName(plant.getScientificName())
                .family(plant.getFamily())
                .genus(plant.getGenus())
                .floweringSeason(plant.getFloweringSeason())
                .region(plant.getRegion())
                .description(plant.getDescription())
                .imageUrl(plant.getImageUrl())
                .createdAt(plant.getCreatedAt())
                .updatedAt(plant.getUpdatedAt())
                .build();

        if (plant.getPollens() != null) {
            List<PollenDTO> pollenDTOs = plant.getPollens()
                    .stream()
                    .map(this::mapPollenToDTO)
                    .collect(Collectors.toList());
            dto.setPollens(pollenDTOs);
        }

        return dto;
    }

    /**
     * Maps a Pollen entity to a PollenDTO.
     *
     * @param pollen the Pollen entity
     * @return the mapped PollenDTO
     */
    private PollenDTO mapPollenToDTO(Pollen pollen) {
        return PollenDTO.builder()
                .id(pollen.getId())
                .name(pollen.getName())
                .shape(pollen.getShape())
                .sizeMicron(pollen.getSizeMicron())
                .surfaceCharacteristic(pollen.getSurfaceCharacteristic())
                .microscopeImage(pollen.getMicroscopeImage())
                .build();
    }
}
