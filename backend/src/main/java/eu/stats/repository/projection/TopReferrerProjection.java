package eu.stats.repository.projection;

/**
 * Defines a narrow projection for top referrer queries.
 * Exposing only the needed columns avoids hydrating full entities for aggregate reporting reads.
 */
public interface TopReferrerProjection {
	
	/**
	 * Keeps referrer available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected referrer
	 */
	String getReferrer();
	
	/**
	 * Keeps referrer category available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected referrer category
	 */
	String getReferrerCategory();
	
	/**
	 * Keeps visits available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected visits
	 */
	Long getVisits();
	
	/**
	 * Keeps unique visitors available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected unique visitors
	 */
	Long getUniqueVisitors();
}
