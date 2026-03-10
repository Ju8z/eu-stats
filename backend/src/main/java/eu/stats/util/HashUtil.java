package eu.stats.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

/**
 * Keeps hashing behavior consistent in one small boundary.
 * Centralizing digest generation avoids subtle formatting differences in privacy-sensitive code paths such
 * as visitor hashing and cache keys.
 */
@Component
public class HashUtil {
	
	/**
	 * Keeps hashing consistent for privacy-related workflows.
	 * Using one helper avoids subtle differences in digest formatting between visitor hashing and other
	 * privacy-sensitive lookups.
	 *
	 * @param value input value
	 * @return hexadecimal Secure Hash Algorithm 256 digest
	 */
	public String sha256Hex(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder(hash.length * 2);
			for (byte b : hash) {
				builder.append(String.format("%02x", b));
			}
			
			return builder.toString();
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
		}
	}
}
