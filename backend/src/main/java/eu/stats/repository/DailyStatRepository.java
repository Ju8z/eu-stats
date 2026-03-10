package eu.stats.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.stats.entity.DailyStat;
import eu.stats.repository.projection.OverviewAggregateProjection;

/**
 * Keeps daily statistics query rules at the storage boundary.
 * That lets services talk in analytics terms while database-specific grouping, sorting, and conflict
 * handling stay close to PostgreSQL.
 */
public interface DailyStatRepository extends JpaRepository<DailyStat, Long> {
	
	/**
	 * Pushes range aggregation into the database.
	 * Counting in PostgreSQL avoids materializing raw events in Java and keeps overview queries efficient on
	 * larger datasets.
	 *
	 * @param siteId   site identifier
	 * @param fromDate start date
	 * @param toDate   end date
	 * @return aggregate totals for the requested range
	 */
	@Query("""
			SELECT
			    CAST(SUM(d.totalPageviews) AS long) AS totalPageviews,
			    CAST(SUM(d.uniqueVisitors) AS long) AS uniqueVisitors
			FROM DailyStat d
			WHERE d.siteId = :siteId
			  AND d.statDate BETWEEN :fromDate AND :toDate
			""")
	OverviewAggregateProjection summarizeRange(@Param("siteId") Long siteId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);
	
	/**
	 * Reads pre-aggregated daily rows for chart building.
	 * Using daily summary data avoids scanning raw page views whenever historical charts need a longer window.
	 *
	 * @param siteId site identifier
	 * @param fromDate start date
	 * @param toDate end date
	 * @return daily statistics rows ordered by date
	 */
	List<DailyStat> findBySiteIdAndStatDateBetweenOrderByStatDateAsc(Long siteId, LocalDate fromDate, LocalDate toDate);
}
