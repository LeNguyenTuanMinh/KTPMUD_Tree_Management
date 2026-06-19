package com.beepollen.repository;

import com.beepollen.entity.Pollen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Spring Data JPA repository for the {@link Pollen} entity.
 * Provides CRUD operations and custom query methods for pollen management,
 * including text-based search across multiple fields and plant associations.
 */
@Repository
public interface PollenRepository extends JpaRepository<Pollen, Long> {

    /**
     * Finds pollen records whose name contains the given string (case-insensitive).
     *
     * @param name the partial name to search for
     * @return a list of matching pollen records
     */
    List<Pollen> findByNameContainingIgnoreCase(String name);

    /**
     * Finds pollen records whose shape contains the given string (case-insensitive).
     *
     * @param shape the partial shape description to search for
     * @return a list of matching pollen records
     */
    List<Pollen> findByShapeContainingIgnoreCase(String shape);

    /**
     * Searches across multiple pollen fields (name, shape, surfaceCharacteristic)
     * using a single keyword with case-insensitive partial matching.
     *
     * @param keyword the keyword to search for across all fields
     * @return a list of pollen records matching the keyword in any of the searchable fields
     */
    @Query("SELECT p FROM Pollen p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.shape) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.surfaceCharacteristic) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Pollen> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Search for pollens by keyword.
     * Searches name and shape.
     */
    @Query("SELECT p FROM Pollen p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.shape) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Pollen> searchPollens(@Param("keyword") String keyword, Pageable pageable);

    List<Pollen> findByCreatedByOrUpdatedBy(String createdBy, String updatedBy);

    /**
     * Finds all pollen types associated with a specific plant through the
     * many-to-many relationship.
     *
     * @param plantId the ID of the plant to find associated pollen for
     * @return a list of pollen records linked to the specified plant
     */
    @Query("SELECT pol FROM Pollen pol JOIN pol.plants p WHERE p.id = :plantId")
    List<Pollen> findByPlantId(@Param("plantId") Long plantId);
}
