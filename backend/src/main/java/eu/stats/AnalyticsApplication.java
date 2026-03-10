package eu.stats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import eu.stats.config.AppProperties;

/**
 * Keeps application startup on one explicit Spring Boot entry point.
 * Using a single bootstrap class avoids hidden initialization paths and makes local runs, tests, and
 * packaged deployments start the same way.
 */
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
@SpringBootApplication
public class AnalyticsApplication {
	
	/**
	 * Starts the application through the standard Spring Boot entry point.
	 * Keeping startup here gives every runtime the same bootstrap path and avoids hidden initialization logic
	 * elsewhere.
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(AnalyticsApplication.class, args);
	}
}
