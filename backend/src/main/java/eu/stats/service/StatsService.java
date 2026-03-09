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
import eu.stats.repository.projection.TopPageProjection;
import eu.stats.repository.projection.TopReferrerProjection;
import eu.stats.service.stats.AggregateSummaryReader;
import eu.stats.service.stats.StatsQueryResolver;
import eu.stats.service.stats.VisitorTimeSeriesService;
import eu.stats.util.DateUtil;

@Service
public class StatsService {
	
	private static final String UNKNOWN_LABEL = "Unknown";
	private static final String DIRECT_REFERRER = "(direct)";
	private static final String EMPTY_VALUE = "";
	private static final int OVERVIEW_TOP_LIMIT = 1;
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
	
	public OverviewStatsResponse overview(Long siteId, String period, LocalDate from, LocalDate to) {
		DateUtil.DateRange range = statsQueryResolver.resolveRange(siteId, period, from, to);
		DateUtil.DateRange previousRange = dateUtil.previous(range);
		
		AggregateSummaryReader.SummaryTotals currentTotals = aggregateSummaryReader.readTotals(siteId, range);
		AggregateSummaryReader.SummaryTotals previousTotals = aggregateSummaryReader.readTotals(siteId, previousRange);
		
		List<TopPageProjection> topPages = pageStatRepository.findTopPages(siteId, range.from(), range.to(), OVERVIEW_TOP_LIMIT);
		List<TopReferrerProjection> topReferrers = referrerStatRepository.findTopReferrers(siteId, range.from(), range.to(),
				OVERVIEW_TOP_LIMIT);
		List<TopGeoProjection> topCountries = geoStatRepository.findTopCountries(siteId, range.from(), range.to());
		
		return new OverviewStatsResponse(
				new OverviewStatsResponse.Period(range.from().toString(), range.to().toString()),
				currentTotals.totalPageviews(),
				currentTotals.uniqueVisitors(),
				topPages.isEmpty() ? EMPTY_VALUE : topPages.getFirst().getPageUrl(),
				topReferrers.isEmpty() ? EMPTY_VALUE : topReferrers.getFirst().getReferrer(),
				topCountries.isEmpty() ? EMPTY_VALUE : topCountries.getFirst().getCountry(),
				new OverviewStatsResponse.Comparison(
						percentChange(currentTotals.totalPageviews(), previousTotals.totalPageviews()),
						percentChange(currentTotals.uniqueVisitors(), previousTotals.uniqueVisitors())));
	}
	
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
	
	public PageStatsResponse topPages(Long siteId, String period, LocalDate from, LocalDate to, int limit) {
		DateUtil.DateRange range = statsQueryResolver.resolveRange(siteId, period, from, to);
		int resolvedLimit = statsQueryResolver.resolveLimit(limit);
		List<PageStatsResponse.Item> data = pageStatRepository.findTopPages(siteId, range.from(), range.to(), resolvedLimit).stream()
				.map(page -> new PageStatsResponse.Item(page.getPageUrl(), page.getPageTitle(), page.getPageviews(), page.getUniqueVisitors()))
				.toList();
		
		return new PageStatsResponse(data);
	}
	
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
	
	public GeoStatsResponse geo(Long siteId, String period, LocalDate from, LocalDate to) {
		DateUtil.DateRange range = statsQueryResolver.resolveRange(siteId, period, from, to);
		List<TopGeoProjection> rows = geoStatRepository.findTopCountries(siteId, range.from(), range.to());
		long totalVisits = rows.stream().mapToLong(TopGeoProjection::getVisits).sum();
		List<GeoStatsResponse.Item> data = rows.stream()
				.map(row -> new GeoStatsResponse.Item(
						row.getCountry(),
						countryName(row.getCountry()),
						row.getVisits(),
						row.getUniqueVisitors(),
						percentage(row.getVisits(), totalVisits)))
				.toList();
		
		return new GeoStatsResponse(data);
	}
	
	public DeviceStatsResponse devices(Long siteId, String period, LocalDate from, LocalDate to) {
		DateUtil.DateRange range = statsQueryResolver.resolveRange(siteId, period, from, to);
		List<DeviceBreakdownProjection> rows = deviceStatRepository.findBreakdown(siteId, range.from(), range.to());
		
		return buildDeviceStatsResponse(rows);
	}
	
	public EventStatsResponse events(Long siteId, String period, LocalDate from, LocalDate to) {
		DateUtil.DateRange range = statsQueryResolver.resolveRange(siteId, period, from, to);
		List<EventStatsResponse.Item> data = eventStatRepository.findEvents(siteId, range.from(), range.to()).stream()
				.map(row -> new EventStatsResponse.Item(row.getEventName(), row.getEventCount(), row.getUniqueVisitors()))
				.toList();
		
		return new EventStatsResponse(data);
	}
	
	public RealTimeResponse realtime(Long siteId) {
		statsQueryResolver.validateSite(siteId);
		
		return realTimeService.getRealtime(siteId);
	}
	
	private DeviceStatsResponse buildDeviceStatsResponse(List<DeviceBreakdownProjection> rows) {
		long totalVisits = rows.stream().mapToLong(this::visits).sum();
		Map<String, Long> visitsByDeviceType = new HashMap<>();
		Map<TechnologyKey, Long> visitsByBrowser = new HashMap<>();
		Map<TechnologyKey, Long> visitsByOperatingSystem = new HashMap<>();
		Map<String, Long> visitsByResolution = new HashMap<>();
		
		for (DeviceBreakdownProjection row : rows) {
			long visits = visits(row);
			visitsByDeviceType.merge(defaultString(row.getDeviceType(), UNKNOWN_LABEL), visits, Long::sum);
			visitsByBrowser.merge(
					new TechnologyKey(defaultString(row.getBrowser(), UNKNOWN_LABEL), defaultString(row.getBrowserVersion(), EMPTY_VALUE)),
					visits,
					Long::sum);
			visitsByOperatingSystem.merge(
					new TechnologyKey(defaultString(row.getOs(), UNKNOWN_LABEL), defaultString(row.getOsVersion(), EMPTY_VALUE)),
					visits,
					Long::sum);
			visitsByResolution.merge(defaultString(row.getScreenResolution(), UNKNOWN_LABEL), visits, Long::sum);
		}
		
		return new DeviceStatsResponse(
				toDeviceItems(visitsByDeviceType, totalVisits),
				toTechItems(visitsByBrowser, totalVisits),
				toTechItems(visitsByOperatingSystem, totalVisits),
				toResolutionItems(visitsByResolution, totalVisits));
	}
	
	private List<DeviceStatsResponse.DeviceItem> toDeviceItems(Map<String, Long> visitsByDeviceType, long totalVisits) {
		return visitsByDeviceType.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.map(entry -> new DeviceStatsResponse.DeviceItem(
						entry.getKey(),
						entry.getValue(),
						percentage(entry.getValue(), totalVisits)))
				.toList();
	}
	
	private List<DeviceStatsResponse.TechItem> toTechItems(Map<TechnologyKey, Long> visitsByTechnology, long totalVisits) {
		return visitsByTechnology.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.limit(TOP_BREAKDOWN_LIMIT)
				.map(entry -> new DeviceStatsResponse.TechItem(
						entry.getKey().name(),
						entry.getKey().version(),
						entry.getValue(),
						percentage(entry.getValue(), totalVisits)))
				.toList();
	}
	
	private List<DeviceStatsResponse.ResolutionItem> toResolutionItems(Map<String, Long> visitsByResolution, long totalVisits) {
		return visitsByResolution.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.limit(TOP_BREAKDOWN_LIMIT)
				.map(entry -> new DeviceStatsResponse.ResolutionItem(
						entry.getKey(),
						entry.getValue(),
						percentage(entry.getValue(), totalVisits)))
				.toList();
	}
	
	private long visits(DeviceBreakdownProjection row) {
		return row.getVisits() == null ? 0L : row.getVisits();
	}
	
	private String defaultString(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}
	
	private String countryName(String countryCode) {
		if (countryCode == null || countryCode.isBlank()) {
			return UNKNOWN_LABEL;
		}
		
		Locale locale = new Locale("", countryCode);
		String name = locale.getDisplayCountry(Locale.ENGLISH);
		
		return name.isBlank() ? countryCode : name;
	}
	
	private double percentage(long part, long total) {
		if (total <= 0L) {
			return 0.0;
		}
		
		return Math.round(((double) part / total) * 1000.0) / 10.0;
	}
	
	private double percentChange(double current, double previous) {
		if (previous == 0.0) {
			return current == 0.0 ? 0.0 : 100.0;
		}
		
		return Math.round(((current - previous) / previous) * 1000.0) / 10.0;
	}
	
	private record TechnologyKey(String name, String version) {
	}
}
