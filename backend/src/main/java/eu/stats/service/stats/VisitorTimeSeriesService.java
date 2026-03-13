package eu.stats.service.stats;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import eu.stats.dto.response.VisitorTimeSeriesResponse;
import eu.stats.entity.DailyStat;
import eu.stats.entity.HourlyStat;
import eu.stats.repository.DailyStatRepository;
import eu.stats.repository.HourlyStatRepository;
import eu.stats.repository.PageViewRepository;
import eu.stats.repository.projection.VisitorTimeseriesProjection;
import eu.stats.util.DateUtil;
import eu.stats.util.VisitorSeriesMode;

/**
 * Translates stored aggregates into chart-ready visitor series.
 * The service chooses the cheapest accurate source for each time window so charts stay responsive without
 * forcing callers to know the storage strategy.
 */
@Service
public class VisitorTimeSeriesService {
	
	private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:00XXX");
	private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00:00XXX");
	
	private final DailyStatRepository dailyStatRepository;
	private final HourlyStatRepository hourlyStatRepository;
	private final PageViewRepository pageViewRepository;
	private final DateUtil dateUtil;
	private final Clock clock;
	
	public VisitorTimeSeriesService(
			DailyStatRepository dailyStatRepository,
			HourlyStatRepository hourlyStatRepository,
			PageViewRepository pageViewRepository,
			DateUtil dateUtil,
			Clock clock) {
		this.dailyStatRepository = dailyStatRepository;
		this.hourlyStatRepository = hourlyStatRepository;
		this.pageViewRepository = pageViewRepository;
		this.dateUtil = dateUtil;
		this.clock = clock;
	}
	
	/**
	 * Routes chart reads to the cheapest accurate data source.
	 * The service chooses between live buckets, hourly aggregates, and daily aggregates so chart endpoints
	 * stay responsive across very different date windows.
	 *
	 * @param siteId site identifier
	 * @param mode   visitor series mode
	 * @param range  date range
	 * @return visitor time series response
	 */
	public VisitorTimeSeriesResponse getSeries(Long siteId, VisitorSeriesMode mode, DateUtil.DateRange range) {
		List<VisitorTimeSeriesResponse.Item> data = switch (mode) {
			case MINUTE -> minuteSeries(siteId);
			case HOUR -> range == null ? rolling24HourSeries(siteId) : hourlySeries(siteId, range);
			case WEEK -> weeklySeries(siteId, range);
			case MONTH -> monthlySeries(siteId, range);
			case DAY -> dailySeries(siteId, range);
		};
		
		return new VisitorTimeSeriesResponse(mode.responseInterval(), data);
	}
	
	/**
	 * Forces a fixed sixty-point window for the live minute chart.
	 * Filling gaps on the server keeps the frontend from having to infer missing minutes or guess whether the
	 * tracker has gone quiet.
	 *
	 * @param siteId site identifier
	 * @return minute-by-minute chart points for the last hour
	 */
	private List<VisitorTimeSeriesResponse.Item> minuteSeries(Long siteId) {
		OffsetDateTime endMinute = OffsetDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES);
		OffsetDateTime startMinute = endMinute.minusMinutes(59);
		OffsetDateTime toExclusive = endMinute.plusMinutes(1);
		
		Map<Long, BucketTotals> totalsByEpochMinute = toEpochBucketMap(pageViewRepository.findMinuteBuckets(siteId, startMinute, toExclusive), 60L);
		List<VisitorTimeSeriesResponse.Item> data = new ArrayList<>();
		
		for (int index = 0; index < 60; index++) {
			OffsetDateTime minute = startMinute.plusMinutes(index);
			long key = minute.toInstant().getEpochSecond() / 60L;
			BucketTotals totals = totalsByEpochMinute.getOrDefault(key, BucketTotals.empty());
			data.add(new VisitorTimeSeriesResponse.Item(minute.format(MINUTE_FORMATTER), totals.uniqueVisitors(), totals.pageviews()));
		}
		
		return data;
	}
	
	/**
	 * Preserves the rolling twenty-four-hour view even though the storage layer is bucketed by hour.
	 * Building the window here keeps the "today" dashboard card aligned with live activity rather than
	 * calendar-day boundaries.
	 *
	 * @param siteId site identifier
	 * @return hourly chart points for the last twenty-four hours
	 */
	private List<VisitorTimeSeriesResponse.Item> rolling24HourSeries(Long siteId) {
		OffsetDateTime endHour = OffsetDateTime.now(clock).truncatedTo(ChronoUnit.HOURS);
		OffsetDateTime startHour = endHour.minusHours(23);
		OffsetDateTime toExclusive = endHour.plusHours(1);
		
		Map<Long, BucketTotals> totalsByEpochHour = toEpochBucketMap(pageViewRepository.findHourlyBuckets(siteId, startHour, toExclusive), 3600L);
		List<VisitorTimeSeriesResponse.Item> data = new ArrayList<>();
		
		for (int index = 0; index < 24; index++) {
			OffsetDateTime hour = startHour.plusHours(index);
			long key = hour.toInstant().getEpochSecond() / 3600L;
			BucketTotals totals = totalsByEpochHour.getOrDefault(key, BucketTotals.empty());
			data.add(new VisitorTimeSeriesResponse.Item(hour.format(HOUR_FORMATTER), totals.uniqueVisitors(), totals.pageviews()));
		}
		
		return data;
	}
	
	/**
	 * Leaves daily data ungrouped when the requested window is already day-based.
	 * That keeps the simplest chart path easy to audit while still reusing the shared daily bucket loader.
	 *
	 * @param siteId site identifier
	 * @param range requested date range
	 * @return daily chart points
	 */
	private List<VisitorTimeSeriesResponse.Item> dailySeries(Long siteId, DateUtil.DateRange range) {
		return loadDailyBuckets(siteId, range).stream()
				.map(DayBucket::toItem)
				.toList();
	}
	
	/**
	 * Prefers pre-aggregated hourly rows but keeps raw data as a safety net.
	 * The fallback lets charts stay available even before the background aggregation job has populated the
	 * hourly table.
	 *
	 * @param siteId site identifier
	 * @param range requested date range
	 * @return hourly chart points
	 */
	private List<VisitorTimeSeriesResponse.Item> hourlySeries(Long siteId, DateUtil.DateRange range) {
		DateUtil.OffsetDateTimeRange dateTimeRange = dateUtil.toOffsetDateTimeRange(range);
		List<HourlyStat> rows = hourlyStatRepository.findBySiteIdAndStatHourBetweenOrderByStatHourAsc(
				siteId,
				dateTimeRange.fromInclusive(),
				dateTimeRange.toExclusive());
		
		if (!rows.isEmpty()) {
			return rows.stream()
					.map(row -> new VisitorTimeSeriesResponse.Item(
							row.getStatHour().format(HOUR_FORMATTER),
							row.getUniqueVisitors(),
							row.getTotalPageviews()))
					.toList();
		}
		
		return pageViewRepository.findHourlyBuckets(siteId, dateTimeRange.fromInclusive(), dateTimeRange.toExclusive()).stream()
				.filter(row -> row.getBucket() != null)
				.map(row -> new VisitorTimeSeriesResponse.Item(
						row.getBucket().atOffset(ZoneOffset.UTC).format(HOUR_FORMATTER),
						longValue(row.getUniqueVisitors()),
						longValue(row.getPageviews())))
				.toList();
	}
	
	/**
	 * Rebuilds calendar weeks from daily buckets instead of storing a second weekly table.
	 * Aggregating here keeps the storage model lean while preserving one consistent week-start rule.
	 *
	 * @param siteId site identifier
	 * @param range requested date range
	 * @return weekly chart points
	 */
	private List<VisitorTimeSeriesResponse.Item> weeklySeries(Long siteId, DateUtil.DateRange range) {
		TreeMap<LocalDate, BucketTotals> totalsByWeekStart = new TreeMap<>();
		
		for (DayBucket bucket : loadDailyBuckets(siteId, range)) {
			LocalDate weekStart = bucket.date().minusDays(bucket.date().getDayOfWeek().getValue() - 1L);
			totalsByWeekStart.merge(weekStart, bucket.totals(), BucketTotals::add);
		}
		
		return totalsByWeekStart.entrySet().stream()
				.map(entry -> new VisitorTimeSeriesResponse.Item(
						entry.getKey().toString(),
						entry.getValue().uniqueVisitors(),
						entry.getValue().pageviews()))
				.toList();
	}
	
	/**
	 * Rebuilds calendar months from daily buckets so long windows stay cheap without another summary table.
	 * Keeping the month rollup here lets the response format evolve independently from storage.
	 *
	 * @param siteId site identifier
	 * @param range requested date range
	 * @return monthly chart points
	 */
	private List<VisitorTimeSeriesResponse.Item> monthlySeries(Long siteId, DateUtil.DateRange range) {
		TreeMap<YearMonth, BucketTotals> totalsByMonth = new TreeMap<>();
		
		for (DayBucket bucket : loadDailyBuckets(siteId, range)) {
			YearMonth month = YearMonth.from(bucket.date());
			totalsByMonth.merge(month, bucket.totals(), BucketTotals::add);
		}
		
		return totalsByMonth.entrySet().stream()
				.map(entry -> new VisitorTimeSeriesResponse.Item(
						entry.getKey().toString(),
						entry.getValue().uniqueVisitors(),
						entry.getValue().pageviews()))
				.toList();
	}
	
	/**
	 * Prefers daily aggregates but falls back to raw page-view buckets when history is still warming up.
	 * That gives chart endpoints one shared place to decide whether precomputed data is trustworthy enough to
	 * use.
	 *
	 * @param siteId site identifier
	 * @param range requested date range
	 * @return normalized daily buckets
	 */
	private List<DayBucket> loadDailyBuckets(Long siteId, DateUtil.DateRange range) {
		List<DailyStat> rows = dailyStatRepository.findBySiteIdAndStatDateBetweenOrderByStatDateAsc(siteId, range.from(), range.to());
		if (!rows.isEmpty()) {
			return rows.stream()
					.map(row -> new DayBucket(
							row.getStatDate(),
							new BucketTotals(row.getUniqueVisitors(), row.getTotalPageviews())))
					.toList();
		}
		
		return pageViewRepository.findDailyBuckets(siteId, range.from(), range.to()).stream()
				.filter(row -> row.getBucket() != null)
				.map(row -> new DayBucket(
						row.getBucket().atOffset(ZoneOffset.UTC).toLocalDate(),
						new BucketTotals(longValue(row.getUniqueVisitors()), longValue(row.getPageviews()))))
				.toList();
	}
	
	/**
	 * Reindexes repository projections by epoch bucket so missing windows can be filled cheaply.
	 * Turning the list into a map keeps the minute and hour builders focused on window shape instead of
	 * repeated lookup scans.
	 *
	 * @param rows repository projection rows
	 * @param epochDivisor divisor that maps instants to the desired bucket size
	 * @return totals keyed by normalized epoch bucket
	 */
	private Map<Long, BucketTotals> toEpochBucketMap(List<VisitorTimeseriesProjection> rows, long epochDivisor) {
		Map<Long, BucketTotals> totalsByEpochBucket = new HashMap<>();
		
		for (VisitorTimeseriesProjection row : rows) {
			Instant bucket = row.getBucket();
			if (bucket == null) {
				continue;
			}
			
			long key = bucket.getEpochSecond() / epochDivisor;
			totalsByEpochBucket.put(key, new BucketTotals(longValue(row.getUniqueVisitors()), longValue(row.getPageviews())));
		}
		
		return totalsByEpochBucket;
	}
	
	/**
	 * Normalizes nullable repository counts before chart assembly begins.
	 * That keeps bucket-building code focused on time logic instead of null checks.
	 *
	 * @param value aggregate count that may be absent
	 * @return numeric value safe for chart math
	 */
	private long longValue(Long value) {
		return value == null ? 0L : value;
	}
	
	/**
	 * Keeps a calendar day and its totals paired while the service reshapes chart data.
	 * Using a small record avoids parallel lists and makes later regrouping logic easier to follow.
	 *
	 * @param date bucket date
	 * @param totals totals for that date
	 */
	private record DayBucket(LocalDate date, BucketTotals totals) {
		
		/**
		 * Delays response-object creation until the final chart shape is known.
		 * That keeps regrouping logic free to operate on lightweight domain buckets first.
		 *
		 * @return chart item for this day bucket
		 */
		private VisitorTimeSeriesResponse.Item toItem() {
			return new VisitorTimeSeriesResponse.Item(date.toString(), totals.uniqueVisitors(), totals.pageviews());
		}
	}
	
	/**
	 * Keeps page-view and visitor counts paired while buckets are merged.
	 * That prevents chart aggregation from accidentally combining only one metric when windows are regrouped.
	 *
	 * @param uniqueVisitors unique visitors in the bucket
	 * @param pageviews page views in the bucket
	 */
	private record BucketTotals(long uniqueVisitors, long pageviews) {
		
		/**
		 * Provides a shared zero-value bucket for gap filling.
		 * Reusing one helper keeps sparse time windows explicit without introducing null buckets.
		 *
		 * @return empty bucket totals
		 */
		private static BucketTotals empty() {
			return new BucketTotals(0L, 0L);
		}
		
		/**
		 * Merges two bucket totals while preserving both metrics together.
		 * That keeps week and month rollups from accidentally favoring page views over visitor counts or vice
		 * versa.
		 *
		 * @param other bucket totals to merge into this one
		 * @return combined bucket totals
		 */
		private BucketTotals add(BucketTotals other) {
			return new BucketTotals(uniqueVisitors + other.uniqueVisitors, pageviews + other.pageviews);
		}
	}
}
