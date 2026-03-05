package eu.stats.dto.response;

import java.time.OffsetDateTime;

public record ErrorResponse(
		
		String code,
		String message,
		OffsetDateTime timestamp,
		String path

) {
	
}
