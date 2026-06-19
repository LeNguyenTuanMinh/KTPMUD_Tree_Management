package com.beepollen.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing a plant species in the Bee Pollen & Plant Management System.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "pollens")
@ToString(exclude = "pollens")
@Entity
@Table(name = "plants")
public class Plant extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String commonName;

    @Column(unique = true, nullable = false, length = 150)
    private String scientificName;

    @Column(length = 100)
    private String family;

    @Column(length = 100)
    private String genus;

    @Column(length = 50)
    private String floweringSeason;

    @Column(length = 100)
    private String region;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String imageUrl;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "plant_pollen",
            joinColumns = @JoinColumn(name = "plant_id"),
            inverseJoinColumns = @JoinColumn(name = "pollen_id")
    )
    @Builder.Default
    private Set<Pollen> pollens = new HashSet<>();
}
