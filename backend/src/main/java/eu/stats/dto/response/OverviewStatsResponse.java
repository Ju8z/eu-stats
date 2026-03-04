package eu.stats.dto.response;

//TODO: Probably comparison is not needed, recheck when graphc bucket is rdy to be implemented
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
			
			Period previousPeriod,
			double pageviewsChange,
			double visitorsChange
	
	) {
	}
}
