package eu.stats.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.stats.dto.request.CreateSiteRequest;
import eu.stats.dto.request.UpdateSiteRequest;
import eu.stats.dto.response.SiteListResponse;
import eu.stats.dto.response.SiteResponse;
import eu.stats.dto.response.SnippetResponse;
import eu.stats.entity.Site;
import eu.stats.exception.SiteNotFoundException;
import eu.stats.repository.SiteRepository;
import eu.stats.service.stats.AggregateSummaryReader;
import eu.stats.util.DateUtil;

/**
 * Centralizes site lifecycle rules.
 * Keeping creation, validation, summary lookup, and snippet access together avoids duplicating
 * site-specific behavior across controllers and analytics services.
 */
@Service
public class SiteService {
	
	private static final long EMPTY_TOTAL = 0L;
	
	private final SiteRepository siteRepository;
	private final SnippetService snippetService;
	private final AggregateSummaryReader aggregateSummaryReader;
	private final Clock clock;
	
	public SiteService(
			SiteRepository siteRepository,
			SnippetService snippetService,
			AggregateSummaryReader aggregateSummaryReader,
			Clock clock) {
		this.siteRepository = siteRepository;
		this.snippetService = snippetService;
		this.aggregateSummaryReader = aggregateSummaryReader;
		this.clock = clock;
	}
	
	/**
	 * Normalizes site input before persistence.
	 * Doing this once keeps domain casing and empty starting totals consistent for every caller that creates a
	 * site.
	 *
	 * @param request create site request
	 * @return site response for the newly created site
	 */
	@Transactional
	public SiteResponse createSite(CreateSiteRequest request) {
		validateDomain(request.domain());
		Site site = new Site();
		site.setDomain(request.domain().trim().toLowerCase());
		site.setName(request.name().trim());
		site.setCreatedAt(OffsetDateTime.now(clock));
		
		Site saved = siteRepository.save(site);
		return toSiteResponse(saved, EMPTY_TOTAL, EMPTY_TOTAL);
	}
	
	/**
	 * Adds recent summary totals to the administration list.
	 * Keeping this composition here prevents controllers from coordinating site reads with separate overview
	 * queries.
	 *
	 * @return site list response
	 */
	public SiteListResponse listSites() {
		List<SiteResponse> sites = siteRepository.findAll().stream()
				.sorted(Comparator.comparing(Site::getCreatedAt).reversed())
				.map(this::toSiteResponse)
				.toList();
		
		return new SiteListResponse(sites);
	}
	
	/**
	 * Reads one site through the shared mapping path.
	 * Reusing the same lookup and mapping logic keeps not-found behavior and last-thirty-day totals consistent
	 * across callers.
	 *
	 * @param siteId site identifier
	 * @return site response for the requested site
	 */
	public SiteResponse getSite(Long siteId) {
		return toSiteResponse(getPublicSite(siteId));
	}
	
	/**
	 * Reapplies creation rules when site data changes.
	 * Normalizing on update prevents domain formatting from drifting based on which endpoint touched the site
	 * last.
	 *
	 * @param siteId site identifier
	 * @param request update site request
	 * @return site response for the updated site
	 */
	@Transactional
	public SiteResponse updateSite(Long siteId, UpdateSiteRequest request) {
		validateDomain(request.domain());
		Site site = getPublicSite(siteId);
		site.setDomain(request.domain().trim().toLowerCase());
		site.setName(request.name().trim());
		
		Site saved = siteRepository.save(site);
		return toSiteResponse(saved);
	}
	
	/**
	 * Routes deletion through the same existence rules used elsewhere.
	 * The service raises the domain-specific missing-site exception here so controller code stays free of
	 * repository-specific error handling.
	 *
	 * @param siteId site identifier
	 */
	@Transactional
	public void delete(Long siteId) {
		Site site = siteRepository.findById(siteId)
				.orElseThrow(() -> new SiteNotFoundException("Site not found"));
		siteRepository.delete(site);
	}
	
	/**
	 * Generates snippets only for validated sites.
	 * That keeps tracker embed generation aligned with the same existence checks used by the rest of the site
	 * application programming interface.
	 *
	 * @param siteId site identifier
	 * @return tracker snippet response
	 */
	public SnippetResponse getSnippet(Long siteId) {
		Site site = getPublicSite(siteId);
		return snippetService.buildSnippet(site);
	}
	
	/**
	 * Centralizes the missing-site check.
	 * Downstream callers can assume the site exists once this method returns and rely on one shared exception
	 * type when it does not.
	 *
	 * @param siteId site identifier
	 * @return resolved site entity
	 */
	public Site getPublicSite(Long siteId) {
		return siteRepository.findById(siteId)
				.orElseThrow(() -> new SiteNotFoundException("Site not found"));
	}
	
	/**
	 * Rejects malformed domains before they can become persistent identifiers.
	 * Keeping validation here protects every create and update path from storing values that would later make
	 * snippet generation, matching, or reporting unreliable.
	 *
	 * @param domain user-supplied domain value
	 */
	private void validateDomain(String domain) {
		String value = domain == null ? "" : domain.trim().toLowerCase();
		int colonIndex = value.lastIndexOf(':');
		String host = colonIndex > 0 ? value.substring(0, colonIndex) : value;
		
		if (host.isEmpty() || host.startsWith(".") || host.startsWith("-")
				|| host.endsWith(".") || host.endsWith("-")) {
			throw new IllegalArgumentException("Invalid domain format");
		}
		
		for (char character : host.toCharArray()) {
			if (!Character.isLetterOrDigit(character) && character != '.' && character != '-') {
				throw new IllegalArgumentException("Invalid domain format");
			}
		}
	}
	
	/**
	 * Reuses the same comparison window that the site list advertises to users.
	 * Keeping the range calculation in one helper prevents the administration summary cards from drifting as
	 * the default lookback window evolves.
	 *
	 * @return last-thirty-day reporting window
	 */
	private DateUtil.DateRange last30DaysRange() {
		LocalDate today = LocalDate.now(clock);
		return new DateUtil.DateRange(today.minusDays(29), today);
	}
	
	/**
	 * Keeps the public response mapping behind one path.
	 * Reusing this helper makes sure every site-facing endpoint applies the same summary lookback and field
	 * ordering.
	 *
	 * @param site resolved site entity
	 * @return site response enriched with the default summary window
	 */
	private SiteResponse toSiteResponse(Site site) {
		AggregateSummaryReader.SummaryTotals summaryTotals = summaryForLast30Days(site.getId());
		return toSiteResponse(site, summaryTotals.totalPageviews(), summaryTotals.uniqueVisitors());
	}
	
	/**
	 * Hides the default administration lookback behind a descriptive name.
	 * That keeps response mapping code focused on intent rather than repeating date-range plumbing.
	 *
	 * @param siteId site identifier
	 * @return summary totals for the default administration window
	 */
	private AggregateSummaryReader.SummaryTotals summaryForLast30Days(Long siteId) {
		return aggregateSummaryReader.readTotals(siteId, last30DaysRange());
	}
	
	/**
	 * Builds the response from already-resolved totals when callers have them on hand.
	 * Separating this mapping step avoids duplicate field-ordering code across create, read, and update
	 * paths.
	 *
	 * @param site resolved site entity
	 * @param totalPageviewsLast30Days page views from the default administration window
	 * @param uniqueVisitorsLast30Days unique visitors from the default administration window
	 * @return site response with summary totals attached
	 */
	private SiteResponse toSiteResponse(Site site, long totalPageviewsLast30Days, long uniqueVisitorsLast30Days) {
		return new SiteResponse(
				site.getId(),
				site.getName(),
				site.getDomain(),
				totalPageviewsLast30Days,
				uniqueVisitorsLast30Days);
	}
}
