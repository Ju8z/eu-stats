package eu.stats.dto.response;

import java.util.List;

public record EventStatsResponse(
		
		List<Item> data

) {
	
	public record Item(
			
			String eventName,
			long count,
			long uniqueVisitors
	
	) {
	}
}
