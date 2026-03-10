package eu.stats.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.stats.entity.PageStat;
import eu.stats.repository.projection.TopPageProjection;

/**
 * Keeps page statistics query rules at the storage boundary.
 * That lets services talk in analytics terms while database-specific grouping, sorting, and conflict
 * handling stay close to PostgreSQL.
 */
public interface PageStatRepository extends JpaRepository<PageStat, Long> {
	/**
	 * Ranks pages in the database before service-level shaping.
	 * Sorting and limiting in PostgreSQL keeps leaderboard queries small and avoids in-memory ranking work in
	 * the service layer.
	 *
	 * @param siteId   site identifier
	 * @param fromDate start date
	 * @param toDate   end date
	 * @param pageable pagination request
	 * @return ranked page rows
	 */
	@Query("""
			SELECT
			    p.pageUrl AS pageUrl,
			    MAX(p.pageTitle) AS pageTitle,
			    CAST(SUM(p.pageviews) AS long) AS pageviews,
			    CAST(SUM(p.uniqueVisitors) AS long) AS uniqueVisitors
			FROM PageStat p
			WHERE p.siteId = :siteId
			  AND p.statDate BETWEEN :fromDate AND :toDate
			GROUP BY p.pageUrl
			ORDER BY SUM(p.pageviews) DESC
			""")
	List<TopPageProjection> findTopPages(@Param("siteId") Long siteId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate,
			Pageable pageable);
	
	/**
	 * Ranks pages in the database before service-level shaping.
	 * Sorting and limiting in PostgreSQL keeps leaderboard queries small and avoids in-memory ranking work in
	 * the service layer.
	 *
	 * @param siteId site identifier
	 * @param fromDate start date
	 * @param toDate end date
	 * @param limit maximum number of rows to return
	 * @return ranked page rows
	 */
	default List<TopPageProjection> findTopPages(Long siteId,
			LocalDate fromDate,
			LocalDate toDate,
			int limit) {
		int safeLimit = Math.max(1, limit);
		
		return findTopPages(siteId, fromDate, toDate, PageRequest.of(0, safeLimit));
	}
}
