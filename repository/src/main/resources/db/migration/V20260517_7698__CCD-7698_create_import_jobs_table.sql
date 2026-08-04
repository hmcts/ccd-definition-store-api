CREATE TABLE import_jobs (
    id                UUID          NOT NULL,
    status            VARCHAR(20)   NOT NULL,
    submitted_by      VARCHAR(255)  NOT NULL,
    submitted_at      TIMESTAMP     NOT NULL DEFAULT now(),
    started_at        TIMESTAMP,
    completed_at      TIMESTAMP,
    error_summary     TEXT,
    warnings          TEXT,
    reindex_task_id   VARCHAR(255),
    CONSTRAINT pk_import_jobs PRIMARY KEY (id),
    CONSTRAINT enum_import_jobs_status CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'EXPIRED'))
);

CREATE INDEX idx_import_jobs_status_started_at ON import_jobs (status, started_at);