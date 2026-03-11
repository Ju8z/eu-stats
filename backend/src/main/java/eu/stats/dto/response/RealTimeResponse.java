package eu.stats.dto.response;

import java.util.List;

/**
 * Defines an immutable response contract for real-time.
 * Using records for responses keeps field names explicit and reduces accidental drift between backend
 * payloads and frontend expectations.
 *
 * @param activeVisitors active visitors
 * @param activeTabs active tabs
 * @param topActivePages top active pages
 */
public record RealTimeResponse(
		
		long activeVisitors,
		long activeTabs,
		List<PageItem> topActivePages

) {
	
	/**
	 * Bundles related values that should travel together.
	 * Keeping this nested record inside real-time response prevents closely related analytics values from
	 * drifting apart as separate arguments or map entries.
	 *
	 * @param url      page address
	 * @param activeTabs active tabs
	 */
	public record PageItem(
			
			String url,
			long activeTabs
	
	) {
	}
}
