package eu.stats.service;

import org.springframework.stereotype.Service;

import eu.stats.config.AppProperties;
import eu.stats.dto.response.SnippetResponse;
import eu.stats.entity.Site;

/**
 * Generates tracker embed code from runtime configuration.
 * Building the snippet on the server keeps the delivered tracker address aligned with the current
 * deployment without hard-coded frontend assumptions.
 */
@Service
public class SnippetService {
	
	private final AppProperties appProperties;
	
	public SnippetService(AppProperties appProperties) {
		this.appProperties = appProperties;
	}
	
	/**
	 * Builds embed code from runtime configuration instead of hard-coded client values.
	 * That keeps tracker addresses correct across environments and prevents callers from reconstructing script
	 * tags inconsistently.
	 *
	 * @param site site
	 * @return tracker snippet response
	 */
	public SnippetResponse buildSnippet(Site site) {
		String html = "<script src=\"" + appProperties.getTrackerBaseUrl() + "/s.js?id=" + site.getId() + "\"></script>";
		
		return new SnippetResponse(html);
	}
}
