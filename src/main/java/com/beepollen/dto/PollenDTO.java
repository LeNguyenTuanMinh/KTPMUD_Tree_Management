package com.beepollen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Pollen entity.
 * Used to transfer pollen data between layers without exposing the entity directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollenDTO {

    private Long id;

    private String name;

    private String shape;

    private Double sizeMicron;

    private String surfaceCharacteristic;

    private String microscopeImage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
