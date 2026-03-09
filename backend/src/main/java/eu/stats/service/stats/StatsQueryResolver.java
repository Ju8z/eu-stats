package eu.stats.service.stats;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import eu.stats.service.SiteService;
import eu.stats.util.DateUtil;
import eu.stats.util.VisitorSeriesMode;

@Component
public class StatsQueryResolver {
	
	private final SiteService siteService;
	private final DateUtil dateUtil;
	
	public StatsQueryResolver(SiteService siteService, DateUtil dateUtil) {
		this.siteService = siteService;
		this.dateUtil = dateUtil;
	}
	
	public DateUtil.DateRange resolveRange(Long siteId, String period, LocalDate from, LocalDate to) {
		validateSite(siteId);
		return resolveRange(period, from, to);
	}
	
	public DateUtil.DateRange resolveRange(String period, LocalDate from, LocalDate to) {
		return dateUtil.resolveDateRange(period, from, to);
	}
	
	public int resolveLimit(int limit) {
		return Math.max(1, limit);
	}
	
	public boolean usesMinuteSeries(String period, String interval) {
		return "1h".equals(period) || VisitorSeriesMode.MINUTE.matchesInterval(interval);
	}
	
	public boolean usesRolling24HourSeries(String period, String interval) {
		return "today".equals(period)
				&& (interval == null || interval.isBlank() || VisitorSeriesMode.HOUR.matchesInterval(interval));
	}
	
	public VisitorSeriesMode resolveVisitorSeriesMode(String interval, DateUtil.DateRange range) {
		String resolvedInterval = dateUtil.resolveInterval(interval, range);
		return VisitorSeriesMode.fromInterval(resolvedInterval);
	}
	
	public void validateSite(Long siteId) {
		siteService.getPublicSite(siteId);
	}
}
