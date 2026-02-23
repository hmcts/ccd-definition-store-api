	------------------------------------------------------------------------------------------------------------------------------
	--List ALL FK deletion statements (provided for completeness)
	--If using pgadmin or dbweaver remove the '\' as these are required for terminal psql only.
	--Note this will output 'drop constraint' statements for all FKs defined in each table of the DB
	copy (SELECT 'ALTER TABLE '||nspname||'.\"'||relname||'\" DROP CONSTRAINT \"'||conname||'\";'
	FROM pg_constraint
	INNER JOIN pg_class ON conrelid=pg_class.oid
	INNER JOIN pg_namespace ON pg_namespace.oid=pg_class.relnamespace
		WHERE relname NOT
					LIKE 'flyway%' and contype = 'f' 
	ORDER BY CASE WHEN contype='f' THEN 0 ELSE 1 END,contype,nspname,relname,conname) to '<path-to-save>/droppingConstraints.sql';
	------------------------------------------------------------------------------------------------------------------------------
	
	------------------------------------------------------------------------------------------------------------------------------	
	--List ALL FK creation statements (provided for completeness)
	--If using pgadmin or dbweaver remove the '\' as these are required for terminal psql only.
	--Note this will output 'add constraint' statements for all FKs defined in each table of the DB
	copy (SELECT 'ALTER TABLE '||nspname||'.\"'||relname||'\" ADD CONSTRAINT \"'||conname||'\" '|| pg_get_constraintdef(pg_constraint.oid)||';'
	FROM pg_constraint
	INNER JOIN pg_class ON conrelid=pg_class.oid
	INNER JOIN pg_namespace ON pg_namespace.oid=pg_class.relnamespace
		WHERE relname NOT
				LIKE 'flyway%' and contype = 'f'
	ORDER BY CASE WHEN contype='f' THEN 0 ELSE 1 END DESC,contype DESC,nspname DESC,
		relname DESC,conname DESC) to '<path-to-save>/createConstraints.sql';
	------------------------------------------------------------------------------------------------------------------------------

	--1
	------------------------------------------------------------------------------------------------------------------------------
	--For Normal clean up activities within CCD BAU the following drop statements should suffice
	--Output is formatted to run in SQL editor
	--Note this will output 'drop constraint' statements for all tables except flyway and complex_field
	SELECT 'ALTER TABLE '||nspname||'."'||relname||'" DROP CONSTRAINT "'||conname||'";'
	FROM pg_constraint
	INNER JOIN pg_class ON conrelid=pg_class.oid
	INNER JOIN pg_namespace ON pg_namespace.oid=pg_class.relnamespace
		WHERE (relname NOT
					LIKE 'flyway%') and contype = 'f' and relname <> 'complex_field'
	ORDER BY CASE WHEN contype='f' THEN 0 ELSE 1 END,contype,nspname,relname,conname
	------------------------------------------------------------------------------------------------------------------------------

    --2
	------------------------------------------------------------------------------------------------------------------------------
	--For Normal clean up activities within CCD BAU the following add statements should suffice
	--Output is formatted to run in SQL editor
	--Note this will output 'add constraint' statements for all tables except flyway and complex_field
	SELECT 'ALTER TABLE '||nspname||'.\"'||relname||'\" ADD CONSTRAINT \"'||conname||'\" '|| pg_get_constraintdef(pg_constraint.oid)||';'
	FROM pg_constraint
	INNER JOIN pg_class ON conrelid=pg_class.oid
	INNER JOIN pg_namespace ON pg_namespace.oid=pg_class.relnamespace
		WHERE (relname NOT
					LIKE 'flyway%') and contype = 'f' and relname <> 'complex_field'
	ORDER BY CASE WHEN contype='f' THEN 0 ELSE 1 END DESC,contype DESC,nspname DESC,
		relname DESC,conname DESC
	------------------------------------------------------------------------------------------------------------------------------


Usage:
1. Run the script (labelled as --1) above, make a note of the drop-constraint statements generated
2. Run the script (labelled as --2) above, make a note of the add-constraint statements generated
(note: once the drop-constraint statements are executed you will no longer be able to generate the add-constraint statements, hence we need to make a note of them before any actual executions)

3. Execute the drop-constraint statements obtained from step 1. above
4. Perform deletion as required
5. Execute the add-constraint statements obtained from step 2. above
