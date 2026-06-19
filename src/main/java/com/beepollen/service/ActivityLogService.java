package com.beepollen.service;

import com.beepollen.entity.BeeColony;
import com.beepollen.entity.CollectionTracking;
import com.beepollen.entity.Plant;
import com.beepollen.entity.Pollen;
import com.beepollen.repository.BeeColonyRepository;
import com.beepollen.repository.CollectionTrackingRepository;
import com.beepollen.repository.PlantRepository;
import com.beepollen.repository.PollenRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final PlantRepository plantRepository;
    private final PollenRepository pollenRepository;
    private final BeeColonyRepository beeColonyRepository;
    private final CollectionTrackingRepository collectionTrackingRepository;

    @Data
    @Builder
    public static class ActivityEntry {
        private String entityType;
        private String entityName;
        private String action;
        private LocalDateTime timestamp;
    }

    public List<ActivityEntry> getRecentActivityForUser(String username, int limit) {
        List<ActivityEntry> activities = new ArrayList<>();

        // Fetch Plant activities
        List<Plant> plants = plantRepository.findByCreatedByOrUpdatedBy(username, username);
        for (Plant p : plants) {
            if (username.equals(p.getCreatedBy())) {
                activities.add(ActivityEntry.builder()
                        .entityType("Plant")
                        .entityName(p.getCommonName())
                        .action("Created")
                        .timestamp(p.getCreatedAt())
                        .build());
            }
            if (username.equals(p.getUpdatedBy()) && p.getUpdatedAt() != null && !p.getUpdatedAt().equals(p.getCreatedAt())) {
                activities.add(ActivityEntry.builder()
                        .entityType("Plant")
                        .entityName(p.getCommonName())
                        .action("Updated")
                        .timestamp(p.getUpdatedAt())
                        .build());
            }
        }

        // Fetch Pollen activities
        List<Pollen> pollens = pollenRepository.findByCreatedByOrUpdatedBy(username, username);
        for (Pollen p : pollens) {
            if (username.equals(p.getCreatedBy())) {
                activities.add(ActivityEntry.builder()
                        .entityType("Pollen")
                        .entityName(p.getName())
                        .action("Created")
                        .timestamp(p.getCreatedAt())
                        .build());
            }
            if (username.equals(p.getUpdatedBy()) && p.getUpdatedAt() != null && !p.getUpdatedAt().equals(p.getCreatedAt())) {
                activities.add(ActivityEntry.builder()
                        .entityType("Pollen")
                        .entityName(p.getName())
                        .action("Updated")
                        .timestamp(p.getUpdatedAt())
                        .build());
            }
        }

        // Fetch Bee Colony activities
        List<BeeColony> colonies = beeColonyRepository.findByCreatedByOrUpdatedBy(username, username);
        for (BeeColony c : colonies) {
            if (username.equals(c.getCreatedBy())) {
                activities.add(ActivityEntry.builder()
                        .entityType("Bee Colony")
                        .entityName(c.getColonyCode())
                        .action("Created")
                        .timestamp(c.getCreatedAt())
                        .build());
            }
            if (username.equals(c.getUpdatedBy()) && c.getUpdatedAt() != null && !c.getUpdatedAt().equals(c.getCreatedAt())) {
                activities.add(ActivityEntry.builder()
                        .entityType("Bee Colony")
                        .entityName(c.getColonyCode())
                        .action("Updated")
                        .timestamp(c.getUpdatedAt())
                        .build());
            }
        }

        // Fetch Collection Tracking activities
        List<CollectionTracking> trackings = collectionTrackingRepository.findByCreatedByOrUpdatedBy(username, username);
        for (CollectionTracking t : trackings) {
            String name = "Tracking #" + t.getId() + " (" + t.getColony().getColonyCode() + ")";
            if (username.equals(t.getCreatedBy())) {
                activities.add(ActivityEntry.builder()
                        .entityType("Collection")
                        .entityName(name)
                        .action("Created")
                        .timestamp(t.getCreatedAt())
                        .build());
            }
            if (username.equals(t.getUpdatedBy()) && t.getUpdatedAt() != null && !t.getUpdatedAt().equals(t.getCreatedAt())) {
                activities.add(ActivityEntry.builder()
                        .entityType("Collection")
                        .entityName(name)
                        .action("Updated")
                        .timestamp(t.getUpdatedAt())
                        .build());
            }
        }

        // Sort by timestamp DESC and take top limit
        return activities.stream()
                .filter(a -> a.getTimestamp() != null)
                .sorted(Comparator.comparing(ActivityEntry::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
