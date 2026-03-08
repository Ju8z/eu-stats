package eu.stats.repository;

import java.time.OffsetDateTime;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PageViewAggregationRepository {
	
	private static final String DAILY_STATS_SQL = """
			INSERT INTO daily_stats (
			    site_id, stat_date, total_pageviews, unique_visitors
			)
			SELECT
			    pv.site_id,
			    DATE(pv.viewed_at) AS stat_date,
			    COUNT(*) AS total_pageviews,
			    COUNT(DISTINCT pv.visitor_hash) AS unique_visitors
			FROM pageviews pv
			WHERE pv.viewed_at >= :viewedSince
			GROUP BY pv.site_id, DATE(pv.viewed_at)
			ON CONFLICT (site_id, stat_date)
			DO UPDATE SET
			    total_pageviews = EXCLUDED.total_pageviews,
			    unique_visitors = EXCLUDED.unique_visitors
			""";
	
	private static final String HOURLY_STATS_SQL = """
			INSERT INTO hourly_stats (site_id, stat_hour, total_pageviews, unique_visitors)
			SELECT
			    pv.site_id,
			    date_trunc('hour', pv.viewed_at) AS stat_hour,
			    COUNT(*) AS total_pageviews,
			    COUNT(DISTINCT pv.visitor_hash) AS unique_visitors
			FROM pageviews pv
			WHERE pv.viewed_at >= :viewedSince
			GROUP BY pv.site_id, date_trunc('hour', pv.viewed_at)
			ON CONFLICT (site_id, stat_hour)
			DO UPDATE SET
			    total_pageviews = EXCLUDED.total_pageviews,
			    unique_visitors = EXCLUDED.unique_visitors
			""";
	
	private static final String PAGE_STATS_SQL = """
			INSERT INTO page_stats (site_id, stat_date, page_url, page_title, pageviews, unique_visitors)
			SELECT
			    pv.site_id,
			    DATE(pv.viewed_at),
			    pv.page_url,
			    MAX(pv.page_title),
			    COUNT(*),
			    COUNT(DISTINCT pv.visitor_hash)
			FROM pageviews pv
			WHERE pv.viewed_at >= :viewedSince
			GROUP BY pv.site_id, DATE(pv.viewed_at), pv.page_url
			ON CONFLICT (site_id, stat_date, page_url)
			DO UPDATE SET
			    page_title = EXCLUDED.page_title,
			    pageviews = EXCLUDED.pageviews,
			    unique_visitors = EXCLUDED.unique_visitors
			""";
	
	private static final String REFERRER_STATS_SQL = """
			INSERT INTO referrer_stats (site_id, stat_date, referrer, referrer_category, visits, unique_visitors)
			SELECT
			    pv.site_id,
			    DATE(pv.viewed_at),
			    COALESCE(NULLIF(pv.referrer, ''), '(direct)'),
			    COALESCE(NULLIF(pv.referrer_category, ''), 'direct'),
			    COUNT(*),
			    COUNT(DISTINCT pv.visitor_hash)
			FROM pageviews pv
			WHERE pv.viewed_at >= :viewedSince
			GROUP BY pv.site_id, DATE(pv.viewed_at), COALESCE(NULLIF(pv.referrer, ''), '(direct)'), COALESCE(NULLIF(pv.referrer_category, ''), 'direct')
			ON CONFLICT (site_id, stat_date, referrer)
			DO UPDATE SET
			    referrer_category = EXCLUDED.referrer_category,
			    visits = EXCLUDED.visits,
			    unique_visitors = EXCLUDED.unique_visitors
			""";
	
	private static final String GEO_STATS_SQL = """
			INSERT INTO geo_stats (site_id, stat_date, country, visits, unique_visitors)
			SELECT
			    pv.site_id,
			    DATE(pv.viewed_at),
			    COALESCE(NULLIF(pv.country, ''), 'ZZ'),
			    COUNT(*),
			    COUNT(DISTINCT pv.visitor_hash)
			FROM pageviews pv
			WHERE pv.viewed_at >= :viewedSince
			GROUP BY pv.site_id, DATE(pv.viewed_at), COALESCE(NULLIF(pv.country, ''), 'ZZ')
			ON CONFLICT (site_id, stat_date, country)
			DO UPDATE SET
			    visits = EXCLUDED.visits,
			    unique_visitors = EXCLUDED.unique_visitors
			""";
	
	private static final String DEVICE_STATS_SQL = """
			INSERT INTO device_stats (
			    site_id, stat_date, device_type, browser, browser_version, os, os_version,
			    screen_resolution, visits, unique_visitors
			)
			SELECT
			    pv.site_id,
			    DATE(pv.viewed_at),
			    pv.device_type,
			    pv.browser,
			    pv.browser_version,
			    pv.os,
			    pv.os_version,
			    pv.screen_resolution,
			    COUNT(*),
			    COUNT(DISTINCT pv.visitor_hash)
			FROM pageviews pv
			WHERE pv.viewed_at >= :viewedSince
			GROUP BY
			    pv.site_id,
			    DATE(pv.viewed_at),
			    pv.device_type,
			    pv.browser,
			    pv.browser_version,
			    pv.os,
			    pv.os_version,
			    pv.screen_resolution
			ON CONFLICT (site_id, stat_date, device_type, browser, os, screen_resolution)
			DO UPDATE SET
			    browser_version = EXCLUDED.browser_version,
			    os_version = EXCLUDED.os_version,
			    visits = EXCLUDED.visits,
			    unique_visitors = EXCLUDED.unique_visitors
			""";
	
	private static final String EVENT_STATS_SQL = """
			INSERT INTO event_stats (site_id, stat_date, event_name, event_count, unique_visitors)
			SELECT
			    pv.site_id,
			    DATE(pv.viewed_at),
			    pv.event_name,
			    COUNT(*),
			    COUNT(DISTINCT pv.visitor_hash)
			FROM pageviews pv
			WHERE pv.viewed_at >= :viewedSince
			  AND pv.event_type <> 'pageview'
			  AND pv.event_name IS NOT NULL
			GROUP BY pv.site_id, DATE(pv.viewed_at), pv.event_name
			ON CONFLICT (site_id, stat_date, event_name)
			DO UPDATE SET
			    event_count = EXCLUDED.event_count,
			    unique_visitors = EXCLUDED.unique_visitors
			""";
	
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	public PageViewAggregationRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}
	
	public long countRecentPageviews(OffsetDateTime viewedSince) {
		Long count = namedParameterJdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM pageviews WHERE viewed_at >= :viewedSince",
				parameters(viewedSince),
				Long.class);
		
		return count == null ? 0L : count;
	}
	
	public int upsertDailyStats(OffsetDateTime viewedSince) {
		return namedParameterJdbcTemplate.update(DAILY_STATS_SQL, parameters(viewedSince));
	}
	
	public int upsertHourlyStats(OffsetDateTime viewedSince) {
		return namedParameterJdbcTemplate.update(HOURLY_STATS_SQL, parameters(viewedSince));
	}
	
	public int upsertPageStats(OffsetDateTime viewedSince) {
		return namedParameterJdbcTemplate.update(PAGE_STATS_SQL, parameters(viewedSince));
	}
	
	public int upsertReferrerStats(OffsetDateTime viewedSince) {
		return namedParameterJdbcTemplate.update(REFERRER_STATS_SQL, parameters(viewedSince));
	}
	
	public int upsertGeoStats(OffsetDateTime viewedSince) {
		return namedParameterJdbcTemplate.update(GEO_STATS_SQL, parameters(viewedSince));
	}
	
	public int upsertDeviceStats(OffsetDateTime viewedSince) {
		return namedParameterJdbcTemplate.update(DEVICE_STATS_SQL, parameters(viewedSince));
	}
	
	public int upsertEventStats(OffsetDateTime viewedSince) {
		return namedParameterJdbcTemplate.update(EVENT_STATS_SQL, parameters(viewedSince));
	}
	
	private MapSqlParameterSource parameters(OffsetDateTime viewedSince) {
		return new MapSqlParameterSource("viewedSince", viewedSince);
	}
}
