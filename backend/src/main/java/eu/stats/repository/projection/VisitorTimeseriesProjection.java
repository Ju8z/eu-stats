package eu.stats.repository.projection;

import java.time.Instant;

/**
 * Defines a narrow projection for visitor time series queries.
 * Exposing only the needed columns avoids hydrating full entities for aggregate reporting reads.
 */
public interface VisitorTimeseriesProjection {
	
	/**
	 * Keeps bucket available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected bucket
	 */
	Instant getBucket();
	
	/**
	 * Keeps unique visitors available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected unique visitors
	 */
	Long getUniqueVisitors();
	
	/**
	 * Keeps page views available without hydrating a full entity.
	 * Narrow projections make aggregate queries cheaper and reduce accidental coupling to the underlying
	 * entity shape.
	 *
	 * @return projected page views
	 */
	Long getPageviews();
}
