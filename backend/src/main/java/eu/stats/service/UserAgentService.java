package eu.stats.service;

import org.springframework.stereotype.Service;

import ua_parser.Client;
import ua_parser.Parser;

/**
 * Normalizes user agent strings into stable analytics labels.
 * Wrapping the parser library here keeps parser failures and default labeling decisions out of the rest of
 * the analytics code.
 */
@Service
public class UserAgentService {
	
	private static final String UNKNOWN = "Unknown";
	private static final String EMPTY = "";
	private static final String DESKTOP = "Desktop";
	private static final String TABLET = "Tablet";
	private static final String MOBILE = "Mobile";
	private static final String IPAD = "iPad";
	private static final String ANDROID = "Android";
	private static final String IPHONE = "iPhone";
	
	private final Parser parser = new Parser();
	
	/**
	 * Normalizes parser output into the small label set used by analytics.
	 * Fallback defaults keep reports stable even when uncommon user agents cannot be classified cleanly.
	 *
	 * @param userAgent user agent string
	 * @return normalized user agent details
	 */
	public UserAgentDetails parse(String userAgent) {
		try {
			Client client = parser.parse(userAgent);
			String browser = client.userAgent != null ? emptyToUnknown(client.userAgent.family) : UNKNOWN;
			String browserVersion = client.userAgent != null ? normalizeVersion(client.userAgent.major) : EMPTY;
			String os = client.os != null ? emptyToUnknown(client.os.family) : UNKNOWN;
			String osVersion = client.os != null ? normalizeVersion(client.os.major) : EMPTY;
			String deviceType = inferDeviceType(userAgent);
			
			return new UserAgentDetails(browser, browserVersion, os, osVersion, deviceType);
		} catch (Exception ex) {
			return new UserAgentDetails(UNKNOWN, EMPTY, UNKNOWN, EMPTY, DESKTOP);
		}
	}
	
	/**
	 * Collapses long-tail user-agent strings into the device labels the dashboard actually uses.
	 * Keeping this heuristic local means analytics can evolve its mobile or tablet rules without spreading
	 * string matching across the codebase.
	 *
	 * @param userAgent raw user-agent string
	 * @return normalized device label used in reports
	 */
	private String inferDeviceType(String userAgent) {
		String ua = userAgent == null ? EMPTY : userAgent.toLowerCase();
		if (ua.contains(toLowerCase(IPAD)) || ua.contains(toLowerCase(TABLET))) {
			return TABLET;
		}
		if (ua.contains(toLowerCase(MOBILE)) || ua.contains(toLowerCase(ANDROID)) || ua.contains(toLowerCase(IPHONE))) {
			return MOBILE;
		}
		
		return DESKTOP;
	}
	
	private String toLowerCase(String devices) {
		return devices.toLowerCase();
	}
	
	/**
	 * Treats parser blanks the same way as missing values.
	 * Doing that once here keeps uncommon user agents from creating empty labels in charts.
	 *
	 * @param value parsed family or platform name
	 * @return known label or the shared unknown placeholder
	 */
	private String emptyToUnknown(String value) {
		return value == null || value.isBlank() ? UNKNOWN : value;
	}
	
	/**
	 * Suppresses empty version fragments so labels stay readable.
	 * The dashboard can show broad browser and operating-system families without rendering noisy blank
	 * version markers.
	 *
	 * @param major parsed major version
	 * @return version text or an empty marker when the parser has no value
	 */
	private String normalizeVersion(String major) {
		return major == null ? EMPTY : major;
	}
	
	/**
	 * Keeps parsed user-agent facets moving through ingestion as one decision.
	 * Returning a record here makes it harder for browser, operating system, and device labels to drift out
	 * of sync as the parsing rules evolve.
	 *
	 * @param browser browser name
	 * @param browserVersion browser version
	 * @param os operating system name
	 * @param osVersion operating system version
	 * @param deviceType device type
	 */
	public record UserAgentDetails(String browser, String browserVersion, String os, String osVersion, String deviceType) {
	}
}
