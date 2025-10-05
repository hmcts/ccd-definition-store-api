--Before deleting anything, it's advised to list all existing jurisdictions and case types
--and double-check what should be deleted.
SELECT DISTINCT j.reference AS j_reference, ct.reference AS ct_reference
FROM jurisdiction AS j, case_type AS ct WHERE j.id = ct.jurisdiction_id;

--
--This will return all the foreign key constraints on the role table
--May be required to drop those keys to speed up the delete process
SELECT conrelid::regclass AS child_table,
       confrelid::regclass AS parent_table,
       conname
FROM pg_constraint
WHERE confrelid = 'role'::regclass AND contype = 'f';

--Using explain and analyze to check the performance of a query
--This will return the query plan for the delete statement
--This will not delete the records found
EXPLAIN DELETE FROM "role"
USING case_type_ids_to_remove
WHERE "role".case_type_id = case_type_ids_to_remove.id;

--This will delete the records found
EXPLAIN ANALYZE DELETE FROM "role"
USING case_type_ids_to_remove
WHERE "role".case_type_id = case_type_ids_to_remove.id;

--Using LOOPs
DO $$
DECLARE
    rows_deleted INTEGER;
    batch_count INTEGER := 0;
BEGIN
    LOOP
        DELETE FROM "role"
        USING case_type_ids_to_remove
        WHERE "role".case_type_id = case_type_ids_to_remove.id
        LIMIT 10000;

        GET DIAGNOSTICS rows_deleted = ROW_COUNT;
        batch_count := batch_count + 1;

        RAISE NOTICE 'Batch %, deleted % rows.', batch_count, rows_deleted;

        EXIT WHEN rows_deleted = 0;

        PERFORM pg_sleep(0.1);  -- Optional pause to reduce load
    END LOOP;

    RAISE NOTICE 'Finished deletion in % batches.', batch_count;
END$$;

--This cleans dead tuples and updates planner statistics:
VACUUM ANALYZE;

--Aggressive: Full Vacuum (Reclaims Disk Space)
--This will lock the table and reclaim disk space
--This rewrites tables and indexes to fully reclaim disk space — but it locks each table exclusively, so use it during maintenance windows.
VACUUM FULL;

--Vacuum a Single Table
VACUUM ANALYZE public.role;
--or
VACUUM FULL public.role;

--Check What Needs Vacuuming
SELECT relname AS table,
       n_live_tup AS live_rows,
       n_dead_tup AS dead_rows,
       round(100.0 * n_dead_tup / NULLIF(n_live_tup + n_dead_tup, 0), 2) AS dead_pct
FROM pg_stat_user_tables
ORDER BY n_dead_tup DESC
LIMIT 10;

--Here's a PostgreSQL script that automatically vacuums only bloated tables — specifically, those with a high number or percentage of dead tuples (i.e., rows deleted/updated but not yet reclaimed).
--This targets tables with:
--  At least 10,000 dead rows, or
--  More than 20% of rows dead
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT schemaname, relname
        FROM pg_stat_user_tables
        WHERE n_dead_tup > 10000
           OR (n_dead_tup > 0 AND (n_dead_tup::numeric / NULLIF(n_live_tup + n_dead_tup, 0)) > 0.2)
    LOOP
        RAISE NOTICE 'Vacuuming %.%', r.schemaname, r.relname;
        EXECUTE format('VACUUM ANALYZE %I.%I;', r.schemaname, r.relname);
    END LOOP;
END$$;

--This will return all the locks on the role table
SELECT
  pg_stat_activity.pid,
  pg_stat_activity.query,
  pg_locks.mode,
  pg_locks.locktype,
  pg_locks.granted
FROM pg_locks
JOIN pg_stat_activity ON pg_locks.pid = pg_stat_activity.pid
WHERE relation::regclass::text = 'role';

--This will terminate the lock on the role table
SELECT pg_terminate_backend(31201);
