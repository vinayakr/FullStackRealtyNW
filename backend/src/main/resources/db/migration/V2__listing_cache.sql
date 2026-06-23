CREATE TABLE listing_cache (
    cache_key    VARCHAR(255) PRIMARY KEY,
    response_json TEXT        NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
