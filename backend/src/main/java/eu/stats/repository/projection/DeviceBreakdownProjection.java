package eu.stats.repository.projection;

public interface DeviceBreakdownProjection {
	
	String getDeviceType();
	
	String getBrowser();
	
	String getBrowserVersion();
	
	String getOs();
	
	String getOsVersion();
	
	Long getVisits();
}
