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

    @NotBlank(message = "Mã bầy ong bắt buộc nhập")
    @Size(max = 50, message = "Mã bầy ong không được vượt quá 50 ký tự")
    private String colonyCode;

    @NotBlank(message = "Tên giống ong bắt buộc nhập")
    @Size(max = 100, message = "Tên giống ong không được vượt quá 100 ký tự")
    private String beeSpecies;

    private Double latitude;

    private Double longitude;

    private String healthStatus = "HEALTHY";

    @Min(value = 0, message = "Số lượng cá thể dự kiến phải lớn hơn hoặc bằng 0")
    private Integer estimatedPopulation;
}
