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

/**
 * Keeps page view query rules at the storage boundary.
 * That lets services talk in analytics terms while database-specific grouping, sorting, and conflict
 * handling stay close to PostgreSQL.
 */
public interface PageViewRepository extends JpaRepository<PageView, Long> {
	
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
	@Query(value = """
			SELECT
			    COUNT(*)::bigint AS totalPageviews,
			    COUNT(DISTINCT visitor_hash)::bigint AS uniqueVisitors
			FROM pageviews
			WHERE site_id = :siteId
			  AND event_type = 'pageview'
			  AND DATE(viewed_at) BETWEEN :fromDate AND :toDate
			""", nativeQuery = true)
	OverviewAggregateProjection summarizeRange(@Param("siteId") Long siteId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);
	
	/**
	 * Pushes daily bucketing into PostgreSQL.
	 * The database groups timestamps directly so application code receives already bucketed totals instead of
	 * rebuilding time windows in memory.
	 *
	 * @param siteId site identifier
	 * @param fromDate start date
	 * @param toDate end date
	 * @return bucketed visitor rows for the requested range
	 */
	@Query(value = """
			SELECT
			    date_trunc('day', viewed_at) AS bucket,
			    COUNT(DISTINCT visitor_hash)::bigint AS uniqueVisitors,
			    COUNT(*)::bigint AS pageviews
			FROM pageviews
			WHERE site_id = :siteId
			  AND event_type = 'pageview'
			  AND DATE(viewed_at) BETWEEN :fromDate AND :toDate
			GROUP BY date_trunc('day', viewed_at)
			ORDER BY date_trunc('day', viewed_at)
			""", nativeQuery = true)
	List<VisitorTimeseriesProjection> findDailyBuckets(@Param("siteId") Long siteId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);
	
	/**
	 * Pushes hourly bucketing into PostgreSQL.
	 * The database groups timestamps directly so application code receives already bucketed totals instead of
	 * rebuilding time windows in memory.
	 *
	 * @param siteId site identifier
	 * @param fromInclusive inclusive start timestamp
	 * @param toExclusive exclusive end timestamp
	 * @return bucketed visitor rows for the requested range
	 */
	@Query(value = """
				SELECT
				    date_trunc('hour', viewed_at) AS bucket,
				    COUNT(DISTINCT visitor_hash)::bigint AS uniqueVisitors,
				    COUNT(*)::bigint AS pageviews
				FROM pageviews
				WHERE site_id = :siteId
			  AND event_type = 'pageview'
				  AND viewed_at >= :fromInclusive
				  AND viewed_at < :toExclusive
				GROUP BY date_trunc('hour', viewed_at)
				ORDER BY date_trunc('hour', viewed_at)
				""", nativeQuery = true)
	List<VisitorTimeseriesProjection> findHourlyBuckets(@Param("siteId") Long siteId,
			@Param("fromInclusive") OffsetDateTime fromInclusive,
			@Param("toExclusive") OffsetDateTime toExclusive);
	
	/**
	 * Pushes minute bucketing into PostgreSQL.
	 * The database groups timestamps directly so application code receives already bucketed totals instead of
	 * rebuilding time windows in memory.
	 *
	 * @param siteId site identifier
	 * @param fromInclusive inclusive start timestamp
	 * @param toExclusive exclusive end timestamp
	 * @return bucketed visitor rows for the requested range
	 */
	@Query(value = """
				SELECT
				    date_trunc('minute', viewed_at) AS bucket,
				    COUNT(DISTINCT visitor_hash)::bigint AS uniqueVisitors,
				    COUNT(*)::bigint AS pageviews
				FROM pageviews
				WHERE site_id = :siteId
			  AND event_type = 'pageview'
				  AND viewed_at >= :fromInclusive
				  AND viewed_at < :toExclusive
				GROUP BY date_trunc('minute', viewed_at)
				ORDER BY date_trunc('minute', viewed_at)
				""", nativeQuery = true)
	List<VisitorTimeseriesProjection> findMinuteBuckets(@Param("siteId") Long siteId,
			@Param("fromInclusive") OffsetDateTime fromInclusive,
			@Param("toExclusive") OffsetDateTime toExclusive);
	
	/**
	 * Removes expired raw data in one database statement.
	 * Deleting at the storage layer keeps the retention job lightweight and avoids loading old page views into
	 * application memory.
	 *
	 * @param cutoff cutoff timestamp
	 * @return number of deleted rows
	 */
	@Modifying
	@Query(value = "DELETE FROM pageviews WHERE viewed_at < :cutoff", nativeQuery = true)
	int deleteOlderThan(@Param("cutoff") OffsetDateTime cutoff);
	
	/**
	 * Counts live visitors from ingestion timestamps.
	 * Using the ingested time keeps delayed heartbeats eligible for live presence as long as they arrived
	 * within the active window.
	 *
	 * @param siteId site identifier
	 * @param fromTs start timestamp
	 * @return number of active visitors in the live window
	 */
	@Query(value = """
			SELECT COUNT(DISTINCT visitor_hash)::bigint
			FROM pageviews
			WHERE site_id = :siteId
			  AND event_type = 'heartbeat'
			  AND COALESCE(created_at, viewed_at) >= :fromTs
			""", nativeQuery = true)
	Long countDistinctVisitorsByIngestedSince(@Param("siteId") Long siteId,
			@Param("fromTs") OffsetDateTime fromTs);
	
	// Keep only the latest heartbeat per visitor in the live window, then group
	// visitors by page_url. This keeps topActivePages aligned with the
	// activeVisitors metric, which is also visitor-based.
	
	/**
	 * Keeps active-page ranking aligned with active-visitor counting.
	 * The query keeps only the latest heartbeat per visitor so page counts describe the same active population
	 * as the visitor total.
	 *
	 * @param siteId site identifier
	 * @param fromTs start timestamp
	 * @param pageable pagination request
	 * @return ranked active pages
	 */
	@Query(value = """
			WITH last_seen AS (
			  SELECT
			    visitor_hash,
			    page_url,
			    ROW_NUMBER() OVER (
			      PARTITION BY visitor_hash
			      ORDER BY COALESCE(created_at, viewed_at) DESC, id DESC
			    ) AS rn
			  FROM pageviews
			  WHERE site_id = :siteId
			    AND event_type = 'heartbeat'
			    AND visitor_hash IS NOT NULL
			    AND COALESCE(created_at, viewed_at) >= :fromTs
			)
			SELECT
			  page_url AS pageUrl,
			  COUNT(*)::bigint AS visitors
			FROM last_seen
			WHERE rn = 1
			GROUP BY page_url
			ORDER BY visitors DESC, page_url ASC
			""", nativeQuery = true)
	List<LivePageProjection> findTopActivePagesByIngestedSince(@Param("siteId") Long siteId,
			@Param("fromTs") OffsetDateTime fromTs,
			Pageable pageable);
	
	/**
	 * Wraps live-page ranking in a bounded convenience method.
	 * The overload hides pagination plumbing and still enforces a minimum positive limit before the query
	 * runs.
	 *
	 * @param siteId site identifier
	 * @param fromTs start timestamp
	 * @param limit maximum number of rows to return
	 * @return ranked active pages
	 */
	default List<LivePageProjection> findTopActivePagesByIngestedSince(Long siteId,
			OffsetDateTime fromTs,
			int limit) {
		int safeLimit = Math.max(1, limit);
		
		return findTopActivePagesByIngestedSince(siteId, fromTs, PageRequest.of(0, safeLimit));
	}
	
}
