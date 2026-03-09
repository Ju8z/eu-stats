package eu.stats.dto.response;

import java.time.OffsetDateTime;

public record SiteResponse(
		
		Long id,
		String name,
		String domain,
		long totalPageviewsLast30Days,
		long uniqueVisitorsLast30Days,
		OffsetDateTime createdAt

) {

}