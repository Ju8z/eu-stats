package eu.stats.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.stats.dto.request.CollectPayload;
import eu.stats.service.CollectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Accepts anonymous tracker payloads
 * Due problem with ads and tracker blockers, the API naming should be short and not contain "analytics" or any other word which can be blocked.
 */
@RestController
@RequestMapping("/api/c")
public class CollectionController {
	
	private final CollectionService collectionService;
	
	public CollectionController(CollectionService collectionService) {
		this.collectionService = collectionService;
	}
	
	/**
	 * Stores a pageview/event payload and returns no content.
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void collect(@Valid @RequestBody CollectPayload payload, HttpServletRequest request) {
		collectionService.collect(payload, request);
	}
	
}
