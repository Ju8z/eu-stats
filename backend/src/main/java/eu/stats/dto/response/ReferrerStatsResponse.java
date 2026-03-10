package eu.stats.dto.response;

import java.util.List;

/**
 * Defines an immutable response contract for referrer statistics.
 * Using records for responses keeps field names explicit and reduces accidental drift between backend
 * payloads and frontend expectations.
 *
 * @param data response data
 */
public record ReferrerStatsResponse(
		
		List<Item> data

) {
	
	/**
	 * Bundles related values that should travel together.
	 * Keeping this nested record inside referrer statistics response prevents closely related analytics values
	 * from drifting apart as separate arguments or map entries.
	 *
	 * @param referrer       referrer
	 * @param category       category
	 * @param visits         visits
	 * @param uniqueVisitors unique visitors
	 */
	public record Item(
			
			String referrer,
			String category,
			long visits,
			long uniqueVisitors
	
	) {
	}
	
}