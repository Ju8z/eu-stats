package eu.stats.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import eu.stats.config.AppProperties;
import eu.stats.dto.response.RealTimeResponse;
import eu.stats.repository.PageViewRepository;

/**
 * Builds the live dashboard view from heartbeat data.
 * The service keeps live-window rules on the server so active visitor counts and active page counts
 * continue to describe the same population.
 */
@Service
public class RealTimeService {
	
	private final PageViewRepository pageViewRepository;
	private final AppProperties appProperties;
	private final Clock clock;
	
	public RealTimeService(PageViewRepository pageViewRepository, AppProperties appProperties, Clock clock) {
		this.pageViewRepository = pageViewRepository;
		this.appProperties = appProperties;
		this.clock = clock;
	}
	
	/**
	 * Builds live metrics from heartbeat data rather than historical aggregates.
	 * The service applies the same live window to active visitors and active pages so both numbers describe
	 * the same active population.
	 *
	 * @param siteId site identifier
	 * @return real-time response
	 */
	public RealTimeResponse getRealtime(Long siteId) {
		// The configured live window acts as a heartbeat timeout: if a tab stops
		// sending heartbeats for longer than this window, it is no longer treated
		// as currently active.
		int windowMinutes = Math.max(5, appProperties.getRealtimeWindowMinutes());
		OffsetDateTime fromTs = OffsetDateTime.now(clock).minusMinutes(windowMinutes);
		Long activeVisitors = pageViewRepository.countDistinctVisitorsByIngestedSince(siteId, fromTs);
		List<RealTimeResponse.PageItem> pages = pageViewRepository.findTopActivePagesByIngestedSince(siteId, fromTs, 10).stream()
				.map(row -> new RealTimeResponse.PageItem(row.getPageUrl(), row.getVisitors() == null ? 0L : row.getVisitors()))
				.toList();
		
		return new RealTimeResponse(activeVisitors == null ? 0L : activeVisitors, pages);
	}
}
