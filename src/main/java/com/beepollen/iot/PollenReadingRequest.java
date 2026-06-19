package com.beepollen.iot;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PollenReadingRequest {
    private Long colonyId;
    private Long plantId;
    private Long pollenId;
    private Double quantityGrams;
    private LocalDate collectedAt;
}
