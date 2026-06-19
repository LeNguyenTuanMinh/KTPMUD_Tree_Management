package com.beepollen.repository;

import com.beepollen.entity.CollectionTracking;
import com.beepollen.entity.Pollen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Spring Data JPA repository for the {@link CollectionTracking} entity.
 * Provides CRUD operations and custom query methods for tracking pollen
 * collection activities, including date-range queries and dashboard aggregations.
 */
@Repository
public interface CollectionTrackingRepository extends JpaRepository<CollectionTracking, Long> {

    /**
     * Finds all collection tracking records for a specific bee colony.
     *
     * @param colonyId the ID of the colony
     * @return a list of collection tracking records for the colony
     */
    List<CollectionTracking> findByColonyId(Long colonyId);

    /**
     * Finds all collection tracking records for a specific pollen type.
     *
     * @param pollenId the ID of the pollen
     * @return a list of collection tracking records for the pollen
     */
    List<CollectionTracking> findByPollenId(Long pollenId);

    /**
     * Finds all collection tracking records within a specified date range (inclusive).
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return a list of collection tracking records within the date range
     */
    List<CollectionTracking> findByCollectionDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Finds all collection tracking records for a specific colony within a date range (inclusive).
     *
     * @param colonyId  the ID of the colony
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return a list of collection tracking records matching the criteria
     */
    List<CollectionTracking> findByColonyIdAndCollectionDateBetween(Long colonyId, LocalDate startDate, LocalDate endDate);

    /**
     * Aggregates collection data to find the most collected pollen types,
     * ordered by total collected weight in descending order.
     * Useful for dashboard analytics.
     *
     * @return a list of Object arrays where index 0 is the pollen name (String)
     *         and index 1 is the total collected weight (Double)
     */
    @Query("SELECT ct.pollen.name, SUM(ct.collectedWeight) as totalWeight " +
            "FROM CollectionTracking ct " +
            "GROUP BY ct.pollen.name " +
            "ORDER BY totalWeight DESC")
    List<Object[]> findMostCollectedPollens();

    /**
     * Calculates the total weight of all collected pollen across all colonies.
     * Returns 0 if no records exist.
     *
     * @return the total collected weight as a Double
     */
    @Query("SELECT COALESCE(SUM(ct.collectedWeight), 0) FROM CollectionTracking ct")
    Double findTotalCollectedWeight();

    /**
     * Finds all distinct pollen types that have been collected by a specific colony.
     *
     * @param colonyId the ID of the colony
     * @return a list of distinct Pollen entities collected by the colony
     */
    @Query("SELECT DISTINCT ct.pollen FROM CollectionTracking ct WHERE ct.colony.id = :colonyId")
    List<Pollen> findPollenSourcesByColonyId(@Param("colonyId") Long colonyId);

    @Query("SELECT c FROM CollectionTracking c WHERE LOWER(c.colony.colonyCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.pollen.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<CollectionTracking> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Aggregates collection weight by month and year starting from a specific date.
     * Uses JPQL 'new map' syntax to return maps directly.
     *
     * @param startDate the start date to filter records
     * @return a list of maps containing year, month, and totalWeight
     */
    @Query("SELECT new map(" +
           "YEAR(ct.collectionDate) as year, " +
           "MONTH(ct.collectionDate) as month, " +
           "SUM(ct.collectedWeight) as totalWeight) " +
           "FROM CollectionTracking ct " +
           "WHERE ct.collectionDate >= :startDate " +
           "GROUP BY YEAR(ct.collectionDate), MONTH(ct.collectionDate) " +
           "ORDER BY YEAR(ct.collectionDate) ASC, MONTH(ct.collectionDate) ASC")
    List<java.util.Map<String, Object>> findMonthlyHarvests(@Param("startDate") LocalDate startDate);

    List<CollectionTracking> findByCreatedByOrUpdatedBy(String createdBy, String updatedBy);
}
