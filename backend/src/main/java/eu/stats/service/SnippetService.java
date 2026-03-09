package eu.stats.service;

import org.springframework.stereotype.Service;

import eu.stats.config.AppProperties;
import eu.stats.dto.response.SnippetResponse;
import eu.stats.entity.Site;

@Service
public class SnippetService {
	
	private final AppProperties appProperties;
	
	public SnippetService(AppProperties appProperties) {
		this.appProperties = appProperties;
	}
	
	public SnippetResponse buildSnippet(Site site) {
		String html = "<script src=\"" + appProperties.getTrackerBaseUrl() + "/s.js?id=" + site.getId() + "\"></script>";
		
		return new SnippetResponse(html, site.getId(), site.getDomain());
	}
}
