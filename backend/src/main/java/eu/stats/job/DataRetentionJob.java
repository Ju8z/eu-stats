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
 * Keeps the raw pageview table under control by removing data that is older
 * than the configured retention window. Default 24 Months and can be changed in .env
 * <p>
 * The application keeps long-term reporting data in aggregate tables, so this
 * job exists to stop detailed visit rows from growing forever.
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
	 * Runs once a day at 00:01 and deletes raw pageviews that have aged past the configured retention period.
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
