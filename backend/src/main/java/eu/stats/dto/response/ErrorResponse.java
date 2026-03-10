package eu.stats.dto.response;

import java.time.OffsetDateTime;

/**
 * Defines an immutable response contract for error.
 * Using records for responses keeps field names explicit and reduces accidental drift between backend
 * payloads and frontend expectations.
 *
 * @param code      code
 * @param message   exception message
 * @param timestamp timestamp supplied by the tracker
 * @param path      path
 */
public record ErrorResponse(
		
		String code,
		String message,
		OffsetDateTime timestamp,
		String path

) {
	
}
