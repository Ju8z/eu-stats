package eu.stats.job;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.stats.config.AppProperties;
import eu.stats.repository.PageViewAggregationRepository;

@Component
public class AggregateStatsJob {
	
	private static final Logger log = LoggerFactory.getLogger(AggregateStatsJob.class);
	private static final long FIXED_RATE_MILLIS = 60_000L;
	
	private final PageViewAggregationRepository pageViewAggregationRepository;
	private final AppProperties appProperties;
	private final Clock clock;
	
	public AggregateStatsJob(PageViewAggregationRepository pageViewAggregationRepository, AppProperties appProperties, Clock clock) {
		this.pageViewAggregationRepository = pageViewAggregationRepository;
		this.appProperties = appProperties;
		this.clock = clock;
	}
	
	@Scheduled(fixedRate = FIXED_RATE_MILLIS)
	public void run() {
		OffsetDateTime viewedSince = OffsetDateTime.now(clock).minus(lookbackWindow());
		long sourceCount = pageViewAggregationRepository.countRecentPageviews(viewedSince);
		log.info("Running AggregateStatsJob (source pageviews since {}: {})", viewedSince, sourceCount);
		
		if (sourceCount == 0L) {
			log.warn("AggregateStatsJob found no source rows in pageviews. Dashboard aggregates will stay unchanged.");
			return;
		}
		
		logStep("daily_stats", pageViewAggregationRepository.upsertDailyStats(viewedSince));
		logStep("hourly_stats", pageViewAggregationRepository.upsertHourlyStats(viewedSince));
		logStep("page_stats", pageViewAggregationRepository.upsertPageStats(viewedSince));
		logStep("referrer_stats", pageViewAggregationRepository.upsertReferrerStats(viewedSince));
		logStep("geo_stats", pageViewAggregationRepository.upsertGeoStats(viewedSince));
		logStep("device_stats", pageViewAggregationRepository.upsertDeviceStats(viewedSince));
		logStep("event_stats", pageViewAggregationRepository.upsertEventStats(viewedSince));
	}
	
	private Duration lookbackWindow() {
		return Duration.ofDays(Math.max(1, appProperties.getAggregationLookbackDays()));
	}
	
	private void logStep(String tableName, int affectedRows) {
		log.info("AggregateStatsJob upserted {} rows into {}", affectedRows, tableName);
	}
}
