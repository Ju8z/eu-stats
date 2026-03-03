package eu.stats.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSiteRequest(
		
		@NotBlank
		@Size(max = 255) String name,
		
		@NotBlank
		@Size(max = 255) String domain
) {
}
