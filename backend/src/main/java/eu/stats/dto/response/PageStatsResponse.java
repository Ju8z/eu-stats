package eu.stats.dto.response;

import java.util.List;

/**
 * Defines an immutable response contract for page statistics.
 * Using records for responses keeps field names explicit and reduces accidental drift between backend
 * payloads and frontend expectations.
 *
 * @param data response data
 */
public record PageStatsResponse(
		
		List<Item> data
) {
	/**
	 * Bundles related values that should travel together.
	 * Keeping this nested record inside page statistics response prevents closely related analytics values
	 * from drifting apart as separate arguments or map entries.
	 *
	 * @param url            page address
	 * @param title          page title
	 * @param pageviews      page views
	 * @param uniqueVisitors unique visitors
	 */
	public record Item(
			
			String url,
			String title,
			long pageviews,
			long uniqueVisitors
	
	) {
	}
}
