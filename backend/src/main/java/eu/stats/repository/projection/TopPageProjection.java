package eu.stats.repository.projection;

public interface TopPageProjection {
	
	String getPageUrl();
	
	String getPageTitle();
	
	Long getPageviews();
	
	Long getUniqueVisitors();
}
