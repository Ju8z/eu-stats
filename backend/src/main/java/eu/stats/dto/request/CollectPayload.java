package eu.stats.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CollectPayload(
		
		@NotNull
		Long siteId,
		
		@NotBlank
		@Size(max = 2048)
		String url,
		
		@Size(max = 500)
		String title,
		
		@Size(max = 500)
		String referrer,
		
		@Size(max = 20)
		String screenResolution,
		
		@Size(max = 20)
		String viewport,
		
		@Size(max = 10)
		String language,
		
		@NotBlank
		@Size(max = 1024)
		String userAgent,
		
		@NotBlank
		@Size(max = 50)
		String eventType,
		
		@Size(max = 255)
		String eventName,
		
		@NotBlank
		String timestamp

) {

}
