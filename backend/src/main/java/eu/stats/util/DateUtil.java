package eu.stats.util;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component
public class DateUtil {
	
	public DateRange resolveDateRange(String period, LocalDate from, LocalDate to) {
		LocalDate today = LocalDate.now();
		if (period == null || period.isBlank()) {
			period = "30d"; // just in case its zero, small fallback for 30d
		}
		
		return switch (period) {
			case "today" -> new DateRange(today, today);
			case "yesterday" -> {
				LocalDate d = today.minusDays(1);
				yield new DateRange(d, d);
			}
			case "7d" -> new DateRange(today.minusDays(6), today);
			case "30d" -> new DateRange(today.minusDays(29), today);
			default -> {
				//TODO: Used for test purpose, can be deleted later
				if (from != null && to != null) {
					yield new DateRange(from, to);
				}
				
				throw new IllegalArgumentException("Unsupported period: " + period);
			}
		};
	}
	
	public DateRange previous(DateRange current) {
		long length = current.to().toEpochDay() - current.from().toEpochDay() + 1;
		LocalDate previousTo = current.from().minusDays(1);
		LocalDate previousFrom = previousTo.minusDays(length - 1);
		
		return new DateRange(previousFrom, previousTo);
	}
	
	public String resolveInterval(String requestedInterval, DateRange range) {
		if (requestedInterval != null && !requestedInterval.isBlank()) {
			return requestedInterval;
		}
		
		long days = range.to().toEpochDay() - range.from().toEpochDay() + 1;
		if (days <= 2) {
			return "hour";
		}
		
		if (days <= 62) {
			return "day";
		}
		
		if (days <= 365) {
			return "week";
		}
		
		return "month";
	}
	
	public record DateRange(LocalDate from, LocalDate to) {
	}
	
}
