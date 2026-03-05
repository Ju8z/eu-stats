package eu.stats.service;

import org.springframework.stereotype.Service;

import ua_parser.Client;
import ua_parser.Parser;

@Service
public class UserAgentService {
	
	private static final String UNKNOWN = "Unknown";
	
	private final Parser parser = new Parser();
	
	public UserAgentDetails parse(String userAgent) {
		try {
			Client client = parser.parse(userAgent);
			String browser = client.userAgent != null ? emptyToUnknown(client.userAgent.family) : UNKNOWN;
			String browserVersion = client.userAgent != null ? normalizeVersion(client.userAgent.major) : "";
			String os = client.os != null ? emptyToUnknown(client.os.family) : UNKNOWN;
			String osVersion = client.os != null ? normalizeVersion(client.os.major) : "";
			String deviceType = inferDeviceType(userAgent);
			
			return new UserAgentDetails(browser, browserVersion, os, osVersion, deviceType);
		} catch (Exception ex) {
			return new UserAgentDetails(UNKNOWN, "", UNKNOWN, "", "desktop");
		}
	}
	
	private String inferDeviceType(String userAgent) {
		String ua = userAgent == null ? "" : userAgent.toLowerCase();
		if (ua.contains("ipad") || ua.contains("tablet")) {
			return "tablet";
		}
		if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
			return "mobile";
		}
		
		return "desktop";
	}
	
	private String emptyToUnknown(String value) {
		return value == null || value.isBlank() ? UNKNOWN : value;
	}
	
	private String normalizeVersion(String major) {
		return major == null ? "" : major;
	}
	
	public record UserAgentDetails(String browser, String browserVersion, String os, String osVersion, String deviceType) {
	}
}
