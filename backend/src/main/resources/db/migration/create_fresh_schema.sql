CREATE SCHEMA IF NOT EXISTS public;
SET search_path TO public;

CREATE TABLE sites
(
    id         BIGSERIAL PRIMARY KEY,
    domain     VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sites_domain ON sites (domain);

CREATE TABLE pageviews
(
    id                BIGSERIAL,
    site_id           BIGINT        NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    visitor_hash      VARCHAR(64)   NOT NULL,
    page_url          VARCHAR(2048) NOT NULL,
    page_title        VARCHAR(500),
    referrer          VARCHAR(500),
    referrer_category VARCHAR(20),
    browser           VARCHAR(100),
    browser_version   VARCHAR(20),
    os                VARCHAR(100),
    os_version        VARCHAR(20),
    device_type       VARCHAR(20),
    screen_resolution VARCHAR(20),
    country           VARCHAR(2),
    event_type        VARCHAR(50)   NOT NULL DEFAULT 'pageview',
    event_name        VARCHAR(255),
    viewed_at         TIMESTAMPTZ   NOT NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, viewed_at)
) PARTITION BY RANGE (viewed_at);

CREATE TABLE pageviews_default
    PARTITION OF pageviews DEFAULT;

CREATE INDEX idx_pv_site_viewed ON pageviews (site_id, viewed_at DESC);
CREATE INDEX idx_pv_site_visitor ON pageviews (site_id, visitor_hash, viewed_at DESC);
CREATE INDEX idx_pv_site_url ON pageviews (site_id, page_url, viewed_at DESC);
CREATE INDEX idx_pv_site_referrer ON pageviews (site_id, referrer, viewed_at DESC);
CREATE INDEX idx_pv_site_country ON pageviews (site_id, country, viewed_at DESC);
CREATE INDEX idx_pv_site_device ON pageviews (site_id, device_type, viewed_at DESC);
CREATE INDEX idx_pv_site_browser ON pageviews (site_id, browser, viewed_at DESC);
CREATE INDEX idx_pv_site_event ON pageviews (site_id, event_type, viewed_at DESC);

CREATE TABLE daily_stats
(
    id              BIGSERIAL PRIMARY KEY,
    site_id         BIGINT      NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    stat_date       DATE        NOT NULL,
    total_pageviews INTEGER     NOT NULL DEFAULT 0,
    unique_visitors INTEGER     NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (site_id, stat_date)
);

CREATE TABLE page_stats
(
    id              BIGSERIAL PRIMARY KEY,
    site_id         BIGINT        NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    stat_date       DATE          NOT NULL,
    page_url        VARCHAR(2048) NOT NULL,
    page_title      VARCHAR(500),
    pageviews       INTEGER       NOT NULL DEFAULT 0,
    unique_visitors INTEGER       NOT NULL DEFAULT 0,
    UNIQUE (site_id, stat_date, page_url)
);

CREATE TABLE referrer_stats
(
    id                BIGSERIAL PRIMARY KEY,
    site_id           BIGINT       NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    stat_date         DATE         NOT NULL,
    referrer          VARCHAR(500) NOT NULL,
    referrer_category VARCHAR(20),
    visits            INTEGER      NOT NULL DEFAULT 0,
    unique_visitors   INTEGER      NOT NULL DEFAULT 0,
    UNIQUE (site_id, stat_date, referrer)
);

CREATE TABLE geo_stats
(
    id              BIGSERIAL PRIMARY KEY,
    site_id         BIGINT     NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    stat_date       DATE       NOT NULL,
    country         VARCHAR(2) NOT NULL,
    visits          INTEGER    NOT NULL DEFAULT 0,
    unique_visitors INTEGER    NOT NULL DEFAULT 0,
    UNIQUE (site_id, stat_date, country)
);

CREATE TABLE device_stats
(
    id                BIGSERIAL PRIMARY KEY,
    site_id           BIGINT  NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    stat_date         DATE    NOT NULL,
    device_type       VARCHAR(20),
    browser           VARCHAR(100),
    browser_version   VARCHAR(20),
    os                VARCHAR(100),
    os_version        VARCHAR(20),
    screen_resolution VARCHAR(20),
    visits            INTEGER NOT NULL DEFAULT 0,
    unique_visitors   INTEGER NOT NULL DEFAULT 0,
    UNIQUE (site_id, stat_date, device_type, browser, browser_version, os, os_version, screen_resolution)
);

CREATE TABLE event_stats
(
    id              BIGSERIAL PRIMARY KEY,
    site_id         BIGINT       NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    stat_date       DATE         NOT NULL,
    event_name      VARCHAR(255) NOT NULL,
    event_count     INTEGER      NOT NULL DEFAULT 0,
    unique_visitors INTEGER      NOT NULL DEFAULT 0,
    UNIQUE (site_id, stat_date, event_name)
);

CREATE TABLE hourly_stats
(
    id              BIGSERIAL PRIMARY KEY,
    site_id         BIGINT      NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    stat_hour       TIMESTAMPTZ NOT NULL,
    total_pageviews INTEGER     NOT NULL DEFAULT 0,
    unique_visitors INTEGER     NOT NULL DEFAULT 0,
    UNIQUE (site_id, stat_hour)
);

CREATE INDEX idx_daily_stats_site_date ON daily_stats (site_id, stat_date DESC);
CREATE INDEX idx_page_stats_lookup ON page_stats (site_id, stat_date DESC, pageviews DESC);
CREATE INDEX idx_referrer_stats_lookup ON referrer_stats (site_id, stat_date DESC, visits DESC);
CREATE INDEX idx_geo_stats_lookup ON geo_stats (site_id, stat_date DESC, visits DESC);
CREATE INDEX idx_device_stats_lookup ON device_stats (site_id, stat_date DESC, visits DESC);
CREATE INDEX idx_event_stats_lookup ON event_stats (site_id, stat_date DESC, event_count DESC);
CREATE INDEX idx_hourly_stats_lookup ON hourly_stats (site_id, stat_hour DESC);
