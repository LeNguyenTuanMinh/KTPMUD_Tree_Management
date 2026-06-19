package com.beepollen.config;

import com.beepollen.entity.*;
import com.beepollen.repository.BeeColonyRepository;
import com.beepollen.repository.CollectionTrackingRepository;
import com.beepollen.repository.PlantRepository;
import com.beepollen.repository.PollenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Seeds the database with mock data for demonstration purposes.
 * Only runs if the database is currently empty.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MockDataSeeder implements CommandLineRunner {

    private final PlantRepository plantRepository;
    private final PollenRepository pollenRepository;
    private final BeeColonyRepository beeColonyRepository;
    private final CollectionTrackingRepository trackingRepository;

    @Override
    public void run(String... args) {
        if (beeColonyRepository.count() > 0) {
            log.info("Database already contains data — skipping mock data generation");
            return;
        }

        log.info("Generating mock data...");

        // 1. Create Pollens
        Pollen sunflowerPollen = new Pollen();
        sunflowerPollen.setName("Sunflower Pollen");
        sunflowerPollen.setShape("Echinate");
        sunflowerPollen.setSizeMicron(35.0);
        sunflowerPollen.setSurfaceCharacteristic("Spiky surface");

        Pollen lavenderPollen = new Pollen();
        lavenderPollen.setName("Lavender Pollen");
        lavenderPollen.setShape("Prolate");
        lavenderPollen.setSizeMicron(25.0);
        lavenderPollen.setSurfaceCharacteristic("Smooth, 6 furrows");

        Pollen manukaPollen = new Pollen();
        manukaPollen.setName("Manuka Pollen");
        manukaPollen.setShape("Triangular");
        manukaPollen.setSizeMicron(15.0);
        manukaPollen.setSurfaceCharacteristic("Slightly sticky");

        Pollen eucalyptusPollen = new Pollen();
        eucalyptusPollen.setName("Eucalyptus Pollen");
        eucalyptusPollen.setShape("Triporate");
        eucalyptusPollen.setSizeMicron(20.0);
        eucalyptusPollen.setSurfaceCharacteristic("Smooth with pores");

        Pollen cloverPollen = new Pollen();
        cloverPollen.setName("Clover Pollen");
        cloverPollen.setShape("Spheroidal");
        cloverPollen.setSizeMicron(28.0);
        cloverPollen.setSurfaceCharacteristic("Reticulate");
        
        List<Pollen> savedPollens = pollenRepository.saveAll(List.of(sunflowerPollen, lavenderPollen, manukaPollen, eucalyptusPollen, cloverPollen));

        Pollen sPollen = savedPollens.get(0);
        Pollen lPollen = savedPollens.get(1);
        Pollen mPollen = savedPollens.get(2);
        Pollen ePollen = savedPollens.get(3);
        Pollen cPollen = savedPollens.get(4);

        // 2. Create Plants & Associate with Pollens
        Plant sunflower = new Plant();
        sunflower.setCommonName("Sunflower (Mock)");
        sunflower.setScientificName("Helianthus annuus (Mock)");
        sunflower.setFamily("Asteraceae");
        sunflower.setFloweringSeason("Summer");
        sunflower.setRegion("Global");
        sunflower.setPollens(Set.of(sPollen));
        
        Plant lavender = new Plant();
        lavender.setCommonName("Lavender (Mock)");
        lavender.setScientificName("Lavandula angustifolia (Mock)");
        lavender.setFamily("Lamiaceae");
        lavender.setFloweringSeason("Summer");
        lavender.setRegion("Mediterranean");
        lavender.setPollens(Set.of(lPollen));
        
        Plant manuka = new Plant();
        manuka.setCommonName("Manuka (Mock)");
        manuka.setScientificName("Leptospermum scoparium (Mock)");
        manuka.setFamily("Myrtaceae");
        manuka.setFloweringSeason("Spring/Summer");
        manuka.setRegion("New Zealand");
        manuka.setPollens(Set.of(mPollen));
        
        Plant eucalyptus = new Plant();
        eucalyptus.setCommonName("Blue Gum (Mock)");
        eucalyptus.setScientificName("Eucalyptus globulus (Mock)");
        eucalyptus.setFamily("Myrtaceae");
        eucalyptus.setFloweringSeason("Autumn/Winter");
        eucalyptus.setRegion("Australia");
        eucalyptus.setPollens(Set.of(ePollen));
        
        Plant clover = new Plant();
        clover.setCommonName("White Clover (Mock)");
        clover.setScientificName("Trifolium repens (Mock)");
        clover.setFamily("Fabaceae");
        clover.setFloweringSeason("Spring");
        clover.setRegion("Europe/Asia");
        clover.setPollens(Set.of(cPollen));

        plantRepository.saveAll(List.of(sunflower, lavender, manuka, eucalyptus, clover));

        // 3. Create Bee Colonies
        BeeColony col1 = new BeeColony();
        col1.setColonyCode("MOCK-001");
        col1.setBeeSpecies("Apis mellifera");
        col1.setLatitude(10.762622);
        col1.setLongitude(106.660172);
        col1.setHealthStatus(HealthStatus.HEALTHY);
        col1.setEstimatedPopulation(50000);

        BeeColony col2 = new BeeColony();
        col2.setColonyCode("MOCK-002");
        col2.setBeeSpecies("Apis cerana");
        col2.setLatitude(10.771234);
        col2.setLongitude(106.653456);
        col2.setHealthStatus(HealthStatus.WEAK);
        col2.setEstimatedPopulation(35000);

        BeeColony col3 = new BeeColony();
        col3.setColonyCode("MOCK-003");
        col3.setBeeSpecies("Apis mellifera");
        col3.setLatitude(10.812345);
        col3.setLongitude(106.712345);
        col3.setHealthStatus(HealthStatus.HEALTHY);
        col3.setEstimatedPopulation(60000);

        BeeColony col4 = new BeeColony();
        col4.setColonyCode("MOCK-004");
        col4.setBeeSpecies("Apis dorsata");
        col4.setLatitude(11.940419);
        col4.setLongitude(108.458313);
        col4.setHealthStatus(HealthStatus.CRITICAL);
        col4.setEstimatedPopulation(15000);

        BeeColony col5 = new BeeColony();
        col5.setColonyCode("MOCK-005");
        col5.setBeeSpecies("Apis cerana");
        col5.setLatitude(11.951122);
        col5.setLongitude(108.461123);
        col5.setHealthStatus(HealthStatus.DEAD);
        col5.setEstimatedPopulation(0);

        List<BeeColony> savedColonies = beeColonyRepository.saveAll(List.of(col1, col2, col3, col4, col5));

        // 4. Create Collection Trackings
        List<BeeColony> activeColonies = List.of(savedColonies.get(0), savedColonies.get(1), savedColonies.get(2), savedColonies.get(3)); // Skip the dead one

        Random random = new Random();
        for (int i = 0; i < 25; i++) {
            CollectionTracking tracking = new CollectionTracking();
            tracking.setColony(activeColonies.get(random.nextInt(activeColonies.size())));
            tracking.setPollen(savedPollens.get(random.nextInt(savedPollens.size())));
            
            // Random weight between 50g and 300g
            double weight = 50 + (250 * random.nextDouble());
            tracking.setCollectedWeight(weight);
            
            // Random date within the last 30 days
            int daysAgo = random.nextInt(30);
            tracking.setCollectionDate(LocalDate.now().minusDays(daysAgo));
            
            tracking.setNote(weight > 200 ? "Excellent harvest!" : "Normal daily harvest.");
            
            trackingRepository.save(tracking);
        }

        log.info("✅ Mock data generation completed successfully!");
    }
}
