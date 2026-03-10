package eu.stats.repository.projection;

/**
 * Defines a narrow projection for device breakdown queries.
 * Exposing only the needed columns avoids hydrating full entities for aggregate reporting reads.
 */
public interface DeviceBreakdownProjection {
	
	/**
	 * Keeps device type available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected device type
	 */
	String getDeviceType();
	
	/**
	 * Keeps operating system name available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected operating system name
	 */
	String getOs();
	
	/**
	 * Keeps operating system version available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected operating system version
	 */
	String getOsVersion();
	
	/**
	 * Keeps visits available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected visits
	 */
	Long getVisits();
}
