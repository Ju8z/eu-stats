ALTER TABLE pageviews
    DROP CONSTRAINT IF EXISTS pageviews_site_id_fkey;

ALTER TABLE pageviews
    ADD CONSTRAINT pageviews_site_id_fkey
        FOREIGN KEY (site_id) REFERENCES sites (id) ON DELETE CASCADE;
