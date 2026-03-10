package eu.stats.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Creates shared web-related infrastructure beans.
 * Keeping these beans together makes external integration setup easy to audit and change without touching
 * business services.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
	
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
	
	/**
	 * Just for Demo purpose to show /demo/ website.
	 *
	 * @param registry registry new path
	 */
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addRedirectViewController("/demo", "/demo/");
		registry.addViewController("/demo/").setViewName("forward:/demo/index.html");
	}
}
