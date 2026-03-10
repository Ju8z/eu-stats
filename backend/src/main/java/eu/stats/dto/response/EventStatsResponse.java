package eu.stats.dto.response;

import java.util.List;

/**
 * Defines an immutable response contract for event statistics.
 * Using records for responses keeps field names explicit and reduces accidental drift between backend
 * payloads and frontend expectations.
 *
 * @param data response data
 */
public record EventStatsResponse(
		
		List<Item> data

) {
	
	/**
	 * Bundles related values that should travel together.
	 * Keeping this nested record inside event statistics response prevents closely related analytics values
	 * from drifting apart as separate arguments or map entries.
	 *
	 * @param eventName      event name
	 * @param count          count
	 * @param uniqueVisitors unique visitors
	 */
	public record Item(
			
			String eventName,
			long count,
			long uniqueVisitors
	
	) {
	}
}
