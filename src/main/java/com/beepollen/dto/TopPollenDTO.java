package com.beepollen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a pollen type and its total collected weight.
 * Used in the dashboard to display the top most-collected pollens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopPollenDTO {

    private String pollenName;

    private Double totalWeight;
}
