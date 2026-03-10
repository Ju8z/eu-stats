package eu.stats.dto.response;

import java.util.List;

/**
 * Defines an immutable response contract for visitor time series.
 * Using records for responses keeps field names explicit and reduces accidental drift between backend
 * payloads and frontend expectations.
 *
 * @param interval requested interval
 * @param data     response data
 */
public record VisitorTimeSeriesResponse(
		
		String interval,
		List<Item> data
) {
	/**
	 * Bundles related values that should travel together.
	 * Keeping this nested record inside visitor time series response prevents closely related analytics values
	 * from drifting apart as separate arguments or map entries.
	 *
	 * @param date           date
	 * @param uniqueVisitors unique visitors
	 * @param pageviews      page views
	 */
	public record Item(
			
			String date,
			long uniqueVisitors,
			long pageviews
	
	) {
	
	}
	
}
