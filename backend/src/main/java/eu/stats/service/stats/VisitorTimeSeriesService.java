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
	
	private List<VisitorTimeSeriesResponse.Item> dailySeries(Long siteId, DateUtil.DateRange range) {
		return loadDailyBuckets(siteId, range).stream()
				.map(DayBucket::toItem)
				.toList();
	}
	
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
	
	private long longValue(Long value) {
		return value == null ? 0L : value;
	}
	
	private record DayBucket(LocalDate date, BucketTotals totals) {
		
		private VisitorTimeSeriesResponse.Item toItem() {
			return new VisitorTimeSeriesResponse.Item(date.toString(), totals.uniqueVisitors(), totals.pageviews());
		}
	}
	
	private record BucketTotals(long uniqueVisitors, long pageviews) {
		
		private static BucketTotals empty() {
			return new BucketTotals(0L, 0L);
		}
		
		private BucketTotals add(BucketTotals other) {
			return new BucketTotals(uniqueVisitors + other.uniqueVisitors, pageviews + other.pageviews);
		}
	}
}
