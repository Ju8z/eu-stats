package eu.stats.service;

import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

import eu.stats.config.AppProperties;
import eu.stats.util.HashUtil;

@Service
public class VisitorHashService {
	
	private final HashUtil hashUtil;
	private final AppProperties appProperties;
	
	public VisitorHashService(HashUtil hashUtil, AppProperties appProperties) {
		this.hashUtil = hashUtil;
		this.appProperties = appProperties;
	}
	
	public String hashVisitor(Long siteId, String clientIp, String userAgent) {
		String dailySalt = hashUtil.sha256Hex(appProperties.getDailySaltSecret() + ":" + LocalDate.now(ZoneOffset.UTC));
		
		// The daily salt breaks linkability across days while preserving same-day unique counting
		String source = siteId + "|" + clientIp + "|" + userAgent + "|" + dailySalt;
		
		return hashUtil.sha256Hex(source);
	}
}
