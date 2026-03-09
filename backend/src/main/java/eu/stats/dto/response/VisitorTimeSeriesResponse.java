package eu.stats.dto.response;

import java.util.List;

public record VisitorTimeSeriesResponse(
		
		String interval,
		List<Item> data

) {
	public record Item(
			
			String date,
			long uniqueVisitors,
			long pageviews
	
	) {
	
	}
	
}