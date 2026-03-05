package eu.stats.dto.response;

import java.util.List;

public record GeoStatsResponse(
		
		List<Item> data

) {
	
	public record Item(
			
			String country,
			String countryName,
			long visits,
			long uniqueVisitors,
			double percentage
	
	) {
	
	}
	
}