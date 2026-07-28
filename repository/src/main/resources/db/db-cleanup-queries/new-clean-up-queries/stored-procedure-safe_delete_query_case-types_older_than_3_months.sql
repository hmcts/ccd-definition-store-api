CREATE OR REPLACE PROCEDURE cleanup_case_types(batch_size int DEFAULT 1000,
    older_than_months int DEFAULT 3)
LANGUAGE plpgsql
AS $$
DECLARE
    summary_msg text;
BEGIN
    ----------------------------------------------------------------------
    -- 1. CREATE HELPER PROCEDURES dynamically
    ----------------------------------------------------------------------

    EXECUTE 'DROP ROUTINE IF EXISTS manage_constraint(regclass, text, text, text)';
    EXECUTE 'DROP ROUTINE IF EXISTS drop_foreign_key_relationships()';
    EXECUTE 'DROP ROUTINE IF EXISTS create_foreign_key_relationships()';
    EXECUTE 'DROP ROUTINE IF EXISTS safe_delete_where(regclass, text, text, int4)';
    EXECUTE 'DROP ROUTINE IF EXISTS prepare_cleanup_temp_tables(int4)';
    EXECUTE 'DROP ROUTINE IF EXISTS drop_cleanup_temp_tables()';
    EXECUTE 'DROP ROUTINE IF EXISTS run_safe_deletes(int4)';

    -- manage_constraint
    EXECUTE $fn$
    CREATE OR REPLACE PROCEDURE manage_constraint(
        tbl regclass,
        constraint_name text,
        action text,
        definition text DEFAULT NULL
    )
    LANGUAGE plpgsql
    AS $body$
    DECLARE
        sql text;
        msg text;
    BEGIN
        IF upper(action) = 'DROP' THEN
            BEGIN
                RAISE NOTICE 'Attempting to drop constraint % on table %', constraint_name, tbl;
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
                RAISE NOTICE 'Attempting to add constraint % on table %', constraint_name, tbl;
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
    $body$;
    $fn$;

    -- drop_foreign_key_relationships
    EXECUTE $fn$
    CREATE OR REPLACE PROCEDURE drop_foreign_key_relationships()
    LANGUAGE plpgsql
    AS $body$
    BEGIN
        RAISE NOTICE 'drop_foreign_key_relationships STARTED';

        RAISE NOTICE 'Attempting to create index if not exists idx_role_case_type_id on table role';
        CREATE INDEX IF NOT EXISTS idx_role_case_type_id ON "role" (case_type_id);
        RAISE NOTICE 'Attempting to create index if not exists idx_case_type_id on table case_type';
        CREATE INDEX IF NOT EXISTS idx_case_type_id ON case_type (id);
        RAISE NOTICE 'Attempting to create index if not exists idx_role_id on table role';
        CREATE INDEX IF NOT EXISTS idx_role_id ON "role" (id);
        RAISE NOTICE 'Attempting to create index if not exists idx_user_role_id on table role';
        CREATE INDEX IF NOT EXISTS idx_user_role_id ON "role" (user_role_id);

        CALL manage_constraint('public.role', 'case_type_id_check', 'DROP');
        CALL manage_constraint('public.role', 'unique_role_case_type_id_role_reference', 'DROP');
        CALL manage_constraint('public.role', 'fk_role_case_type_id_case_type_id', 'DROP');
        CALL manage_constraint('public.case_field', 'fk_case_field_case_type_id', 'DROP');
        CALL manage_constraint('public.case_field_acl', 'fk_case_field_acl_case_field_id_case_field_id', 'DROP');
        CALL manage_constraint('public.case_field_acl', 'fk_case_field_acl_role_id_role_id', 'DROP');
        CALL manage_constraint('public.case_type_acl', 'fk_case_type_acl_case_type_id', 'DROP');
        CALL manage_constraint('public.case_type_acl', 'fk_case_type_acl_role_id_role_id', 'DROP');
        CALL manage_constraint('public.category', 'fk_category_case_type_id', 'DROP');
        CALL manage_constraint('public.challenge_question', 'fk_challenge_question_case_type_id', 'DROP');
        CALL manage_constraint('public.complex_field_acl', 'fk_complex_field_acl_case_field_id_case_field_id', 'DROP');
        CALL manage_constraint('public.complex_field_acl', 'fk_complex_field_acl_role_id_role_id', 'DROP');
        CALL manage_constraint('public.display_group', 'fk_display_group_case_type_id', 'DROP');
        CALL manage_constraint('public.display_group', 'fk_display_group_role_id', 'DROP');
        CALL manage_constraint('public.display_group_case_field', 'fk_display_group_case_field_case_field_id', 'DROP');
        CALL manage_constraint('public.event', 'fk_event_case_type_id', 'DROP');
        CALL manage_constraint('public.role_to_access_profiles', 'fk_case_field_role_to_access_profiles', 'DROP');
        CALL manage_constraint('public.search_alias_field', 'fk_search_alias_field_case_type_id', 'DROP');
        CALL manage_constraint('public.search_cases_result_fields', 'fk_search_cases_result_fields_case_field_id_case_field_id', 'DROP');
        CALL manage_constraint('public.search_cases_result_fields', 'fk_search_cases_result_fields_case_type_id', 'DROP');
        CALL manage_constraint('public.search_input_case_field', 'fk_display_group_role_id', 'DROP');
        CALL manage_constraint('public.search_input_case_field', 'fk_search_input_case_field_case_field_id', 'DROP');
        CALL manage_constraint('public.search_input_case_field', 'fk_search_input_case_field_case_type_id', 'DROP');
        CALL manage_constraint('public.search_result_case_field', 'fk_display_group_role_id', 'DROP');
        CALL manage_constraint('public.search_result_case_field', 'fk_search_result_case_field_case_field_id', 'DROP');
        CALL manage_constraint('public.search_result_case_field', 'fk_search_result_case_field_case_type_id', 'DROP');
        CALL manage_constraint('public.search_criteria', 'fk_case_field_search_criteria', 'DROP');
        CALL manage_constraint('public.search_party', 'fk_case_field_search_party', 'DROP');
        CALL manage_constraint('public.state', 'fk_state_case_type_id', 'DROP');
        CALL manage_constraint('public.workbasket_case_field', 'fk_display_group_role_id', 'DROP');
        CALL manage_constraint('public.workbasket_case_field', 'fk_workbasket_case_field_case_field_id', 'DROP');
        CALL manage_constraint('public.workbasket_case_field', 'fk_workbasket_case_field_case_type_id', 'DROP');
        CALL manage_constraint('public.workbasket_input_case_field', 'fk_display_group_role_id', 'DROP');
        CALL manage_constraint('public.workbasket_input_case_field', 'fk_workbasket_input_case_field_case_field_id', 'DROP');
        CALL manage_constraint('public.workbasket_input_case_field', 'fk_workbasket_input_case_field_case_type_id', 'DROP');
        CALL manage_constraint('public.access_type_role', 'fk_access_type_role_case_type_id', 'DROP');
        CALL manage_constraint('public.access_type', 'fk_access_type_case_type_id', 'DROP');
        CALL manage_constraint('public.search_cases_result_fields', 'fk_search_cases_result_fields_role_id_role_id', 'DROP');
        CALL manage_constraint('public.state_acl', 'fk_state_acl_role_id_role_id', 'DROP');
        CALL manage_constraint('public.event_acl', 'fk_event_acl_role_id_role_id', 'DROP');
        
        RAISE NOTICE 'drop_foreign_key_relationships FINISHED';

    END;
    $body$;
    $fn$;

    -- create_foreign_key_relationships
    EXECUTE $fn$
    CREATE OR REPLACE PROCEDURE create_foreign_key_relationships()
    LANGUAGE plpgsql
    AS $body$
    BEGIN
        
        RAISE NOTICE 'create_foreign_key_relationships STARTED';
       
        CALL manage_constraint(
        'public.role',
        'fk_role_case_type_id_case_type_id',
        'ADD',
        'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );
        
        CALL manage_constraint(
        'public.role',
        'unique_role_case_type_id_role_reference',
        'ADD',
        'UNIQUE (case_type_id, reference)'
        );
    
        CALL manage_constraint(
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

        CALL manage_constraint(
            'public.case_field',
            'fk_case_field_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );
    
        CALL manage_constraint(
        'public.case_field_acl',
        'fk_case_field_acl_case_field_id_case_field_id',
        'ADD',
        'FOREIGN KEY (case_field_id) REFERENCES case_field(id)'
        );

        CALL manage_constraint(
        'public.case_field_acl',
        'fk_case_field_acl_role_id_role_id',
        'ADD',
        'FOREIGN KEY (role_id) REFERENCES role(id)'
        );
    
        CALL manage_constraint(
            'public.case_type_acl',
            'fk_case_type_acl_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );

        CALL manage_constraint(
            'public.case_type_acl',
            'fk_case_type_acl_role_id_role_id',
            'ADD',
            'FOREIGN KEY (role_id) REFERENCES role(id)'
        );

        CALL manage_constraint(
            'public.category',
            'fk_category_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );

        CALL manage_constraint(
            'public.challenge_question',
            'fk_challenge_question_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );
    
        CALL manage_constraint(
            'public.complex_field_acl',
            'fk_complex_field_acl_case_field_id_case_field_id',
            'ADD',
            'FOREIGN KEY (case_field_id) REFERENCES case_field(id)'
        );

        CALL manage_constraint(
            'public.complex_field_acl',
            'fk_complex_field_acl_role_id_role_id',
            'ADD',
            'FOREIGN KEY (role_id) REFERENCES role(id)'
        );
    
        CALL manage_constraint(
            'public.display_group',
            'fk_display_group_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );

        CALL manage_constraint(
            'public.display_group_case_field',
            'fk_display_group_case_field_case_field_id',
            'ADD',
            'FOREIGN KEY (case_field_id) REFERENCES case_field(id)'
        );

        CALL manage_constraint(
            'public.workbasket_input_case_field',
            'fk_display_group_role_id',
            'ADD',
            'FOREIGN KEY (role_id) REFERENCES role(id)'
        );
    
        CALL manage_constraint(
            'public.workbasket_input_case_field',
            'fk_workbasket_input_case_field_case_field_id',
            'ADD',
            'FOREIGN KEY (case_field_id) REFERENCES case_field(id)'
        );

        CALL manage_constraint(
            'public.workbasket_input_case_field',
            'fk_workbasket_input_case_field_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );

        CALL manage_constraint(
            'public.workbasket_case_field',
            'fk_display_group_role_id',
            'ADD',
            'FOREIGN KEY (role_id) REFERENCES role(id)'
        );

        CALL manage_constraint(
            'public.workbasket_case_field',
            'fk_workbasket_case_field_case_field_id',
            'ADD',
            'FOREIGN KEY (case_field_id) REFERENCES case_field(id)'
        );

        CALL manage_constraint(
            'public.workbasket_case_field',
            'fk_workbasket_case_field_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );
    
        CALL manage_constraint(
            'public.search_result_case_field',
            'fk_display_group_role_id',
            'ADD',
            'FOREIGN KEY (role_id) REFERENCES role(id)'
        );

        CALL manage_constraint(
            'public.search_result_case_field',
            'fk_search_result_case_field_case_field_id',
            'ADD',
            'FOREIGN KEY (case_field_id) REFERENCES case_field(id)'
        );

        CALL manage_constraint(
            'public.search_result_case_field',
            'fk_search_result_case_field_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );
    
        CALL manage_constraint(
            'public.search_input_case_field',
            'fk_display_group_role_id',
            'ADD',
            'FOREIGN KEY (role_id) REFERENCES role(id)'
        );

        CALL manage_constraint(
            'public.search_input_case_field',
            'fk_search_input_case_field_case_field_id',
            'ADD',
            'FOREIGN KEY (case_field_id) REFERENCES case_field(id)'
        );

        CALL manage_constraint(
            'public.search_input_case_field',
            'fk_search_input_case_field_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );
    
        CALL manage_constraint(
            'public.display_group',
            'fk_display_group_role_id',
            'ADD',
            'FOREIGN KEY (role_id) REFERENCES role(id)'
        );

        CALL manage_constraint(
            'public.event',
            'fk_event_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );

        CALL manage_constraint(
            'public.event_acl',
            'fk_event_acl_role_id_role_id',
            'ADD',
            'FOREIGN KEY (role_id) REFERENCES role(id)'
        );

        CALL manage_constraint(
            'public.role_to_access_profiles',
            'fk_case_field_role_to_access_profiles',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );

        CALL manage_constraint(
            'public.search_alias_field',
            'fk_search_alias_field_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );

        CALL manage_constraint(
            'public.search_cases_result_fields',
            'fk_search_cases_result_fields_case_field_id_case_field_id',
            'ADD',
            'FOREIGN KEY (case_field_id) REFERENCES case_field(id)'
        );

        CALL manage_constraint(
            'public.search_cases_result_fields',
            'fk_search_cases_result_fields_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );
    
        CALL manage_constraint(
            'public.search_cases_result_fields',
            'fk_search_cases_result_fields_role_id_role_id',
            'ADD',
            'FOREIGN KEY (role_id) REFERENCES role(id)'
        );

        CALL manage_constraint(
            'public.search_criteria',
            'fk_case_field_search_criteria',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );

        CALL manage_constraint(
            'public.search_party',
            'fk_case_field_search_party',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );

        CALL manage_constraint(
            'public.state',
            'fk_state_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );
    
        CALL manage_constraint(
            'public.state_acl',
            'fk_state_acl_role_id_role_id',
            'ADD',
            'FOREIGN KEY (role_id) REFERENCES role(id)'
        );

        CALL manage_constraint(
            'public.access_type_role',
            'fk_access_type_role_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );

        CALL manage_constraint(
            'public.access_type',
            'fk_access_type_case_type_id',
            'ADD',
            'FOREIGN KEY (case_type_id) REFERENCES case_type(id)'
        );
        
        RAISE NOTICE 'create_foreign_key_relationships FINISHED';
    END;
    $body$;
    $fn$;

    -- prepare_cleanup_temp_tables
    EXECUTE $fn$
    CREATE OR REPLACE PROCEDURE prepare_cleanup_temp_tables(older_than_months int)
    LANGUAGE plpgsql
    AS $body$
        DECLARE
    	row_count int;
    BEGIN
        RAISE NOTICE 'prepare_cleanup_temp_tables STARTED';
       
        DROP TABLE IF EXISTS case_type_ids_to_remove;
        DROP TABLE IF EXISTS valid_field_type_ids;
        DROP TABLE IF EXISTS removable_case_fields;
        DROP TABLE IF EXISTS removable_field_type_ids;
        DROP TABLE IF EXISTS removable_events;
        DROP TABLE IF EXISTS removable_states;

        -- Create log output table if not exists
    	CREATE TABLE IF NOT EXISTS ddl_log (
    	    log_time TIMESTAMP DEFAULT now(),
    	    action TEXT,
    	    table_name TEXT,
    	    message TEXT
    	);

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

    	EXECUTE 'SELECT COUNT(*) FROM case_type_ids_to_remove' INTO row_count;

    	RAISE NOTICE 'Created temp table case_type_ids_to_remove for records older than % months with % rows',
        	older_than_months, row_count;
        CREATE INDEX idx_case_type_ids_to_remove_id ON case_type_ids_to_remove(id);
        ANALYZE case_type_ids_to_remove;

        BEGIN
            CREATE TEMP TABLE valid_field_type_ids AS
            SELECT id FROM field_type WHERE jurisdiction_id IS NULL;
            CREATE INDEX idx_valid_field_type_ids_id ON valid_field_type_ids(id);
            ANALYZE valid_field_type_ids;
            RAISE NOTICE 'Created temp table valid_field_type_ids with % rows',
                (SELECT COUNT(*) FROM valid_field_type_ids);
        EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'Could not create valid_field_type_ids: %', SQLERRM;
        END;

        BEGIN
            CREATE TEMP TABLE removable_case_fields AS
            SELECT id, case_type_id
            FROM case_field
            WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
              AND field_type_id NOT IN (SELECT id FROM valid_field_type_ids);
            CREATE INDEX idx_removable_case_fields_id ON removable_case_fields(id);
            CREATE INDEX idx_removable_case_fields_case_type_id ON removable_case_fields(case_type_id);
            ANALYZE removable_case_fields;
            RAISE NOTICE 'Created temp table removable_case_fields with % rows',
                (SELECT COUNT(*) FROM removable_case_fields);
        EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'Could not create removable_case_fields: %', SQLERRM;
        END;

        BEGIN
            CREATE TEMP TABLE removable_field_type_ids AS
            SELECT DISTINCT cf.field_type_id AS id
            FROM case_field cf
            JOIN field_type ft ON ft.id = cf.field_type_id
            WHERE cf.case_type_id IN (SELECT id FROM case_type_ids_to_remove)
              AND cf.field_type_id NOT IN (SELECT id FROM valid_field_type_ids)
              AND ft.jurisdiction_id IS NOT NULL;
            CREATE INDEX idx_removable_field_type_ids_id ON removable_field_type_ids(id);
            ANALYZE removable_field_type_ids;
            RAISE NOTICE 'Created temp table removable_field_type_ids with % rows',
                (SELECT COUNT(*) FROM removable_field_type_ids);
        EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'Could not create removable_field_type_ids: %', SQLERRM;
        END;

        BEGIN
            CREATE TEMP TABLE removable_events AS
            SELECT id FROM event
            WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove);
            CREATE INDEX idx_removable_events_id ON removable_events(id);
            ANALYZE removable_events;
            RAISE NOTICE 'Created temp table removable_events with % rows',
                (SELECT COUNT(*) FROM removable_events);
        EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'Could not create removable_events: %', SQLERRM;
        END;

        BEGIN
            CREATE TEMP TABLE removable_states AS
            SELECT id FROM state
            WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove);
            CREATE INDEX idx_removable_states_id ON removable_states(id);
            ANALYZE removable_states;
            RAISE NOTICE 'Created temp table removable_states with % rows',
                (SELECT COUNT(*) FROM removable_states);
        EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'Could not create removable_states: %', SQLERRM;
        END;
        
        RAISE NOTICE 'prepare_cleanup_temp_tables FINISHED';
    END;
    $body$;
    $fn$;

    -- drop_cleanup_temp_tables
    EXECUTE $fn$
    CREATE OR REPLACE PROCEDURE drop_cleanup_temp_tables()
    LANGUAGE plpgsql
    AS $body$
    BEGIN
        RAISE NOTICE 'drop_cleanup_temp_tables STARTED';
        DROP TABLE IF EXISTS case_type_ids_to_remove;
        DROP TABLE IF EXISTS valid_field_type_ids;
        DROP TABLE IF EXISTS removable_case_fields;
        DROP TABLE IF EXISTS removable_field_type_ids;
        DROP TABLE IF EXISTS removable_events;
        DROP TABLE IF EXISTS removable_states;
        RAISE NOTICE 'drop_cleanup_temp_tables FINISHED';
    END;
    $body$;
    $fn$;

    -- safe_delete_where
    EXECUTE $fn$
    CREATE OR REPLACE PROCEDURE safe_delete_where(
        tbl regclass,
        pk_column text,
        where_clause text,
        batch_size int DEFAULT 1000
    )
    LANGUAGE plpgsql
    AS $body$
    DECLARE
        rows_deleted int;
        total_deleted int := 0;
        full_tbl_name text := tbl::text;
    BEGIN
        LOOP
            RAISE NOTICE 'Attempting to delete up to % rows from %', batch_size, full_tbl_name;
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
            EXIT WHEN rows_deleted = 0;
            total_deleted := total_deleted + rows_deleted;
            RAISE NOTICE 'Batch deleted % rows from %', rows_deleted, full_tbl_name;
            INSERT INTO ddl_log(action, table_name, message)
            VALUES ('DELETE', full_tbl_name,
                    'Deleted batch of ' || rows_deleted || ' rows from ' || full_tbl_name);

            COMMIT;
        END LOOP;
        RAISE NOTICE 'Total deleted from %: % rows', full_tbl_name, total_deleted;
        INSERT INTO ddl_log(action, table_name, message)
        VALUES ('DELETE SUMMARY', full_tbl_name,
                'Total deleted ' || total_deleted || ' rows from ' || full_tbl_name);

        COMMIT;
    END;
    $body$;
    $fn$;

    -- run_safe_deletes (your long list of CALL safe_delete_where calls)
    EXECUTE $fn$
    CREATE OR REPLACE PROCEDURE run_safe_deletes(batch_size int DEFAULT 500)
    LANGUAGE plpgsql
    AS $body$
    BEGIN

        RAISE NOTICE 'run_safe_deletes STARTED with batch_size=%', batch_size;
       
        IF EXISTS (SELECT 1 FROM removable_case_fields) THEN
            CALL safe_delete_where(
                'case_field_acl',
                'case_field_id',
                'case_field_id IN (SELECT id FROM removable_case_fields)',
                batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping case_field_acl delete: removable_case_fields is empty';
        END IF;
        
        -- display_group_case_field
        IF EXISTS (SELECT 1 FROM removable_case_fields) THEN
            CALL safe_delete_where(
              'display_group_case_field',
              'case_field_id',
              'case_field_id IN (SELECT id FROM removable_case_fields)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping display_group_case_field: removable_case_fields empty';
        END IF;

        -- event_case_field_complex_type
        IF EXISTS (SELECT 1 FROM removable_events) THEN
            CALL safe_delete_where(
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
        ELSE
            RAISE NOTICE 'Skipping event_case_field_complex_type: removable_events empty';
        END IF;

        -- event_case_field
        IF EXISTS (SELECT 1 FROM removable_case_fields) THEN
            CALL safe_delete_where(
              'event_case_field',
              'case_field_id',
              'case_field_id IN (SELECT id FROM removable_case_fields)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping event_case_field: removable_case_fields empty';
        END IF;

        -- complex_field_acl
        IF EXISTS (SELECT 1 FROM removable_case_fields) THEN
            CALL safe_delete_where(
              'complex_field_acl',
              'case_field_id',
              'case_field_id IN (SELECT id FROM removable_case_fields)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping complex_field_acl: removable_case_fields empty';
        END IF;

        -- search_result_case_field
        IF EXISTS (SELECT 1 FROM removable_case_fields) THEN
            CALL safe_delete_where(
              'search_result_case_field',
              'case_field_id',
              'case_field_id IN (SELECT id FROM removable_case_fields)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping search_result_case_field: removable_case_fields empty';
        END IF;

        -- workbasket_case_field
        IF EXISTS (SELECT 1 FROM removable_case_fields) THEN
            CALL safe_delete_where(
              'workbasket_case_field',
              'case_field_id',
              'case_field_id IN (SELECT id FROM removable_case_fields)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping workbasket_case_field: removable_case_fields empty';
        END IF;

        -- search_input_case_field
        IF EXISTS (SELECT 1 FROM removable_case_fields) THEN
            CALL safe_delete_where(
              'search_input_case_field',
              'case_field_id',
              'case_field_id IN (SELECT id FROM removable_case_fields)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping search_input_case_field: removable_case_fields empty';
        END IF;

        -- workbasket_input_case_field
        IF EXISTS (SELECT 1 FROM removable_case_fields) THEN
            CALL safe_delete_where(
              'workbasket_input_case_field',
              'case_field_id',
              'case_field_id IN (SELECT id FROM removable_case_fields)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping workbasket_input_case_field: removable_case_fields empty';
        END IF;

        -- search_cases_result_fields
        IF EXISTS (SELECT 1 FROM removable_case_fields)
           AND EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'search_cases_result_fields',
              'case_field_id',
              'case_field_id IN (SELECT id FROM removable_case_fields)
               AND case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping search_cases_result_fields: driver tables empty';
        END IF;

        -- case_field (delete the case fields themselves)
        IF EXISTS (SELECT 1 FROM removable_case_fields) THEN
            CALL safe_delete_where(
              'case_field',
              'id',
              'id IN (SELECT id FROM removable_case_fields)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping case_field: removable_case_fields empty';
        END IF;

        -- case_type_acl
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'case_type_acl',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping case_type_acl: case_type_ids_to_remove empty';
        END IF;

        -- event_post_state
        IF EXISTS (SELECT 1 FROM removable_events) THEN
            CALL safe_delete_where(
              'event_post_state',
              'case_event_id',
              'case_event_id IN (SELECT id FROM removable_events)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping event_post_state: removable_events empty';
        END IF;

        -- event_acl
        IF EXISTS (SELECT 1 FROM removable_events) THEN
            CALL safe_delete_where(
              'event_acl',
              'event_id',
              'event_id IN (SELECT id FROM removable_events)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping event_acl: removable_events empty';
        END IF;

        -- event_pre_state
        IF EXISTS (SELECT 1 FROM removable_events) THEN
            CALL safe_delete_where(
              'event_pre_state',
              'event_id',
              'event_id IN (SELECT id FROM removable_events)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping event_pre_state: removable_events empty';
        END IF;

        -- event_webhook
        IF EXISTS (SELECT 1 FROM removable_events) THEN
            CALL safe_delete_where(
              'event_webhook',
              'event_id',
              'event_id IN (SELECT id FROM removable_events)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping event_webhook: removable_events empty';
        END IF;

        -- state_acl
        IF EXISTS (SELECT 1 FROM removable_states) THEN
            CALL safe_delete_where(
              'state_acl',
              'state_id',
              'state_id IN (SELECT id FROM removable_states)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping state_acl: removable_states empty';
        END IF;

        -- state
        IF EXISTS (SELECT 1 FROM removable_states) THEN
            CALL safe_delete_where(
              'state',
              'id',
              'id IN (SELECT id FROM removable_states)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping state: removable_states empty';
        END IF;

        -- redundant search/workbasket deletes
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove)
           AND EXISTS (SELECT 1 FROM removable_case_fields) THEN
            CALL safe_delete_where(
              'search_cases_result_fields',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)
               AND case_field_id IN (SELECT id FROM removable_case_fields)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping search_cases_result_fields (redundant): driver tables empty';
        END IF;

        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove)
           AND EXISTS (SELECT 1 FROM removable_case_fields) THEN
            CALL safe_delete_where(
              'search_input_case_field',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)
               AND case_field_id IN (SELECT id FROM removable_case_fields)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping search_input_case_field (redundant): driver tables empty';
        END IF;

        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove)
           AND EXISTS (SELECT 1 FROM removable_case_fields) THEN
            CALL safe_delete_where(
              'search_result_case_field',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)
               AND case_field_id IN (SELECT id FROM removable_case_fields)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping search_result_case_field (redundant): driver tables empty';
        END IF;

        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove)
           AND EXISTS (SELECT 1 FROM removable_case_fields) THEN
            CALL safe_delete_where(
              'workbasket_case_field',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)
               AND case_field_id IN (SELECT id FROM removable_case_fields)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping workbasket_case_field (redundant): driver tables empty';
        END IF;

        -- case_field_acl (by case_type)
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'case_field_acl',
              'case_field_id',
              'case_field_id IN (
                   SELECT id FROM case_field WHERE case_type_id IN
                       (SELECT id FROM case_type_ids_to_remove)
               )',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping case_field_acl (case_type): driver empty';
        END IF;

        -- display_group_case_field (by case_type)
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'display_group_case_field',
              'display_group_id',
              'display_group_id IN (
                   SELECT id FROM display_group WHERE case_type_id IN
                       (SELECT id FROM case_type_ids_to_remove)
               )',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping display_group_case_field (case_type): driver empty';
        END IF;

        -- event_case_field_complex_type (by case_type)
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'event_case_field_complex_type',
              'event_case_field_id',
              'event_case_field_id IN (
                   SELECT id FROM event_case_field WHERE event_id IN
                       (SELECT id FROM event WHERE case_type_id IN
                           (SELECT id FROM case_type_ids_to_remove))
               )',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping event_case_field_complex_type (case_type): driver empty';
        END IF;

        -- complex_field_acl (by case_type)
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'complex_field_acl',
              'case_field_id',
              'case_field_id IN (
                   SELECT id FROM case_field WHERE case_type_id IN
                       (SELECT id FROM case_type_ids_to_remove)
               )',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping complex_field_acl (case_type): driver empty';
        END IF;

        -- search_cases_result_fields (by case_type_id)
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'search_cases_result_fields',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping search_cases_result_fields: case_type_ids_to_remove empty';
        END IF;

        -- search_input_case_field (by case_type_id)
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'search_input_case_field',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping search_input_case_field: case_type_ids_to_remove empty';
        END IF;

        -- search_result_case_field (by case_type_id)
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'search_result_case_field',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping search_result_case_field: case_type_ids_to_remove empty';
        END IF;

        -- workbasket_case_field (by case_type_id)
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'workbasket_case_field',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping workbasket_case_field: case_type_ids_to_remove empty';
        END IF;

        -- workbasket_input_case_field (by case_type_id)
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'workbasket_input_case_field',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping workbasket_input_case_field: case_type_ids_to_remove empty';
        END IF;

        -- event_case_field (by case_type_id)
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'event_case_field',
              'event_id',
              'event_id IN (
                   SELECT id FROM event WHERE case_type_id IN
                       (SELECT id FROM case_type_ids_to_remove)
               )',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping event_case_field (case_type): driver empty';
        END IF;

        -- case_field (by case_type_id)
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'case_field',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping case_field (case_type): driver empty';
        END IF;

        -- role_to_access_profiles
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'role_to_access_profiles',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping role_to_access_profiles: driver empty';
        END IF;

        -- display_group (by case_type_id)
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'display_group',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping display_group: driver empty';
        END IF;

        -- event (by case_type_id)
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'event',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping event: driver empty';
        END IF;

        -- search_criteria
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'search_criteria',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping search_criteria: driver empty';
        END IF;

        -- search_party
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'search_party',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping search_party: driver empty';
        END IF;

        -- search_alias_field
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'search_alias_field',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping search_alias_field: driver empty';
        END IF;

        -- category
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'category',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping category: driver empty';
        END IF;

        -- challenge_question
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'challenge_question',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping challenge_question: driver empty';
        END IF;

        -- role
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'role',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping role: driver empty';
        END IF;

        -- access_type_role
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'access_type_role',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping access_type_role: driver empty';
        END IF;

        -- access_type
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'access_type',
              'case_type_id',
              'case_type_id IN (SELECT id FROM case_type_ids_to_remove)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping access_type: driver empty';
        END IF;

        -- field_type_list_item
        IF EXISTS (SELECT 1 FROM removable_field_type_ids) THEN
            CALL safe_delete_where(
              'field_type_list_item',
              'field_type_id',
              'field_type_id IN (SELECT id FROM removable_field_type_ids)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping field_type_list_item: removable_field_type_ids empty';
        END IF;

        -- complex_field
        IF EXISTS (SELECT 1 FROM removable_field_type_ids) THEN
            CALL safe_delete_where(
              'complex_field',
              'id',
              'field_type_id IN (SELECT id FROM removable_field_type_ids)
               OR complex_field_type_id IN (SELECT id FROM removable_field_type_ids)',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping complex_field: removable_field_type_ids empty';
        END IF;

        -- field_type
        IF EXISTS (SELECT 1 FROM removable_field_type_ids) THEN
            CALL safe_delete_where(
              'field_type',
              'id',
              'id IN (SELECT id FROM removable_field_type_ids)
               AND id NOT IN (SELECT id FROM valid_field_type_ids)
               AND jurisdiction_id IS NOT NULL',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping field_type: removable_field_type_ids empty';
        END IF;

        -- final case_type cleanup
        IF EXISTS (SELECT 1 FROM case_type_ids_to_remove) THEN
            CALL safe_delete_where(
              'case_type',
              'id',
              'id IN (SELECT id FROM case_type_ids_to_remove)
               AND jurisdiction_id IS NOT NULL',
              batch_size
            );
        ELSE
            RAISE NOTICE 'Skipping case_type final cleanup: driver empty';
        END IF;
       
        RAISE NOTICE 'run_safe_deletes FINISHED';

    END;
    $body$;
    $fn$;

    ----------------------------------------------------------------------
    -- 1. RUN THE CLEANUP PIPELINE
    ----------------------------------------------------------------------

    CREATE TABLE IF NOT EXISTS ddl_log (
        log_time TIMESTAMP DEFAULT now(),
        action TEXT,
        table_name TEXT,
        message TEXT
    );
    COMMIT;

    CALL drop_foreign_key_relationships();
    COMMIT;

    CALL prepare_cleanup_temp_tables(older_than_months::int);
    COMMIT;

    CALL run_safe_deletes(batch_size::int);
    COMMIT;

    CALL drop_cleanup_temp_tables();
    COMMIT;

    CALL create_foreign_key_relationships();
    COMMIT;
    
    ----------------------------------------------------------------------
    -- 2. CLEAN UP HELPER PROCEDURES
    ----------------------------------------------------------------------
    EXECUTE 'DROP ROUTINE IF EXISTS manage_constraint(regclass, text, text, text)';
    EXECUTE 'DROP ROUTINE IF EXISTS drop_foreign_key_relationships()';
    EXECUTE 'DROP ROUTINE IF EXISTS create_foreign_key_relationships()';
    EXECUTE 'DROP ROUTINE IF EXISTS safe_delete_where(regclass, text, text, int4)';
    EXECUTE 'DROP ROUTINE IF EXISTS prepare_cleanup_temp_tables(int4)';
    EXECUTE 'DROP ROUTINE IF EXISTS drop_cleanup_temp_tables()';
    EXECUTE 'DROP ROUTINE IF EXISTS run_safe_deletes(int4)';

    ----------------------------------------------------------------------
    -- 3. FINAL SUMMARY STEP
    ----------------------------------------------------------------------

    summary_msg := format(
        'cleanup_case_types procedure finished successfully with batch_size = %s, data_older_than = %s, at %s',
        batch_size,
        older_than_months,
        now()
    );

    RAISE NOTICE '%', summary_msg;
	INSERT INTO ddl_log(action, table_name, message)
	VALUES (
	    'SUMMARY',
	    'cleanup_case_types',
	    summary_msg
	);

    COMMIT;

END;
$$;
