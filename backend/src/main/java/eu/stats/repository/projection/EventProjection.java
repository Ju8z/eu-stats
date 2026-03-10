package eu.stats.repository.projection;

/**
 * Defines a narrow projection for event queries.
 * Exposing only the needed columns avoids hydrating full entities for aggregate reporting reads.
 */
public interface EventProjection {
	
	/**
	 * Keeps event name available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected event name
	 */
	String getEventName();
	
	/**
	 * Keeps event count available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected event count
	 */
	Long getEventCount();
	
	/**
	 * Keeps unique visitors available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected unique visitors
	 */
	Long getUniqueVisitors();
}
