CREATE INDEX idx_daily_stats_site_date ON daily_stats (site_id, stat_date DESC);
CREATE INDEX idx_page_stats_lookup ON page_stats (site_id, stat_date DESC, pageviews DESC);
CREATE INDEX idx_referrer_stats_lookup ON referrer_stats (site_id, stat_date DESC, visits DESC);
CREATE INDEX idx_geo_stats_lookup ON geo_stats (site_id, stat_date DESC, visits DESC);
CREATE INDEX idx_device_stats_lookup ON device_stats (site_id, stat_date DESC, visits DESC);
CREATE INDEX idx_utm_stats_lookup ON utm_stats (site_id, stat_date DESC, visits DESC);
CREATE INDEX idx_event_stats_lookup ON event_stats (site_id, stat_date DESC, event_count DESC);
CREATE INDEX idx_hourly_stats_lookup ON hourly_stats (site_id, stat_hour DESC);
