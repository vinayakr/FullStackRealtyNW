CREATE TABLE marketing_opt_ins (
    id          SERIAL PRIMARY KEY,
    email       VARCHAR(255) UNIQUE NOT NULL,
    name        VARCHAR(100),
    interests   TEXT NOT NULL DEFAULT '',
    source      VARCHAR(100) DEFAULT 'website',
    opted_in_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_marketing_opt_ins_email ON marketing_opt_ins(email);
