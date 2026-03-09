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
	
	public SiteListResponse listSites() {
		List<SiteResponse> sites = siteRepository.findAll().stream()
				.sorted(Comparator.comparing(Site::getCreatedAt).reversed())
				.map(this::toSiteResponse)
				.toList();
		
		return new SiteListResponse(sites);
	}
	
	public SiteResponse getSite(Long siteId) {
		return toSiteResponse(getPublicSite(siteId));
	}
	
	@Transactional
	public SiteResponse updateSite(Long siteId, UpdateSiteRequest request) {
		validateDomain(request.domain());
		Site site = getPublicSite(siteId);
		site.setDomain(request.domain().trim().toLowerCase());
		site.setName(request.name().trim());
		
		Site saved = siteRepository.save(site);
		return toSiteResponse(saved);
	}
	
	@Transactional
	public void delete(Long siteId) {
		Site site = siteRepository.findById(siteId)
				.orElseThrow(() -> new SiteNotFoundException("Site not found"));
		siteRepository.delete(site);
	}
	
	public SnippetResponse getSnippet(Long siteId) {
		Site site = getPublicSite(siteId);
		return snippetService.buildSnippet(site);
	}
	
	public Site getPublicSite(Long siteId) {
		return siteRepository.findById(siteId)
				.orElseThrow(() -> new SiteNotFoundException("Site not found"));
	}
	
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
	
	private DateUtil.DateRange last30DaysRange() {
		LocalDate today = LocalDate.now(clock);
		return new DateUtil.DateRange(today.minusDays(29), today);
	}
	
	private SiteResponse toSiteResponse(Site site) {
		AggregateSummaryReader.SummaryTotals summaryTotals = summaryForLast30Days(site.getId());
		return toSiteResponse(site, summaryTotals.totalPageviews(), summaryTotals.uniqueVisitors());
	}
	
	private AggregateSummaryReader.SummaryTotals summaryForLast30Days(Long siteId) {
		return aggregateSummaryReader.readTotals(siteId, last30DaysRange());
	}
	
	private SiteResponse toSiteResponse(Site site, long totalPageviewsLast30Days, long uniqueVisitorsLast30Days) {
		return new SiteResponse(
				site.getId(),
				site.getName(),
				site.getDomain(),
				totalPageviewsLast30Days,
				uniqueVisitorsLast30Days,
				site.getCreatedAt());
	}
}
