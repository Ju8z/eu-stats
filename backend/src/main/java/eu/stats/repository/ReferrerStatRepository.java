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

public interface ReferrerStatRepository extends JpaRepository<ReferrerStat, Long> {
	
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
	
	default List<TopReferrerProjection> findTopReferrers(Long siteId,
			LocalDate fromDate,
			LocalDate toDate,
			int limit) {
		int safeLimit = Math.max(1, limit);
		
		return findTopReferrers(siteId, fromDate, toDate, PageRequest.of(0, safeLimit));
	}
}
