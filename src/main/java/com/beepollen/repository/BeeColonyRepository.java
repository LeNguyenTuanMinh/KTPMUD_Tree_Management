package com.beepollen.repository;

import com.beepollen.entity.BeeColony;
import com.beepollen.entity.HealthStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Spring Data JPA repository for the {@link BeeColony} entity.
 * Provides CRUD operations and custom query methods for bee colony management,
 * including lookups by colony code, species, and health status.
 */
@Repository
public interface BeeColonyRepository extends JpaRepository<BeeColony, Long> {

    /**
     * Finds a bee colony by its unique colony code.
     *
     * @param colonyCode the colony code to search for
     * @return an Optional containing the colony if found
     */
    Optional<BeeColony> findByColonyCode(String colonyCode);

    /**
     * Finds bee colonies whose species name contains the given string (case-insensitive).
     *
     * @param beeSpecies the partial species name to search for
     * @return a list of matching bee colonies
     */
    List<BeeColony> findByBeeSpeciesContainingIgnoreCase(String beeSpecies);

    /**
     * Finds all bee colonies with a specific health status.
     *
     * @param healthStatus the health status to filter by
     * @return a list of colonies with the specified health status
     */
    List<BeeColony> findByHealthStatus(HealthStatus healthStatus);

    /**
     * Checks whether a bee colony with the given colony code already exists.
     *
     * @param colonyCode the colony code to check
     * @return true if a colony with the code exists, false otherwise
     */
    Boolean existsByColonyCode(String colonyCode);

    /**
     * Searches across bee colony fields (colonyCode, beeSpecies)
     * using a single keyword with case-insensitive partial matching.
     *
     * @param keyword the keyword to search for across searchable fields
     * @return a list of bee colonies matching the keyword
     */
    @Query("SELECT b FROM BeeColony b WHERE LOWER(b.colonyCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.beeSpecies) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<BeeColony> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<BeeColony> findByCreatedByOrUpdatedBy(String createdBy, String updatedBy);
}
