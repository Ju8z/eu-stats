DROP INDEX IF EXISTS idx_utm_stats_lookup;

DROP TABLE IF EXISTS utm_stats;

ALTER TABLE pageviews
    DROP COLUMN IF EXISTS utm_source;

ALTER TABLE pageviews
    DROP COLUMN IF EXISTS utm_medium;

ALTER TABLE pageviews
    DROP COLUMN IF EXISTS utm_campaign;
