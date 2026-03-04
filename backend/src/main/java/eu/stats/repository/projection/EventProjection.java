package eu.stats.repository.projection;

public interface EventProjection {
	
	String getEventName();
	
	Long getEventCount();
	
	Long getUniqueVisitors();
}
