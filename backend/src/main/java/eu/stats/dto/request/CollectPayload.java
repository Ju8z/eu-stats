package eu.stats.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Defines an immutable request boundary for collect.
 * Keeping validation on the request record makes malformed input fail at the edge before deeper processing
 * begins.
 *
 * @param siteId    site identifier
 * @param url       page address
 * @param title     page title
 * @param referrer  referrer
 * @param userAgent user agent string
 * @param eventType event type
 * @param eventName event name
 * @param timestamp timestamp supplied by the tracker
 */
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
