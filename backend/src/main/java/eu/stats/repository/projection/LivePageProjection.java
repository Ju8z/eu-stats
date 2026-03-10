package eu.stats.repository.projection;

/**
 * Defines a narrow projection for live page queries.
 * Exposing only the needed columns avoids hydrating full entities for aggregate reporting reads.
 */
public interface LivePageProjection {
	
	/**
	 * Keeps page address available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected page address
	 */
	String getPageUrl();
	
	/**
	 * Keeps visitors available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected visitors
	 */
	Long getVisitors();
	
}
