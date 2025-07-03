-- - Execute the following DROP statements before starting the cleanup

-- - May require these missing index's (safe to execute as part of execution)
-CREATE INDEX IF NOT EXISTS idx_role_case_type_id ON "role" (case_type_id);
-CREATE INDEX IF NOT EXISTS idx_case_type_id ON case_type (id);
-CREATE INDEX IF NOT EXISTS idx_role_id ON "role" (id);
-CREATE INDEX IF NOT EXISTS idx_user_role_id ON "role" (user_role_id);

-- - The following DROP statements should be executed as otherwise the deletions from the role table will take a very long time
ALTER TABLE public."role" DROP CONSTRAINT case_type_id_check;
ALTER TABLE public."role" DROP CONSTRAINT unique_role_case_type_id_role_reference;
ALTER TABLE public."role" DROP CONSTRAINT "fk_role_case_type_id_case_type_id";
ALTER TABLE public."case_field_acl" DROP CONSTRAINT "fk_case_field_acl_role_id_role_id";
ALTER TABLE public."case_type_acl" DROP CONSTRAINT "fk_case_type_acl_role_id_role_id";
ALTER TABLE public."complex_field_acl" DROP CONSTRAINT "fk_complex_field_acl_role_id_role_id";
ALTER TABLE public."display_group" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."search_input_case_field" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."search_result_case_field" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."workbasket_case_field" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."workbasket_input_case_field" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."search_cases_result_fields" DROP CONSTRAINT "fk_search_cases_result_fields_role_id_role_id";
ALTER TABLE public."state_acl" DROP CONSTRAINT "fk_state_acl_role_id_role_id";
ALTER TABLE public."event_acl" DROP CONSTRAINT "fk_event_acl_role_id_role_id";

--- - ** Actual Data Clean UP START **

--- -DROP required temp tables if already existing
DO $$
BEGIN
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
END $$;

--BEGIN;

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
DO
$$
DECLARE
    batch_size CONSTANT INTEGER := 1000;
    rows_deleted INTEGER;
BEGIN
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
      AND ct.created_at <= NOW() - INTERVAL '3 months';

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

    LOOP
        WITH del AS (
            DELETE FROM case_field_acl
			WHERE case_field_id IN (SELECT id FROM removable_case_fields)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from case_field_acl', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM display_group_case_field
			WHERE case_field_id IN (SELECT id FROM removable_case_fields)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from display_group_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM event_case_field_complex_type
			WHERE event_case_field_id IN (
    			SELECT ecf.id
    			FROM event_case_field ecf
    			JOIN removable_case_fields rcf ON ecf.case_field_id = rcf.id
    			WHERE ecf.event_id IN (SELECT id FROM removable_events)
			)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from event_case_field_complex_type', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM event_case_field
			WHERE case_field_id IN (SELECT id FROM removable_case_fields)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from event_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM complex_field_acl
			WHERE case_field_id IN (SELECT id FROM removable_case_fields)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from complex_field_acl', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM search_result_case_field
			WHERE case_field_id IN (SELECT id FROM removable_case_fields)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from search_result_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM workbasket_case_field
			WHERE case_field_id IN (SELECT id FROM removable_case_fields)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from workbasket_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM search_input_case_field
			WHERE case_field_id IN (SELECT id FROM removable_case_fields)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from search_input_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM workbasket_input_case_field
			WHERE case_field_id IN (SELECT id FROM removable_case_fields)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from workbasket_input_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM search_cases_result_fields
			scrf
			USING removable_case_fields rcf
			WHERE scrf.case_field_id = rcf.id
  			AND scrf.case_type_id IN (SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from search_cases_result_fields', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM case_field
			WHERE id IN (SELECT id FROM removable_case_fields)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM case_type_acl
			WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from case_type_acl', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM display_group_case_field
			WHERE case_field_id IN (SELECT id FROM removable_case_fields)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from display_group_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM event_post_state
			WHERE case_event_id IN (SELECT id FROM removable_events)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from event_post_state', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM event_acl
			WHERE event_id IN (SELECT id FROM removable_events)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from event_acl', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM event_pre_state
			WHERE event_id IN (SELECT id FROM removable_events)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from event_pre_state', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM event_webhook
			WHERE event_id IN (SELECT id FROM removable_events)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from event_webhook', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM state_acl
			WHERE state_id IN (SELECT id FROM removable_states)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from state_acl', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM state
			WHERE id IN (SELECT id FROM removable_states)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from state', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM search_cases_result_fields
			WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
  			AND case_field_id IN (SELECT id FROM removable_case_fields)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from search_cases_result_fields', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM search_input_case_field
			WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
  			AND case_field_id IN (SELECT id FROM removable_case_fields)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from search_input_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM search_result_case_field
			WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
  			AND case_field_id IN (SELECT id FROM removable_case_fields)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from search_result_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM workbasket_case_field
			WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
  			AND case_field_id IN (SELECT id FROM removable_case_fields)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from workbasket_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM field_type
			WHERE id IN (
    			SELECT DISTINCT field_type_id
    			FROM case_field
    			WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
			)
			AND id NOT IN (SELECT id FROM valid_field_type_ids)
			AND jurisdiction_id IS NOT NULL
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from field_type', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM case_field_acl
			WHERE case_field_id IN
    		(SELECT id FROM case_field WHERE case_type_id IN
        		(SELECT id FROM case_type_ids_to_remove)
    		)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from case_field_acl', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM display_group_case_field
			WHERE display_group_id IN
    		(SELECT id FROM display_group WHERE case_type_id IN
        		(SELECT id FROM case_type_ids_to_remove)
    		)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from display_group_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM event_case_field_complex_type
			WHERE event_case_field_id IN
		    (SELECT id FROM event_case_field WHERE event_id IN
		        (SELECT id FROM event WHERE case_type_id IN
		            (SELECT id FROM case_type_ids_to_remove)
		        )
		    )
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from event_case_field_complex_type', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM complex_field_acl
			WHERE case_field_id IN
		    (SELECT id FROM case_field WHERE case_type_id IN
		        (SELECT id FROM case_type_ids_to_remove)
		    )
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from complex_field_acl', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM search_cases_result_fields
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from search_cases_result_fields', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM search_input_case_field
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from search_input_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM search_result_case_field
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from search_result_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM workbasket_case_field
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from workbasket_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM workbasket_input_case_field
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from workbasket_input_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM event_case_field
			WHERE event_id IN
    		(SELECT id FROM event WHERE case_type_id IN
        		(SELECT id FROM case_type_ids_to_remove)
    		)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from event_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM case_field
			cf WHERE cf.case_type_id IN (SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM role_to_access_profiles
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from role_to_access_profiles', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM display_group_case_field
			WHERE display_group_id IN
		    (SELECT id FROM display_group WHERE case_type_id IN
		        (SELECT id FROM case_type_ids_to_remove)
		    )
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from display_group_case_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM display_group
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from display_group', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM event
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from event', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM search_criteria
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from search_criteria', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM search_party
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from search_party', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM search_alias_field
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from search_alias_field', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM category
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from category', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM challenge_question
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from challenge_question', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM access_type_role
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from access_type_role', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM access_type
			WHERE case_type_id IN
    		(SELECT id FROM case_type_ids_to_remove)
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from access_type', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);


    LOOP
        WITH del AS (
            DELETE FROM case_type
			WHERE id IN (SELECT id FROM case_type_ids_to_remove)
	  		AND jurisdiction_id IS NOT NULL
            LIMIT batch_size
            RETURNING 1
        )
        SELECT COUNT(*) INTO rows_deleted FROM del;
        EXIT WHEN rows_deleted = 0;
        RAISE NOTICE 'Deleted % rows from case_type', rows_deleted;
    END LOOP;
    PERFORM pg_sleep(1);

END
$$;

--- - ** Actual Data Clean UP END **

-- Note: The jurisdiction_id check is to ensure we only delete case types that are not system-defined.
-- This is important to prevent accidental deletion of system case types.

--COMMIT;

-- Clean up temp tables after transaction completes
DO $$
BEGIN
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
END $$;

--Execute the below ALTER statements to re-add the FK constraints on Table 'role' which was removed as part of the clean up process
ALTER TABLE public."role" ADD CONSTRAINT "fk_role_case_type_id_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."role"
ADD CONSTRAINT unique_role_case_type_id_role_reference
UNIQUE (case_type_id, reference);
ALTER TABLE public."role"
ADD CONSTRAINT case_type_id_check CHECK (
  (
    CASE
      WHEN dtype = 'CASEROLE' THEN
        CASE
          WHEN case_type_id IS NOT NULL THEN 1
          ELSE 0
        END
      ELSE 1
    END = 1
  )
);
ALTER TABLE public."case_field_acl" ADD CONSTRAINT "fk_case_field_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."case_type_acl" ADD CONSTRAINT "fk_case_type_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."complex_field_acl" ADD CONSTRAINT "fk_complex_field_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."workbasket_input_case_field" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."workbasket_case_field" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."search_result_case_field" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."search_input_case_field" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."display_group" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."event_acl" ADD CONSTRAINT "fk_event_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."search_cases_result_fields" ADD CONSTRAINT "fk_search_cases_result_fields_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."state_acl" ADD CONSTRAINT "fk_state_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);

