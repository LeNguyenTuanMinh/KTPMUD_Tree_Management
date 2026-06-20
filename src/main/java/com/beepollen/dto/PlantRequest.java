package com.beepollen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Request object for creating and updating Plant entities.
 * Contains validation constraints for all user-submitted fields.
 */
@Data
public class PlantRequest {

    @NotBlank(message = "Tên phổ thông bắt buộc nhập")
    @Size(max = 100, message = "Tên phổ thông không được vượt quá 100 ký tự")
    private String commonName;

    @NotBlank(message = "Tên khoa học bắt buộc nhập")
    @Size(max = 150, message = "Tên khoa học không được vượt quá 150 ký tự")
    private String scientificName;

    @Size(max = 100, message = "Họ không được vượt quá 100 ký tự")
    private String family;

    @Size(max = 100, message = "Chi không được vượt quá 100 ký tự")
    private String genus;

    @Size(max = 50, message = "Mùa hoa không được vượt quá 50 ký tự")
    private String floweringSeason;

    @Size(max = 100, message = "Khu vực phân bố không được vượt quá 100 ký tự")
    private String region;

    private String description;

    @Size(max = 500, message = "Đường dẫn ảnh không được vượt quá 500 ký tự")
    private String imageUrl;

    /**
     * List of Pollen IDs to associate with this plant.
     */
    private List<Long> pollenIds = new ArrayList<>();
}
