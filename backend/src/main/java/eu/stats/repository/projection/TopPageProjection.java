package eu.stats.repository.projection;

/**
 * Defines a narrow projection for top page queries.
 * Exposing only the needed columns avoids hydrating full entities for aggregate reporting reads.
 */
public interface TopPageProjection {
	
	/**
	 * Keeps page address available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected page address
	 */
	String getPageUrl();
	
	/**
	 * Keeps page title available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected page title
	 */
	String getPageTitle();
	
	/**
	 * Keeps page views available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected page views
	 */
	Long getPageviews();
	
	/**
	 * Keeps unique visitors available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected unique visitors
	 */
	Long getUniqueVisitors();
}
