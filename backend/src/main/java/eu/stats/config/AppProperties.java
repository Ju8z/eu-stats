package eu.stats.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Collects runtime analytics settings in one typed object.
 * That keeps environment-driven behavior discoverable and avoids repeated string-based configuration
 * lookups across the codebase.
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {
	
	private String trackerBaseUrl;
	private String dailySaltSecret;
	private String geoIpApiUrl;
	private List<String> corsOrigins;
	private int dataRetentionMonths;
	private int aggregationLookbackDays;
	private int realtimeWindowMinutes;
	
	/**
	 * Exposes tracker base address through typed configuration access.
	 * Callers use this accessor so configuration keys do not leak through the codebase as repeated string
	 * lookups.
	 *
	 * @return configured tracker base address
	 */
	public String getTrackerBaseUrl() {
		return trackerBaseUrl;
	}
	
	/**
	 * Allows Spring to bind tracker base address from external configuration.
	 * The properties object stays mutable because framework binding populates it after bean construction.
	 *
	 * @param trackerBaseUrl tracker base address
	 */
	public void setTrackerBaseUrl(String trackerBaseUrl) {
		this.trackerBaseUrl = trackerBaseUrl;
	}
	
	/**
	 * Exposes daily salt secret through typed configuration access.
	 * Callers use this accessor so configuration keys do not leak through the codebase as repeated string
	 * lookups.
	 *
	 * @return configured daily salt secret
	 */
	public String getDailySaltSecret() {
		return dailySaltSecret;
	}
	
	/**
	 * Allows Spring to bind daily salt secret from external configuration.
	 * The properties object stays mutable because framework binding populates it after bean construction.
	 *
	 * @param dailySaltSecret daily salt secret
	 */
	public void setDailySaltSecret(String dailySaltSecret) {
		this.dailySaltSecret = dailySaltSecret;
	}
	
	/**
	 * Exposes data retention months through typed configuration access.
	 * Callers use this accessor so configuration keys do not leak through the codebase as repeated string
	 * lookups.
	 *
	 * @return configured data retention months
	 */
	public int getDataRetentionMonths() {
		return dataRetentionMonths;
	}
	
	/**
	 * Allows Spring to bind data retention months from external configuration.
	 * The properties object stays mutable because framework binding populates it after bean construction.
	 *
	 * @param dataRetentionMonths data retention months
	 */
	public void setDataRetentionMonths(int dataRetentionMonths) {
		this.dataRetentionMonths = dataRetentionMonths;
	}
	
	/**
	 * Exposes aggregation lookback days through typed configuration access.
	 * Callers use this accessor so configuration keys do not leak through the codebase as repeated string
	 * lookups.
	 *
	 * @return configured aggregation lookback days
	 */
	public int getAggregationLookbackDays() {
		return aggregationLookbackDays;
	}
	
	/**
	 * Allows Spring to bind aggregation lookback days from external configuration.
	 * The properties object stays mutable because framework binding populates it after bean construction.
	 *
	 * @param aggregationLookbackDays aggregation lookback days
	 */
	public void setAggregationLookbackDays(int aggregationLookbackDays) {
		this.aggregationLookbackDays = aggregationLookbackDays;
	}
	
	/**
	 * Exposes real-time window minutes through typed configuration access.
	 * Callers use this accessor so configuration keys do not leak through the codebase as repeated string
	 * lookups.
	 *
	 * @return configured real-time window minutes
	 */
	public int getRealtimeWindowMinutes() {
		return realtimeWindowMinutes;
	}
	
	/**
	 * Allows Spring to bind real-time window minutes from external configuration.
	 * The properties object stays mutable because framework binding populates it after bean construction.
	 *
	 * @param realtimeWindowMinutes real-time window minutes
	 */
	public void setRealtimeWindowMinutes(int realtimeWindowMinutes) {
		this.realtimeWindowMinutes = realtimeWindowMinutes;
	}
	
	/**
	 * Exposes cross-origin resource sharing origins through typed configuration access.
	 * Callers use this accessor so configuration keys do not leak through the codebase as repeated string
	 * lookups.
	 *
	 * @return configured cross-origin resource sharing origins
	 */
	public List<String> getCorsOrigins() {
		return corsOrigins;
	}
	
	/**
	 * Allows Spring to bind cross-origin resource sharing origins from external configuration.
	 * The properties object stays mutable because framework binding populates it after bean construction.
	 *
	 * @param corsOrigins cross-origin resource sharing origins
	 */
	public void setCorsOrigins(List<String> corsOrigins) {
		this.corsOrigins = corsOrigins;
	}
	
	/**
	 * Exposes geographic internet protocol application programming interface address through typed
	 * configuration access.
	 * Callers use this accessor so configuration keys do not leak through the codebase as repeated string
	 * lookups.
	 *
	 * @return configured geographic internet protocol application programming interface address
	 */
	public String getGeoIpApiUrl() {
		return geoIpApiUrl;
	}
	
	/**
	 * Allows Spring to bind geographic internet protocol application programming interface address from
	 * external configuration.
	 * The properties object stays mutable because framework binding populates it after bean construction.
	 *
	 * @param geoIpApiUrl geographic internet protocol application programming interface address
	 */
	public void setGeoIpApiUrl(String geoIpApiUrl) {
		this.geoIpApiUrl = geoIpApiUrl;
	}
}
