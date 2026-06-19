package com.beepollen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for creating and updating Pollen entities.
 * Contains validation constraints for all user-submitted fields.
 */
@Data
public class PollenRequest {

    @NotBlank(message = "Pollen name is required")
    @Size(max = 100, message = "Pollen name must not exceed 100 characters")
    private String name;

    @Size(max = 50, message = "Shape must not exceed 50 characters")
    private String shape;

    private Double sizeMicron;

    @Size(max = 200, message = "Surface characteristic must not exceed 200 characters")
    private String surfaceCharacteristic;

    @Size(max = 500, message = "Microscope image URL must not exceed 500 characters")
    private String microscopeImage;

    /**
     * List of Plant IDs to associate with this pollen.
     */
    private List<Long> plantIds = new ArrayList<>();
}
