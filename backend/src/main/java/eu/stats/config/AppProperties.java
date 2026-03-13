package eu.stats.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Keeps runtime analytics settings in one Spring-bound object.
 * A typed home for these values makes deployment changes easier to audit and prevents services from
 * duplicating property-key knowledge.
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
	 * Gives snippet and tracker-delivery code one shared origin.
	 * Reading this through the properties object keeps embed URLs aligned with the environment that is
	 * actually serving the tracker.
	 *
	 * @return configured tracker base address
	 */
	public String getTrackerBaseUrl() {
		return trackerBaseUrl;
	}
	
	/**
	 * Lets deployment configuration decide where tracker assets should point.
	 * Spring binds the value here so different environments can publish the tracker from different hosts
	 * without code changes.
	 *
	 * @param trackerBaseUrl tracker base address
	 */
	public void setTrackerBaseUrl(String trackerBaseUrl) {
		this.trackerBaseUrl = trackerBaseUrl;
	}
	
	/**
	 * Gives hashing code one place to obtain the rotating salt secret.
	 * Centralizing the lookup keeps privacy-sensitive identifiers consistent across every request processed
	 * on the same day.
	 *
	 * @return configured daily salt secret
	 */
	public String getDailySaltSecret() {
		return dailySaltSecret;
	}
	
	/**
	 * Lets operations rotate the daily hashing secret without recompiling the application.
	 * Spring binds the secret here because the value belongs to deployment policy, not source code.
	 *
	 * @param dailySaltSecret daily salt secret
	 */
	public void setDailySaltSecret(String dailySaltSecret) {
		this.dailySaltSecret = dailySaltSecret;
	}
	
	/**
	 * Gives the retention job a single source of truth for cleanup policy.
	 * Keeping the window here makes privacy retention adjustable per deployment without changing code.
	 *
	 * @return configured data retention months
	 */
	public int getDataRetentionMonths() {
		return dataRetentionMonths;
	}
	
	/**
	 * Lets operators tighten or relax raw-data retention through configuration.
	 * Spring binds the value here because the retention window is an environment decision, not a compile-time
	 * constant.
	 *
	 * @param dataRetentionMonths data retention months
	 */
	public void setDataRetentionMonths(int dataRetentionMonths) {
		this.dataRetentionMonths = dataRetentionMonths;
	}
	
	/**
	 * Gives the aggregation job a shared late-arrival safety window.
	 * Reading the lookback from one place keeps scheduled refreshes and operational expectations in sync.
	 *
	 * @return configured aggregation lookback days
	 */
	public int getAggregationLookbackDays() {
		return aggregationLookbackDays;
	}
	
	/**
	 * Lets operations tune how aggressively aggregation jobs rescan recent traffic.
	 * Spring binds the value here so late-event tolerance can change without a rebuild.
	 *
	 * @param aggregationLookbackDays aggregation lookback days
	 */
	public void setAggregationLookbackDays(int aggregationLookbackDays) {
		this.aggregationLookbackDays = aggregationLookbackDays;
	}
	
	/**
	 * Gives live dashboard queries one consistent definition of "recent".
	 * Using a shared window length keeps real-time widgets aligned instead of letting each query invent its
	 * own cutoff.
	 *
	 * @return configured real-time window minutes
	 */
	public int getRealtimeWindowMinutes() {
		return realtimeWindowMinutes;
	}
	
	/**
	 * Lets deployments decide how fresh "live" analytics should feel.
	 * Spring binds the value here so operators can widen or tighten the real-time window without touching
	 * query code.
	 *
	 * @param realtimeWindowMinutes real-time window minutes
	 */
	public void setRealtimeWindowMinutes(int realtimeWindowMinutes) {
		this.realtimeWindowMinutes = realtimeWindowMinutes;
	}
	
	/**
	 * Gives web configuration a deployment-specific allowlist for browser access.
	 * Keeping origins here makes cross-origin policy changeable without editing controller or filter code.
	 *
	 * @return configured cross-origin resource sharing origins
	 */
	public List<String> getCorsOrigins() {
		return corsOrigins;
	}
	
	/**
	 * Lets each environment declare which browser origins may call the API.
	 * Spring binds the allowlist here because cross-origin policy is an operational concern that changes
	 * between deployments.
	 *
	 * @param corsOrigins cross-origin resource sharing origins
	 */
	public void setCorsOrigins(List<String> corsOrigins) {
		this.corsOrigins = corsOrigins;
	}
	
	/**
	 * Gives geographic enrichment one configurable upstream endpoint.
	 * Keeping the provider URL here makes it possible to swap services or environments without rewriting the
	 * enrichment code.
	 *
	 * @return configured geographic internet protocol application programming interface address
	 */
	public String getGeoIpApiUrl() {
		return geoIpApiUrl;
	}
	
	/**
	 * Lets deployments choose the upstream geographic lookup endpoint.
	 * Spring binds the value here so provider changes remain a configuration concern instead of a code change.
	 *
	 * @param geoIpApiUrl geographic internet protocol application programming interface address
	 */
	public void setGeoIpApiUrl(String geoIpApiUrl) {
		this.geoIpApiUrl = geoIpApiUrl;
	}
}
