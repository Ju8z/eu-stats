ALTER TABLE daily_stats
    DROP COLUMN IF EXISTS bounce_count;

ALTER TABLE daily_stats
    DROP COLUMN IF EXISTS total_session_duration_seconds;

ALTER TABLE daily_stats
    DROP COLUMN IF EXISTS total_page_depth;
