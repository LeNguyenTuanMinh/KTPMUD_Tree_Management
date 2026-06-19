package com.beepollen.iot;

import com.beepollen.entity.BeeColony;
import com.beepollen.entity.Plant;
import com.beepollen.entity.Pollen;
import com.beepollen.repository.BeeColonyRepository;
import com.beepollen.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "iot.simulator.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class IotDeviceSimulator {

    private final BeeColonyRepository beeColonyRepository;
    private final PlantRepository plantRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${iot.simulator.report-probability:0.7}")
    private double reportProbability;

    @Value("${iot.simulator.min-quantity-grams:5}")
    private double minQuantity;

    @Value("${iot.simulator.max-quantity-grams:80}")
    private double maxQuantity;

    @Value("${iot.api.key}")
    private String apiKey;

    private boolean isPaused = false;

    public void togglePause() {
        this.isPaused = !this.isPaused;
        log.info("IoT Simulator paused state changed to: {}", this.isPaused);
    }

    public boolean isPaused() {
        return this.isPaused;
    }

    @Transactional
    @Scheduled(initialDelayString = "${iot.simulator.initial-delay-ms}", fixedDelayString = "${iot.simulator.interval-ms}")
    public void simulateTick() {
        log.info("--- IoT Simulator Tick Started ---");

        if (isPaused) {
            log.info("IoT Simulator is currently paused. Skipping tick.");
            return;
        }

        List<Plant> allPlants = plantRepository.findAll();
        List<Plant> plantsWithPollen = allPlants.stream()
                .filter(p -> p.getPollens() != null && !p.getPollens().isEmpty())
                .collect(Collectors.toList());

        if (plantsWithPollen.isEmpty()) {
            log.warn("No plants with pollen available. Skipping IoT simulation tick.");
            return;
        }

        List<BeeColony> colonies = beeColonyRepository.findAll();
        if (colonies.isEmpty()) {
            log.warn("No bee colonies available. Skipping IoT simulation tick.");
            return;
        }

        String apiUrl = "http://localhost:" + serverPort + "/api/iot/pollen-readings";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-IoT-Api-Key", apiKey);

        for (BeeColony colony : colonies) {
            try {
                if (random.nextDouble() > reportProbability) {
                    continue; // Skip this colony for this tick
                }

                Plant selectedPlant = selectPlant(plantsWithPollen);
                Pollen selectedPollen = selectPollen(selectedPlant);
                double quantity = minQuantity + (maxQuantity - minQuantity) * random.nextDouble();

                PollenReadingRequest request = new PollenReadingRequest();
                request.setColonyId(colony.getId());
                request.setPlantId(selectedPlant.getId());
                request.setPollenId(selectedPollen != null ? selectedPollen.getId() : null);
                request.setQuantityGrams(quantity);
                request.setCollectedAt(LocalDate.now());

                HttpEntity<PollenReadingRequest> entity = new HttpEntity<>(request, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Bầy ong #{} báo cáo {}g phấn hoa từ {}", colony.getId(), String.format("%.2f", quantity), selectedPlant.getCommonName());
                } else {
                    log.warn("Failed to report for colony #{}. Status: {}", colony.getId(), response.getStatusCode());
                }

            } catch (Exception e) {
                log.warn("Error simulating data for colony #{}: {}", colony.getId(), e.getMessage());
            }
        }
        
        log.info("--- IoT Simulator Tick Ended ---");
    }

    private Plant selectPlant(List<Plant> validPlants) {
        // Implement season filtering
        String currentSeason = getCurrentSeason();
        List<Plant> inSeasonPlants = validPlants.stream()
                .filter(p -> p.getFloweringSeason() != null && p.getFloweringSeason().toLowerCase().contains(currentSeason.toLowerCase()))
                .collect(Collectors.toList());

        if (!inSeasonPlants.isEmpty()) {
            return inSeasonPlants.get(random.nextInt(inSeasonPlants.size()));
        }

        // Fallback to random valid plant
        return validPlants.get(random.nextInt(validPlants.size()));
    }

    private Pollen selectPollen(Plant plant) {
        if (plant.getPollens() == null || plant.getPollens().isEmpty()) {
            return null;
        }
        int index = random.nextInt(plant.getPollens().size());
        int i = 0;
        for (Pollen pollen : plant.getPollens()) {
            if (i == index) return pollen;
            i++;
        }
        return null;
    }

    private String getCurrentSeason() {
        int month = LocalDate.now().getMonthValue();
        if (month >= 3 && month <= 5) return "Spring";
        if (month >= 6 && month <= 8) return "Summer";
        if (month >= 9 && month <= 11) return "Autumn";
        return "Winter";
    }
}
