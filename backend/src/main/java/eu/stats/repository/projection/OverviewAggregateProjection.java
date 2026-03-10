package eu.stats.repository.projection;

/**
 * Defines a narrow projection for overview aggregate queries.
 * Exposing only the needed columns avoids hydrating full entities for aggregate reporting reads.
 */
public interface OverviewAggregateProjection {
	
	/**
	 * Keeps total page views available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected total page views
	 */
	Long getTotalPageviews();
	
	/**
	 * Keeps unique visitors available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected unique visitors
	 */
	Long getUniqueVisitors();
}
