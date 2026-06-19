package com.beepollen.service;

import com.beepollen.dto.BeeColonyDTO;
import com.beepollen.dto.BeeColonyRequest;
import com.beepollen.entity.BeeColony;
import com.beepollen.entity.HealthStatus;
import com.beepollen.exception.DuplicateResourceException;
import com.beepollen.exception.ResourceNotFoundException;
import com.beepollen.repository.BeeColonyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service layer for BeeColony CRUD operations.
 * Handles business logic, validation, and entity-DTO mapping.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BeeColonyService {

    private final BeeColonyRepository beeColonyRepository;

    /**
     * Retrieves all bee colonies.
     *
     * @return list of all colonies as DTOs
     */
    @Transactional(readOnly = true)
    public Page<BeeColonyDTO> getAllColonies(Pageable pageable) {
        log.debug("Fetching all bee colonies");
        return beeColonyRepository.findAll(pageable).map(this::mapToDTO);
    }

    /**
     * Retrieves a single bee colony by its ID.
     *
     * @param id the colony ID
     * @return the colony DTO
     * @throws ResourceNotFoundException if the colony is not found
     */
    @Transactional(readOnly = true)
    public BeeColonyDTO getColonyById(Long id) {
        log.debug("Fetching bee colony with id: {}", id);
        BeeColony colony = beeColonyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BeeColony", "id", id));
        return mapToDTO(colony);
    }

    /**
     * Creates a new bee colony.
     *
     * @param request the colony creation request
     * @return the created colony DTO
     * @throws DuplicateResourceException if a colony with the same code already exists
     */
    public BeeColonyDTO createColony(BeeColonyRequest request) {
        log.info("Creating new bee colony with code: {}", request.getColonyCode());

        if (beeColonyRepository.existsByColonyCode(request.getColonyCode())) {
            throw new DuplicateResourceException("BeeColony", "colonyCode", request.getColonyCode());
        }

        BeeColony colony = new BeeColony();
        colony.setColonyCode(request.getColonyCode());
        colony.setBeeSpecies(request.getBeeSpecies());
        colony.setLatitude(request.getLatitude());
        colony.setLongitude(request.getLongitude());
        colony.setHealthStatus(parseHealthStatus(request.getHealthStatus()));
        colony.setEstimatedPopulation(request.getEstimatedPopulation());

        BeeColony savedColony = beeColonyRepository.save(colony);
        log.info("Successfully created bee colony with id: {}", savedColony.getId());

        return mapToDTO(savedColony);
    }

    /**
     * Updates an existing bee colony.
     *
     * @param id      the colony ID to update
     * @param request the colony update request
     * @return the updated colony DTO
     * @throws ResourceNotFoundException  if the colony is not found
     * @throws DuplicateResourceException if the new colony code already exists for another colony
     */
    public BeeColonyDTO updateColony(Long id, BeeColonyRequest request) {
        log.info("Updating bee colony with id: {}", id);

        BeeColony existingColony = beeColonyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BeeColony", "id", id));

        // Check for duplicate colony code if it has changed
        if (!existingColony.getColonyCode().equals(request.getColonyCode())
                && beeColonyRepository.existsByColonyCode(request.getColonyCode())) {
            throw new DuplicateResourceException("BeeColony", "colonyCode", request.getColonyCode());
        }

        existingColony.setColonyCode(request.getColonyCode());
        existingColony.setBeeSpecies(request.getBeeSpecies());
        existingColony.setLatitude(request.getLatitude());
        existingColony.setLongitude(request.getLongitude());
        existingColony.setHealthStatus(parseHealthStatus(request.getHealthStatus()));
        existingColony.setEstimatedPopulation(request.getEstimatedPopulation());

        BeeColony updatedColony = beeColonyRepository.save(existingColony);
        log.info("Successfully updated bee colony with id: {}", updatedColony.getId());

        return mapToDTO(updatedColony);
    }

    /**
     * Deletes a bee colony by its ID.
     *
     * @param id the colony ID to delete
     * @throws ResourceNotFoundException if the colony is not found
     */
    public void deleteColony(Long id) {
        log.info("Deleting bee colony with id: {}", id);

        BeeColony colony = beeColonyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BeeColony", "id", id));

        beeColonyRepository.deleteById(colony.getId());
        log.info("Successfully deleted bee colony with id: {}", id);
    }

    /**
     * Searches colonies by keyword. Returns all colonies if keyword is blank.
     *
     * @param keyword the search keyword
     * @return list of matching colonies as DTOs
     */
    @Transactional(readOnly = true)
    public Page<BeeColonyDTO> searchColonies(String keyword, Pageable pageable) {
        log.debug("Searching bee colonies with keyword: '{}'", keyword);

        if (keyword == null || keyword.isBlank()) {
            return getAllColonies(pageable);
        }

        return beeColonyRepository.searchByKeyword(keyword, pageable).map(this::mapToDTO);
    }

    /**
     * Retrieves colonies filtered by health status.
     *
     * @param status the health status string
     * @return list of colonies with the specified health status
     */
    @Transactional(readOnly = true)
    public List<BeeColonyDTO> getColoniesByHealthStatus(String status) {
        log.debug("Fetching bee colonies with health status: {}", status);

        HealthStatus healthStatus = parseHealthStatus(status);
        return beeColonyRepository.findByHealthStatus(healthStatus)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Maps a BeeColony entity to its DTO representation.
     *
     * @param colony the entity to map
     * @return the mapped DTO
     */
    private BeeColonyDTO mapToDTO(BeeColony colony) {
        return BeeColonyDTO.builder()
                .id(colony.getId())
                .colonyCode(colony.getColonyCode())
                .beeSpecies(colony.getBeeSpecies())
                .latitude(colony.getLatitude())
                .longitude(colony.getLongitude())
                .healthStatus(colony.getHealthStatus() != null ? colony.getHealthStatus().name() : null)
                .estimatedPopulation(colony.getEstimatedPopulation())
                .createdAt(colony.getCreatedAt())
                .updatedAt(colony.getUpdatedAt())
                .build();
    }

    /**
     * Parses a string to a HealthStatus enum value.
     *
     * @param status the status string
     * @return the parsed HealthStatus enum value
     * @throws IllegalArgumentException if the status string is invalid
     */
    private HealthStatus parseHealthStatus(String status) {
        if (status == null || status.isBlank()) {
            return HealthStatus.HEALTHY;
        }
        try {
            return HealthStatus.valueOf(status.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid health status value: '{}', defaulting to HEALTHY", status);
            return HealthStatus.HEALTHY;
        }
    }
}
