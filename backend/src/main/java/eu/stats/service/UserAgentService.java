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
	
	private String emptyToUnknown(String value) {
		return value == null || value.isBlank() ? UNKNOWN : value;
	}
	
	private String normalizeVersion(String major) {
		return major == null ? EMPTY : major;
	}
	
	/**
	 * Bundles related values that should travel together.
	 * Keeping this nested record inside user agent service prevents closely related analytics values from
	 * drifting apart as separate arguments or map entries.
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
