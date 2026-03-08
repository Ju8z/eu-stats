package eu.stats.dto.response;

public record OverviewStatsResponse(
		
		Period period,
		long totalPageviews,
		long uniqueVisitors,
		String topPage,
		String topReferrer,
		String topCountry,
		Comparison comparison

) {
	
	public record Period(
			
			String from,
			String to
	
	) {
	}
	
	public record Comparison(
			
			double pageviewsChange,
			double visitorsChange
	
	) {
	}
}
