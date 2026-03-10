package eu.stats.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

import eu.stats.config.AppProperties;
import eu.stats.util.HashUtil;

/**
 * Builds short-lived visitor identifiers for counting.
 * Recomputing the hash with a daily salt preserves useful uniqueness for analytics while intentionally
 * preventing long-term cross-day tracking.
 */
@Service
public class VisitorHashService {
	
	private final HashUtil hashUtil;
	private final AppProperties appProperties;
	private final Clock clock;
	
	public VisitorHashService(HashUtil hashUtil, AppProperties appProperties, Clock clock) {
		this.hashUtil = hashUtil;
		this.appProperties = appProperties;
		this.clock = clock;
	}
	
	/**
	 * Creates a privacy-preserving visitor key for short-term uniqueness.
	 * The hash includes a daily salt so the same person can be counted within a day without becoming a durable
	 * cross-day identifier.
	 *
	 * @param siteId    site identifier
	 * @param clientIp  client internet protocol address
	 * @param userAgent user agent string
	 * @return daily visitor hash value
	 */
	public String hashVisitor(Long siteId, String clientIp, String userAgent) {
		String dailySalt = hashUtil.sha256Hex(appProperties.getDailySaltSecret() + ":" + LocalDate.now(clock.withZone(ZoneOffset.UTC)));
		String source = siteId + "|" + clientIp + "|" + userAgent + "|" + dailySalt;
		
		return hashUtil.sha256Hex(source);
	}
}
