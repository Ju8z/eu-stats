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
 * Keeps site administration endpoints consistent.
 * The controller stays thin so normalization, existence checks, and response shaping all follow one service
 * path.
 */
@RestController
@RequestMapping("/api/sites")
public class SiteController {
	
	private final SiteService siteService;
	
	public SiteController(SiteService siteService) {
		this.siteService = siteService;
	}
	
	/**
	 * Keeps site creation behind one endpoint.
	 * The controller stays focused on request binding so normalization and persistence rules remain
	 * centralized in the service layer.
	 *
	 * @param request create site request
	 * @return site response for the newly created site
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SiteResponse create(@Valid @RequestBody CreateSiteRequest request) {
		return siteService.createSite(request);
	}
	
	/**
	 * Keeps site listing separate from site detail reads.
	 * That lets administration views refresh the full list without requesting one site at a time.
	 *
	 * @return site list response
	 */
	@GetMapping
	public SiteListResponse list() {
		return siteService.listSites();
	}
	
	/**
	 * Keeps single-site reads on a dedicated endpoint.
	 * The controller delegates immediately so not-found handling and summary mapping stay identical across
	 * callers.
	 *
	 * @param siteId site identifier
	 * @return site response for the requested site
	 */
	@GetMapping("/{siteId}")
	public SiteResponse get(@PathVariable Long siteId) {
		return siteService.getSite(siteId);
	}
	
	/**
	 * Keeps site updates behind the same public boundary as creation.
	 * The service can therefore reuse the same normalization and validation rules instead of splitting them
	 * across endpoints.
	 *
	 * @param siteId site identifier
	 * @param request update site request
	 * @return site response for the updated site
	 */
	@PutMapping("/{siteId}")
	public SiteResponse update(@PathVariable Long siteId, @Valid @RequestBody UpdateSiteRequest request) {
		return siteService.updateSite(siteId, request);
	}
	
	/**
	 * Keeps site deletion explicit in the public interface.
	 * The controller delegates directly so repository-specific error handling does not leak into web code.
	 *
	 * @param siteId site identifier
	 */
	@DeleteMapping("/{siteId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long siteId) {
		siteService.delete(siteId);
	}
	
	/**
	 * Keeps snippet generation behind a site-aware endpoint.
	 * The server remains the source of truth for tracker address formatting and site validation instead of
	 * leaving that logic to clients.
	 *
	 * @param siteId site identifier
	 * @return tracker snippet response
	 */
	@GetMapping("/{siteId}/snippet")
	public SnippetResponse snippet(@PathVariable Long siteId) {
		return siteService.getSnippet(siteId);
	}
}
