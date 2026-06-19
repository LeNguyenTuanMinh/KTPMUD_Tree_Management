package com.beepollen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.beepollen.entity.CollectionSource;

/**
 * Data Transfer Object for Collection Tracking entities.
 * Includes denormalized colony and pollen fields for convenient display
 * without requiring additional lookups on the client side.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionTrackingDTO {

    private Long id;

    private Long colonyId;

    /** Denormalized colony code for display purposes. */
    private String colonyCode;

    /** Denormalized bee species from the associated colony. */
    private String beeSpecies;

    private Long pollenId;

    /** Denormalized pollen name for display purposes. */
    private String pollenName;

    private Double collectedWeight;

    private LocalDate collectionDate;

    private String note;

    private CollectionSource source;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
