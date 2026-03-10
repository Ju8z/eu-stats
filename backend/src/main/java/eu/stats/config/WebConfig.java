package eu.stats.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Creates shared web-related infrastructure beans.
 * Keeping these beans together makes external integration setup easy to audit and change without touching
 * business services.
 */
@Configuration
public class WebConfig {
	
	/**
	 * Registers a shared web client bean.
	 * Centralizing the client here avoids ad hoc HTTP client construction in services that depend on external
	 * calls.
	 *
	 * @param builder web client builder
	 * @return shared web client bean
	 */
	@Bean
	public RestClient restClient(RestClient.Builder builder) {
		return builder.build();
	}
}
