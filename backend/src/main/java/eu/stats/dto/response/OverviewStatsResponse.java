package eu.stats.dto.response;

/**
 * Defines an immutable response contract for overview statistics.
 * Using records for responses keeps field names explicit and reduces accidental drift between backend
 * payloads and frontend expectations.
 *
 * @param totalPageviews total page views
 * @param uniqueVisitors unique visitors
 * @param comparison     comparison values
 */
public record OverviewStatsResponse(
		
		long totalPageviews,
		long uniqueVisitors,
		Comparison comparison

) {
	
	/**
	 * Bundles related values that should travel together.
	 * Keeping this nested record inside overview statistics response prevents closely related analytics values
	 * from drifting apart as separate arguments or map entries.
	 *
	 * @param pageviewsChange change in page views
	 * @param visitorsChange  change in visitors
	 */
	public record Comparison(
			
			double pageviewsChange,
			double visitorsChange
	
	) {
	}
}
