package eu.stats.dto.response;

import java.util.List;

public record DeviceStatsResponse(
		
		List<DeviceItem> deviceTypes,
		List<TechItem> browsers,
		List<TechItem> operatingSystems,
		List<ResolutionItem> screenResolutions

) {
	
	public record DeviceItem(
			
			String type,
			long visits,
			double percentage
	) {
	}
	
	public record TechItem(
			
			String name,
			String version,
			long visits,
			double percentage
	
	) {
	}
	
	public record ResolutionItem(
			
			String resolution,
			long visits,
			double percentage
	
	) {
	}
}