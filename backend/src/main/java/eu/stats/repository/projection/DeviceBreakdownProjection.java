package eu.stats.repository.projection;

// TODO: Do a check since, Spring Data needs somewhere to map those aliased columns, and couldnt find any other way how to do this, maybe there is some option
//  later
public interface DeviceBreakdownProjection {
	
	String getDeviceType();
	
	String getBrowser();
	
	String getBrowserVersion();
	
	String getOs();
	
	String getOsVersion();
	
	String getScreenResolution();
	
	Long getVisits();
}
