package eu.stats.util;

import java.net.URI;
import java.util.Set;

import org.springframework.stereotype.Component;

//TODO: For own purpouse, just want to check if its going to work, for example if there is an add on google.com or someone suggest my work :P
// cane be removed later after testing. Maybe add check for headless mode to see crawlers???
@Component
public class ReferrerClassifier {
	
	private static final Set<String> SEARCH_DOMAINS = Set.of(
			"google.com", "bing.com", "duckduckgo.com", "yahoo.com", "yandex.com", "baidu.com"
	);
	
	private static final Set<String> SOCIAL_DOMAINS = Set.of(
			"twitter.com", "x.com", "facebook.com", "instagram.com", "linkedin.com", "reddit.com", "t.co"
	);
	
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
			return ex.getLocalizedMessage();
		}
	}
	
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
