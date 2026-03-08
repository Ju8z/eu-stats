package eu.stats.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

@Component
public class DateUtil {

    private final Clock clock;

    public DateUtil(Clock clock) {
        this.clock = clock;
    }

    public DateRange resolveDateRange(String period, LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now(clock);
        if (period == null || period.isBlank()) {
            period = "30d";
        }

        return switch (period) {
            case "today" -> new DateRange(today, today);
            case "yesterday" -> {
                LocalDate yesterday = today.minusDays(1);
                yield new DateRange(yesterday, yesterday);
            }
            case "7d" -> new DateRange(today.minusDays(6), today);
            case "30d" -> new DateRange(today.minusDays(29), today);
            default -> {
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

    public OffsetDateTimeRange toOffsetDateTimeRange(DateRange range) {
        return new OffsetDateTimeRange(
                range.from().atStartOfDay(clock.getZone()).toOffsetDateTime(),
                range.to().plusDays(1).atStartOfDay(clock.getZone()).toOffsetDateTime());
    }

    public record DateRange(LocalDate from, LocalDate to) {
    }

    public record OffsetDateTimeRange(OffsetDateTime fromInclusive, OffsetDateTime toExclusive) {
    }
}
