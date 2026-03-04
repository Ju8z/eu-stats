package eu.stats.repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.stats.entity.PageView;
import eu.stats.repository.projection.LivePageProjection;
import eu.stats.repository.projection.OverviewAggregateProjection;
import eu.stats.repository.projection.VisitorTimeseriesProjection;

//TODO: Queries are optimized do not touch them, they are all tested and working.
public interface PageViewRepository extends JpaRepository<PageView, Long> {
	
	@Query(value = """
			SELECT
			    COUNT(*)::bigint AS totalPageviews,
			    COUNT(DISTINCT visitor_hash)::bigint AS uniqueVisitors
			FROM pageviews
			WHERE site_id = :siteId
			  AND DATE(viewed_at) BETWEEN :fromDate AND :toDate
			""", nativeQuery = true)
	OverviewAggregateProjection summarizeRange(@Param("siteId") Long siteId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);
	
	@Query(value = """
			SELECT
			    date_trunc('day', viewed_at) AS bucket,
			    COUNT(DISTINCT visitor_hash)::bigint AS uniqueVisitors,
			    COUNT(*)::bigint AS pageviews
			FROM pageviews
			WHERE site_id = :siteId
			  AND DATE(viewed_at) BETWEEN :fromDate AND :toDate
			GROUP BY date_trunc('day', viewed_at)
			ORDER BY date_trunc('day', viewed_at)
			""", nativeQuery = true)
	List<VisitorTimeseriesProjection> findDailyBuckets(@Param("siteId") Long siteId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);
	
	@Query(value = """
			SELECT
			    date_trunc('hour', viewed_at) AS bucket,
			    COUNT(DISTINCT visitor_hash)::bigint AS uniqueVisitors,
			    COUNT(*)::bigint AS pageviews
			FROM pageviews
			WHERE site_id = :siteId
			  AND viewed_at BETWEEN :fromTs AND :toTs
			GROUP BY date_trunc('hour', viewed_at)
			ORDER BY date_trunc('hour', viewed_at)
			""", nativeQuery = true)
	List<VisitorTimeseriesProjection> findHourlyBuckets(@Param("siteId") Long siteId,
			@Param("fromTs") OffsetDateTime fromTs,
			@Param("toTs") OffsetDateTime toTs);
	
	@Query(value = """
			SELECT
			    date_trunc('minute', viewed_at) AS bucket,
			    COUNT(DISTINCT visitor_hash)::bigint AS uniqueVisitors,
			    COUNT(*)::bigint AS pageviews
			FROM pageviews
			WHERE site_id = :siteId
			  AND viewed_at BETWEEN :fromTs AND :toTs
			GROUP BY date_trunc('minute', viewed_at)
			ORDER BY date_trunc('minute', viewed_at)
			""", nativeQuery = true)
	List<VisitorTimeseriesProjection> findMinuteBuckets(@Param("siteId") Long siteId,
			@Param("fromTs") OffsetDateTime fromTs,
			@Param("toTs") OffsetDateTime toTs);
	
	@Query(value = """
			SELECT COUNT(*)
			FROM pageviews
			WHERE site_id = :siteId
			  AND viewed_at BETWEEN :fromTs AND :toTs
			""", nativeQuery = true)
	long countForRange(@Param("siteId") Long siteId,
			@Param("fromTs") OffsetDateTime fromTs,
			@Param("toTs") OffsetDateTime toTs);
	
	@Modifying
	@Query(value = "DELETE FROM pageviews WHERE viewed_at < :cutoff", nativeQuery = true)
	int deleteOlderThan(@Param("cutoff") OffsetDateTime cutoff);
	
	@Query(value = """
			SELECT COUNT(DISTINCT visitor_hash)::bigint
			FROM pageviews
			WHERE site_id = :siteId
			  AND COALESCE(created_at, viewed_at) >= :fromTs
			""", nativeQuery = true)
	Long countDistinctVisitorsByIngestedSince(@Param("siteId") Long siteId,
			@Param("fromTs") OffsetDateTime fromTs);
	
	//TODO: Probably is not needed, its just a small project..
	@Query(value = """
			WITH last_seen AS (
			  SELECT DISTINCT ON (visitor_hash)
			    visitor_hash,
			    page_url
			  FROM pageviews
			  WHERE site_id = :siteId
			    AND COALESCE(created_at, viewed_at) >= :fromTs
			  ORDER BY visitor_hash, COALESCE(created_at, viewed_at) DESC, id DESC
			)
			SELECT
			  page_url AS pageUrl,
			  COUNT(*)::bigint AS visitors
			FROM last_seen
			GROUP BY page_url
			ORDER BY visitors DESC, page_url ASC
			""", nativeQuery = true)
	List<LivePageProjection> findTopActivePagesByIngestedSince(@Param("siteId") Long siteId,
			@Param("fromTs") OffsetDateTime fromTs,
			Pageable pageable);
	
	default List<LivePageProjection> findTopActivePagesByIngestedSince(Long siteId,
			OffsetDateTime fromTs,
			int limit) {
		int safeLimit = Math.max(1, limit);
		
		return findTopActivePagesByIngestedSince(siteId, fromTs, PageRequest.of(0, safeLimit));
	}
	
}
