CREATE OR REPLACE PROCEDURE public.cleanup_case_type_data()
 LANGUAGE plpgsql
AS $procedure$
DECLARE
    drop_rec RECORD;
    batch_size CONSTANT INTEGER := 1000;
    rows_deleted INTEGER;
BEGIN
	-- - May require these missing index's (safe to execute as part of execution)
	CREATE INDEX IF NOT EXISTS idx_role_case_type_id ON "role" (case_type_id);
	CREATE INDEX IF NOT EXISTS idx_case_type_id ON case_type (id);
	CREATE INDEX IF NOT EXISTS idx_role_id ON "role" (id);
	CREATE INDEX IF NOT EXISTS idx_user_role_id ON "role" (user_role_id);

	-- - The following DROP statements should be executed as otherwise the deletions from the role table will take a very long time

    -- Step 1: Create a temporary table to hold table/constraint pairs
    CREATE TEMP TABLE tmp_constraints_to_drop (
        table_name TEXT,
        constraint_name TEXT
    ) ON COMMIT DROP;

    -- Step 2: Insert table/constraint combinations
    INSERT INTO tmp_constraints_to_drop (table_name, constraint_name) VALUES
        ('public.role', 'case_type_id_check'),
        ('public.case_type', 'some_constraint_on_case_type'),
        ('public.field_type', 'field_type_jurisdiction_id_fkey'),
       	('public.role',  'case_type_id_check'),
       	('public.role',  'unique_role_case_type_id_role_reference'),
       	('public.role',  'fk_role_case_type_id_case_type_id'),
		('public.case_field_acl',  'fk_case_field_acl_role_id_role_id'),
		('public.case_type_acl',  'fk_case_type_acl_role_id_role_id'),
		('public.complex_field_acl',  'fk_complex_field_acl_role_id_role_id'),
		('public.display_group',  'fk_display_group_role_id'),
		('public.search_input_case_field',  'fk_display_group_role_id'),
		('public.search_result_case_field',  'fk_display_group_role_id'),
		('public.workbasket_case_field',  'fk_display_group_role_id'),
		('public.workbasket_input_case_field',  'fk_display_group_role_id'),
		('public.search_cases_result_fields',  'fk_search_cases_result_fields_role_id_role_id'),
		('public.state_acl',  'fk_state_acl_role_id_role_id'),
		('public.event_acl',  'fk_event_acl_role_id_role_id');

    -- Step 3: Loop through and drop each constraint safely
    FOR drop_rec IN SELECT * FROM tmp_constraints_to_drop LOOP
        BEGIN
            EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', drop_rec.table_name, drop_rec.constraint_name);
            RAISE NOTICE 'Dropped constraint % from table %', drop_rec.constraint_name, drop_rec.table_name;
        EXCEPTION
            WHEN undefined_object THEN
                RAISE NOTICE 'Constraint % on table % does not exist, skipping.', drop_rec.constraint_name, drop_rec.table_name;
            WHEN others THEN
                RAISE NOTICE 'Error dropping constraint % on table %: %', drop_rec.constraint_name, drop_rec.table_name, SQLERRM;
        END;
    END LOOP;


	--- - ** Actual Data Clean UP START **

	--- -DROP required temp tables if already existing

	  BEGIN
	    DROP TABLE IF EXISTS case_type_ids_to_remove;
	  EXCEPTION WHEN OTHERS THEN
	    -- ignore error
	  END;

	  BEGIN
	    DROP TABLE IF EXISTS valid_field_type_ids;
	  EXCEPTION WHEN OTHERS THEN
	    -- ignore error
	  END;

      BEGIN
        DROP TABLE IF EXISTS removable_case_fields;
      EXCEPTION WHEN OTHERS THEN
        -- ignore error
      END;

      BEGIN
        DROP TABLE IF EXISTS removable_events;
      EXCEPTION WHEN OTHERS THEN
        -- ignore error
      END;

      BEGIN
        DROP TABLE IF EXISTS removable_states;
      EXCEPTION WHEN OTHERS THEN
        -- ignore error
      END;


    -- =========================================
    -- ⚠️ Tables containing static (base types)

    -- - public.field_type
    -- - public.complex_field
    -- - public.case_field

    -- - Care must be taken to not delete any base types
    -- =========================================

    -- These queries are designed to be run in a controlled environment where the data integrity and relationships are well understood.
    -- Always ensure to backup your data before running such delete operations.

    -- ******************************************

	-- Create log output table if not exists
	CREATE TABLE IF NOT EXISTS ddl_log (
	    log_time TIMESTAMP DEFAULT now(),
	    action TEXT,
	    table_name TEXT,
	    message TEXT
	);

    -- Prepare temporary tables
    CREATE TEMP TABLE case_type_ids_to_remove AS
    SELECT id
    FROM case_type ct
    INNER JOIN (
        SELECT reference, MAX(version) AS max_version
        FROM case_type
        GROUP BY reference
    ) grouped_ct
    ON ct.reference = grouped_ct.reference
    WHERE ct.version != grouped_ct.max_version
      AND ct.created_at <= NOW() - INTERVAL '1 minutes';

    CREATE TEMP TABLE valid_field_type_ids AS
    SELECT id FROM field_type WHERE jurisdiction_id IS NULL;

    CREATE TEMP TABLE removable_case_fields AS
    SELECT id, case_type_id FROM case_field
    WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
      AND field_type_id NOT IN (SELECT id FROM valid_field_type_ids);

    CREATE TEMP TABLE removable_events AS
    SELECT id FROM event WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove);

    CREATE TEMP TABLE removable_states AS
    SELECT id FROM state WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove);

    PERFORM pg_sleep(1);

   	PERFORM pg_sleep(1);

    -- - ====================
    -- - Start data clean up
    -- - ====================

    -- - Delete from table case_field_acl
    BEGIN
        <<batch_delete_loop_case_field_acl>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM case_field_acl
                WHERE case_field_id IN (SELECT id FROM removable_case_fields)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM case_field_acl
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_case_field_acl WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from case_field_acl', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'case_field_acl', 'Deleted ' || rows_deleted || ' rows from case_field_acl');
        END LOOP batch_delete_loop_case_field_acl;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
        	RAISE NOTICE 'Skipping deletion from case_field_acl due to error: %', SQLERRM;
    END;

    -- - Delete from table display_group_case_field
    BEGIN
        <<batch_delete_loop_display_group_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM display_group_case_field
                WHERE case_field_id IN (SELECT id FROM removable_case_fields)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM display_group_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_display_group_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from display_group_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'display_group_case_field', 'Deleted ' || rows_deleted || ' rows from display_group_case_field');
        END LOOP batch_delete_loop_display_group_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from display_group_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table event_case_field_complex_type
    BEGIN
        <<batch_delete_loop_event_case_field_complex_type>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM event_case_field_complex_type
	                WHERE event_case_field_id IN (
				    SELECT ecf.id
				    FROM event_case_field ecf
				    JOIN removable_case_fields rcf ON ecf.case_field_id = rcf.id
				    WHERE ecf.event_id IN (SELECT id FROM removable_events)
				)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM event_case_field_complex_type
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_event_case_field_complex_type WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from event_case_field_complex_type', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'event_case_field_complex_type', 'Deleted ' || rows_deleted || ' rows from event_case_field_complex_type');
        END LOOP batch_delete_loop_event_case_field_complex_type;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from event_case_field_complex_type due to error: %', SQLERRM;
    END;

    -- - Delete from table event_case_field
    BEGIN
        <<batch_delete_loop_event_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM event_case_field
                WHERE case_field_id IN (SELECT id FROM removable_case_fields)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM event_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_event_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from event_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'event_case_field', 'Deleted ' || rows_deleted || ' rows from event_case_field');
        END LOOP batch_delete_loop_event_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from event_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table complex_field_acl
    BEGIN
        <<batch_delete_loop_complex_field_acl>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM complex_field_acl
                WHERE case_field_id IN (SELECT id FROM removable_case_fields)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM complex_field_acl
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_complex_field_acl WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from complex_field_acl', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'complex_field_acl', 'Deleted ' || rows_deleted || ' rows from complex_field_acl');
        END LOOP batch_delete_loop_complex_field_acl;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from complex_field_acl due to error: %', SQLERRM;
    END;

    -- - Delete from table search_result_case_field
    BEGIN
        <<batch_delete_loop_search_result_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM search_result_case_field
                WHERE case_field_id IN (SELECT id FROM removable_case_fields)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM search_result_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_search_result_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from search_result_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'search_result_case_field', 'Deleted ' || rows_deleted || ' rows from search_result_case_field');
        END LOOP batch_delete_loop_search_result_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from search_result_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table workbasket_case_field
    BEGIN
        <<batch_delete_loop_workbasket_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM workbasket_case_field
                WHERE case_field_id IN (SELECT id FROM removable_case_fields)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM workbasket_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_workbasket_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from workbasket_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'workbasket_case_field', 'Deleted ' || rows_deleted || ' rows from workbasket_case_field');
        END LOOP batch_delete_loop_workbasket_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from workbasket_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table search_input_case_field
    BEGIN
        <<batch_delete_loop_search_input_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM search_input_case_field
                WHERE case_field_id IN (SELECT id FROM removable_case_fields)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM search_input_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_search_input_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from search_input_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'search_input_case_field', 'Deleted ' || rows_deleted || ' rows from search_input_case_field');
        END LOOP batch_delete_loop_search_input_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from search_input_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table workbasket_input_case_field
    BEGIN
        <<batch_delete_loop_workbasket_input_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM workbasket_input_case_field
                WHERE case_field_id IN (SELECT id FROM removable_case_fields)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM workbasket_input_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_workbasket_input_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from workbasket_input_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'workbasket_input_case_field', 'Deleted ' || rows_deleted || ' rows from workbasket_input_case_field');
        END LOOP batch_delete_loop_workbasket_input_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from workbasket_input_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table search_cases_result_fields
    BEGIN
        <<batch_delete_loop_search_cases_result_fields>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM search_cases_result_fields
                WHERE EXISTS (
				    SELECT 1 FROM removable_case_fields rcf
				    WHERE search_cases_result_fields.case_field_id = rcf.id
				    AND search_cases_result_fields.case_type_id IN (SELECT id FROM case_type_ids_to_remove)
				)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM search_cases_result_fields
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_search_cases_result_fields WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from search_cases_result_fields', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'search_cases_result_fields', 'Deleted ' || rows_deleted || ' rows from search_cases_result_fields');
        END LOOP batch_delete_loop_search_cases_result_fields;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from search_cases_result_fields due to error: %', SQLERRM;
    END;

    -- - Delete from table case_field
    BEGIN
        <<batch_delete_loop_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM case_field
                WHERE id IN (SELECT id FROM removable_case_fields)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'case_field', 'Deleted ' || rows_deleted || ' rows from case_field');
        END LOOP batch_delete_loop_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table case_type_acl
    BEGIN
        <<batch_delete_loop_case_type_acl>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM case_type_acl
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM case_type_acl
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_case_type_acl WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from case_type_acl', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'case_type_acl', 'Deleted ' || rows_deleted || ' rows from case_type_acl');
        END LOOP batch_delete_loop_case_type_acl;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from case_type_acl due to error: %', SQLERRM;
    END;

    -- - Delete from table event_post_state
    BEGIN
        <<batch_delete_loop_event_post_state>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM event_post_state
                WHERE case_event_id IN (SELECT id FROM removable_events)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM event_post_state
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_event_post_state WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from event_post_state', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'event_post_state', 'Deleted ' || rows_deleted || ' rows from event_post_state');
        END LOOP batch_delete_loop_event_post_state;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from event_post_state due to error: %', SQLERRM;
    END;

    -- - Delete from table event_acl
    BEGIN
        <<batch_delete_loop_event_acl>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM event_acl
                WHERE event_id IN (SELECT id FROM removable_events)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM event_acl
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_event_acl WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from event_acl', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'event_acl', 'Deleted ' || rows_deleted || ' rows from event_acl');
        END LOOP batch_delete_loop_event_acl;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from event_acl due to error: %', SQLERRM;
    END;

    -- - Delete from table event_pre_state
    BEGIN
        <<batch_delete_loop_event_pre_state>>
        LOOP
            WITH to_delete AS (
                SELECT event_id FROM event_pre_state
                WHERE event_id IN (SELECT id FROM removable_events)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM event_pre_state
                WHERE event_id IN (SELECT event_id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_event_pre_state WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from event_pre_state', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'event_pre_state', 'Deleted ' || rows_deleted || ' rows from event_pre_state');
        END LOOP batch_delete_loop_event_pre_state;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from event_pre_state due to error: %', SQLERRM;
    END;

    -- - Delete from table event_pre_state
    BEGIN
        <<batch_delete_loop_event_pre_state>>
        LOOP
            WITH to_delete AS (
                SELECT state_id FROM event_pre_state
                WHERE state_id IN (SELECT id FROM removable_states)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM event_pre_state
                WHERE state_id IN (SELECT state_id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_event_pre_state WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from event_pre_state', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'event_pre_state', 'Deleted ' || rows_deleted || ' rows from event_pre_state');
        END LOOP batch_delete_loop_event_pre_state;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from event_pre_state due to error: %', SQLERRM;
    END;

    -- - Delete from table event_webhook
    BEGIN
        <<batch_delete_loop_event_webhook>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM event_webhook
                WHERE event_id IN (SELECT id FROM removable_events)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM event_webhook
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_event_webhook WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from event_webhook', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'event_webhook', 'Deleted ' || rows_deleted || ' rows from event_webhook');
        END LOOP batch_delete_loop_event_webhook;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from event_webhook due to error: %', SQLERRM;
    END;

    -- - Delete from table state_acl
    BEGIN
        <<batch_delete_loop_state_acl>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM state_acl
                WHERE state_id IN (SELECT id FROM removable_states)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM state_acl
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_state_acl WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from state_acl', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'state_acl', 'Deleted ' || rows_deleted || ' rows from state_acl');
        END LOOP batch_delete_loop_state_acl;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from state_acl due to error: %', SQLERRM;
    END;

    -- - Delete from table state
    BEGIN
        <<batch_delete_loop_state>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM state
                WHERE id IN (SELECT id FROM removable_states)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM state
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_state WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from state', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'state', 'Deleted ' || rows_deleted || ' rows from state');
        END LOOP batch_delete_loop_state;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from state due to error: %', SQLERRM;
    END;

    -- - Delete from table search_cases_result_fields
    BEGIN
        <<batch_delete_loop_search_cases_result_fields>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM search_cases_result_fields
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
				AND case_field_id IN (SELECT id FROM removable_case_fields)
				                LIMIT batch_size
            ),
            del AS (
                DELETE FROM search_cases_result_fields
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_search_cases_result_fields WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from search_cases_result_fields', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'search_cases_result_fields', 'Deleted ' || rows_deleted || ' rows from search_cases_result_fields');
        END LOOP batch_delete_loop_search_cases_result_fields;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from search_cases_result_fields due to error: %', SQLERRM;
    END;

    -- - Delete from table search_input_case_field
    BEGIN
        <<batch_delete_loop_search_input_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM search_input_case_field
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
				AND case_field_id IN (SELECT id FROM removable_case_fields)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM search_input_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_search_input_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from search_input_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'search_input_case_field', 'Deleted ' || rows_deleted || ' rows from search_input_case_field');
        END LOOP batch_delete_loop_search_input_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from search_input_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table search_result_case_field
    BEGIN
        <<batch_delete_loop_search_result_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM search_result_case_field
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
				AND case_field_id IN (SELECT id FROM removable_case_fields)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM search_result_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_search_result_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from search_result_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'search_result_case_field', 'Deleted ' || rows_deleted || ' rows from search_result_case_field');
        END LOOP batch_delete_loop_search_result_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from search_result_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table workbasket_case_field
    BEGIN
        <<batch_delete_loop_workbasket_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM workbasket_case_field
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
				AND case_field_id IN (SELECT id FROM removable_case_fields)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM workbasket_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_workbasket_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from workbasket_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'workbasket_case_field', 'Deleted ' || rows_deleted || ' rows from workbasket_case_field');
        END LOOP batch_delete_loop_workbasket_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from workbasket_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table field_type
    BEGIN
        <<batch_delete_loop_field_type>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM field_type
                WHERE id IN (
				    SELECT DISTINCT field_type_id
				    FROM case_field
				    WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
				)
				AND id NOT IN (SELECT id FROM valid_field_type_ids)
				AND jurisdiction_id IS NOT NULL
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM field_type
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_field_type WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from field_type', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'field_type', 'Deleted ' || rows_deleted || ' rows from field_type');
        END LOOP batch_delete_loop_field_type;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from field_type due to error: %', SQLERRM;
    END;

    -- - Delete from table case_field_acl
    BEGIN
        <<batch_delete_loop_case_field_acl>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM case_field_acl
                WHERE case_field_id IN (
				    SELECT id FROM case_field
				    WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
				)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM case_field_acl
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_case_field_acl WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from case_field_acl', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'case_field_acl', 'Deleted ' || rows_deleted || ' rows from case_field_acl');
        END LOOP batch_delete_loop_case_field_acl;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from case_field_acl due to error: %', SQLERRM;
    END;

    -- - Delete from table display_group_case_field
    BEGIN
        <<batch_delete_loop_display_group_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM display_group_case_field
                WHERE display_group_id IN (
				    SELECT id FROM display_group
				    WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
				)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM display_group_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_display_group_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from display_group_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'display_group_case_field', 'Deleted ' || rows_deleted || ' rows from display_group_case_field');
        END LOOP batch_delete_loop_display_group_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from display_group_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table event_case_field_complex_type
    BEGIN
        <<batch_delete_loop_event_case_field_complex_type>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM event_case_field_complex_type
                WHERE event_case_field_id IN (
				    SELECT id FROM event_case_field
				    WHERE event_id IN (
				        SELECT id FROM event
				        WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
				    )
				)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM event_case_field_complex_type
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_event_case_field_complex_type WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from event_case_field_complex_type', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'event_case_field_complex_type', 'Deleted ' || rows_deleted || ' rows from event_case_field_complex_type');
        END LOOP batch_delete_loop_event_case_field_complex_type;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from event_case_field_complex_type due to error: %', SQLERRM;
    END;

    -- - Delete from table complex_field_acl
    BEGIN
        <<batch_delete_loop_complex_field_acl>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM complex_field_acl
                WHERE case_field_id IN (
				    SELECT id FROM case_field
				    WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
				)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM complex_field_acl
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_complex_field_acl WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from complex_field_acl', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'complex_field_acl', 'Deleted ' || rows_deleted || ' rows from complex_field_acl');
        END LOOP batch_delete_loop_complex_field_acl;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from complex_field_acl due to error: %', SQLERRM;
    END;

    -- - Delete from table search_cases_result_fields
    BEGIN
        <<batch_delete_loop_search_cases_result_fields>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM search_cases_result_fields
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM search_cases_result_fields
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_search_cases_result_fields WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from search_cases_result_fields', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'search_cases_result_fields', 'Deleted ' || rows_deleted || ' rows from search_cases_result_fields');
        END LOOP batch_delete_loop_search_cases_result_fields;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from search_cases_result_fields due to error: %', SQLERRM;
    END;

    -- - Delete from table search_input_case_field
    BEGIN
        <<batch_delete_loop_search_input_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM search_input_case_field
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM search_input_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_search_input_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from search_input_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'search_input_case_field', 'Deleted ' || rows_deleted || ' rows from search_input_case_field');
        END LOOP batch_delete_loop_search_input_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from search_input_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table search_result_case_field
    BEGIN
        <<batch_delete_loop_search_result_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM search_result_case_field
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM search_result_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_search_result_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from search_result_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'search_result_case_field', 'Deleted ' || rows_deleted || ' rows from search_result_case_field');
        END LOOP batch_delete_loop_search_result_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from search_result_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table workbasket_case_field
    BEGIN
        <<batch_delete_loop_workbasket_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM workbasket_case_field
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM workbasket_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_workbasket_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from workbasket_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'workbasket_case_field', 'Deleted ' || rows_deleted || ' rows from workbasket_case_field');
        END LOOP batch_delete_loop_workbasket_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from workbasket_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table workbasket_input_case_field
    BEGIN
        <<batch_delete_loop_workbasket_input_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM workbasket_input_case_field
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM workbasket_input_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_workbasket_input_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from workbasket_input_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'workbasket_input_case_field', 'Deleted ' || rows_deleted || ' rows from workbasket_input_case_field');
        END LOOP batch_delete_loop_workbasket_input_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from workbasket_input_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table event_case_field
    BEGIN
        <<batch_delete_loop_event_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM event_case_field
                WHERE event_id IN (
				    SELECT id FROM event
				    WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
				)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM event_case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_event_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from event_case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'event_case_field', 'Deleted ' || rows_deleted || ' rows from event_case_field');
        END LOOP batch_delete_loop_event_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from event_case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table case_field
    BEGIN
        <<batch_delete_loop_case_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM case_field
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM case_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_case_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from case_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'case_field', 'Deleted ' || rows_deleted || ' rows from case_field');
        END LOOP batch_delete_loop_case_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from case_field due to error: %', SQLERRM;
    END;

    -- - Delete from table role_to_access_profiles
    BEGIN
        <<batch_delete_loop_role_to_access_profiles>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM role_to_access_profiles
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM role_to_access_profiles
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_role_to_access_profiles WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from role_to_access_profiles', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'role_to_access_profiles', 'Deleted ' || rows_deleted || ' rows from role_to_access_profiles');
        END LOOP batch_delete_loop_role_to_access_profiles;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from role_to_access_profiles due to error: %', SQLERRM;
    END;

    -- - Delete from table display_group
    BEGIN
        <<batch_delete_loop_display_group>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM display_group
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM display_group
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_display_group WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from display_group', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'display_group', 'Deleted ' || rows_deleted || ' rows from display_group');
        END LOOP batch_delete_loop_display_group;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from display_group due to error: %', SQLERRM;
    END;

    -- - Delete from table event
    BEGIN
        <<batch_delete_loop_event>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM event
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM event
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_event WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from event', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'event', 'Deleted ' || rows_deleted || ' rows from event');
        END LOOP batch_delete_loop_event;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from event due to error: %', SQLERRM;
    END;

    -- - Delete from table search_criteria
    BEGIN
        <<batch_delete_loop_search_criteria>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM search_criteria
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM search_criteria
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_search_criteria WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from search_criteria', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'search_criteria', 'Deleted ' || rows_deleted || ' rows from search_criteria');
        END LOOP batch_delete_loop_search_criteria;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from search_criteria due to error: %', SQLERRM;
    END;

    -- - Delete from table search_party
    BEGIN
        <<batch_delete_loop_search_party>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM search_party
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM search_party
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_search_party WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from search_party', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'search_party', 'Deleted ' || rows_deleted || ' rows from search_party');
        END LOOP batch_delete_loop_search_party;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from search_party due to error: %', SQLERRM;
    END;

    -- - Delete from table search_alias_field
    BEGIN
        <<batch_delete_loop_search_alias_field>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM search_alias_field
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM search_alias_field
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_search_alias_field WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from search_alias_field', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'search_alias_field', 'Deleted ' || rows_deleted || ' rows from search_alias_field');
        END LOOP batch_delete_loop_search_alias_field;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from search_alias_field due to error: %', SQLERRM;
    END;

    -- - Delete from table category
    BEGIN
        <<batch_delete_loop_category>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM category
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM category
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_category WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from category', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'category', 'Deleted ' || rows_deleted || ' rows from category');
        END LOOP batch_delete_loop_category;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from category due to error: %', SQLERRM;
    END;

    -- - Delete from table challenge_question
    BEGIN
        <<batch_delete_loop_challenge_question>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM challenge_question
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM challenge_question
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_challenge_question WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from challenge_question', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'challenge_question', 'Deleted ' || rows_deleted || ' rows from challenge_question');
        END LOOP batch_delete_loop_challenge_question;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from challenge_question due to error: %', SQLERRM;
    END;

    -- - Delete from table access_type_role
    BEGIN
        <<batch_delete_loop_access_type_role>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM access_type_role
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM access_type_role
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_access_type_role WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from access_type_role', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'access_type_role', 'Deleted ' || rows_deleted || ' rows from access_type_role');
        END LOOP batch_delete_loop_access_type_role;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from access_type_role due to error: %', SQLERRM;
    END;

    -- - Delete from table access_type
    BEGIN
        <<batch_delete_loop_access_type>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM access_type
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM access_type
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_access_type WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from access_type', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'access_type', 'Deleted ' || rows_deleted || ' rows from access_type');
        END LOOP batch_delete_loop_access_type;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from access_type due to error: %', SQLERRM;
    END;

    -- - Delete from table role
    BEGIN
        <<batch_delete_loop_role>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM role
                WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM role
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_role WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from search_party', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'search_party', 'Deleted ' || rows_deleted || ' rows from search_party');
        END LOOP batch_delete_loop_role;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from search_party due to error: %', SQLERRM;
    END;

    -- - Delete from table case_type
    BEGIN
        <<batch_delete_loop_case_type>>
        LOOP
            WITH to_delete AS (
                SELECT id FROM case_type
                WHERE id IN (SELECT id FROM case_type_ids_to_remove)
				AND jurisdiction_id IS NOT NULL
                LIMIT batch_size
            ),
            del AS (
                DELETE FROM case_type
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT COUNT(*) INTO rows_deleted FROM del;

            EXIT batch_delete_loop_case_type WHEN rows_deleted = 0;
            RAISE NOTICE 'Deleted % rows from case_type', rows_deleted;
            INSERT INTO ddl_log(action, table_name, message)
    		VALUES ('DELETE', 'case_type', 'Deleted ' || rows_deleted || ' rows from case_typea');
        END LOOP batch_delete_loop_case_type;
        PERFORM pg_sleep(1);
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Skipping deletion from case_type due to error: %', SQLERRM;
    END;

    -- - ====================
    -- - End data clean up
    -- - ====================

    -- - Begin clean up temporary tables
    BEGIN
        DROP TABLE IF EXISTS case_type_ids_to_remove;
        EXCEPTION WHEN OTHERS THEN
        -- ignore error
    END;

    BEGIN
        DROP TABLE IF EXISTS valid_field_type_ids;
        EXCEPTION WHEN OTHERS THEN
        -- ignore error
    END;

    BEGIN
        DROP TABLE IF EXISTS removable_case_fields;
        EXCEPTION WHEN OTHERS THEN
        -- ignore error
    END;

    BEGIN
        DROP TABLE IF EXISTS removable_events;
        EXCEPTION WHEN OTHERS THEN
        -- ignore error
    END;

    BEGIN
        DROP TABLE IF EXISTS removable_states;
        EXCEPTION WHEN OTHERS THEN
        -- ignore error
    END;
    -- - End clean up temporary tables

    -- - Begin re-add FK relationships
    BEGIN
        EXECUTE 'ALTER TABLE public.role ADD CONSTRAINT "fk_role_case_type_id_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id)';
        RAISE NOTICE 'Successfully added constraint on %', 'public.role';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping constraint on % due to error: %', 'public.role', SQLERRM;
    END;

    BEGIN
        EXECUTE 'ALTER TABLE public.role ADD CONSTRAINT unique_role_case_type_id_role_reference UNIQUE (case_type_id, reference)';
        RAISE NOTICE 'Successfully added constraint on %', 'public.role';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping constraint on % due to error: %', 'public.role', SQLERRM;
    END;

    BEGIN
        EXECUTE 'ALTER TABLE public.role ADD CONSTRAINT case_type_id_check CHECK (
      (
        CASE
          WHEN dtype = ''CASEROLE'' THEN
            CASE
              WHEN case_type_id IS NOT NULL THEN 1
              ELSE 0
            END
          ELSE 1
        END = 1
      )
    )';
        RAISE NOTICE 'Successfully added constraint on %', 'public.role';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping constraint on % due to error: %', 'public.role', SQLERRM;
    END;

    BEGIN
        EXECUTE 'ALTER TABLE public.case_field_acl ADD CONSTRAINT "fk_case_field_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id)';
        RAISE NOTICE 'Successfully added constraint on %', 'public.case_field_acl';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping constraint on % due to error: %', 'public.case_field_acl', SQLERRM;
    END;

    BEGIN
        EXECUTE 'ALTER TABLE public.case_type_acl ADD CONSTRAINT "fk_case_type_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id)';
        RAISE NOTICE 'Successfully added constraint on %', 'public.case_type_acl';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping constraint on % due to error: %', 'public.case_type_acl', SQLERRM;
    END;

    BEGIN
        EXECUTE 'ALTER TABLE public.complex_field_acl ADD CONSTRAINT "fk_complex_field_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id)';
        RAISE NOTICE 'Successfully added constraint on %', 'public.complex_field_acl';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping constraint on % due to error: %', 'public.complex_field_acl', SQLERRM;
    END;

    BEGIN
        EXECUTE 'ALTER TABLE public.workbasket_input_case_field ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id)';
        RAISE NOTICE 'Successfully added constraint on %', 'public.workbasket_input_case_field';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping constraint on % due to error: %', 'public.workbasket_input_case_field', SQLERRM;
    END;

    BEGIN
        EXECUTE 'ALTER TABLE public.workbasket_case_field ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id)';
        RAISE NOTICE 'Successfully added constraint on %', 'public.workbasket_case_field';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping constraint on % due to error: %', 'public.workbasket_case_field', SQLERRM;
    END;

    BEGIN
        EXECUTE 'ALTER TABLE public.search_result_case_field ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id)';
        RAISE NOTICE 'Successfully added constraint on %', 'public.search_result_case_field';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping constraint on % due to error: %', 'public.search_result_case_field', SQLERRM;
    END;

    BEGIN
        EXECUTE 'ALTER TABLE public.search_input_case_field ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id)';
        RAISE NOTICE 'Successfully added constraint on %', 'public.search_input_case_field';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping constraint on % due to error: %', 'public.search_input_case_field', SQLERRM;
    END;

    BEGIN
        EXECUTE 'ALTER TABLE public.display_group ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id)';
        RAISE NOTICE 'Successfully added constraint on %', 'public.display_group';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping constraint on % due to error: %', 'public.display_group', SQLERRM;
    END;

    BEGIN
        EXECUTE 'ALTER TABLE public.event_acl ADD CONSTRAINT "fk_event_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id)';
        RAISE NOTICE 'Successfully added constraint on %', 'public.event_acl';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping constraint on % due to error: %', 'public.event_acl', SQLERRM;
    END;

    BEGIN
        EXECUTE 'ALTER TABLE public.search_cases_result_fields ADD CONSTRAINT "fk_search_cases_result_fields_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id)';
        RAISE NOTICE 'Successfully added constraint on %', 'public.search_cases_result_fields';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping constraint on % due to error: %', 'public.search_cases_result_fields', SQLERRM;
    END;

    BEGIN
        EXECUTE 'ALTER TABLE public.state_acl ADD CONSTRAINT "fk_state_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id)';
        RAISE NOTICE 'Successfully added constraint on %', 'public.state_acl';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping constraint on % due to error: %', 'public.state_acl', SQLERRM;
    END;
    -- - End re-add FK relationships
END
$procedure$
;
