package eu.stats.dto.response;

import java.util.List;

/**
 * Defines an immutable response contract for real-time.
 * Using records for responses keeps field names explicit and reduces accidental drift between backend
 * payloads and frontend expectations.
 *
 * @param activeVisitors active visitors
 * @param topActivePages top active pages
 */
public record RealTimeResponse(
		
		long activeVisitors,
		List<PageItem> topActivePages

) {
	
	/**
	 * Bundles related values that should travel together.
	 * Keeping this nested record inside real-time response prevents closely related analytics values from
	 * drifting apart as separate arguments or map entries.
	 *
	 * @param url      page address
	 * @param visitors visitors
	 */
	public record PageItem(
			
			String url,
			long visitors
	
	) {
	}
}
