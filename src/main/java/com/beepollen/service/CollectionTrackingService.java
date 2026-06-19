package com.beepollen.service;

import com.beepollen.dto.CollectionTrackingDTO;
import com.beepollen.dto.CollectionTrackingRequest;
import com.beepollen.dto.HarvestReportDTO;
import com.beepollen.entity.BeeColony;
import com.beepollen.entity.CollectionTracking;
import com.beepollen.entity.Pollen;
import com.beepollen.exception.ResourceNotFoundException;
import com.beepollen.repository.BeeColonyRepository;
import com.beepollen.repository.CollectionTrackingRepository;
import com.beepollen.repository.PollenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service layer for Collection Tracking CRUD operations.
 * Handles entity-to-DTO mapping with denormalized colony/pollen fields.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CollectionTrackingService {

    private final CollectionTrackingRepository collectionTrackingRepository;
    private final BeeColonyRepository beeColonyRepository;
    private final PollenRepository pollenRepository;

    /**
     * Retrieves all collection tracking records.
     *
     * @return list of all tracking DTOs
     */
    @Transactional(readOnly = true)
    public Page<CollectionTrackingDTO> getAllTrackings(Pageable pageable) {
        log.debug("Fetching all collection tracking records");
        return collectionTrackingRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CollectionTrackingDTO> searchTrackings(String keyword, Pageable pageable) {
        log.debug("Searching collection trackings with keyword: '{}'", keyword);
        if (keyword == null || keyword.isBlank()) {
            return getAllTrackings(pageable);
        }
        return collectionTrackingRepository.searchByKeyword(keyword, pageable).map(this::mapToDTO);
    }

    /**
     * Retrieves a single tracking record by ID.
     *
     * @param id the tracking record ID
     * @return the tracking DTO
     * @throws ResourceNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public CollectionTrackingDTO getTrackingById(Long id) {
        log.debug("Fetching collection tracking with id: {}", id);
        CollectionTracking tracking = collectionTrackingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CollectionTracking", "id", id));
        return mapToDTO(tracking);
    }

    /**
     * Creates a new collection tracking record.
     *
     * @param request the creation request
     * @return the created tracking DTO
     * @throws ResourceNotFoundException if colony or pollen not found
     */
    public CollectionTrackingDTO createTracking(CollectionTrackingRequest request) {
        log.info("Creating new collection tracking for colony id: {} and pollen id: {}",
                request.getColonyId(), request.getPollenId());

        BeeColony colony = beeColonyRepository.findById(request.getColonyId())
                .orElseThrow(() -> new ResourceNotFoundException("BeeColony", "id", request.getColonyId()));

        Pollen pollen = pollenRepository.findById(request.getPollenId())
                .orElseThrow(() -> new ResourceNotFoundException("Pollen", "id", request.getPollenId()));

        CollectionTracking tracking = new CollectionTracking();
        tracking.setColony(colony);
        tracking.setPollen(pollen);
        tracking.setCollectedWeight(request.getCollectedWeight());
        tracking.setCollectionDate(request.getCollectionDate());
        tracking.setNote(request.getNote());
        tracking.setSource(com.beepollen.entity.CollectionSource.MANUAL);

        CollectionTracking saved = collectionTrackingRepository.save(tracking);
        log.info("Created collection tracking with id: {}", saved.getId());
        return mapToDTO(saved);
    }

    /**
     * Creates a new collection tracking record from IoT simulation.
     *
     * @param request the creation request
     * @return the created tracking DTO
     * @throws ResourceNotFoundException if colony or pollen not found
     */
    public CollectionTrackingDTO createIotTracking(CollectionTrackingRequest request) {
        log.info("Creating new collection tracking from IoT for colony id: {} and pollen id: {}",
                request.getColonyId(), request.getPollenId());

        BeeColony colony = beeColonyRepository.findById(request.getColonyId())
                .orElseThrow(() -> new ResourceNotFoundException("BeeColony", "id", request.getColonyId()));

        Pollen pollen = pollenRepository.findById(request.getPollenId())
                .orElseThrow(() -> new ResourceNotFoundException("Pollen", "id", request.getPollenId()));

        CollectionTracking tracking = new CollectionTracking();
        tracking.setColony(colony);
        tracking.setPollen(pollen);
        tracking.setCollectedWeight(request.getCollectedWeight());
        tracking.setCollectionDate(request.getCollectionDate());
        tracking.setNote(request.getNote());
        tracking.setSource(com.beepollen.entity.CollectionSource.IOT_SIMULATED);

        CollectionTracking saved = collectionTrackingRepository.save(tracking);
        log.info("Created collection tracking from IoT with id: {}", saved.getId());
        return mapToDTO(saved);
    }

    /**
     * Updates an existing collection tracking record.
     *
     * @param id      the tracking record ID
     * @param request the update request
     * @return the updated tracking DTO
     * @throws ResourceNotFoundException if tracking, colony, or pollen not found
     */
    public CollectionTrackingDTO updateTracking(Long id, CollectionTrackingRequest request) {
        log.info("Updating collection tracking with id: {}", id);

        CollectionTracking tracking = collectionTrackingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CollectionTracking", "id", id));

        BeeColony colony = beeColonyRepository.findById(request.getColonyId())
                .orElseThrow(() -> new ResourceNotFoundException("BeeColony", "id", request.getColonyId()));

        Pollen pollen = pollenRepository.findById(request.getPollenId())
                .orElseThrow(() -> new ResourceNotFoundException("Pollen", "id", request.getPollenId()));

        tracking.setColony(colony);
        tracking.setPollen(pollen);
        tracking.setCollectedWeight(request.getCollectedWeight());
        tracking.setCollectionDate(request.getCollectionDate());
        tracking.setNote(request.getNote());

        CollectionTracking updated = collectionTrackingRepository.save(tracking);
        log.info("Updated collection tracking with id: {}", updated.getId());
        return mapToDTO(updated);
    }

    /**
     * Deletes a collection tracking record by ID.
     *
     * @param id the tracking record ID
     * @throws ResourceNotFoundException if not found
     */
    public void deleteTracking(Long id) {
        log.info("Deleting collection tracking with id: {}", id);
        if (!collectionTrackingRepository.existsById(id)) {
            throw new ResourceNotFoundException("CollectionTracking", "id", id);
        }
        collectionTrackingRepository.deleteById(id);
        log.info("Deleted collection tracking with id: {}", id);
    }

    /**
     * Retrieves all tracking records for a specific colony.
     *
     * @param colonyId the colony ID
     * @return list of tracking DTOs
     */
    @Transactional(readOnly = true)
    public List<CollectionTrackingDTO> getTrackingsByColonyId(Long colonyId) {
        log.debug("Fetching collection trackings for colony id: {}", colonyId);
        return collectionTrackingRepository.findByColonyId(colonyId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all tracking records for a specific pollen type.
     *
     * @param pollenId the pollen ID
     * @return list of tracking DTOs
     */
    @Transactional(readOnly = true)
    public List<CollectionTrackingDTO> getTrackingsByPollenId(Long pollenId) {
        log.debug("Fetching collection trackings for pollen id: {}", pollenId);
        return collectionTrackingRepository.findByPollenId(pollenId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves tracking records within a date range.
     *
     * @param start the start date (inclusive)
     * @param end   the end date (inclusive)
     * @return list of tracking DTOs
     */
    @Transactional(readOnly = true)
    public List<CollectionTrackingDTO> getTrackingsByDateRange(LocalDate start, LocalDate end) {
        log.debug("Fetching collection trackings between {} and {}", start, end);
        return collectionTrackingRepository.findByCollectionDateBetween(start, end)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Generates a harvest report for the specified date range.
     * Groups by colony and pollen, and calculates totals and top contributors.
     */
    @Transactional(readOnly = true)
    public HarvestReportDTO generateReport(LocalDate fromDate, LocalDate toDate) {
        log.info("Generating harvest report from {} to {}", fromDate, toDate);
        List<CollectionTracking> trackings = collectionTrackingRepository.findByCollectionDateBetween(fromDate, toDate);
        
        HarvestReportDTO report = new HarvestReportDTO();
        report.setFromDate(fromDate);
        report.setToDate(toDate);
        report.setTotalRecords(trackings.size());
        
        if (trackings.isEmpty()) {
            report.setTotalWeightKg(0.0);
            report.setTopColonyCode("N/A");
            report.setTopColonyWeightKg(0.0);
            report.setTopPollenName("N/A");
            report.setTopPollenWeightKg(0.0);
            report.setColonySummaries(List.of());
            report.setPollenSummaries(List.of());
            return report;
        }

        double totalWeightGrams = trackings.stream().mapToDouble(CollectionTracking::getCollectedWeight).sum();
        report.setTotalWeightKg(totalWeightGrams / 1000.0);

        // Group by Colony
        Map<BeeColony, List<CollectionTracking>> byColony = trackings.stream()
                .filter(ct -> ct.getColony() != null)
                .collect(Collectors.groupingBy(CollectionTracking::getColony));

        List<HarvestReportDTO.ColonySummary> colonySummaries = byColony.entrySet().stream().map(entry -> {
            BeeColony colony = entry.getKey();
            List<CollectionTracking> cts = entry.getValue();
            double weightG = cts.stream().mapToDouble(CollectionTracking::getCollectedWeight).sum();
            return new HarvestReportDTO.ColonySummary(
                    colony.getColonyCode(),
                    colony.getBeeSpecies(),
                    weightG / 1000.0,
                    cts.size()
            );
        }).sorted((a, b) -> Double.compare(b.getTotalWeight(), a.getTotalWeight())).collect(Collectors.toList());

        report.setColonySummaries(colonySummaries);
        if (!colonySummaries.isEmpty()) {
            HarvestReportDTO.ColonySummary top = colonySummaries.get(0);
            report.setTopColonyCode(top.getColonyCode());
            report.setTopColonyWeightKg(top.getTotalWeight());
        } else {
            report.setTopColonyCode("N/A");
            report.setTopColonyWeightKg(0.0);
        }

        // Group by Pollen
        Map<Pollen, List<CollectionTracking>> byPollen = trackings.stream()
                .filter(ct -> ct.getPollen() != null)
                .collect(Collectors.groupingBy(CollectionTracking::getPollen));

        List<HarvestReportDTO.PollenSummary> pollenSummaries = byPollen.entrySet().stream().map(entry -> {
            Pollen pollen = entry.getKey();
            List<CollectionTracking> cts = entry.getValue();
            double weightG = cts.stream().mapToDouble(CollectionTracking::getCollectedWeight).sum();
            return new HarvestReportDTO.PollenSummary(
                    pollen.getName(),
                    pollen.getShape(),
                    weightG / 1000.0,
                    cts.size()
            );
        }).sorted((a, b) -> Double.compare(b.getTotalWeight(), a.getTotalWeight())).collect(Collectors.toList());

        report.setPollenSummaries(pollenSummaries);
        if (!pollenSummaries.isEmpty()) {
            HarvestReportDTO.PollenSummary top = pollenSummaries.get(0);
            report.setTopPollenName(top.getPollenName());
            report.setTopPollenWeightKg(top.getTotalWeight());
        } else {
            report.setTopPollenName("N/A");
            report.setTopPollenWeightKg(0.0);
        }

        return report;
    }

    /**
     * Maps a CollectionTracking entity to its DTO representation,
     * including denormalized colony and pollen fields.
     *
     * @param ct the entity to map
     * @return the mapped DTO
     */
    CollectionTrackingDTO mapToDTO(CollectionTracking ct) {
        return CollectionTrackingDTO.builder()
                .id(ct.getId())
                .colonyId(ct.getColony() != null ? ct.getColony().getId() : null)
                .colonyCode(ct.getColony() != null ? ct.getColony().getColonyCode() : null)
                .beeSpecies(ct.getColony() != null ? ct.getColony().getBeeSpecies() : null)
                .pollenId(ct.getPollen() != null ? ct.getPollen().getId() : null)
                .pollenName(ct.getPollen() != null ? ct.getPollen().getName() : null)
                .collectedWeight(ct.getCollectedWeight())
                .collectionDate(ct.getCollectionDate())
                .note(ct.getNote())
                .source(ct.getSource())
                .createdAt(ct.getCreatedAt())
                .updatedAt(ct.getUpdatedAt())
                .build();
    }
}
