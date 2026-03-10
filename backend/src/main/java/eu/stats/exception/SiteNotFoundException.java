package eu.stats.exception;

/**
 * Marks a missing site as a domain-level failure.
 * Using a dedicated exception lets the global error handler map this common case to a predictable client
 * response.
 */
public class SiteNotFoundException extends RuntimeException {
	
	public SiteNotFoundException(String message) {
		super(message);
	}
	
}