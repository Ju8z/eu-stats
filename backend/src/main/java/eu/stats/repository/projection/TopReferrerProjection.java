package eu.stats.repository.projection;

public interface TopReferrerProjection {
	
	String getReferrer();
	
	String getReferrerCategory();
	
	Long getVisits();
	
	Long getUniqueVisitors();
}
