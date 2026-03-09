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

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	private final Clock clock;
	
	public GlobalExceptionHandler(Clock clock) {
		this.clock = clock;
	}
	
	@ExceptionHandler(SiteNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleSiteNotFound(SiteNotFoundException ex, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "SITE_NOT_FOUND", ex.getMessage(), request.getRequestURI());
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request.getRequestURI());
	}
	
	// Just a typical 404
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleMissingResource(NoResourceFoundException ex, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resource not found", request.getRequestURI());
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
		log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error", request.getRequestURI());
	}
	
	private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, String path) {
		return ResponseEntity.status(status)
				.body(new ErrorResponse(code, message, OffsetDateTime.now(clock), path));
	}
}
