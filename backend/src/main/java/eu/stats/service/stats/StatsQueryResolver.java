package eu.stats.service.stats;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import eu.stats.service.SiteService;
import eu.stats.util.DateUtil;
import eu.stats.util.VisitorSeriesMode;

/**
 * Keeps date-range and interval rules in one place.
 * Centralizing these heuristics prevents controllers and services from drifting on special cases such as
 * live windows or custom ranges.
 */
@Component
public class StatsQueryResolver {
	
	private final SiteService siteService;
	private final DateUtil dateUtil;
	
	public StatsQueryResolver(SiteService siteService, DateUtil dateUtil) {
		this.siteService = siteService;
		this.dateUtil = dateUtil;
	}
	
	/**
	 * Keeps range resolution behind one shared rule set.
	 * Centralizing the logic prevents controllers and services from drifting on named periods, custom ranges,
	 * and missing-site checks.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from   start date
	 * @param to     end date
	 * @return resolved date range
	 */
	public DateUtil.DateRange resolveRange(Long siteId, String period, LocalDate from, LocalDate to) {
		validateSite(siteId);
		return resolveRange(period, from, to);
	}
	
	/**
	 * Keeps range resolution behind one shared rule set.
	 * Centralizing the logic prevents controllers and services from drifting on named periods, custom ranges,
	 * and missing-site checks.
	 *
	 * @param period reporting period
	 * @param from start date
	 * @param to end date
	 * @return resolved date range
	 */
	public DateUtil.DateRange resolveRange(String period, LocalDate from, LocalDate to) {
		return dateUtil.resolveDateRange(period, from, to);
	}
	
	/**
	 * Enforces a minimum positive limit.
	 * The guard prevents zero or negative values from leaking into repository paging logic.
	 *
	 * @param limit maximum number of rows to return
	 * @return safe limit value
	 */
	public int resolveLimit(int limit) {
		return Math.max(1, limit);
	}
	
	/**
	 * Captures the short-window exception for minute buckets.
	 * Keeping the rule here prevents special-case chart logic from spreading across controllers and services.
	 *
	 * @param period reporting period
	 * @param interval requested interval
	 * @return true when minute buckets should be used
	 */
	public boolean usesMinuteSeries(String period, String interval) {
		return "1h".equals(period) || VisitorSeriesMode.MINUTE.matchesInterval(interval);
	}
	
	/**
	 * Captures the special case for the rolling hourly view.
	 * The dashboard can request today while the server still decides when the last twenty-four hours are a
	 * better representation than calendar-day buckets.
	 *
	 * @param period reporting period
	 * @param interval requested interval
	 * @return true when the rolling hourly series should be used
	 */
	public boolean usesRolling24HourSeries(String period, String interval) {
		return "today".equals(period)
				&& (interval == null || interval.isBlank() || VisitorSeriesMode.HOUR.matchesInterval(interval));
	}
	
	/**
	 * Translates request input into an internal bucket strategy.
	 * The indirection keeps user-facing interval text decoupled from the enum used by query code.
	 *
	 * @param interval requested interval
	 * @param range date range
	 * @return resolved visitor series mode
	 */
	public VisitorSeriesMode resolveVisitorSeriesMode(String interval, DateUtil.DateRange range) {
		String resolvedInterval = dateUtil.resolveInterval(interval, range);
		return VisitorSeriesMode.fromInterval(resolvedInterval);
	}
	
	/**
	 * Reuses the shared site lookup as a guard clause.
	 * Downstream read paths can assume the site exists once this method returns.
	 *
	 * @param siteId site identifier
	 */
	public void validateSite(Long siteId) {
		siteService.getPublicSite(siteId);
	}
}
