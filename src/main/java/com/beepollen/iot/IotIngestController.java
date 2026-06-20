package com.beepollen.iot;

import com.beepollen.dto.CollectionTrackingDTO;
import com.beepollen.dto.CollectionTrackingRequest;
import com.beepollen.service.CollectionTrackingService;
import com.beepollen.repository.BeeColonyRepository;
import com.beepollen.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/iot")
@RequiredArgsConstructor
@Slf4j
public class IotIngestController {

    private final CollectionTrackingService collectionTrackingService;
    private final BeeColonyRepository beeColonyRepository;
    private final PlantRepository plantRepository;

    @Value("${iot.api.key}")
    private String configuredApiKey;

    @PostMapping("/pollen-readings")
    public ResponseEntity<?> ingestPollenReading(
            @RequestHeader(value = "X-IoT-Api-Key", required = false) String apiKey,
            @RequestBody PollenReadingRequest request) {

        if (apiKey == null || !apiKey.equals(configuredApiKey)) {
            log.warn("Truy cập IoT trái phép: API key không hợp lệ hoặc bị thiếu");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Header X-IoT-Api-Key không hợp lệ hoặc bị thiếu"));
        }

        if (request.getQuantityGrams() == null || request.getQuantityGrams() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Sản lượng (quantityGrams) phải lớn hơn 0"));
        }

        if (request.getColonyId() == null || !beeColonyRepository.existsById(request.getColonyId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy bầy ong với id: " + request.getColonyId()));
        }

        if (request.getPlantId() == null || !plantRepository.existsById(request.getPlantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy loài thực vật với id: " + request.getPlantId()));
        }

        // Validate pollenId existence if it's not null, though the service will also do it.
        // Since CollectionTracking requires a Pollen, pollenId must be provided.
        if (request.getPollenId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "pollenId là bắt buộc"));
        }

        CollectionTrackingRequest trackingRequest = new CollectionTrackingRequest();
        trackingRequest.setColonyId(request.getColonyId());
        trackingRequest.setPollenId(request.getPollenId());
        trackingRequest.setCollectedWeight(request.getQuantityGrams());
        trackingRequest.setCollectionDate(request.getCollectedAt() != null ? request.getCollectedAt() : java.time.LocalDate.now());
        trackingRequest.setNote("Dữ liệu mô phỏng từ IoT");

        try {
            CollectionTrackingDTO saved = collectionTrackingService.createIotTracking(trackingRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Xử lý dữ liệu IoT thất bại", e);
            // Returning 404 explicitly if pollen is not found (ResourceNotFoundException)
            if (e instanceof com.beepollen.exception.ResourceNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }
}
