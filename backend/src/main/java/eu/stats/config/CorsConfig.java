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
 * Keeps cross-origin access rules close to configuration.
 * Centralizing origin handling avoids scattering deployment-specific access rules across controllers.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

	private final AppProperties appProperties;

	public CorsConfig(AppProperties appProperties) {
		this.appProperties = appProperties;
	}
	
	/**
	 * Applies origin rules from configuration instead of controllers.
	 * Keeping cross-origin handling here makes deployment-specific access changes a configuration concern
	 * rather than an endpoint concern.
	 *
	 * @param registry cross-origin resource sharing registry
	 */
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
