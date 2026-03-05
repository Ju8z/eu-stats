package eu.stats.service;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.stats.dto.request.CollectPayload;
import eu.stats.entity.PageView;
import eu.stats.repository.PageViewRepository;
import eu.stats.repository.SiteRepository;
import eu.stats.util.ReferrerClassifier;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class CollectionService {
	
	private static final Logger log = LoggerFactory.getLogger(CollectionService.class);
	
	private final SiteRepository siteRepository;
	private final PageViewRepository pageViewRepository;
	private final VisitorHashService visitorHashService;
	private final GeoIpService geoIpService;
	private final UserAgentService userAgentService;
	private final ReferrerClassifier referrerClassifier;
	
	public CollectionService(SiteRepository siteRepository, PageViewRepository pageViewRepository,
			VisitorHashService visitorHashService, GeoIpService geoIpService, UserAgentService userAgentService,
			ReferrerClassifier referrerClassifier) {
		this.siteRepository = siteRepository;
		this.pageViewRepository = pageViewRepository;
		this.visitorHashService = visitorHashService;
		this.geoIpService = geoIpService;
		this.userAgentService = userAgentService;
		this.referrerClassifier = referrerClassifier;
	}
	
	@Transactional
	public void collect(CollectPayload payload, HttpServletRequest request) {
		if (!siteRepository.existsById(payload.siteId())) {
			log.warn("Dropping collect payload for unknown siteId={}", payload.siteId());
			return;
		}
		
		String clientIp = request.getRemoteAddr();
		
		String visitorHash = visitorHashService.hashVisitor(payload.siteId(), clientIp, payload.userAgent());
		GeoIpService.GeoIpResult geoIpResult = geoIpService.resolve(clientIp);
		UserAgentService.UserAgentDetails userAgentDetails = userAgentService.parse(payload.userAgent());
		
		PageView pageView = new PageView(
				null,
				payload.siteId(),
				visitorHash,
				sanitizePath(payload.url()),
				payload.title(),
				referrerClassifier.normalizeDomain(payload.referrer()),
				referrerClassifier.classify(payload.referrer()),
				userAgentDetails.browser(),
				userAgentDetails.browserVersion(),
				userAgentDetails.os(),
				userAgentDetails.osVersion(),
				userAgentDetails.deviceType(),
				payload.screenResolution(),
				payload.viewport(),
				payload.language(),
				geoIpResult.country(),
				null,
				null,
				null,
				payload.eventType(),
				payload.eventName(),
				parseTimestamp(payload.timestamp()),
				OffsetDateTime.now()
		);
		
		pageViewRepository.save(pageView);
		log.debug("Stored pageview: siteId={}, eventType={}, viewedAt={}",
				payload.siteId(), payload.eventType(), pageView.getViewedAt());
	}
	
	private OffsetDateTime parseTimestamp(String timestamp) {
		try {
			return OffsetDateTime.parse(timestamp);
		} catch (DateTimeParseException ex) {
			return OffsetDateTime.now();
		}
	}
	
	private String sanitizePath(String url) {
		if (url == null || url.isBlank()) {
			return "/";
		}
		
		try {
			URI uri = URI.create(url);
			String path = uri.getPath();
			
			return path == null || path.isBlank() ? "/" : path;
		} catch (Exception ex) {
			return "/";
		}
	}
}
