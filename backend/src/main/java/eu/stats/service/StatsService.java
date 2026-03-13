package eu.stats.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import eu.stats.dto.response.DeviceStatsResponse;
import eu.stats.dto.response.EventStatsResponse;
import eu.stats.dto.response.GeoStatsResponse;
import eu.stats.dto.response.OverviewStatsResponse;
import eu.stats.dto.response.PageStatsResponse;
import eu.stats.dto.response.RealTimeResponse;
import eu.stats.dto.response.ReferrerStatsResponse;
import eu.stats.dto.response.VisitorTimeSeriesResponse;
import eu.stats.repository.DeviceStatRepository;
import eu.stats.repository.EventStatRepository;
import eu.stats.repository.GeoStatRepository;
import eu.stats.repository.PageStatRepository;
import eu.stats.repository.ReferrerStatRepository;
import eu.stats.repository.projection.DeviceBreakdownProjection;
import eu.stats.repository.projection.TopGeoProjection;
import eu.stats.service.stats.AggregateSummaryReader;
import eu.stats.service.stats.StatsQueryResolver;
import eu.stats.service.stats.VisitorTimeSeriesService;
import eu.stats.util.DateUtil;

/**
 * Centralizes dashboard read rules.
 * It is the place where aggregated tables, live queries, fallback labels, and response shaping are kept
 * consistent across dashboard endpoints.
 */
@Service
public class StatsService {
	
	private static final String UNKNOWN_LABEL = "Unknown";
	private static final String DIRECT_REFERRER = "(direct)";
	private static final String EMPTY_VALUE = "";
	private static final int TOP_BREAKDOWN_LIMIT = 20;
	
	private final StatsQueryResolver statsQueryResolver;
	private final AggregateSummaryReader aggregateSummaryReader;
	private final VisitorTimeSeriesService visitorTimeSeriesService;
	private final DateUtil dateUtil;
	private final PageStatRepository pageStatRepository;
	private final ReferrerStatRepository referrerStatRepository;
	private final GeoStatRepository geoStatRepository;
	private final DeviceStatRepository deviceStatRepository;
	private final EventStatRepository eventStatRepository;
	private final RealTimeService realTimeService;
	
	public StatsService(
			StatsQueryResolver statsQueryResolver,
			AggregateSummaryReader aggregateSummaryReader,
			VisitorTimeSeriesService visitorTimeSeriesService,
			DateUtil dateUtil,
			PageStatRepository pageStatRepository,
			ReferrerStatRepository referrerStatRepository,
			GeoStatRepository geoStatRepository,
			DeviceStatRepository deviceStatRepository,
			EventStatRepository eventStatRepository,
			RealTimeService realTimeService) {
		this.statsQueryResolver = statsQueryResolver;
		this.aggregateSummaryReader = aggregateSummaryReader;
		this.visitorTimeSeriesService = visitorTimeSeriesService;
		this.dateUtil = dateUtil;
		this.pageStatRepository = pageStatRepository;
		this.referrerStatRepository = referrerStatRepository;
		this.geoStatRepository = geoStatRepository;
		this.deviceStatRepository = deviceStatRepository;
		this.eventStatRepository = eventStatRepository;
		this.realTimeService = realTimeService;
	}
	
	/**
	 * Combines current and previous windows in one place.
	 * That keeps comparison percentages and summary totals consistent across every overview card without
	 * asking the frontend to coordinate multiple analytics calls.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from   start date
	 * @param to     end date
	 * @return overview response for the requested site and window
	 */
	public OverviewStatsResponse overview(Long siteId, String period, LocalDate from, LocalDate to) {
		DateUtil.DateRange range = statsQueryResolver.resolveRange(siteId, period, from, to);
		DateUtil.DateRange previousRange = dateUtil.previous(range);
		
		AggregateSummaryReader.SummaryTotals currentTotals = aggregateSummaryReader.readTotals(siteId, range);
		AggregateSummaryReader.SummaryTotals previousTotals = aggregateSummaryReader.readTotals(siteId, previousRange);
		
		return new OverviewStatsResponse(
				currentTotals.totalPageviews(),
				currentTotals.uniqueVisitors(),
				new OverviewStatsResponse.Comparison(
						percentChange(currentTotals.totalPageviews(), previousTotals.totalPageviews()),
						percentChange(currentTotals.uniqueVisitors(), previousTotals.uniqueVisitors())));
	}
	
	/**
	 * Chooses the cheapest accurate bucket strategy for visitor charts.
	 * Special live windows stay on minute or rolling-hour data, while longer ranges move to aggregate tables
	 * so the client does not need to understand storage details.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from start date
	 * @param to end date
	 * @param interval requested interval
	 * @return visitor time series response
	 */
	public VisitorTimeSeriesResponse visitors(Long siteId, String period, LocalDate from, LocalDate to, String interval) {
		statsQueryResolver.validateSite(siteId);
		
		if (statsQueryResolver.usesMinuteSeries(period, interval)) {
			return visitorTimeSeriesService.getSeries(siteId, eu.stats.util.VisitorSeriesMode.MINUTE, null);
		}
		
		if (statsQueryResolver.usesRolling24HourSeries(period, interval)) {
			return visitorTimeSeriesService.getSeries(siteId, eu.stats.util.VisitorSeriesMode.HOUR, null);
		}
		
		DateUtil.DateRange range = statsQueryResolver.resolveRange(period, from, to);
		
		return visitorTimeSeriesService.getSeries(siteId, statsQueryResolver.resolveVisitorSeriesMode(interval, range), range);
	}
	
	/**
	 * Applies ranking limits and response shaping close to the read model.
	 * Keeping the limit guard here prevents callers from triggering unbounded leaderboard queries and keeps
	 * ranked page data consistent across endpoints.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from start date
	 * @param to end date
	 * @param limit maximum number of rows to return
	 * @return page ranking response
	 */
	public PageStatsResponse topPages(Long siteId, String period, LocalDate from, LocalDate to, int limit) {
		DateUtil.DateRange range = statsQueryResolver.resolveRange(siteId, period, from, to);
		int resolvedLimit = statsQueryResolver.resolveLimit(limit);
		List<PageStatsResponse.Item> data = pageStatRepository.findTopPages(siteId, range.from(), range.to(), resolvedLimit).stream()
				.map(page -> new PageStatsResponse.Item(page.getPageUrl(), page.getPageTitle(), page.getPageviews(), page.getUniqueVisitors()))
				.toList();
		
		return new PageStatsResponse(data);
	}
	
	/**
	 * Normalizes raw referrer values before they leave the service.
	 * Blank sources become direct traffic here so dashboard consumers do not need to understand
	 * storage-specific null or empty-string conventions.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from start date
	 * @param to end date
	 * @param limit maximum number of rows to return
	 * @return referrer ranking response
	 */
	public ReferrerStatsResponse topReferrers(Long siteId, String period, LocalDate from, LocalDate to, int limit) {
		DateUtil.DateRange range = statsQueryResolver.resolveRange(siteId, period, from, to);
		int resolvedLimit = statsQueryResolver.resolveLimit(limit);
		List<ReferrerStatsResponse.Item> data = referrerStatRepository.findTopReferrers(siteId, range.from(), range.to(), resolvedLimit)
				.stream()
				.map(referrer -> new ReferrerStatsResponse.Item(
						defaultString(referrer.getReferrer(), DIRECT_REFERRER),
						referrer.getReferrerCategory(),
						referrer.getVisits(),
						referrer.getUniqueVisitors()))
				.toList();
		
		return new ReferrerStatsResponse(data);
	}
	
	/**
	 * Adds presentation-focused geography labels on the server side.
	 * Country names are derived here so repository queries can stay storage-oriented while the frontend
	 * receives the only fields it currently renders.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from start date
	 * @param to end date
	 * @return geographic breakdown response
	 */
	public GeoStatsResponse geo(Long siteId, String period, LocalDate from, LocalDate to) {
		DateUtil.DateRange range = statsQueryResolver.resolveRange(siteId, period, from, to);
		List<TopGeoProjection> rows = geoStatRepository.findTopCountries(siteId, range.from(), range.to());
		List<GeoStatsResponse.Item> data = rows.stream()
				.map(row -> new GeoStatsResponse.Item(
						countryName(row.getCountry()),
						row.getVisits()))
				.toList();
		
		return new GeoStatsResponse(data);
	}
	
	/**
	 * Aggregates technical dimensions into the slices the dashboard actually renders.
	 * Doing this on the server keeps grouping rules consistent and avoids returning unused browser and
	 * percentage fields to the frontend.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from start date
	 * @param to end date
	 * @return device breakdown response
	 */
	public DeviceStatsResponse devices(Long siteId, String period, LocalDate from, LocalDate to) {
		DateUtil.DateRange range = statsQueryResolver.resolveRange(siteId, period, from, to);
		List<DeviceBreakdownProjection> rows = deviceStatRepository.findBreakdown(siteId, range.from(), range.to());
		
		return buildDeviceStatsResponse(rows);
	}
	
	/**
	 * Keeps event reporting isolated from page view reporting.
	 * That separation preserves flexibility for custom event names while reusing the same date-range rules as
	 * other analytics reads.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from start date
	 * @param to end date
	 * @return event breakdown response
	 */
	public EventStatsResponse events(Long siteId, String period, LocalDate from, LocalDate to) {
		DateUtil.DateRange range = statsQueryResolver.resolveRange(siteId, period, from, to);
		List<EventStatsResponse.Item> data = eventStatRepository.findEvents(siteId, range.from(), range.to()).stream()
				.map(row -> new EventStatsResponse.Item(row.getEventName(), row.getEventCount(), row.getUniqueVisitors()))
				.toList();
		
		return new EventStatsResponse(data);
	}
	
	/**
	 * Validates the site before running live queries.
	 * Failing early avoids wasting work on heartbeat queries for missing sites and keeps the live endpoint
	 * consistent with the rest of the application.
	 *
	 * @param siteId site identifier
	 * @return real-time response
	 */
	public RealTimeResponse realtime(Long siteId) {
		statsQueryResolver.validateSite(siteId);
		
		return realTimeService.getRealtime(siteId);
	}
	
	/**
	 * Collapses raw device rows into the exact slices the dashboard expects.
	 * Doing the grouping here keeps repository queries storage-oriented while the response model stays stable
	 * even if the underlying dimensions grow more detailed.
	 *
	 * @param rows raw device breakdown rows
	 * @return device response shaped for dashboard charts
	 */
	private DeviceStatsResponse buildDeviceStatsResponse(List<DeviceBreakdownProjection> rows) {
		Map<String, Long> visitsByDeviceType = new HashMap<>();
		Map<TechnologyKey, Long> visitsByOperatingSystem = new HashMap<>();
		
		for (DeviceBreakdownProjection row : rows) {
			long visits = visits(row);
			visitsByDeviceType.merge(defaultString(row.getDeviceType(), UNKNOWN_LABEL), visits, Long::sum);
			visitsByOperatingSystem.merge(
					new TechnologyKey(defaultString(row.getOs(), UNKNOWN_LABEL), defaultString(row.getOsVersion(), EMPTY_VALUE)),
					visits,
					Long::sum);
		}
		
		return new DeviceStatsResponse(
				toDeviceItems(visitsByDeviceType),
				toTechItems(visitsByOperatingSystem));
	}
	
	/**
	 * Applies presentation ordering only after device totals have been merged.
	 * Keeping sorting here avoids leaking chart concerns back into the aggregation loop.
	 *
	 * @param visitsByDeviceType merged visit totals by device label
	 * @return sorted device chart items
	 */
	private List<DeviceStatsResponse.DeviceItem> toDeviceItems(Map<String, Long> visitsByDeviceType) {
		return visitsByDeviceType.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.map(entry -> new DeviceStatsResponse.DeviceItem(
						entry.getKey(),
						entry.getValue()))
				.toList();
	}
	
	/**
	 * Applies the dashboard cap after operating-system totals have been combined.
	 * Limiting here ensures the chart keeps the real leaders instead of trimming detail too early during
	 * accumulation.
	 *
	 * @param visitsByTechnology merged visit totals by operating-system key
	 * @return sorted operating-system chart items
	 */
	private List<DeviceStatsResponse.TechItem> toTechItems(Map<TechnologyKey, Long> visitsByTechnology) {
		return visitsByTechnology.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.limit(TOP_BREAKDOWN_LIMIT)
				.map(entry -> new DeviceStatsResponse.TechItem(
						entry.getKey().name(),
						entry.getKey().version(),
						entry.getValue()))
				.toList();
	}
	
	/**
	 * Treats missing aggregate counts as zero before totals are merged or sorted.
	 * That keeps breakdown ranking deterministic even when repository projections omit a value.
	 *
	 * @param row raw breakdown projection
	 * @return visit count with nulls normalized to zero
	 */
	private long visits(DeviceBreakdownProjection row) {
		return row.getVisits() == null ? 0L : row.getVisits();
	}
	
	/**
	 * Converts blank storage values into intentional labels before they reach the client.
	 * Normalizing once here keeps chart legends stable across breakdown endpoints.
	 *
	 * @param value candidate label value
	 * @param fallback fallback label when the value is blank
	 * @return normalized non-blank label
	 */
	private String defaultString(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}
	
	/**
	 * Expands stored country codes on the server so the frontend stays free of locale lookup logic.
	 * That keeps geography charts readable without shipping a second country-name mapping layer.
	 *
	 * @param countryCode stored country code
	 * @return display-friendly country label
	 */
	private String countryName(String countryCode) {
		if (countryCode == null || countryCode.isBlank()) {
			return UNKNOWN_LABEL;
		}
		
		Locale locale = new Locale("", countryCode);
		String name = locale.getDisplayCountry(Locale.ENGLISH);
		
		return name.isBlank() ? countryCode : name;
	}
	
	/**
	 * Centralizes comparison math for overview cards.
	 * Handling zero baselines in one helper keeps every percentage card consistent on edge cases that would
	 * otherwise invite ad hoc formulas.
	 *
	 * @param current current metric value
	 * @param previous previous-window metric value
	 * @return rounded percentage change for the overview response
	 */
	private double percentChange(double current, double previous) {
		if (previous == 0.0) {
			return current == 0.0 ? 0.0 : 100.0;
		}
		
		return Math.round(((current - previous) / previous) * 1000.0) / 10.0;
	}
	
	/**
	 * Keeps operating-system name and version paired while rows are being merged.
	 * Using a dedicated key avoids string-concatenation rules leaking into the grouping logic.
	 *
	 * @param name technology name
	 * @param version technology version
	 */
	private record TechnologyKey(String name, String version) {
	}
}
