package com.beepollen.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Request payload for creating or updating a Collection Tracking record.
 * Includes Jakarta Bean Validation constraints for server-side validation.
 */
@Data
public class CollectionTrackingRequest {

    @NotNull(message = "Colony is required")
    private Long colonyId;

    @NotNull(message = "Pollen is required")
    private Long pollenId;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be greater than 0")
    private Double collectedWeight;

    @NotNull(message = "Collection date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate collectionDate;

    private String note;
}
