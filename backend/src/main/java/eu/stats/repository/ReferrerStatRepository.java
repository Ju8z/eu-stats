package eu.stats.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.stats.entity.ReferrerStat;
import eu.stats.repository.projection.TopReferrerProjection;

/**
 * Keeps referrer statistics query rules at the storage boundary.
 * That lets services talk in analytics terms while database-specific grouping, sorting, and conflict
 * handling stay close to PostgreSQL.
 */
public interface ReferrerStatRepository extends JpaRepository<ReferrerStat, Long> {
	
	/**
	 * Ranks referrers in the database before service-level normalization.
	 * That keeps direct-traffic cleanup and display labeling separate from the storage query while still
	 * returning a bounded result set.
	 *
	 * @param siteId   site identifier
	 * @param fromDate start date
	 * @param toDate   end date
	 * @param pageable pagination request
	 * @return ranked referrer rows
	 */
	@Query("""
			SELECT
			    r.referrer AS referrer,
			    MAX(r.referrerCategory) AS referrerCategory,
			    CAST(SUM(r.visits) AS long) AS visits,
			    CAST(SUM(r.uniqueVisitors) AS long) AS uniqueVisitors
			FROM ReferrerStat r
			WHERE r.siteId = :siteId
			  AND r.statDate BETWEEN :fromDate AND :toDate
			GROUP BY r.referrer
			ORDER BY SUM(r.visits) DESC
			""")
	List<TopReferrerProjection> findTopReferrers(@Param("siteId") Long siteId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate,
			Pageable pageable);
	
	/**
	 * Ranks referrers in the database before service-level normalization.
	 * That keeps direct-traffic cleanup and display labeling separate from the storage query while still
	 * returning a bounded result set.
	 *
	 * @param siteId site identifier
	 * @param fromDate start date
	 * @param toDate end date
	 * @param limit maximum number of rows to return
	 * @return ranked referrer rows
	 */
	default List<TopReferrerProjection> findTopReferrers(Long siteId,
			LocalDate fromDate,
			LocalDate toDate,
			int limit) {
		int safeLimit = Math.max(1, limit);
		
		return findTopReferrers(siteId, fromDate, toDate, PageRequest.of(0, safeLimit));
	}
}
