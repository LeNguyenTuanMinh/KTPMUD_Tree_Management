package com.beepollen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * JPA entity representing a pollen type in the Bee Pollen & Plant Management System.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "plants")
@ToString(exclude = "plants")
@Entity
@Table(name = "pollens")
public class Pollen extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String shape;

    private Double sizeMicron;

    @Column(length = 200)
    private String surfaceCharacteristic;

    @Column(length = 500)
    private String microscopeImage;

    @ManyToMany(mappedBy = "pollens")
    @Builder.Default
    private Set<Plant> plants = new HashSet<>();
}
