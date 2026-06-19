package com.beepollen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregated dashboard data including entity counts, collection statistics,
 * top collected pollens, and the latest collection tracking records.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    private long totalPlants;

    private long totalPollens;

    private long totalColonies;

    private long totalCollections;

    private double totalCollectedWeight;

    /** Top 5 most collected pollen types by total weight. */
    private List<TopPollenDTO> topPollens;

    /** Latest 10 collection tracking records ordered by collection date descending. */
    private List<CollectionTrackingDTO> latestCollections;

    /** Monthly harvest data for Line Chart. */
    private List<java.util.Map<String, Object>> monthlyHarvests;

    /** Pollen distribution data for Pie Chart. */
    private List<java.util.Map<String, Object>> pollenDistribution;
}
