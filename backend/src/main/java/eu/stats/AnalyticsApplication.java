package eu.stats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import eu.stats.config.AppProperties;

//TODO: Probably EnableAsync is not needed, it was a suggestion from stackoverflow. Check and delete later
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
@SpringBootApplication
public class AnalyticsApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(AnalyticsApplication.class, args);
	}
}
