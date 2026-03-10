package eu.stats.dto.response;

/**
 * Defines an immutable response contract for site.
 * Using records for responses keeps field names explicit and reduces accidental drift between backend
 * payloads and frontend expectations.
 *
 * @param id                       identifier
 * @param name                     name
 * @param domain                   domain
 * @param totalPageviewsLast30Days total page views last30 days
 * @param uniqueVisitorsLast30Days unique visitors last30 days
 */
public record SiteResponse(
		
		Long id,
		String name,
		String domain,
		long totalPageviewsLast30Days,
		long uniqueVisitorsLast30Days

) {

}
