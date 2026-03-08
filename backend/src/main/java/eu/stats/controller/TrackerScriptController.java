package eu.stats.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

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
 * Serves the lightweight tracker script.
 */
@RestController
public class TrackerScriptController {
	
	/**
	 * Returns the tracker JavaScript with cache-friendly headers.
	 * Filename should be short written because of adblockers.
	 */
	@GetMapping(value = "/s.js", produces = "application/javascript")
	public ResponseEntity<String> statisticsJs(@RequestParam("id") Long siteId) throws IOException {
		// Site ID is validated client-side in script and server-side on /api/p. So im just forcing siteId= to be set :)
		ClassPathResource resource = new ClassPathResource("static/tracker.js");
		String body = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
		Duration cacheAge = Duration.ofDays(1);
		
		return ResponseEntity.ok()
				.contentType(MediaType.valueOf("application/javascript"))
				.cacheControl(CacheControl.maxAge(cacheAge).cachePublic())
				.header(HttpHeaders.VARY, "Accept-Encoding")
				.body(body);
	}
	
}