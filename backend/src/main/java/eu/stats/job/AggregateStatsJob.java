package eu.stats.job;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.stats.repository.PageViewAggregationRepository;

@Component
public class AggregateStatsJob {
	
	private static final Logger log = LoggerFactory.getLogger(AggregateStatsJob.class);
	private static final Duration RECENT_LOOKBACK = Duration.ofDays(2);
	
	private final PageViewAggregationRepository pageviewAggregationRepository;
	private final Clock clock;
	
	public AggregateStatsJob(PageViewAggregationRepository pageviewAggregationRepository, Clock clock) {
		this.pageviewAggregationRepository = pageviewAggregationRepository;
		this.clock = clock;
	}
	
	@Scheduled(fixedRate = 5000)
	public void run() {
		OffsetDateTime viewedSince = OffsetDateTime.now(clock).minus(RECENT_LOOKBACK);
		long sourceCount = pageviewAggregationRepository.countRecentPageviews(viewedSince);
		log.info("Running AggregateStatsJob (source pageviews since {}: {})", viewedSince, sourceCount);
		
		if (sourceCount == 0L) {
			log.warn("AggregateStatsJob found no source rows in pageviews. Dashboard aggregates will stay unchanged.");
		}
		
		logStep("daily_stats", pageviewAggregationRepository.upsertDailyStats(viewedSince));
		logStep("hourly_stats", pageviewAggregationRepository.upsertHourlyStats(viewedSince));
		logStep("page_stats", pageviewAggregationRepository.upsertPageStats(viewedSince));
		logStep("referrer_stats", pageviewAggregationRepository.upsertReferrerStats(viewedSince));
		logStep("geo_stats", pageviewAggregationRepository.upsertGeoStats(viewedSince));
		logStep("device_stats", pageviewAggregationRepository.upsertDeviceStats(viewedSince));
		logStep("event_stats", pageviewAggregationRepository.upsertEventStats(viewedSince));
	}
	
	private void logStep(String tableName, int affectedRows) {
		log.info("AggregateStatsJob upserted {} rows into {}", affectedRows, tableName);
	}
}
