package com.beepollen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for BeeColony entity.
 * Used for transferring colony data between service and presentation layers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeeColonyDTO {

    private Long id;

    private String colonyCode;

    private String beeSpecies;

    private Double latitude;

    private Double longitude;

    private String healthStatus;

    private Integer estimatedPopulation;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
