package eu.stats.dto.response;

import java.util.List;

public record RealTimeResponse(
		
		long activeVisitors,
		List<PageItem> topActivePages

) {
	
	public record PageItem(
			
			String url,
			long visitors
	
	) {
	}
}
