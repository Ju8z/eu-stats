package eu.stats.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows cross-origin requests from your frontend to the backend API (browsers block these by default)
 * Without it, frontend or tracker script would get blocked by the browser with a CORS error whenever it call the backend
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
	
	private final AppProperties appProperties;
	
	public CorsConfig(AppProperties appProperties) {
		this.appProperties = appProperties;
	}
	
	@Override
	public void addCorsMappings(@NonNull CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOriginPatterns(resolveAllowedOriginPatterns().toArray(String[]::new))
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("*")
				.allowCredentials(true);
	}
	
	private List<String> resolveAllowedOriginPatterns() {
		Set<String> allowedOriginPatterns = new LinkedHashSet<>();
		List<String> origins = appProperties.getCorsOrigins();
		
		if (origins == null || origins.isEmpty()) {
			allowedOriginPatterns.add("*");
		} else {
			for (String originEntry : origins) {
				if (originEntry == null) {
					continue;
				}
				// Supports both YAML list values and comma-separated env var values.
				for (String token : originEntry.split(",")) {
					String trimmed = token.trim();
					if (!trimmed.isEmpty()) {
						allowedOriginPatterns.add(trimmed);
					}
				}
			}
		}
		
		// Local file-based test pages send Origin: null.
		allowedOriginPatterns.add("null");
		// Spring's origin pattern syntax for any port uses [*].
		allowedOriginPatterns.add("http://localhost:[*]");
		allowedOriginPatterns.add("http://127.0.0.1:[*]");
		
		return new ArrayList<>(allowedOriginPatterns);
	}
}
