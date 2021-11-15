--list all tables vs row count (with table exclusions)
WITH tempTable AS (SELECT table_schema,table_name FROM
			 information_schema.tables WHERE table_name not
			 LIKE 'pg_%' AND table_name not
			 LIKE 'flyway%' AND table_schema IN ('public'))
SELECT table_schema, table_name, (xpath('/row/c/text()',
    query_to_xml(format('SELECT COUNT(*) AS c FROM %I.%I', table_schema, table_name), false, true, '')))[1]::text::int
	AS records_count FROM tempTable ORDER BY 3 DESC;

SELECT jurisdiction_id, COUNT(*) FROM field_type WHERE created_at<='yyyy-mm-dd' GROUP BY 1;

SELECT jurisdiction_id, COUNT(*) FROM field_type WHERE created_at<= 'now'::timestamp - '3 MONTH'::interval GROUP BY 1;

SELECT relname AS TableName, n_live_tup AS LiveTuples, n_dead_tup AS DeadTuples
    FROM pg_stat_user_tables ORDER BY n_dead_tup desc ;

SELECT
    listitems0_.field_type_id   AS field_ty5_19_1_,
    listitems0_.id              AS id1_19_1_,
    listitems0_.id              AS id1_19_0_,
    listitems0_.field_type_id   AS field_ty5_19_0_,
    listitems0_.label           AS label2_19_0_,
    listitems0_.display_order   AS display_3_19_0_,
    listitems0_.value           AS value4_19_0_
FROM field_type_list_item listitems0_
WHERE
    listitems0_.field_type_id IN
    (
        SELECT fieldtypee1_.id FROM case_field casefields0_ inner join field_type fieldtypee1_ on casefields0_.field_type_id=fieldtypee1_.id
        WHERE
            casefields0_.case_type_id IN
            (
                SELECT casetypeen0_.id FROM case_type casetypeen0_ cross join jurisdiction jurisdicti2_
                WHERE
                    casetypeen0_.jurisdiction_id=jurisdicti2_.id
                    AND (casetypeen0_.version IN
                            (
                                SELECT max(casetypeen1_.version)
                                FROM case_type casetypeen1_
                                WHERE casetypeen1_.reference=casetypeen0_.reference
                            )
                        )
                     AND jurisdicti2_.reference=? --? should be a varchar value
             )
    )

SELECT COUNT(*) FROM field_type WHERE created_at <= 'yyyy-mm-dd';

SELECT * FROM field_type ft WHERE ft.jurisdiction_id!=null AND created_at<='yyyy-mm-dd';

SELECT COUNT(*) FROM field_type ft WHERE ft.jurisdiction_id=null AND created_at<='yyyy-mm-dd';

SELECT jurisdiction_id, COUNT(*) FROM field_type GROUP BY 1;

--Can set this to treat empty AS null\pset null '[null]'
--Null display is "[null]".

SELECT jurisdiction_id, COUNT(*) FROM field_type WHERE created_at<='yyyy-mm-dd' GROUP BY 1;

SELECT COUNT(*) FROM field_type;

SELECT COUNT(*) FROM field_type ft WHERE jurisdiction_id>0 AND created_at<='yyyy-mm-dd';

SELECT ct.id, ct.reference, ct.created_at, COUNT(cf.*) AS "Case Field Count"
FROM case_type ct, case_field cf
WHERE
	ct.id = cf.case_type_id
    AND (SELECT COUNT(*) FROM field_type ft WHERE ft.id=cf.field_type_id)=0
GROUP BY 1, 2, 3
ORDER BY 2 desc, 3 asc

-- Query to return ALL case types for which a higher version already exists
-- These should therefore be historical records
SELECT ct.id, ct.created_at, ct.reference, ct.version FROM case_type ct INNER JOIN
        (SELECT reference, MAX("version") AS MaxVersion
        FROM case_type
        GROUP BY reference) grouped_ct
    ON ct.reference = grouped_ct.reference
	AND (ct.version != grouped_ct.MaxVersion)


