package com.beepollen.service;

import com.beepollen.dto.PollenDTO;
import com.beepollen.dto.PollenRequest;
import com.beepollen.entity.Plant;
import com.beepollen.entity.Pollen;
import com.beepollen.exception.ResourceNotFoundException;
import com.beepollen.repository.PlantRepository;
import com.beepollen.repository.PollenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service layer for Pollen CRUD operations.
 * Handles business logic, entity-DTO mapping, and manages
 * the bidirectional Many-to-Many relationship with Plant entities.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PollenService {

    private final PollenRepository pollenRepository;
    private final PlantRepository plantRepository;

    /**
     * Retrieves all pollens and converts them to DTOs.
     *
     * @return list of all pollens as DTOs
     */
    @Transactional(readOnly = true)
    public Page<PollenDTO> getAllPollens(Pageable pageable) {
        log.debug("Fetching all pollens");
        return pollenRepository.findAll(pageable).map(this::mapToDTO);
    }

    /**
     * Retrieves a single pollen by its ID.
     *
     * @param id the pollen ID
     * @return the pollen as a DTO
     * @throws ResourceNotFoundException if the pollen is not found
     */
    @Transactional(readOnly = true)
    public PollenDTO getPollenById(Long id) {
        log.debug("Fetching pollen with id: {}", id);
        Pollen pollen = pollenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pollen", "id", id));
        return mapToDTO(pollen);
    }

    /**
     * Creates a new pollen entity from the given request.
     * If plantIds are provided, associates the pollen with those plants
     * by adding it to each plant's pollens set (Plant is the owning side of M:N).
     *
     * @param request the pollen creation request
     * @return the created pollen as a DTO
     */
    public PollenDTO createPollen(PollenRequest request) {
        log.info("Creating new pollen with name: {}", request.getName());

        Pollen pollen = new Pollen();
        pollen.setName(request.getName());
        pollen.setShape(request.getShape());
        pollen.setSizeMicron(request.getSizeMicron());
        pollen.setSurfaceCharacteristic(request.getSurfaceCharacteristic());
        pollen.setMicroscopeImage(request.getMicroscopeImage());

        // Save the pollen first to get a managed entity with an ID
        Pollen savedPollen = pollenRepository.save(pollen);

        // Associate with plants if plantIds are provided
        if (request.getPlantIds() != null && !request.getPlantIds().isEmpty()) {
            for (Long plantId : request.getPlantIds()) {
                Plant plant = plantRepository.findById(plantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Plant", "id", plantId));
                plant.getPollens().add(savedPollen);
                plantRepository.save(plant);
            }
        }

        log.info("Pollen created successfully with id: {}", savedPollen.getId());
        return mapToDTO(savedPollen);
    }

    /**
     * Updates an existing pollen entity.
     * Handles the plant association update by:
     * 1. Removing the pollen from all previously associated plants' pollens sets
     * 2. Adding the pollen to the newly specified plants' pollens sets
     *
     * @param id      the ID of the pollen to update
     * @param request the pollen update request
     * @return the updated pollen as a DTO
     * @throws ResourceNotFoundException if the pollen is not found
     */
    public PollenDTO updatePollen(Long id, PollenRequest request) {
        log.info("Updating pollen with id: {}", id);

        Pollen pollen = pollenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pollen", "id", id));

        // Update basic fields
        pollen.setName(request.getName());
        pollen.setShape(request.getShape());
        pollen.setSizeMicron(request.getSizeMicron());
        pollen.setSurfaceCharacteristic(request.getSurfaceCharacteristic());
        pollen.setMicroscopeImage(request.getMicroscopeImage());

        // Handle plant associations update:
        // Step 1 - Remove this pollen from all plants that previously had it
        Set<Plant> currentPlants = new HashSet<>(pollen.getPlants());
        for (Plant plant : currentPlants) {
            plant.getPollens().remove(pollen);
            plantRepository.save(plant);
        }

        // Step 2 - Add this pollen to the newly specified plants
        if (request.getPlantIds() != null && !request.getPlantIds().isEmpty()) {
            for (Long plantId : request.getPlantIds()) {
                Plant plant = plantRepository.findById(plantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Plant", "id", plantId));
                plant.getPollens().add(pollen);
                plantRepository.save(plant);
            }
        }

        Pollen updatedPollen = pollenRepository.save(pollen);
        log.info("Pollen updated successfully with id: {}", updatedPollen.getId());
        return mapToDTO(updatedPollen);
    }

    /**
     * Deletes a pollen by its ID.
     * First removes the pollen from all associated plants' pollens sets
     * to maintain referential integrity.
     *
     * @param id the ID of the pollen to delete
     * @throws ResourceNotFoundException if the pollen is not found
     */
    public void deletePollen(Long id) {
        log.info("Deleting pollen with id: {}", id);

        Pollen pollen = pollenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pollen", "id", id));

        // Remove pollen from all associated plants' pollens sets first
        Set<Plant> associatedPlants = new HashSet<>(pollen.getPlants());
        for (Plant plant : associatedPlants) {
            plant.getPollens().remove(pollen);
            plantRepository.save(plant);
        }

        pollenRepository.deleteById(id);
        log.info("Pollen deleted successfully with id: {}", id);
    }

    /**
     * Searches pollens by a keyword. Returns all pollens if the keyword is blank.
     *
     * @param keyword the search keyword
     * @return list of matching pollens as DTOs
     */
    @Transactional(readOnly = true)
    public Page<PollenDTO> searchPollens(String keyword, Pageable pageable) {
        log.debug("Searching pollens with keyword: {}", keyword);
        if (keyword == null || keyword.isBlank()) {
            return getAllPollens(pageable);
        }
        return pollenRepository.searchByKeyword(keyword, pageable).map(this::mapToDTO);
    }

    /**
     * Retrieves all pollens associated with a specific plant.
     *
     * @param plantId the plant ID
     * @return list of pollens associated with the plant as DTOs
     */
    @Transactional(readOnly = true)
    public List<PollenDTO> getPollensByPlantId(Long plantId) {
        log.debug("Fetching pollens for plant id: {}", plantId);
        return pollenRepository.findByPlantId(plantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Maps a Pollen entity to a PollenDTO.
     *
     * @param pollen the Pollen entity
     * @return the mapped PollenDTO
     */
    private PollenDTO mapToDTO(Pollen pollen) {
        PollenDTO dto = new PollenDTO();
        dto.setId(pollen.getId());
        dto.setName(pollen.getName());
        dto.setShape(pollen.getShape());
        dto.setSizeMicron(pollen.getSizeMicron());
        dto.setSurfaceCharacteristic(pollen.getSurfaceCharacteristic());
        dto.setMicroscopeImage(pollen.getMicroscopeImage());
        dto.setCreatedAt(pollen.getCreatedAt());
        dto.setUpdatedAt(pollen.getUpdatedAt());
        return dto;
    }
}
