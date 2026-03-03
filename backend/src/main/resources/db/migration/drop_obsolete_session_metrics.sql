ALTER TABLE daily_stats
    DROP COLUMN IF EXISTS total_sessions;

ALTER TABLE hourly_stats
    DROP COLUMN IF EXISTS total_sessions;
