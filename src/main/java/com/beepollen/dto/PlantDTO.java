package com.beepollen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Plant entity.
 * Used to transfer plant data between layers without exposing the entity directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantDTO {

    private Long id;

    private String commonName;

    private String scientificName;

    private String family;

    private String genus;

    private String floweringSeason;

    private String region;

    private String description;

    private String imageUrl;

    @Builder.Default
    private List<PollenDTO> pollens = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
