CREATE TABLE daily_stats
(
    id                             BIGSERIAL PRIMARY KEY,
    site_id                        BIGINT      NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    stat_date                      DATE        NOT NULL,
    total_pageviews                INTEGER     NOT NULL DEFAULT 0,
    unique_visitors                INTEGER     NOT NULL DEFAULT 0,
    total_sessions                 INTEGER     NOT NULL DEFAULT 0,
    bounce_count                   INTEGER     NOT NULL DEFAULT 0,
    total_session_duration_seconds BIGINT      NOT NULL DEFAULT 0,
    total_page_depth               INTEGER     NOT NULL DEFAULT 0,
    created_at                     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
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
    city            VARCHAR(100),
    visits          INTEGER    NOT NULL DEFAULT 0,
    unique_visitors INTEGER    NOT NULL DEFAULT 0,
    UNIQUE (site_id, stat_date, country, city)
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
    UNIQUE (site_id, stat_date, device_type, browser, os, screen_resolution)
);

CREATE TABLE utm_stats
(
    id              BIGSERIAL PRIMARY KEY,
    site_id         BIGINT  NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    stat_date       DATE    NOT NULL,
    utm_source      VARCHAR(255),
    utm_medium      VARCHAR(255),
    utm_campaign    VARCHAR(255),
    visits          INTEGER NOT NULL DEFAULT 0,
    unique_visitors INTEGER NOT NULL DEFAULT 0,
    UNIQUE (site_id, stat_date, utm_source, utm_medium, utm_campaign)
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
    total_sessions  INTEGER     NOT NULL DEFAULT 0,
    UNIQUE (site_id, stat_hour)
);
