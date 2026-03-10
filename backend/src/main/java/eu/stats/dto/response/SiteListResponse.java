package eu.stats.dto.response;

import java.util.List;

/**
 * Defines an immutable response contract for site list.
 * Using records for responses keeps field names explicit and reduces accidental drift between backend
 * payloads and frontend expectations.
 *
 * @param sites sites
 */
public record SiteListResponse(
		
		List<SiteResponse> sites

) {

}