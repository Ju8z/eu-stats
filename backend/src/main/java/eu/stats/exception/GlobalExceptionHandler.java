package eu.stats.exception;

import java.time.Clock;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import eu.stats.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Keeps client-visible error formatting consistent.
 * Centralizing exception translation lets controllers focus on success paths while failures still produce
 * one stable response shape.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	private final Clock clock;
	
	public GlobalExceptionHandler(Clock clock) {
		this.clock = clock;
	}
	
	/**
	 * Maps a missing site to the public error envelope.
	 * The dedicated handler keeps a common domain failure predictable for clients without forcing controllers
	 * to know HTTP status details.
	 *
	 * @param ex      missing-site failure raised by the domain layer
	 * @param request incoming servlet request
	 * @return error response for the missing-site case
	 */
	@ExceptionHandler(SiteNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleSiteNotFound(SiteNotFoundException ex, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "SITE_NOT_FOUND", ex.getMessage(), request.getRequestURI());
	}
	
	/**
	 * Maps guard-clause failures to a client error response.
	 * That keeps invalid request parameters from surfacing as generic server errors.
	 *
	 * @param ex invalid-input failure raised by validation or guard clauses
	 * @param request incoming servlet request
	 * @return error response for invalid input
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request.getRequestURI());
	}
	
	/**
	 * Normalizes framework-generated 404 responses into the shared error envelope.
	 * Clients do not need to special-case missing routes because the same payload shape is used for domain
	 * and framework not-found cases.
	 *
	 * @param ex missing-resource signal raised by Spring
	 * @param request incoming servlet request
	 * @return error response for a missing resource
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleMissingResource(NoResourceFoundException ex, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resource not found", request.getRequestURI());
	}
	
	/**
	 * Provides a final fallback for uncaught failures.
	 * The handler keeps error bodies consistent even when the root cause was not anticipated explicitly.
	 *
	 * @param ex unexpected failure that escaped more specific handlers
	 * @param request incoming servlet request
	 * @return error response for an unexpected failure
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
		log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error", request.getRequestURI());
	}
	
	/**
	 * Centralizes envelope creation so every handler stamps errors the same way.
	 * Sharing this path keeps timestamps, codes, and payload shape aligned across unrelated failure modes.
	 *
	 * @param status  HTTP status that matches the failure category
	 * @param code    stable client-facing error code
	 * @param message human-readable error message
	 * @param path    request path that triggered the failure
	 * @return response entity with the shared error envelope
	 */
	private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, String path) {
		return ResponseEntity.status(status)
				.body(new ErrorResponse(code, message, OffsetDateTime.now(clock), path));
	}
}
