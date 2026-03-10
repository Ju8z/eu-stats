package eu.stats.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

/**
 * Keeps reporting window rules deterministic.
 * Routing relative date calculations through a clock-backed helper makes comparison logic predictable and
 * easier to reason about across environments.
 */
@Component
public class DateUtil {

    private final Clock clock;

    public DateUtil(Clock clock) {
        this.clock = clock;
    }
	
	/**
	 * Keeps named-period parsing predictable.
	 * Only a small set of relative windows is accepted implicitly so custom ranges remain explicit and
	 * public query semantics stay stable.
	 *
	 * @param period reporting period
	 * @param from   start date
	 * @param to     end date
	 * @return resolved date range
	 */
    public DateRange resolveDateRange(String period, LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now(clock);
        if (period == null || period.isBlank()) {
            period = "30d";
        }
		
		switch (period) {
			case "today":
				return new DateRange(today, today);
			case "yesterday":
                LocalDate yesterday = today.minusDays(1);
				return new DateRange(yesterday, yesterday);
			case "7d":
				return new DateRange(today.minusDays(6), today);
			case "30d":
				return new DateRange(today.minusDays(29), today);
			default:
                if (from != null && to != null) {
					return new DateRange(from, to);
                }

                throw new IllegalArgumentException("Unsupported period: " + period);
		}
	}
	
	/**
	 * Builds a like-for-like comparison window.
	 * Using an equal-length previous range keeps percentage comparisons grounded in periods of the same
	 * size.
	 *
	 * @param current current
	 * @return previous date range with the same length
	 */
    public DateRange previous(DateRange current) {
        long length = current.to().toEpochDay() - current.from().toEpochDay() + 1;
        LocalDate previousTo = current.from().minusDays(1);
        LocalDate previousFrom = previousTo.minusDays(length - 1);

        return new DateRange(previousFrom, previousTo);
	}
	
	/**
	 * Chooses a chart interval that stays readable as the window grows.
	 * The method intentionally lowers granularity for longer ranges so responses stay compact and visual
	 * noise stays manageable.
	 *
	 * @param requestedInterval requested interval
	 * @param range date range
	 * @return interval label for the requested range
	 */
    public String resolveInterval(String requestedInterval, DateRange range) {
        if (requestedInterval != null && !requestedInterval.isBlank()) {
            return requestedInterval;
        }

        long days = range.to().toEpochDay() - range.from().toEpochDay() + 1;
        if (days <= 2) {
            return VisitorSeriesMode.HOUR.responseInterval();
        }

        if (days <= 62) {
            return VisitorSeriesMode.DAY.responseInterval();
        }

        if (days <= 365) {
            return VisitorSeriesMode.WEEK.responseInterval();
        }

        return VisitorSeriesMode.MONTH.responseInterval();
	}
	
	/**
	 * Converts date-only windows into safe timestamp boundaries.
	 * Using an inclusive start and exclusive end avoids double counting records that land exactly on
	 * midnight boundaries.
	 *
	 * @param range date range
	 * @return offset date time range with inclusive and exclusive bounds
	 */
    public OffsetDateTimeRange toOffsetDateTimeRange(DateRange range) {
        return new OffsetDateTimeRange(
                range.from().atStartOfDay(clock.getZone()).toOffsetDateTime(),
                range.to().plusDays(1).atStartOfDay(clock.getZone()).toOffsetDateTime());
	}
	
	/**
	 * Bundles related values that should travel together.
	 * Keeping this nested record inside date helper prevents closely related analytics values from drifting
	 * apart as separate arguments or map entries.
	 *
	 * @param from start date
	 * @param to end date
	 */
    public record DateRange(LocalDate from, LocalDate to) {
	}
	
	/**
	 * Bundles related values that should travel together.
	 * Keeping this nested record inside date helper prevents closely related analytics values from drifting
	 * apart as separate arguments or map entries.
	 *
	 * @param fromInclusive inclusive start timestamp
	 * @param toExclusive exclusive end timestamp
	 */
    public record OffsetDateTimeRange(OffsetDateTime fromInclusive, OffsetDateTime toExclusive) {
    }
}
