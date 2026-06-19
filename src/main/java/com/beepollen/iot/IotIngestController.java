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
            log.warn("Unauthorized IoT request: Invalid or missing API key");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or missing X-IoT-Api-Key header"));
        }

        if (request.getQuantityGrams() == null || request.getQuantityGrams() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "quantityGrams must be greater than 0"));
        }

        if (request.getColonyId() == null || !beeColonyRepository.existsById(request.getColonyId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "BeeColony not found with id: " + request.getColonyId()));
        }

        if (request.getPlantId() == null || !plantRepository.existsById(request.getPlantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Plant not found with id: " + request.getPlantId()));
        }

        // Validate pollenId existence if it's not null, though the service will also do it.
        // Since CollectionTracking requires a Pollen, pollenId must be provided.
        if (request.getPollenId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "pollenId is required"));
        }

        CollectionTrackingRequest trackingRequest = new CollectionTrackingRequest();
        trackingRequest.setColonyId(request.getColonyId());
        trackingRequest.setPollenId(request.getPollenId());
        trackingRequest.setCollectedWeight(request.getQuantityGrams());
        trackingRequest.setCollectionDate(request.getCollectedAt() != null ? request.getCollectedAt() : java.time.LocalDate.now());
        trackingRequest.setNote("IoT simulated data");

        try {
            CollectionTrackingDTO saved = collectionTrackingService.createIotTracking(trackingRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Failed to process IoT reading", e);
            // Returning 404 explicitly if pollen is not found (ResourceNotFoundException)
            if (e instanceof com.beepollen.exception.ResourceNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }
}
