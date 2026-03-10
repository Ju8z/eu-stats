package eu.stats.dto.response;

import java.util.List;

/**
 * Defines an immutable response contract for geographic statistics.
 * Using records for responses keeps field names explicit and reduces accidental drift between backend
 * payloads and frontend expectations.
 *
 * @param data response data
 */
public record GeoStatsResponse(
		
		List<Item> data

) {
	
	/**
	 * Bundles related values that should travel together.
	 * Keeping this nested record inside geographic statistics response prevents closely related analytics
	 * values from drifting apart as separate arguments or map entries.
	 *
	 * @param countryName country name
	 * @param visits      visits
	 */
	public record Item(
			
			String countryName,
			long visits
	
	) {
	
	}
	
}
