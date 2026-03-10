package eu.stats.repository.projection;

/**
 * Defines a narrow projection for top geographic queries.
 * Exposing only the needed columns avoids hydrating full entities for aggregate reporting reads.
 */
public interface TopGeoProjection {
	
	/**
	 * Keeps country code available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected country code
	 */
	String getCountry();
	
	/**
	 * Keeps visits available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected visits
	 */
	Long getVisits();
}
