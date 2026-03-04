package eu.stats.dto.response;

import java.util.List;

public record PageStatsResponse(
		
		List<Item> data

) {
	public record Item(
			
			String url,
			String title,
			long pageviews,
			long uniqueVisitors
	
	) {
	}
}
