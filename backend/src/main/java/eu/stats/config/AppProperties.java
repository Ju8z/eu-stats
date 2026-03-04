package eu.stats.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
	
	private String trackerBaseUrl;
	private String dailySaltSecret;
	private int dataRetentionMonths;
	private int realtimeWindowMinutes;
	private List<String> corsOrigins;
	private String geoIpApiUrl;
	
	public String getTrackerBaseUrl() {
		return trackerBaseUrl;
	}
	
	public void setTrackerBaseUrl(String trackerBaseUrl) {
		this.trackerBaseUrl = trackerBaseUrl;
	}
	
	public String getDailySaltSecret() {
		return dailySaltSecret;
	}
	
	public void setDailySaltSecret(String dailySaltSecret) {
		this.dailySaltSecret = dailySaltSecret;
	}
	
	public int getDataRetentionMonths() {
		return dataRetentionMonths;
	}
	
	public void setDataRetentionMonths(int dataRetentionMonths) {
		this.dataRetentionMonths = dataRetentionMonths;
	}
	
	public int getRealtimeWindowMinutes() {
		return realtimeWindowMinutes;
	}
	
	public void setRealtimeWindowMinutes(int realtimeWindowMinutes) {
		this.realtimeWindowMinutes = realtimeWindowMinutes;
	}
	
	public List<String> getCorsOrigins() {
		return corsOrigins;
	}
	
	public void setCorsOrigins(List<String> corsOrigins) {
		this.corsOrigins = corsOrigins;
	}
	
	public String getGeoIpApiUrl() {
		return geoIpApiUrl;
	}
	
	public void setGeoIpApiUrl(String geoIpApiUrl) {
		this.geoIpApiUrl = geoIpApiUrl;
	}
}
