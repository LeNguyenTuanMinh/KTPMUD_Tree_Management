package com.beepollen.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.ToString;

import java.util.List;

/**
 * JPA entity representing a bee colony in the Bee Pollen & Plant Management System.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "collectionTrackings")
@ToString(exclude = "collectionTrackings")
@Entity
@Table(name = "bee_colonies")
public class BeeColony extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String colonyCode;

    @Column(nullable = false, length = 100)
    private String beeSpecies;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private HealthStatus healthStatus = HealthStatus.HEALTHY;

    private Integer estimatedPopulation;

    @OneToMany(mappedBy = "colony", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectionTracking> collectionTrackings;
}
