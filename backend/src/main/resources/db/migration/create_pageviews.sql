CREATE TABLE pageviews
(
    id                BIGSERIAL,
    site_id           BIGINT        NOT NULL REFERENCES sites (id),
    visitor_hash      VARCHAR(64)   NOT NULL,
    session_id        VARCHAR(64)   NOT NULL,
    page_url          VARCHAR(2048) NOT NULL,
    page_title        VARCHAR(500),
    referrer          VARCHAR(500),
    referrer_category VARCHAR(20),
    utm_source        VARCHAR(255),
    utm_medium        VARCHAR(255),
    utm_campaign      VARCHAR(255),
    browser           VARCHAR(100),
    browser_version   VARCHAR(20),
    os                VARCHAR(100),
    os_version        VARCHAR(20),
    device_type       VARCHAR(20),
    screen_resolution VARCHAR(20),
    viewport          VARCHAR(20),
    language          VARCHAR(10),
    country           VARCHAR(2),
    city              VARCHAR(100),
    continent         VARCHAR(2),
    subdivision       VARCHAR(10),
    event_type        VARCHAR(50)   NOT NULL DEFAULT 'pageview',
    event_name        VARCHAR(255),
    viewed_at         TIMESTAMPTZ   NOT NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, viewed_at)
) PARTITION BY RANGE (viewed_at);

CREATE INDEX idx_pv_site_viewed ON pageviews (site_id, viewed_at DESC);
CREATE INDEX idx_pv_site_visitor ON pageviews (site_id, visitor_hash, viewed_at DESC);
CREATE INDEX idx_pv_site_url ON pageviews (site_id, page_url, viewed_at DESC);
CREATE INDEX idx_pv_site_referrer ON pageviews (site_id, referrer, viewed_at DESC);
CREATE INDEX idx_pv_site_country ON pageviews (site_id, country, viewed_at DESC);
CREATE INDEX idx_pv_site_device ON pageviews (site_id, device_type, viewed_at DESC);
CREATE INDEX idx_pv_site_browser ON pageviews (site_id, browser, viewed_at DESC);
CREATE INDEX idx_pv_site_event ON pageviews (site_id, event_type, viewed_at DESC);
CREATE INDEX idx_pv_session ON pageviews (session_id, viewed_at DESC);
