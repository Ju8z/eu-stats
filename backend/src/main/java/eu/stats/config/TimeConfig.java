package eu.stats.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the application clock as a bean.
 * Injecting time through configuration keeps time-sensitive logic consistent and easier to override when
 * behavior needs to be controlled.
 */
@Configuration
public class TimeConfig {
	
	/**
	 * Registers the application clock.
	 * A single injected clock keeps time-sensitive behavior consistent and easier to control when behavior
	 * needs to be verified.
	 *
	 * @return shared application clock bean
	 */
	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}
}
