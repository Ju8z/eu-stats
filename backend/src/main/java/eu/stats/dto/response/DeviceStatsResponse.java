package eu.stats.dto.response;

import java.util.List;

/**
 * Defines an immutable response contract for device statistics.
 * Using records for responses keeps field names explicit and reduces accidental drift between backend
 * payloads and frontend expectations.
 *
 * @param deviceTypes      device types
 * @param operatingSystems operating systems
 */
public record DeviceStatsResponse(
		
		List<DeviceItem> deviceTypes,
		List<TechItem> operatingSystems

) {
	
	/**
	 * Bundles related values that should travel together.
	 * Keeping this nested record inside device statistics response prevents closely related analytics values
	 * from drifting apart as separate arguments or map entries.
	 *
	 * @param type   type
	 * @param visits visits
	 */
	public record DeviceItem(
			
			String type,
			long visits
	) {
	}
	
	/**
	 * Bundles related values that should travel together.
	 * Keeping this nested record inside device statistics response prevents closely related analytics values
	 * from drifting apart as separate arguments or map entries.
	 *
	 * @param name name
	 * @param version version
	 * @param visits visits
	 */
	public record TechItem(
			
			String name,
			String version,
			long visits
	
	) {
	}
	
}
