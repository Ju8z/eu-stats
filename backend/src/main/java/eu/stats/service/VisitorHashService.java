package eu.stats.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

import eu.stats.config.AppProperties;
import eu.stats.util.HashUtil;

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
	
	public String hashVisitor(Long siteId, String clientIp, String userAgent) {
		String dailySalt = hashUtil.sha256Hex(appProperties.getDailySaltSecret() + ":" + LocalDate.now(clock.withZone(ZoneOffset.UTC)));
		String source = siteId + "|" + clientIp + "|" + userAgent + "|" + dailySalt;
		
		return hashUtil.sha256Hex(source);
	}
}
