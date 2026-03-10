package eu.stats.dto.response;

/**
 * Defines an immutable response contract for snippet.
 * Using records for responses keeps field names explicit and reduces accidental drift between backend
 * payloads and frontend expectations.
 *
 * @param snippetHtml snippet html
 */
public record SnippetResponse(
		
		String snippetHtml

) {

}
