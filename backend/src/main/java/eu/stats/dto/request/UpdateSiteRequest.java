package eu.stats.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Defines an immutable request boundary for update site.
 * Keeping validation on the request record makes malformed input fail at the edge before deeper processing
 * begins.
 *
 * @param name   name
 * @param domain domain
 */
public record UpdateSiteRequest(
		
		@NotBlank
		@Size(max = 255)
		String name,
		
		@NotBlank
		@Size(max = 255)
		String domain
) {
	
}
