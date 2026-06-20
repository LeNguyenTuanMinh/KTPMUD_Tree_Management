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

    @NotNull(message = "Bầy ong bắt buộc nhập")
    private Long colonyId;

    @NotNull(message = "Phấn hoa bắt buộc nhập")
    private Long pollenId;

    @NotNull(message = "Sản lượng bắt buộc nhập")
    @Positive(message = "Sản lượng phải lớn hơn 0")
    private Double collectedWeight;

    @NotNull(message = "Ngày thu hoạch bắt buộc nhập")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate collectionDate;

    private String note;
}
