package com.beepollen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;

/**
 * JPA entity representing a pollen collection tracking record
 * in the Bee Pollen & Plant Management System.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"colony", "pollen"})
@Entity
@Table(name = "collection_tracking")
public class CollectionTracking extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colony_id", nullable = false)
    private BeeColony colony;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pollen_id", nullable = false)
    private Pollen pollen;

    @Column(nullable = false)
    private Double collectedWeight;

    @Column(nullable = false)
    private LocalDate collectionDate;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CollectionSource source = CollectionSource.MANUAL;
}
