package eu.stats.util;

import java.net.URI;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Collapses inbound referrers into stable reporting categories.
 * That keeps traffic source charts readable and prevents small host variations from fragmenting the same
 * source across multiple buckets.
 */
@Component
public class ReferrerClassifier {
	
	private static final Set<String> SEARCH_DOMAINS = Set.of(
			"google.com", "bing.com", "duckduckgo.com", "yahoo.com", "yandex.com", "baidu.com");
	
	private static final Set<String> SOCIAL_DOMAINS = Set.of(
			"twitter.com", "x.com", "facebook.com", "instagram.com", "linkedin.com", "reddit.com", "t.co");
	
	/**
	 * Normalizes host names before aggregation.
	 * Collapsing protocol and common subdomain differences here prevents the same source from splitting across
	 * multiple report buckets.
	 *
	 * @param referrer referrer
	 * @return normalized referrer domain
	 */
	public String normalizeDomain(String referrer) {
		if (referrer == null || referrer.isBlank()) {
			return "";
		}
		
		try {
			URI uri = URI.create(referrer.startsWith("http") ? referrer : "https://" + referrer);
			String host = uri.getHost();
			if (host == null) {
				return "";
			}
			
			return host.toLowerCase().replaceFirst("^www\\.", "");
		} catch (Exception ex) {
			return "";
		}
	}
	
	/**
	 * Maps many external domains into a small reporting vocabulary.
	 * Keeping this classification stable makes source charts readable over time even as inbound referrer hosts
	 * vary in detail.
	 *
	 * @param referrer referrer
	 * @return stable referrer category
	 */
	public String classify(String referrer) {
		String domain = normalizeDomain(referrer);
		if (domain.isBlank()) {
			return "direct";
		}
		if (SEARCH_DOMAINS.stream().anyMatch(domain::endsWith)) {
			return "search";
		}
		if (SOCIAL_DOMAINS.stream().anyMatch(domain::endsWith)) {
			return "social";
		}
		
		return "other";
	}
}
