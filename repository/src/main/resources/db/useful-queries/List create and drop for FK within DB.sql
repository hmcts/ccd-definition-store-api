--List FK creation statements
copy (SELECT 'ALTER TABLE '||nspname||'.\"'||relname||'\" ADD CONSTRAINT \"'||conname||'\" '|| pg_get_constraintdef(pg_constraint.oid)||';'
FROM pg_constraint
INNER JOIN pg_class ON conrelid=pg_class.oid
INNER JOIN pg_namespace ON pg_namespace.oid=pg_class.relnamespace
	  WHERE relname NOT
			 LIKE 'flyway%'
ORDER BY CASE WHEN contype='f' THEN 0 ELSE 1 END DESC,contype DESC,nspname DESC,
	  relname DESC,conname DESC) to '<path-to-save>/createConstraints.sql';

--List FK deletion statements
copy (SELECT 'ALTER TABLE '||nspname||'.\"'||relname||'\" DROP CONSTRAINT \"'||conname||'\";'
FROM pg_constraint
INNER JOIN pg_class ON conrelid=pg_class.oid
INNER JOIN pg_namespace ON pg_namespace.oid=pg_class.relnamespace
    WHERE relname NOT
    			 LIKE 'flyway%'
ORDER BY CASE WHEN contype='f' THEN 0 ELSE 1 END,contype,nspname,relname,conname) to '<path-to-save>/droppingConstraints.sql';

--Usage
--1. Execute drop of constraints
--2. Perform deletion as required
--3. Execute re-creation of constraints
