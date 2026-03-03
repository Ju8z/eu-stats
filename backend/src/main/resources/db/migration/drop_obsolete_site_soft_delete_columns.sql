DROP INDEX IF EXISTS idx_sites_active;

ALTER TABLE sites
    DROP COLUMN IF EXISTS deleted_at;

ALTER TABLE sites
    DROP COLUMN IF EXISTS is_active;
