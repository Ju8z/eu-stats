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
 * Keeps tracker ingestion at a minimal web boundary.
 * The controller limits itself to request binding so enrichment, privacy-sensitive decisions, and
 * silent-drop rules live in the service layer.
 */
@RestController
@RequestMapping("/api/c")
public class CollectionController {
	
	private final CollectionService collectionService;
	
	public CollectionController(CollectionService collectionService) {
		this.collectionService = collectionService;
	}
	
	/**
	 * Keeps tracker ingestion on a minimal endpoint.
	 * The controller avoids response-shaping logic so tracker calls stay lightweight and the service decides
	 * which payloads are safe to drop.
	 *
	 * @param payload tracking payload
	 * @param request incoming servlet request
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void collect(@Valid @RequestBody CollectPayload payload, HttpServletRequest request) {
		collectionService.collect(payload, request);
	}
	
}
