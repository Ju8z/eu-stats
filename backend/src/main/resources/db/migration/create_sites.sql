CREATE TABLE sites
(
    id         BIGSERIAL PRIMARY KEY,
    domain     VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_sites_domain ON sites (domain);
CREATE INDEX idx_sites_active ON sites (is_active);
