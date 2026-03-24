-- ============================================================
-- Cleanup helper functions + run script (psql-safe, no DO block)
-- ============================================================

-- ------------------------------------------------------------
-- 1) Create temp tables used for deletes
--    NOTE: temp tables are SESSION-scoped. Run the whole script
--    in the same connection/session.
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION prepare_cleanup_temp_tables(
    case_type_references text[] DEFAULT ARRAY['ABCD']
)
RETURNS void
LANGUAGE plpgsql
AS $$
BEGIN
    -- Create log output table if not exists (persistent table)
    CREATE TABLE IF NOT EXISTS ddl_log (
        log_time  TIMESTAMP DEFAULT now(),
        action    TEXT,
        table_name TEXT,
        message   TEXT
    );

    -- Drop temp tables if they already exist
    DROP TABLE IF EXISTS case_type_ids_to_remove;
    DROP TABLE IF EXISTS valid_field_type_ids;
    DROP TABLE IF EXISTS removable_case_fields;
    DROP TABLE IF EXISTS removable_events;
    DROP TABLE IF EXISTS removable_states;

    -- Create temp table of case types to remove
    CREATE TEMP TABLE case_type_ids_to_remove AS
    SELECT id
    FROM case_type
    WHERE reference = ANY (case_type_references);

    RAISE NOTICE 'Created temp table case_type_ids_to_remove for records with % rows',
        (SELECT COUNT(*) FROM case_type_ids_to_remove);

    -- Valid (static/base) field types
    CREATE TEMP TABLE valid_field_type_ids AS
    SELECT id
    FROM field_type
    WHERE jurisdiction_id IS NULL;

    RAISE NOTICE 'Created temp table valid_field_type_ids with % rows',
        (SELECT COUNT(*) FROM valid_field_type_ids);

    -- Case fields to be removed (ignore base/static field types)
    CREATE TEMP TABLE removable_case_fields AS
    SELECT id, case_type_id
    FROM case_field
    WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
      AND field_type_id NOT IN (SELECT id FROM valid_field_type_ids);

    RAISE NOTICE 'Created temp table removable_case_fields with % rows',
        (SELECT COUNT(*) FROM removable_case_fields);

    -- Events to be removed
    CREATE TEMP TABLE removable_events AS
    SELECT id
    FROM event
    WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove);

    RAISE NOTICE 'Created temp table removable_events with % rows',
        (SELECT COUNT(*) FROM removable_events);

    -- States to be removed
    CREATE TEMP TABLE removable_states AS
    SELECT id
    FROM state
    WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove);

    RAISE NOTICE 'Created temp table removable_states with % rows',
        (SELECT COUNT(*) FROM removable_states);
END;
$$;


-- ------------------------------------------------------------
-- 2) Drop temp tables
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION drop_cleanup_temp_tables()
RETURNS void
LANGUAGE plpgsql
AS $$
BEGIN
    DROP TABLE IF EXISTS case_type_ids_to_remove;
    DROP TABLE IF EXISTS valid_field_type_ids;
    DROP TABLE IF EXISTS removable_case_fields;
    DROP TABLE IF EXISTS removable_events;
    DROP TABLE IF EXISTS removable_states;
END;
$$;


-- ------------------------------------------------------------
-- 3) Batch delete helper (dynamic WHERE clause, batched by ctid)
--    WARNING: where_clause is injected as raw SQL text. Only call
--    this with trusted inputs.
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION safe_delete_where(
    tbl          regclass,     -- target table
    pk_column    text,         -- PK column (kept for signature compatibility)
    where_clause text,         -- just the WHERE condition (without "WHERE")
    batch_size   int DEFAULT 1000
)
RETURNS void
LANGUAGE plpgsql
AS $$
DECLARE
    rows_deleted   int;
    total_deleted  int := 0;
BEGIN
    <<batch_loop>>
    LOOP
        EXECUTE format(
            'WITH keys AS (
                 SELECT ctid, %I AS pk_val
                 FROM %s
                 WHERE %s
                 LIMIT %s
             )
             DELETE FROM %s t
             USING keys k
             WHERE t.ctid = k.ctid',
            pk_column,
            tbl,                -- regclass is safe with %s
            where_clause,
            batch_size,
            tbl
        );

        GET DIAGNOSTICS rows_deleted = ROW_COUNT;
        EXIT batch_loop WHEN rows_deleted = 0;

        total_deleted := total_deleted + rows_deleted;

        RAISE NOTICE 'Batch deleted % rows from %', rows_deleted, tbl::text;
        INSERT INTO ddl_log(action, table_name, message)
        VALUES ('DELETE', tbl::text,
                'Deleted batch of ' || rows_deleted || ' rows from ' || tbl::text);
    END LOOP;

    RAISE NOTICE 'Total deleted from %: % rows', tbl::text, total_deleted;
    INSERT INTO ddl_log(action, table_name, message)
    VALUES ('DELETE SUMMARY', tbl::text,
            'Total deleted ' || total_deleted || ' rows from ' || tbl::text);
END;
$$;


-- ------------------------------------------------------------
-- 4) Execute the deletes using the temp tables
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION run_safe_deletes(batch_size int DEFAULT 500)
RETURNS void
LANGUAGE plpgsql
AS $$
BEGIN
	-- Guard: ensure temp table exists (in this session)
	IF to_regclass('pg_temp.case_type_ids_to_remove') IS NULL THEN
		RAISE NOTICE 'Skipping deletes: temp table case_type_ids_to_remove does not exist in this session.';
		RETURN;
	END IF;
	
	-- Guard: ensure there is actually something to delete
	IF NOT EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
	    RAISE NOTICE 'Skipping deletes: case_type_ids_to_remove is empty (0 rows).';
	    RETURN;
	END IF;
 
    -- Case field related deletes
    PERFORM safe_delete_where(
      'case_field_acl',
      'case_field_id',
      'case_field_id IN (SELECT id FROM removable_case_fields)',
      batch_size
    );

    PERFORM safe_delete_where(
      'display_group_case_field',
      'case_field_id',
      'case_field_id IN (SELECT id FROM removable_case_fields)',
      batch_size
    );

    PERFORM safe_delete_where(
      'event_case_field_complex_type',
      'event_case_field_id',
      'event_case_field_id IN (
           SELECT ecf.id
           FROM event_case_field ecf
           JOIN removable_case_fields rcf ON ecf.case_field_id = rcf.id
           WHERE ecf.event_id IN (SELECT id FROM removable_events)
       )',
      batch_size
    );

    PERFORM safe_delete_where(
      'event_case_field',
      'case_field_id',
      'case_field_id IN (SELECT id FROM removable_case_fields)',
      batch_size
    );

    PERFORM safe_delete_where(
      'complex_field_acl',
      'case_field_id',
      'case_field_id IN (SELECT id FROM removable_case_fields)',
      batch_size
    );

    PERFORM safe_delete_where(
      'search_result_case_field',
      'case_field_id',
      'case_field_id IN (SELECT id FROM removable_case_fields)',
      batch_size
    );

    PERFORM safe_delete_where(
      'workbasket_case_field',
      'case_field_id',
      'case_field_id IN (SELECT id FROM removable_case_fields)',
      batch_size
    );

    PERFORM safe_delete_where(
      'search_input_case_field',
      'case_field_id',
      'case_field_id IN (SELECT id FROM removable_case_fields)',
      batch_size
    );

    PERFORM safe_delete_where(
      'workbasket_input_case_field',
      'case_field_id',
      'case_field_id IN (SELECT id FROM removable_case_fields)',
      batch_size
    );

    PERFORM safe_delete_where(
      'search_cases_result_fields',
      'case_field_id',
      'case_field_id IN (SELECT id FROM removable_case_fields)
       AND case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    -- Delete the case fields themselves
    PERFORM safe_delete_where(
      'case_field',
      'id',
      'id IN (SELECT id FROM removable_case_fields)',
      batch_size
    );

    -- Other related deletions
    PERFORM safe_delete_where(
      'case_type_acl',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'event_post_state',
      'case_event_id',
      'case_event_id IN (SELECT id FROM removable_events)',
      batch_size
    );

    PERFORM safe_delete_where(
      'event_acl',
      'event_id',
      'event_id IN (SELECT id FROM removable_events)',
      batch_size
    );

    PERFORM safe_delete_where(
      'event_pre_state',
      'event_id',
      'event_id IN (SELECT id FROM removable_events)',
      batch_size
    );

    PERFORM safe_delete_where(
      'event_webhook',
      'event_id',
      'event_id IN (SELECT id FROM removable_events)',
      batch_size
    );

    PERFORM safe_delete_where(
      'state_acl',
      'state_id',
      'state_id IN (SELECT id FROM removable_states)',
      batch_size
    );

    PERFORM safe_delete_where(
      'state',
      'id',
      'id IN (SELECT id FROM removable_states)',
      batch_size
    );

    -- Redundant search/workbasket deletes (as per your original)
    PERFORM safe_delete_where(
      'search_cases_result_fields',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)
       AND case_field_id IN (SELECT id FROM removable_case_fields)',
      batch_size
    );

    PERFORM safe_delete_where(
      'search_input_case_field',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)
       AND case_field_id IN (SELECT id FROM removable_case_fields)',
      batch_size
    );

    PERFORM safe_delete_where(
      'search_result_case_field',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)
       AND case_field_id IN (SELECT id FROM removable_case_fields)',
      batch_size
    );

    PERFORM safe_delete_where(
      'workbasket_case_field',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)
       AND case_field_id IN (SELECT id FROM removable_case_fields)',
      batch_size
    );

    -- Remove orphan field_types (kept as per your original intent)
    PERFORM safe_delete_where(
      'field_type',
      'id',
      'id IN (
           SELECT DISTINCT field_type_id
           FROM case_field
           WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
       )
       AND id NOT IN (SELECT id FROM valid_field_type_ids)
       AND jurisdiction_id IS NOT NULL',
      batch_size
    );

    -- Additional deletes (kept from your original)
    PERFORM safe_delete_where(
      'case_field_acl',
      'case_field_id',
      'case_field_id IN (
           SELECT id FROM case_field WHERE case_type_id IN
               (SELECT id FROM case_type_ids_to_remove)
       )',
      batch_size
    );

    PERFORM safe_delete_where(
      'display_group_case_field',
      'display_group_id',
      'display_group_id IN (
           SELECT id FROM display_group WHERE case_type_id IN
               (SELECT id FROM case_type_ids_to_remove)
       )',
      batch_size
    );

    PERFORM safe_delete_where(
      'event_case_field_complex_type',
      'event_case_field_id',
      'event_case_field_id IN (
           SELECT id FROM event_case_field WHERE event_id IN
               (SELECT id FROM event WHERE case_type_id IN
                   (SELECT id FROM case_type_ids_to_remove))
       )',
      batch_size
    );

    PERFORM safe_delete_where(
      'complex_field_acl',
      'case_field_id',
      'case_field_id IN (
           SELECT id FROM case_field WHERE case_type_id IN
               (SELECT id FROM case_type_ids_to_remove)
       )',
      batch_size
    );

    PERFORM safe_delete_where(
      'search_cases_result_fields',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'search_input_case_field',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'search_result_case_field',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'workbasket_case_field',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'workbasket_input_case_field',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'event_case_field',
      'event_id',
      'event_id IN (
           SELECT id FROM event WHERE case_type_id IN
               (SELECT id FROM case_type_ids_to_remove)
       )',
      batch_size
    );

    PERFORM safe_delete_where(
      'case_field',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'role_to_access_profiles',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'display_group',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'event',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'search_criteria',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'search_party',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'search_alias_field',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'category',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'challenge_question',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'role',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'access_type_role',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    PERFORM safe_delete_where(
      'access_type',
      'case_type_id',
      'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
      batch_size
    );

    -- Final cleanup
    PERFORM safe_delete_where(
      'case_type',
      'id',
      'id IN (SELECT id FROM case_type_ids_to_remove)
       AND jurisdiction_id IS NOT NULL',
      batch_size
    );
END;
$$;


-- ============================================================
-- RUN SEQUENCE (assumes these functions already exist in your DB)
-- ============================================================

-- Create temp tables for the cleanup
-- You can pass multiple references, e.g. ARRAY['CIVIL-5111','CIVIL-5222']
SELECT prepare_cleanup_temp_tables(ARRAY['CIVIL-5111']);

-- Perform deletions in batches
SELECT run_safe_deletes(1000);

-- Drop temp tables
SELECT drop_cleanup_temp_tables();

