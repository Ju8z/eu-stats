package eu.stats.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves the tracker script through the application itself.
 * That keeps the delivered script aligned with server-side configuration and site validation instead of
 * relying on handwritten embed snippets.
 */
@RestController
public class TrackerScriptController {
	
	/**
	 * Keeps tracker delivery behind a site-aware endpoint.
	 * Serving the script here keeps embed snippets stable while still letting the backend control cache rules
	 * and ship the current bundled tracker implementation.
	 *
	 * @param siteId site identifier
	 * @return tracker script response
	 * @throws IOException when the packaged tracker asset cannot be read
	 */
	@GetMapping(value = "/s.js", produces = "application/javascript")
	public ResponseEntity<String> statisticsJs(@RequestParam("id") Long siteId) throws IOException {
		ClassPathResource resource = new ClassPathResource("static/tracker.js");
		String body = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
		
		return ResponseEntity.ok()
				.contentType(MediaType.valueOf("application/javascript"))
				.cacheControl(CacheControl.noStore().mustRevalidate())
				.header(HttpHeaders.VARY, "Accept-Encoding")
				.body(body);
	}
	
}
