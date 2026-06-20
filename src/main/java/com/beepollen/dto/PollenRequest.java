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

    @NotBlank(message = "Tên phấn hoa bắt buộc nhập")
    @Size(max = 100, message = "Tên phấn hoa không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 50, message = "Hình dạng không được vượt quá 50 ký tự")
    private String shape;

    private Double sizeMicron;

    @Size(max = 200, message = "Đặc điểm bề mặt không được vượt quá 200 ký tự")
    private String surfaceCharacteristic;

    @Size(max = 500, message = "Đường dẫn ảnh hiển vi không được vượt quá 500 ký tự")
    private String microscopeImage;

    /**
     * List of Plant IDs to associate with this pollen.
     */
    private List<Long> plantIds = new ArrayList<>();
}
