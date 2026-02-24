CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_case_type_snapshot_reference_version_id
    ON case_type_snapshot USING btree (LOWER(case_type_reference), version_id);
