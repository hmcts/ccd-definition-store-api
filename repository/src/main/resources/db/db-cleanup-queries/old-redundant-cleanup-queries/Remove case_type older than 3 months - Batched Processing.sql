DO $$
DECLARE
    batch_size INT := 10000;
    rows_deleted INT;
BEGIN
    -- Log start
    RAISE NOTICE 'Starting batch deletion of old case types...';

    -- Temporary table: case types to remove
    CREATE TEMP TABLE tmp_case_type_ids AS
    SELECT ct.id
    FROM case_type ct
    INNER JOIN (
        SELECT reference, MAX(version) AS max_version
        FROM case_type
        GROUP BY reference
    ) grouped_ct
    ON ct.reference = grouped_ct.reference
    WHERE ct.version != grouped_ct.max_version
      AND ct.created_at <= NOW() - INTERVAL '3 months';

    -- Valid static field types
    CREATE TEMP TABLE tmp_valid_field_type_ids AS
    SELECT id
    FROM field_type
    WHERE reference IN ('Text', 'Number', 'DateTime')
      AND jurisdiction_id IS NULL
    ORDER BY version
    LIMIT 1;

    -- Case fields to remove
    CREATE TEMP TABLE tmp_case_field_ids AS
    SELECT id
    FROM case_field
    WHERE case_type_id IN (SELECT id FROM tmp_case_type_ids)
      AND field_type_id NOT IN (SELECT id FROM tmp_valid_field_type_ids);

    -- Example batch delete with logging: case_field_acl
    LOOP
        DELETE FROM case_field_acl
        WHERE case_field_id IN (
            SELECT id FROM tmp_case_field_ids
            LIMIT batch_size
        )
        RETURNING 1 INTO rows_deleted;
        EXIT WHEN NOT FOUND;
        RAISE NOTICE 'Deleted % rows from case_field_acl', rows_deleted;
    END LOOP;

    LOOP
        DELETE FROM display_group_case_field
        WHERE case_field_id IN (
            SELECT id FROM tmp_case_field_ids
            LIMIT batch_size
        )
        RETURNING 1 INTO rows_deleted;
        EXIT WHEN NOT FOUND;
        RAISE NOTICE 'Deleted % rows from display_group_case_field', rows_deleted;
    END LOOP;

    LOOP
        DELETE FROM event_case_field
        WHERE case_field_id IN (
            SELECT id FROM tmp_case_field_ids
            LIMIT batch_size
        )
        RETURNING 1 INTO rows_deleted;
        EXIT WHEN NOT FOUND;
        RAISE NOTICE 'Deleted % rows from event_case_field', rows_deleted;
    END LOOP;

    -- Repeat similar pattern for other large tables if needed...

    LOOP
        DELETE FROM case_field
        WHERE id IN (
            SELECT id FROM tmp_case_field_ids
            LIMIT batch_size
        )
        RETURNING 1 INTO rows_deleted;
        EXIT WHEN NOT FOUND;
        RAISE NOTICE 'Deleted % rows from case_field', rows_deleted;
    END LOOP;

    -- Final deletes (non-batched, smaller)
    DELETE FROM event_post_state WHERE case_event_id IN (
        SELECT id FROM event WHERE case_type_id IN (SELECT id FROM tmp_case_type_ids)
    );
    RAISE NOTICE 'Deleted from event_post_state';

    DELETE FROM event_pre_state WHERE event_id IN (
        SELECT id FROM event WHERE case_type_id IN (SELECT id FROM tmp_case_type_ids)
    );
    RAISE NOTICE 'Deleted from event_pre_state';

    DELETE FROM "event" WHERE case_type_id IN (SELECT id FROM tmp_case_type_ids);
    RAISE NOTICE 'Deleted from event';

    DELETE FROM state WHERE case_type_id IN (SELECT id FROM tmp_case_type_ids);
    RAISE NOTICE 'Deleted from state';

    DELETE FROM case_type_acl WHERE case_type_id IN (SELECT id FROM tmp_case_type_ids);
    RAISE NOTICE 'Deleted from case_type_acl';

    DELETE FROM case_type WHERE id IN (SELECT id FROM tmp_case_type_ids)
      AND jurisdiction_id IS NOT NULL;
    RAISE NOTICE 'Deleted from case_type';

    -- Drop temp tables
    DROP TABLE IF EXISTS tmp_case_type_ids;
    DROP TABLE IF EXISTS tmp_valid_field_type_ids;
    DROP TABLE IF EXISTS tmp_case_field_ids;

    RAISE NOTICE 'Batch deletion completed successfully.';

END $$;
