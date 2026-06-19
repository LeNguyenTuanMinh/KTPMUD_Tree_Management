package com.beepollen.service;

import com.beepollen.dto.CollectionTrackingDTO;
import com.beepollen.dto.DashboardDTO;
import com.beepollen.dto.TopPollenDTO;
import com.beepollen.entity.CollectionTracking;
import com.beepollen.repository.BeeColonyRepository;
import com.beepollen.repository.CollectionTrackingRepository;
import com.beepollen.repository.PlantRepository;
import com.beepollen.repository.PollenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service responsible for aggregating dashboard data from multiple repositories.
 * Provides entity counts, collection statistics, top pollens, and latest records.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final PlantRepository plantRepository;
    private final PollenRepository pollenRepository;
    private final BeeColonyRepository beeColonyRepository;
    private final CollectionTrackingRepository collectionTrackingRepository;

    /**
     * Aggregates all dashboard data into a single DTO.
     *
     * @return the populated dashboard DTO
     */
    @Transactional(readOnly = true)
    public DashboardDTO getDashboardData() {
        log.debug("Aggregating dashboard data");

        long totalPlants = plantRepository.count();
        long totalPollens = pollenRepository.count();
        long totalColonies = beeColonyRepository.count();
        long totalCollections = collectionTrackingRepository.count();

        Double totalWeight = collectionTrackingRepository.findTotalCollectedWeight();
        double totalCollectedWeight = Optional.ofNullable(totalWeight).orElse(0.0);

        List<TopPollenDTO> topPollens = mapTopPollens();
        List<CollectionTrackingDTO> latestCollections = mapLatestCollections();
        
        List<Map<String, Object>> monthlyHarvests = buildMonthlyHarvests();
        List<Map<String, Object>> pollenDistribution = buildPollenDistribution();

        DashboardDTO dashboard = DashboardDTO.builder()
                .totalPlants(totalPlants)
                .totalPollens(totalPollens)
                .totalColonies(totalColonies)
                .totalCollections(totalCollections)
                .totalCollectedWeight(totalCollectedWeight)
                .topPollens(topPollens)
                .latestCollections(latestCollections)
                .monthlyHarvests(monthlyHarvests)
                .pollenDistribution(pollenDistribution)
                .build();

        log.debug("Dashboard data aggregated: plants={}, pollens={}, colonies={}, collections={}, weight={}",
                totalPlants, totalPollens, totalColonies, totalCollections, totalCollectedWeight);

        return dashboard;
    }

    /**
     * Maps the raw Object[] results from findMostCollectedPollens() into TopPollenDTOs.
     * Takes the top 5 entries.
     *
     * @return list of top pollen DTOs (max 5)
     */
    private List<TopPollenDTO> mapTopPollens() {
        List<Object[]> rawResults = collectionTrackingRepository.findMostCollectedPollens();
        if (rawResults == null || rawResults.isEmpty()) {
            return Collections.emptyList();
        }

        return rawResults.stream()
                .limit(5)
                .map(row -> TopPollenDTO.builder()
                        .pollenName(row[0] != null ? row[0].toString() : "Unknown")
                        .totalWeight(row[1] != null ? ((Number) row[1]).doubleValue() : 0.0)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the latest 10 collection tracking records ordered by collection date descending,
     * and maps them to DTOs.
     *
     * @return list of latest tracking DTOs (max 10)
     */
    private List<CollectionTrackingDTO> mapLatestCollections() {
        List<CollectionTracking> allSorted = collectionTrackingRepository.findAll(
                Sort.by(Sort.Direction.DESC, "collectionDate"));

        return allSorted.stream()
                .limit(10)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Maps a CollectionTracking entity to its DTO representation,
     * including denormalized colony and pollen fields.
     *
     * @param ct the entity to map
     * @return the mapped DTO
     */
    private CollectionTrackingDTO mapToDTO(CollectionTracking ct) {
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
                .createdAt(ct.getCreatedAt())
                .updatedAt(ct.getUpdatedAt())
                .build();
    }

    private List<Map<String, Object>> buildMonthlyHarvests() {
        LocalDate startDate = LocalDate.now().minusMonths(11).withDayOfMonth(1);
        List<Map<String, Object>> dbHarvests = collectionTrackingRepository.findMonthlyHarvests(startDate);

        List<Map<String, Object>> filledHarvests = new ArrayList<>();
        YearMonth current = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(LocalDate.now());

        while (!current.isAfter(end)) {
            final int year = current.getYear();
            final int month = current.getMonthValue();

            Double totalWeight = 0.0;
            if (dbHarvests != null) {
                for (Map<String, Object> dbH : dbHarvests) {
                    Number y = (Number) dbH.get("year");
                    Number m = (Number) dbH.get("month");
                    if (y != null && m != null && y.intValue() == year && m.intValue() == month) {
                        Number w = (Number) dbH.get("totalWeight");
                        totalWeight = w != null ? w.doubleValue() : 0.0;
                        break;
                    }
                }
            }

            Map<String, Object> map = new HashMap<>();
            map.put("monthLabel", "Tháng " + month + "/" + year);
            map.put("totalWeight", totalWeight);
            filledHarvests.add(map);

            current = current.plusMonths(1);
        }
        return filledHarvests;
    }

    private List<Map<String, Object>> buildPollenDistribution() {
        List<Object[]> rawResults = collectionTrackingRepository.findMostCollectedPollens();
        List<Map<String, Object>> distribution = new ArrayList<>();
        
        if (rawResults == null || rawResults.isEmpty()) {
            return distribution;
        }

        if (rawResults.size() <= 5) {
            for (Object[] row : rawResults) {
                Map<String, Object> map = new HashMap<>();
                map.put("pollenName", row[0] != null ? row[0].toString() : "Unknown");
                map.put("totalWeight", row[1] != null ? ((Number) row[1]).doubleValue() : 0.0);
                distribution.add(map);
            }
        } else {
            for (int i = 0; i < 5; i++) {
                Object[] row = rawResults.get(i);
                Map<String, Object> map = new HashMap<>();
                map.put("pollenName", row[0] != null ? row[0].toString() : "Unknown");
                map.put("totalWeight", row[1] != null ? ((Number) row[1]).doubleValue() : 0.0);
                distribution.add(map);
            }
            double otherWeight = 0.0;
            for (int i = 5; i < rawResults.size(); i++) {
                Object[] row = rawResults.get(i);
                otherWeight += row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            }
            if (otherWeight > 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("pollenName", "Khác");
                map.put("totalWeight", otherWeight);
                distribution.add(map);
            }
        }
        return distribution;
    }
}
