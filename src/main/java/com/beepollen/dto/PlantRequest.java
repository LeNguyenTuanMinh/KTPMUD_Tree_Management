package com.beepollen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Request object for creating and updating Plant entities.
 * Contains validation constraints for all user-submitted fields.
 */
@Data
public class PlantRequest {

    @NotBlank(message = "Common name is required")
    @Size(max = 100)
    private String commonName;

    @NotBlank(message = "Scientific name is required")
    @Size(max = 150)
    private String scientificName;

    @Size(max = 100)
    private String family;

    @Size(max = 100)
    private String genus;

    @Size(max = 50)
    private String floweringSeason;

    @Size(max = 100)
    private String region;

    private String description;

    @Size(max = 500)
    private String imageUrl;

    /**
     * List of Pollen IDs to associate with this plant.
     */
    private List<Long> pollenIds = new ArrayList<>();
}
