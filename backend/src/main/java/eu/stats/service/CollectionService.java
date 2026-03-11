package eu.stats.service;

import java.net.URI;
import java.time.Clock;
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

/**
 * Centralizes tracker ingestion rules.
 * Keeping enrichment, privacy-sensitive hashing, and silent-drop behavior in one service prevents
 * controller and persistence code from diverging on ingestion decisions.
 */
@Service
public class CollectionService {
	
	private static final Logger log = LoggerFactory.getLogger(CollectionService.class);
	
	private final SiteRepository siteRepository;
	private final PageViewRepository pageViewRepository;
	private final VisitorHashService visitorHashService;
	private final GeoIpService geoIpService;
	private final UserAgentService userAgentService;
	private final ReferrerClassifier referrerClassifier;
	private final Clock clock;
	
	public CollectionService(
			SiteRepository siteRepository,
			PageViewRepository pageViewRepository,
			VisitorHashService visitorHashService,
			GeoIpService geoIpService,
			UserAgentService userAgentService,
			ReferrerClassifier referrerClassifier,
			Clock clock) {
		this.siteRepository = siteRepository;
		this.pageViewRepository = pageViewRepository;
		this.visitorHashService = visitorHashService;
		this.geoIpService = geoIpService;
		this.userAgentService = userAgentService;
		this.referrerClassifier = referrerClassifier;
		this.clock = clock;
	}
	
	/**
	 * Drops invalid tracker payloads before they can affect stored analytics.
	 * Unknown sites are ignored deliberately so the tracker endpoint stays low-noise and does not leak site
	 * existence rules, while valid payloads are enriched before persistence.
	 *
	 * @param payload tracking payload
	 * @param request incoming servlet request
	 */
	@Transactional
	public void collect(CollectPayload payload, HttpServletRequest request) {
		if (payload == null || !siteRepository.existsById(payload.siteId())) {
			log.warn("Dropping collect payload for unknown siteId={}", payload == null ? null : payload.siteId());
			return;
		}
		
		String clientIp = request.getRemoteAddr();
		PageView pageView = buildPageView(payload, clientIp);
		pageViewRepository.save(pageView);
		
		log.debug("Stored pageview: siteId={}, eventType={}, viewedAt={}",
				payload.siteId(), payload.eventType(), pageView.getViewedAt());
	}
	
	private PageView buildPageView(CollectPayload payload, String clientIp) {
		String visitorHash = visitorHashService.hashVisitor(payload.siteId(), clientIp, payload.userAgent());
		GeoIpService.GeoIpResult geoIpResult = geoIpService.resolve(clientIp);
		UserAgentService.UserAgentDetails userAgentDetails = userAgentService.parse(payload.userAgent());
		String normalizedReferrer = referrerClassifier.normalizeDomain(payload.referrer());
		
		return new PageView(
				null,
				payload.siteId(),
				visitorHash,
				sanitizePath(payload.url()),
				payload.title(),
				normalizedReferrer,
				referrerClassifier.classify(payload.referrer()),
				userAgentDetails.browser(),
				userAgentDetails.browserVersion(),
				userAgentDetails.os(),
				userAgentDetails.osVersion(),
				userAgentDetails.deviceType(),
				geoIpResult.country(),
				payload.eventType(),
				payload.eventName(),
				parseViewedAt(payload.timestamp()),
				OffsetDateTime.now(clock));
	}
	
	private OffsetDateTime parseViewedAt(String timestamp) {
		try {
			return OffsetDateTime.parse(timestamp);
		} catch (DateTimeParseException ex) {
			return OffsetDateTime.now(clock);
		}
	}
	
	private String sanitizePath(String url) {
		if (url == null || url.isBlank()) {
			return "/";
		}
		
		try {
			URI uri = URI.create(url);
			String path = uri.getPath();
			if (path == null || path.isBlank()) {
				return "/";
			}
			
			if ("file".equalsIgnoreCase(uri.getScheme())) {
				String normalizedPath = path.replace('\\', '/');
				int demoSegmentIndex = normalizedPath.lastIndexOf("/demo/");
				if (demoSegmentIndex >= 0) {
					return normalizedPath.substring(demoSegmentIndex);
				}
			}
			
			return path;
		} catch (Exception ex) {
			return "/";
		}
	}
}
