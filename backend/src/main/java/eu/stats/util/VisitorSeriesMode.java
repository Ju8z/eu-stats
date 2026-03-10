package eu.stats.util;

/**
 * Defines the fixed set of visitor bucket strategies.
 * Using an enumeration keeps internal query planning and external interval labels synchronized instead of
 * relying on free-form strings.
 */
public enum VisitorSeriesMode {
	MINUTE("minute"),
	HOUR("hour"),
	DAY("day"),
	WEEK("week"),
	MONTH("month");
	
	private final String responseInterval;
	
	VisitorSeriesMode(String responseInterval) {
		this.responseInterval = responseInterval;
	}
	
	/**
	 * Maps external interval text to the internal bucket strategy.
	 * Keeping the translation in the enum prevents request parsing code from hard-coding enum knowledge in
	 * multiple places.
	 *
	 * @param interval requested interval
	 * @return matching visitor series mode
	 */
	public static VisitorSeriesMode fromInterval(String interval) {
		return switch (interval) {
			case "hour" -> HOUR;
			case "week" -> WEEK;
			case "month" -> MONTH;
			default -> DAY;
		};
	}
	
	/**
	 * Exposes the response label owned by this mode.
	 * The enum remains the single source of truth so serialized interval names cannot drift away from query
	 * planning.
	 *
	 * @return response interval label
	 */
	public String responseInterval() {
		return responseInterval;
	}
	
	/**
	 * Compares request input to this mode without case sensitivity.
	 * The comparison stays tolerant of casing differences while still enforcing the fixed supported value set.
	 *
	 * @param interval requested interval
	 * @return true when the requested interval refers to this mode
	 */
	public boolean matchesInterval(String interval) {
		return responseInterval.equalsIgnoreCase(interval);
	}
}
