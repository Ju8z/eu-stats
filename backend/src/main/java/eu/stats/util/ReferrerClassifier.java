package eu.stats.util;

import java.net.URI;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Utility component for classifying referrer sources.
 * <p>
 * Provides methods to normalize referrer URLs and classify them into categories:
 * search engines, social media platforms, direct, and other sources.
 * Used primarily for analytics tracking to understand traffic sources.
 */
@Component
public class ReferrerClassifier {
	
	/**
	 * Set of common search engine domains for referrer classification.
	 */
	private static final Set<String> SEARCH_DOMAINS = Set.of(
			"google.com", "bing.com", "duckduckgo.com", "yahoo.com", "yandex.com", "baidu.com");
	
	/**
	 * Set of common social media domains for referrer classification.
	 */
	private static final Set<String> SOCIAL_DOMAINS = Set.of(
			"twitter.com", "x.com", "facebook.com", "instagram.com", "linkedin.com", "reddit.com", "t.co");
	
	/**
	 * Normalizes a referrer URL by extracting and cleaning the domain.
	 * <p>
	 * Removes the www prefix and converts to lowercase. Returns an empty string
	 * if the referrer is null, blank, or invalid.
	 *
	 * @param referrer the referrer URL to normalize
	 * @return the normalized domain without www prefix, or empty string if invalid
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
	 * Classifies a referrer into one of four categories.
	 * <p>
	 * Categories are determined by checking the normalized domain against known
	 * search engine and social media domains. Falls back to "direct" for blank
	 * referrers and "other" for unmatched domains.
	 *
	 * @param referrer the referrer URL to classify
	 * @return classification category: "search", "social", "direct", or "other"
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
