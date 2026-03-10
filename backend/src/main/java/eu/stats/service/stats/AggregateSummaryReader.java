package eu.stats.service.stats;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import eu.stats.repository.DailyStatRepository;
import eu.stats.repository.PageViewRepository;
import eu.stats.repository.projection.OverviewAggregateProjection;
import eu.stats.util.DateUtil;

/**
 * Reads overview totals with a cheap-first fallback strategy.
 * It prefers aggregated tables for normal traffic but can fall back to raw page view data so dashboards
 * still work before background jobs have populated summaries.
 */
@Component
public class AggregateSummaryReader {
	
	private final DailyStatRepository dailyStatRepository;
	private final PageViewRepository pageViewRepository;
	
	public AggregateSummaryReader(DailyStatRepository dailyStatRepository, PageViewRepository pageViewRepository) {
		this.dailyStatRepository = dailyStatRepository;
		this.pageViewRepository = pageViewRepository;
	}
	
	/**
	 * Uses a cheap-first summary path.
	 * Aggregated daily rows are preferred because they are cheaper to query, but the fallback keeps dashboards
	 * usable before refresh jobs finish.
	 *
	 * @param siteId site identifier
	 * @param range  date range
	 * @return summary totals for the requested range
	 */
	public SummaryTotals readTotals(Long siteId, DateUtil.DateRange range) {
		return readTotals(siteId, range.from(), range.to());
	}
	
	/**
	 * Uses a cheap-first summary path.
	 * Aggregated daily rows are preferred because they are cheaper to query, but the fallback keeps dashboards
	 * usable before refresh jobs finish.
	 *
	 * @param siteId site identifier
	 * @param fromDate start date
	 * @param toDate end date
	 * @return summary totals for the requested range
	 */
	public SummaryTotals readTotals(Long siteId, LocalDate fromDate, LocalDate toDate) {
		SummaryTotals aggregatedTotals = toSummaryTotals(dailyStatRepository.summarizeRange(siteId, fromDate, toDate));
		if (aggregatedTotals.hasData()) {
			return aggregatedTotals;
		}
		
		return toSummaryTotals(pageViewRepository.summarizeRange(siteId, fromDate, toDate));
	}
	
	private SummaryTotals toSummaryTotals(OverviewAggregateProjection projection) {
		if (projection == null) {
			return SummaryTotals.empty();
		}
		
		return new SummaryTotals(longValue(projection.getTotalPageviews()), longValue(projection.getUniqueVisitors()));
	}
	
	private long longValue(Long value) {
		return value == null ? 0L : value;
	}
	
	/**
	 * Bundles related values that should travel together.
	 * Keeping this nested record inside aggregate summary reader prevents closely related analytics values
	 * from drifting apart as separate arguments or map entries.
	 *
	 * @param totalPageviews total page views
	 * @param uniqueVisitors unique visitors
	 */
	public record SummaryTotals(long totalPageviews, long uniqueVisitors) {
		
		/**
		 * Provides a zero-value summary object.
		 * Returning an object instead of null keeps fallback handling explicit and avoids null checks across
		 * summary consumers.
		 *
		 * @return zero-value summary totals
		 */
		public static SummaryTotals empty() {
			return new SummaryTotals(0L, 0L);
		}
		
		/**
		 * Distinguishes real aggregates from the zero-value fallback.
		 * Callers use this guard to decide whether a fallback query against raw page views is still needed.
		 *
		 * @return true when any summary metric is present
		 */
		public boolean hasData() {
			return totalPageviews > 0L || uniqueVisitors > 0L;
		}
	}
}
