package eu.stats.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.stats.dto.request.CreateSiteRequest;
import eu.stats.dto.request.UpdateSiteRequest;
import eu.stats.dto.response.SiteListResponse;
import eu.stats.dto.response.SiteResponse;
import eu.stats.dto.response.SnippetResponse;
import eu.stats.service.SiteService;
import jakarta.validation.Valid;

/**
 * Manages tracking site instances.
 */
@RestController
@RequestMapping("/api/sites")
public class SiteController {
	
	private final SiteService siteService;
	
	public SiteController(SiteService siteService) {
		this.siteService = siteService;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SiteResponse create(@Valid @RequestBody CreateSiteRequest request) {
		return siteService.createSite(request);
	}
	
	@GetMapping
	public SiteListResponse list() {
		return siteService.listSites();
	}
	
	@GetMapping("/{siteId}")
	public SiteResponse get(@PathVariable Long siteId) {
		return siteService.getSite(siteId);
	}
	
	@PutMapping("/{siteId}")
	public SiteResponse update(@PathVariable Long siteId, @Valid @RequestBody UpdateSiteRequest request) {
		return siteService.updateSite(siteId, request);
	}
	
	@DeleteMapping("/{siteId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long siteId) {
		siteService.delete(siteId);
	}
	
	@GetMapping("/{siteId}/snippet")
	public SnippetResponse snippet(@PathVariable Long siteId) {
		return siteService.getSnippet(siteId);
	}
}
