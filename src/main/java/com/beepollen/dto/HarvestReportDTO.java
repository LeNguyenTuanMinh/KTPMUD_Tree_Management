package com.beepollen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HarvestReportDTO {

    private LocalDate fromDate;
    private LocalDate toDate;
    
    private double totalWeightKg;
    private long totalRecords;
    
    private String topColonyCode;
    private double topColonyWeightKg;
    
    private String topPollenName;
    private double topPollenWeightKg;
    
    private List<ColonySummary> colonySummaries;
    private List<PollenSummary> pollenSummaries;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ColonySummary {
        private String colonyCode;
        private String location;
        private double totalWeight;
        private long recordCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PollenSummary {
        private String pollenName;
        private String colorCode;
        private double totalWeight;
        private long recordCount;
    }
}
