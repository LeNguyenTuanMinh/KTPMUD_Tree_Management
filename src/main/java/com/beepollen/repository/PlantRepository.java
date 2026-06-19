package com.beepollen.repository;

import com.beepollen.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Spring Data JPA repository for the {@link Plant} entity.
 * Provides CRUD operations and custom query methods for plant management,
 * including text-based search across multiple fields and pollen associations.
 */
@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {

    /**
     * Finds plants whose common name contains the given string (case-insensitive).
     *
     * @param commonName the partial common name to search for
     * @return a list of matching plants
     */
    List<Plant> findByCommonNameContainingIgnoreCase(String commonName);

    /**
     * Finds plants whose scientific name contains the given string (case-insensitive).
     *
     * @param scientificName the partial scientific name to search for
     * @return a list of matching plants
     */
    List<Plant> findByScientificNameContainingIgnoreCase(String scientificName);

    /**
     * Finds plants whose family contains the given string (case-insensitive).
     *
     * @param family the partial family name to search for
     * @return a list of matching plants
     */
    List<Plant> findByFamilyContainingIgnoreCase(String family);

    /**
     * Finds plants whose region contains the given string (case-insensitive).
     *
     * @param region the partial region name to search for
     * @return a list of matching plants
     */
    List<Plant> findByRegionContainingIgnoreCase(String region);

    /**
     * Finds a plant by its exact scientific name.
     *
     * @param scientificName the exact scientific name to search for
     * @return an Optional containing the plant if found
     */
    Optional<Plant> findByScientificName(String scientificName);

    /**
     * Searches across multiple plant fields (commonName, scientificName, family, region)
     * using a single keyword with case-insensitive partial matching.
     *
     * @param keyword the keyword to search for across all fields
     * @return a list of plants matching the keyword in any of the searchable fields
     */
    @Query("SELECT p FROM Plant p WHERE LOWER(p.commonName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.scientificName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.family) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.region) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Plant> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Finds all plants associated with a specific pollen type through the
     * many-to-many relationship.
     *
     * @param pollenId the ID of the pollen to find associated plants for
     * @return a list of plants linked to the specified pollen
     */
    @Query("SELECT p FROM Plant p JOIN p.pollens pol WHERE pol.id = :pollenId")
    List<Plant> findByPollenId(@Param("pollenId") Long pollenId);

    List<Plant> findByCreatedByOrUpdatedBy(String createdBy, String updatedBy);
}
