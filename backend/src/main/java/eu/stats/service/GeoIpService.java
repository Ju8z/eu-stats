package eu.stats.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import eu.stats.config.AppProperties;
import eu.stats.util.HashUtil;

/**
 * Resolves geographic data without keeping raw addresses around longer than needed.
 * Caching results by hashed internet protocol address keeps repeated lookups cheap while avoiding plain
 * addresses as long-lived cache keys.
 */
@Service
public class GeoIpService {
	
	private static final Duration CACHE_TTL = Duration.ofHours(24);
	
	private final RestClient restClient;
	private final AppProperties appProperties;
	private final HashUtil hashUtil;
	private final Clock clock;
	private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
	
	public GeoIpService(RestClient restClient, AppProperties appProperties, HashUtil hashUtil, Clock clock) {
		this.restClient = restClient;
		this.appProperties = appProperties;
		this.hashUtil = hashUtil;
		this.clock = clock;
	}
	
	/**
	 * Hides network lookup latency and failure handling from ingestion code.
	 * Caching by hashed address keeps repeat lookups cheap without turning raw addresses into long-lived cache
	 * keys.
	 *
	 * @param ip internet protocol address
	 * @return geographic lookup result
	 */
	public GeoIpResult resolve(String ip) {
		String hashedIp = hashUtil.sha256Hex(ip);
		Instant now = Instant.now(clock);
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
	
	/**
	 * Keeps cached lookup data and its expiry bound together.
	 * Storing both values in one record makes it harder for future cache changes to reuse stale location
	 * data by accident.
	 *
	 * @param value     cached lookup result
	 * @param expiresAt instant after which the cache entry must be ignored
	 */
	private record CacheEntry(GeoIpResult value, Instant expiresAt) {
	}
	
	/**
	 * Keeps geolocation enrichment explicit even when the external lookup has little or no data.
	 * Returning a small record here gives the success path and the failure fallback one shared contract.
	 *
	 * @param country country code
	 */
	public record GeoIpResult(String country) {
		
		/**
		 * Provides a neutral geographic lookup result.
		 * The collection flow can keep running when enrichment fails because callers receive an explicit empty
		 * object instead of dealing with null.
		 *
		 * @return empty geographic lookup result
		 */
		public static GeoIpResult empty() {
			return new GeoIpResult(null);
		}
	}
}
