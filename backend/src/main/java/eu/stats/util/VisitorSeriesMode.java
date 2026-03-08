package eu.stats.util;

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
	
	public static VisitorSeriesMode fromInterval(String interval) {
		return switch (interval) {
			case "hour" -> HOUR;
			case "week" -> WEEK;
			case "month" -> MONTH;
			default -> DAY;
		};
	}
	
	public String responseInterval() {
		return responseInterval;
	}
	
	public boolean matchesInterval(String interval) {
		return responseInterval.equalsIgnoreCase(interval);
	}
}
