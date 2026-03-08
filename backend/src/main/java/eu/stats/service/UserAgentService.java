package eu.stats.service;

import org.springframework.stereotype.Service;

import ua_parser.Client;
import ua_parser.Parser;

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
	
	public record UserAgentDetails(String browser, String browserVersion, String os, String osVersion, String deviceType) {
	}
}
