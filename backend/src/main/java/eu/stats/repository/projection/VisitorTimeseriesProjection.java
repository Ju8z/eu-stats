package eu.stats.repository.projection;

import java.time.Instant;

public interface VisitorTimeseriesProjection {
	
	Instant getBucket();
	
	Long getUniqueVisitors();
	
	Long getPageviews();
}
