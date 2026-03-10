package eu.stats.job;

import java.time.Clock;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.stats.config.AppProperties;
import eu.stats.repository.PageViewRepository;

/**
 * Enforces raw event retention without touching dashboard aggregates.
 * Running retention separately keeps privacy-related cleanup explicit and lets the cutoff change through
 * configuration rather than code edits.
 */
@Component
public class DataRetentionJob {
	
	private static final Logger log = LoggerFactory.getLogger(DataRetentionJob.class);
	
	private final PageViewRepository pageViewRepository;
	private final AppProperties appProperties;
	private final Clock clock;
	
	public DataRetentionJob(PageViewRepository pageViewRepository, AppProperties appProperties, Clock clock) {
		this.pageViewRepository = pageViewRepository;
		this.appProperties = appProperties;
		this.clock = clock;
	}
	
	/**
	 * Deletes raw events beyond the configured retention window.
	 * Calculating the cutoff at runtime keeps privacy retention adjustable through configuration instead of
	 * code changes.
	 */
	@Transactional
	@Scheduled(cron = "0 1 0 * * *")
	public void run() {
		log.info("Running DataRetentionJob");
		
		OffsetDateTime cutoff = OffsetDateTime.now(clock).minusMonths(appProperties.getDataRetentionMonths());
		int deleted = pageViewRepository.deleteOlderThan(cutoff);
		
		log.info("Deleted {} pageview rows older than {}", deleted, cutoff);
	}
}
