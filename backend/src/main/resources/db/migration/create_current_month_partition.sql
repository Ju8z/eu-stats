-- Initial partition for March 2026.
-- Its can be automated with Spring Cronjob to create another month or just run this script and change:
-- 1) table name suffix (YYYY_MM)
-- 2) FROM date (first day of month)
-- 3) TO date (first day of next month)
CREATE TABLE IF NOT EXISTS pageviews_2026_03
    PARTITION OF pageviews
        FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');
