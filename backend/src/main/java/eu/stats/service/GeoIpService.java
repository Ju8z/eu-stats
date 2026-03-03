package eu.stats.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import eu.stats.config.AppProperties;
import eu.stats.util.HashUtil;

@Service
public class GeoIpService {
	
	private static final Duration CACHE_TTL = Duration.ofHours(24);
	
	private final RestClient restClient;
	private final AppProperties appProperties;
	private final HashUtil hashUtil;
	private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
	
	public GeoIpService(RestClient restClient, AppProperties appProperties, HashUtil hashUtil) {
		this.restClient = restClient;
		this.appProperties = appProperties;
		this.hashUtil = hashUtil;
	}
	
	public GeoIpResult resolve(String ip) {
		String hashedIp = hashUtil.sha256Hex(ip);
		Instant now = Instant.now();
		
		CacheEntry cached = cache.get(hashedIp);
		if (cached != null && cached.expiresAt().isAfter(now)) {
			return cached.value();
		}
		
		try {
			GeoIpResult response = restClient.get()
					.uri(appProperties.getGeoIpApiUrl() + "/{ip}", ip)
					.retrieve()
					.body(GeoIpResult.class);
			
			GeoIpResult resolved = response == null ? GeoIpResult.empty() : response;
			cache.put(hashedIp, new CacheEntry(resolved, now.plus(CACHE_TTL)));
			return resolved;
		} catch (Exception ex) {
			return GeoIpResult.empty();
		}
	}
	
	private record CacheEntry(GeoIpResult value, Instant expiresAt) {
	}
	
	public record GeoIpResult(String ip, String country) {
		
		public static GeoIpResult empty() {
			return new GeoIpResult(null, null);
		}
	}
}
