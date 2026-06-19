package com.beepollen.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request object for creating and updating BeeColony entities.
 * Contains validation constraints for incoming API/form data.
 */
@Data
public class BeeColonyRequest {

    @NotBlank(message = "Colony code is required")
    @Size(max = 50, message = "Colony code must not exceed 50 characters")
    private String colonyCode;

    @NotBlank(message = "Bee species is required")
    @Size(max = 100, message = "Bee species must not exceed 100 characters")
    private String beeSpecies;

    private Double latitude;

    private Double longitude;

    private String healthStatus = "HEALTHY";

    @Min(value = 0, message = "Estimated population must be zero or positive")
    private Integer estimatedPopulation;
}
