DROP INDEX IF EXISTS idx_pv_session;

ALTER TABLE pageviews
    DROP COLUMN IF EXISTS session_id;
