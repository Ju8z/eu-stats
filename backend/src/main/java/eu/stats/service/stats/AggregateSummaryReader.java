package eu.stats.service.stats;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import eu.stats.repository.DailyStatRepository;
import eu.stats.repository.PageViewRepository;
import eu.stats.repository.projection.OverviewAggregateProjection;
import eu.stats.util.DateUtil;

@Component
public class AggregateSummaryReader {
	
	private final DailyStatRepository dailyStatRepository;
	private final PageViewRepository pageViewRepository;
	
	public AggregateSummaryReader(DailyStatRepository dailyStatRepository, PageViewRepository pageViewRepository) {
		this.dailyStatRepository = dailyStatRepository;
		this.pageViewRepository = pageViewRepository;
	}
	
	public SummaryTotals readTotals(Long siteId, DateUtil.DateRange range) {
		return readTotals(siteId, range.from(), range.to());
	}
	
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
	
	public record SummaryTotals(long totalPageviews, long uniqueVisitors) {
		
		public static SummaryTotals empty() {
			return new SummaryTotals(0L, 0L);
		}
		
		public boolean hasData() {
			return totalPageviews > 0L || uniqueVisitors > 0L;
		}
	}
}
