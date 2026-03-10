package eu.stats.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.stats.dto.response.DeviceStatsResponse;
import eu.stats.dto.response.EventStatsResponse;
import eu.stats.dto.response.GeoStatsResponse;
import eu.stats.dto.response.OverviewStatsResponse;
import eu.stats.dto.response.PageStatsResponse;
import eu.stats.dto.response.RealTimeResponse;
import eu.stats.dto.response.ReferrerStatsResponse;
import eu.stats.dto.response.VisitorTimeSeriesResponse;
import eu.stats.service.StatsService;

/**
 * Keeps dashboard reads split by concern.
 * Separate endpoints let the frontend refresh only the widget it needs while still sharing one set of range
 * and validation rules underneath.
 */
@RestController
@RequestMapping("/api/sites/{siteId}/d")
public class StatsController {
	
	private final StatsService statsService;
	
	public StatsController(StatsService statsService) {
		this.statsService = statsService;
	}
	
	/**
	 * Keeps headline metrics on a dedicated endpoint.
	 * The dashboard can refresh top-line cards without paying for heavier breakdown queries, while range rules
	 * still come from the service layer.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from   start date
	 * @param to     end date
	 * @return overview response for the requested window
	 */
	@GetMapping("/summary")
	public OverviewStatsResponse overview(@PathVariable Long siteId,
			@RequestParam(required = false, defaultValue = "30d") String period,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return statsService.overview(siteId, period, from, to);
	}
	
	/**
	 * Keeps chart data separate from summary data.
	 * The service can choose minute, hourly, or calendar aggregation without forcing the client to know which
	 * storage path is used.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from start date
	 * @param to end date
	 * @param interval requested interval
	 * @return visitor time series response for the requested window
	 */
	@GetMapping("/audience")
	public VisitorTimeSeriesResponse visitors(@PathVariable Long siteId,
			@RequestParam(required = false, defaultValue = "30d") String period,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) String interval) {
		return statsService.visitors(siteId, period, from, to, interval);
	}
	
	/**
	 * Keeps ranked content reads separate from other dashboard queries.
	 * That avoids over-fetching when only page leaderboard data needs to be refreshed.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from start date
	 * @param to end date
	 * @param limit maximum number of rows to return
	 * @return page ranking response for the requested window
	 */
	@GetMapping("/content")
	public PageStatsResponse pages(@PathVariable Long siteId,
			@RequestParam(required = false, defaultValue = "30d") String period,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false, defaultValue = "20") int limit) {
		return statsService.topPages(siteId, period, from, to, limit);
	}
	
	/**
	 * Keeps traffic source ranking separate from other dashboard sections.
	 * The client can request only this breakdown while the service still applies the same direct-traffic
	 * normalization rules.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from start date
	 * @param to end date
	 * @param limit maximum number of rows to return
	 * @return referrer ranking response for the requested window
	 */
	@GetMapping("/sources")
	public ReferrerStatsResponse referrers(@PathVariable Long siteId,
			@RequestParam(required = false, defaultValue = "30d") String period,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false, defaultValue = "20") int limit) {
		return statsService.topReferrers(siteId, period, from, to, limit);
	}
	
	/**
	 * Keeps geographic breakdown retrieval separate from other dashboard sections.
	 * The service can expand country codes and calculate percentages in one place before the response leaves
	 * the server.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from start date
	 * @param to end date
	 * @return geographic breakdown response for the requested window
	 */
	@GetMapping("/regions")
	public GeoStatsResponse geo(@PathVariable Long siteId,
			@RequestParam(required = false, defaultValue = "30d") String period,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return statsService.geo(siteId, period, from, to);
	}
	
	/**
	 * Keeps technology breakdown retrieval separate from other dashboard sections.
	 * The service can collapse device, browser, and operating system rows into frontend-friendly groups
	 * centrally.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from start date
	 * @param to end date
	 * @return device breakdown response for the requested window
	 */
	@GetMapping("/tech")
	public DeviceStatsResponse devices(@PathVariable Long siteId,
			@RequestParam(required = false, defaultValue = "30d") String period,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return statsService.devices(siteId, period, from, to);
	}
	
	/**
	 * Keeps custom event analytics separate from page view analytics.
	 * That separation lets event reporting evolve without changing the content or visitor endpoints.
	 *
	 * @param siteId site identifier
	 * @param period reporting period
	 * @param from start date
	 * @param to end date
	 * @return event breakdown response for the requested window
	 */
	@GetMapping("/actions")
	public EventStatsResponse events(@PathVariable Long siteId,
			@RequestParam(required = false, defaultValue = "30d") String period,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return statsService.events(siteId, period, from, to);
	}
	
	/**
	 * Keeps live activity reads separate from historical reporting.
	 * The endpoint can use heartbeat-specific rules without leaking those rules into historical analytics
	 * queries.
	 *
	 * @param siteId site identifier
	 * @return real-time response for the requested site
	 */
	@GetMapping("/live")
	public RealTimeResponse realtime(@PathVariable Long siteId) {
		return statsService.realtime(siteId);
	}
}
