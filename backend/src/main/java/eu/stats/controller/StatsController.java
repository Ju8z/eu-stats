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
 * Provides analytics datasets for a site dashboard.
 */
@RestController
@RequestMapping("/api/sites/{siteId}/d")
public class StatsController {
	
	private final StatsService statsService;
	
	public StatsController(StatsService statsService) {
		this.statsService = statsService;
	}
	
	@GetMapping("/summary")
	public OverviewStatsResponse overview(@PathVariable Long siteId,
			@RequestParam(required = false, defaultValue = "30d") String period,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return statsService.overview(siteId, period, from, to);
	}
	
	@GetMapping("/audience")
	public VisitorTimeSeriesResponse visitors(@PathVariable Long siteId,
			@RequestParam(required = false, defaultValue = "30d") String period,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) String interval) {
		return statsService.visitors(siteId, period, from, to, interval);
	}
	
	@GetMapping("/content")
	public PageStatsResponse pages(@PathVariable Long siteId,
			@RequestParam(required = false, defaultValue = "30d") String period,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false, defaultValue = "20") int limit) {
		return statsService.topPages(siteId, period, from, to, limit);
	}
	
	@GetMapping("/sources")
	public ReferrerStatsResponse referrers(@PathVariable Long siteId,
			@RequestParam(required = false, defaultValue = "30d") String period,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false, defaultValue = "20") int limit) {
		return statsService.topReferrers(siteId, period, from, to, limit);
	}
	
	@GetMapping("/regions")
	public GeoStatsResponse geo(@PathVariable Long siteId,
			@RequestParam(required = false, defaultValue = "30d") String period,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return statsService.geo(siteId, period, from, to);
	}
	
	@GetMapping("/tech")
	public DeviceStatsResponse devices(@PathVariable Long siteId,
			@RequestParam(required = false, defaultValue = "30d") String period,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return statsService.devices(siteId, period, from, to);
	}
	
	@GetMapping("/actions")
	public EventStatsResponse events(@PathVariable Long siteId,
			@RequestParam(required = false, defaultValue = "30d") String period,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return statsService.events(siteId, period, from, to);
	}
	
	@GetMapping("/live")
	public RealTimeResponse realtime(@PathVariable Long siteId) {
		return statsService.realtime(siteId);
	}
}
