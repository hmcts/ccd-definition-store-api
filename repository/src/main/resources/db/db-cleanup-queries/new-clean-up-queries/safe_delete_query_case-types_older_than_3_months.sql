CREATE OR REPLACE FUNCTION manage_constraint(
    tbl regclass,         -- table name
    constraint_name text, -- constraint name
    action text,          -- 'DROP' or 'ADD'
    definition text DEFAULT NULL  -- constraint definition (for ADD)
) RETURNS void AS $$
DECLARE
    sql text;
    msg text;
BEGIN
    IF upper(action) = 'DROP' THEN
        BEGIN
            sql := format('ALTER TABLE %s DROP CONSTRAINT %I', tbl, constraint_name);
            EXECUTE sql;

            msg := format('Dropped constraint %s on table %s', constraint_name, tbl);
            RAISE NOTICE '%', msg;
            INSERT INTO ddl_log(action, table_name, message)
            VALUES ('DROP CONSTRAINT', tbl::text, msg);

        EXCEPTION
            WHEN undefined_object THEN
                msg := format('Constraint %s does not exist on table %s (DROP skipped)',
                              constraint_name, tbl);
                RAISE NOTICE '%', msg;
                INSERT INTO ddl_log(action, table_name, message)
                VALUES ('DROP CONSTRAINT', tbl::text, msg);

            WHEN others THEN
                msg := format('Failed to drop constraint %s on table %s: %s',
                              constraint_name, tbl, SQLERRM);
                RAISE NOTICE '%', msg;
                INSERT INTO ddl_log(action, table_name, message)
                VALUES ('DROP CONSTRAINT', tbl::text, msg);
        END;

    ELSIF upper(action) = 'ADD' THEN
        IF definition IS NULL THEN
            RAISE EXCEPTION 'Definition required for ADD action';
        END IF;

        BEGIN
            sql := format('ALTER TABLE %s ADD CONSTRAINT %I %s',
                          tbl, constraint_name, definition);
            EXECUTE sql;

            msg := format('Added constraint %s on table %s', constraint_name, tbl);
            RAISE NOTICE '%', msg;
            INSERT INTO ddl_log(action, table_name, message)
            VALUES ('ADD CONSTRAINT', tbl::text, msg);

        EXCEPTION
            WHEN duplicate_object THEN
                msg := format('Constraint %s already exists on table %s (ADD skipped)',
                              constraint_name, tbl);
                RAISE NOTICE '%', msg;
                INSERT INTO ddl_log(action, table_name, message)
                VALUES ('ADD CONSTRAINT', tbl::text, msg);

            WHEN others THEN
                msg := format('Failed to add constraint %s on table %s: %s',
                              constraint_name, tbl, SQLERRM);
                RAISE NOTICE '%', msg;
                INSERT INTO ddl_log(action, table_name, message)
                VALUES ('ADD CONSTRAINT', tbl::text, msg);
        END;

    ELSE
        RAISE EXCEPTION 'Invalid action: %, must be DROP or ADD', action;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION drop_foreign_key_relationships()
RETURNS void AS
$$
BEGIN
    
    --May require these missing index's
    CREATE INDEX IF NOT EXISTS idx_role_case_type_id ON "role" (case_type_id);
    CREATE INDEX IF NOT EXISTS idx_case_type_id ON case_type (id);
    CREATE INDEX IF NOT EXISTS idx_role_id ON "role" (id);
    CREATE INDEX IF NOT EXISTS idx_user_role_id ON "role" (user_role_id);

    PERFORM manage_constraint('public.role', 'case_type_id_check', 'DROP');
    PERFORM manage_constraint('public.role', 'unique_role_case_type_id_role_reference', 'DROP');
    PERFORM manage_constraint('public.role', 'fk_role_case_type_id_case_type_id', 'DROP');
    PERFORM manage_constraint('public.case_field_acl', 'case_field_acl', 'DROP');
    PERFORM manage_constraint('public.case_type_acl', 'fk_case_type_acl_role_id_role_id', 'DROP');
    PERFORM manage_constraint('public.complex_field_acl', 'fk_complex_field_acl_role_id_role_id', 'DROP');
    PERFORM manage_constraint('public.display_group', 'fk_display_group_role_id', 'DROP');
    PERFORM manage_constraint('public.search_input_case_field', 'fk_display_group_role_id', 'DROP');
    PERFORM manage_constraint('public.search_result_case_field', 'fk_display_group_role_id', 'DROP');
    PERFORM manage_constraint('public.workbasket_case_field', 'fk_display_group_role_id', 'DROP');
    PERFORM manage_constraint('public.workbasket_input_case_field', 'fk_display_group_role_id', 'DROP');
    PERFORM manage_constraint('public.search_cases_result_fields', 'fk_search_cases_result_fields_role_id_role_id', 'DROP');
    PERFORM manage_constraint('public.state_acl', 'fk_state_acl_role_id_role_id', 'DROP');
    PERFORM manage_constraint('public.event_acl', 'fk_event_acl_role_id_role_id', 'DROP');

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_foreign_key_relationships()
RETURNS void AS
$$
BEGIN
    
    PERFORM manage_constraint(
    'public.role',
    'fk_role_case_type_id_case_type_id',
    'ADD',
    'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
    );
    
    PERFORM manage_constraint(
    'public.role',
    'unique_role_case_type_id_role_reference',
    'ADD',
    'UNIQUE (case_type_id, reference)'
    );

    PERFORM manage_constraint(
        'public.role',
        'case_type_id_check',
        'ADD',
        'CHECK (
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
        )'
    );

    PERFORM manage_constraint(
    'public.case_field_acl',
    'fk_case_field_acl_role_id_role_id',
    'ADD',
    'FOREIGN KEY (role_id) REFERENCES role(id)'
    );

    PERFORM manage_constraint(
        'public.case_type_acl',
        'fk_case_type_acl_role_id_role_id',
        'ADD',
        'FOREIGN KEY (role_id) REFERENCES role(id)'
    );

    PERFORM manage_constraint(
        'public.complex_field_acl',
        'fk_complex_field_acl_role_id_role_id',
        'ADD',
        'FOREIGN KEY (role_id) REFERENCES role(id)'
    );

    PERFORM manage_constraint(
        'public.workbasket_input_case_field',
        'fk_display_group_role_id',
        'ADD',
        'FOREIGN KEY (role_id) REFERENCES role(id)'
    );

    PERFORM manage_constraint(
        'public.workbasket_case_field',
        'fk_display_group_role_id',
        'ADD',
        'FOREIGN KEY (role_id) REFERENCES role(id)'
    );

    PERFORM manage_constraint(
        'public.search_result_case_field',
        'fk_display_group_role_id',
        'ADD',
        'FOREIGN KEY (role_id) REFERENCES role(id)'
    );

    PERFORM manage_constraint(
        'public.search_input_case_field',
        'fk_display_group_role_id',
        'ADD',
        'FOREIGN KEY (role_id) REFERENCES role(id)'
    );

    PERFORM manage_constraint(
        'public.display_group',
        'fk_display_group_role_id',
        'ADD',
        'FOREIGN KEY (role_id) REFERENCES role(id)'
    );

    PERFORM manage_constraint(
        'public.event_acl',
        'fk_event_acl_role_id_role_id',
        'ADD',
        'FOREIGN KEY (role_id) REFERENCES role(id)'
    );

    PERFORM manage_constraint(
        'public.search_cases_result_fields',
        'fk_search_cases_result_fields_role_id_role_id',
        'ADD',
        'FOREIGN KEY (role_id) REFERENCES role(id)'
    );

    PERFORM manage_constraint(
        'public.state_acl',
        'fk_state_acl_role_id_role_id',
        'ADD',
        'FOREIGN KEY (role_id) REFERENCES role(id)'
    );

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION prepare_cleanup_temp_tables(older_than_months int DEFAULT 3)
RETURNS void AS
$$
BEGIN
    -- Create log output table if not exists
	CREATE TABLE IF NOT EXISTS ddl_log (
	    log_time TIMESTAMP DEFAULT now(),
	    action TEXT,
	    table_name TEXT,
	    message TEXT
	);

    -- Drop temp tables if they already exist
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

    EXECUTE format(
      'CREATE TEMP TABLE case_type_ids_to_remove AS
        SELECT id
        FROM case_type ct
        INNER JOIN (
            SELECT reference, MAX(version) AS max_version
            FROM case_type
            GROUP BY reference
        ) grouped_ct
        ON ct.reference = grouped_ct.reference
        WHERE ct.version != grouped_ct.max_version
        AND ct.created_at <= NOW() - INTERVAL ''%s MONTH''
        ORDER BY id ASC;',
        older_than_months
    );
    
    RAISE NOTICE 'Created temp table case_type_ids_to_remove for records older than % months with % rows',
        older_than_months, (SELECT COUNT(*) FROM case_type_ids_to_remove);

    -- Create temp table of valid (static/base) field types
    CREATE TEMP TABLE valid_field_type_ids AS
    SELECT id
    FROM field_type
    WHERE jurisdiction_id IS NULL;

    RAISE NOTICE 'Created temp table valid_field_type_ids with % rows',
        (SELECT COUNT(*) FROM valid_field_type_ids);

    -- create temp table of case_fields to be removed ignoring any base / static types
    CREATE TEMP TABLE removable_case_fields AS
    SELECT id, case_type_id
    FROM case_field
    WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
    AND field_type_id NOT IN (SELECT id FROM valid_field_type_ids);

    RAISE NOTICE 'Created temp table removable_case_fields with % rows',
        (SELECT COUNT(*) FROM removable_case_fields);

    CREATE TEMP TABLE removable_events AS
    SELECT id
    FROM event
    WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove);

    RAISE NOTICE 'Created temp table removable_events with % rows',
        (SELECT COUNT(*) FROM removable_events);

    CREATE TEMP TABLE removable_states AS
    SELECT id
    FROM state
        WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove);

    RAISE NOTICE 'Created temp table removable_states with % rows',
        (SELECT COUNT(*) FROM removable_states);

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION drop_cleanup_temp_tables()
RETURNS void AS
$$
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

END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION safe_delete_where(
    tbl          regclass,     -- target table
    pk_column    text,         -- PK column
    where_clause text,         -- just the WHERE condition (without "WHERE")
    batch_size   int DEFAULT 1000
)
RETURNS void
LANGUAGE plpgsql
AS $$
DECLARE
    rows_deleted int;
    total_deleted int := 0;
    full_tbl_name text := tbl::text;
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
            pk_column, full_tbl_name, where_clause, batch_size,
            full_tbl_name
        );

        GET DIAGNOSTICS rows_deleted = ROW_COUNT;
        EXIT batch_loop WHEN rows_deleted = 0;

        total_deleted := total_deleted + rows_deleted;

        RAISE NOTICE 'Batch deleted % rows from %', rows_deleted, full_tbl_name;
        INSERT INTO ddl_log(action, table_name, message)
        VALUES ('DELETE', full_tbl_name,
                'Deleted batch of ' || rows_deleted || ' rows from ' || full_tbl_name);
    END LOOP;

    RAISE NOTICE 'Total deleted from %: % rows', full_tbl_name, total_deleted;
    INSERT INTO ddl_log(action, table_name, message)
    VALUES ('DELETE SUMMARY', full_tbl_name,
            'Total deleted ' || total_deleted || ' rows from ' || full_tbl_name);
END;
$$;

CREATE OR REPLACE FUNCTION run_safe_deletes(batch_size int DEFAULT 500)
RETURNS void AS
$$
BEGIN
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

    -- Redundant search/workbasket deletes
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

    -- Remove orphan field_types
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
$$ LANGUAGE plpgsql;

--Certain FKs need to be dropped as the deletions will take too long otherwise
SELECT drop_foreign_key_relationships();
--create the various temp tables which form the bulk of the data to clean but ensuring base types are not included
SELECT prepare_cleanup_temp_tables(3);
--deletions based on the temp tables created, in batches of 1000
SELECT run_safe_deletes(1000);
--destroy temp tables created as part of this script
SELECT drop_cleanup_temp_tables();
--finally re-create the FKs dropped earlier to restore DB integrity
SELECT create_foreign_key_relationships();

DROP FUNCTION IF EXISTS manage_constraint(regclass, text, text, text);
DROP FUNCTION IF EXISTS drop_foreign_key_relationships();
DROP FUNCTION IF EXISTS create_foreign_key_relationships();
DROP FUNCTION IF EXISTS safe_delete_where(regclass, text, text, int4);
DROP FUNCTION IF EXISTS prepare_cleanup_temp_tables(int4);
DROP FUNCTION IF EXISTS drop_cleanup_temp_tables();
DROP FUNCTION IF EXISTS run_safe_deletes(int4);
