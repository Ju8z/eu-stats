package eu.stats.dto.response;

import java.util.List;

public record ReferrerStatsResponse(
		
		List<Item> data

) {
	
	public record Item(
			
			String referrer,
			String category,
			long visits,
			long uniqueVisitors
	
	) {
	}
	
}