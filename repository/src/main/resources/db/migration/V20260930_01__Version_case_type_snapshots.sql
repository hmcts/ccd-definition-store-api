-- Existing rows and legacy inserts are rebuilt by revision-aware readers.
ALTER TABLE case_type_snapshot
    ADD COLUMN format_revision INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN format_write_count BIGINT NOT NULL DEFAULT 0;

-- Legacy upserts omit both new columns. Defaults do not apply to their UPDATE branch,
-- so invalidate the inherited revision unless the writer advances the counter.
CREATE FUNCTION invalidate_legacy_snapshot_format() RETURNS trigger AS $$
BEGIN
    IF NEW.format_write_count = OLD.format_write_count THEN
        NEW.format_revision := 0;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER invalidate_legacy_snapshot_format
    BEFORE UPDATE ON case_type_snapshot
    FOR EACH ROW EXECUTE FUNCTION invalidate_legacy_snapshot_format();
