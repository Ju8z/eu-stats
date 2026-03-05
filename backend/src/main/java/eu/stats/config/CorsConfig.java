package eu.stats.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
				.allowedMethods("GET", "POST", "PUT", "DELETE")
				.allowedHeaders("*")
				.allowCredentials(true);
	}
	
	private List<String> resolveAllowedOriginPatterns() {
		List<String> origins = appProperties.getCorsOrigins();
		
		// Supports both YAML list values and comma-separated env var values
		List<String> configured = (origins == null || origins.isEmpty())
				? List.of("*")
				: origins.stream()
				.filter(Objects::nonNull)
				.flatMap(entry -> Arrays.stream(entry.split(",")))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toList();
		
		List<String> result = new ArrayList<>(configured);
		result.add("null");                     // local file-based test pages send Origin: null
		result.add("http://localhost:[*]");      // Spring's origin pattern syntax for any port
		result.add("http://127.0.0.1:[*]");
		
		return result;
	}
}
