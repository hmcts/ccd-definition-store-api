--list all tables vs row count (with table exclusions)
with tempTable as (SELECT table_schema,table_name FROM
			 information_schema.tables where table_name not
			 like 'pg_%' and table_name not
			 like 'flyway%' and table_schema in ('public'))
select table_schema, table_name, (xpath('/row/c/text()',
    query_to_xml(format('select count(*) as c from %I.%I', table_schema, table_name), false, true, '')))[1]::text::int
	as records_count from tempTable ORDER BY 3 DESC;

select jurisdiction_id, count(*) from field_type where created_at<='yyyy-mm-dd' group by 1;

select jurisdiction_id, count(*) from field_type where created_at<= 'now'::timestamp - '3 MONTH'::interval group by 1;

SELECT relname AS TableName, n_live_tup AS LiveTuples, n_dead_tup AS DeadTuples
    FROM pg_stat_user_tables order by n_dead_tup desc ;

select
    listitems0_.field_type_id   as field_ty5_19_1_,
    listitems0_.id              as id1_19_1_,
    listitems0_.id              as id1_19_0_,
    listitems0_.field_type_id   as field_ty5_19_0_,
    listitems0_.label           as label2_19_0_,
    listitems0_.display_order   as display_3_19_0_,
    listitems0_.value           as value4_19_0_

from field_type_list_item listitems0_
where
    listitems0_.field_type_id in
    (
        select fieldtypee1_.id from case_field casefields0_ inner join field_type fieldtypee1_ on casefields0_.field_type_id=fieldtypee1_.id
        where
            casefields0_.case_type_id in
            (
                select casetypeen0_.id from case_type casetypeen0_ cross join jurisdiction jurisdicti2_
                where
                    casetypeen0_.jurisdiction_id=jurisdicti2_.id
                    and (casetypeen0_.version in
                            (
                                select max(casetypeen1_.version)
                                from case_type casetypeen1_
                                where casetypeen1_.reference=casetypeen0_.reference
                            )
                        )
                     and jurisdicti2_.reference=? --? should be a varchar value
             )
    )

select count(*) from field_type where created_at <= 'yyyy-mm-dd';

select * from field_type ft where ft.jurisdiction_id!=null and created_at<='yyyy-mm-dd';

select count(*) from field_type ft where ft.jurisdiction_id=null and created_at<='yyyy-mm-dd';

select jurisdiction_id, count(*) from field_type group by 1;

--Can set this to treat empty as null\pset null '[null]'
--Null display is "[null]".

select jurisdiction_id, count(*) from field_type where created_at<='yyyy-mm-dd' group by 1;

select count(*) from field_type;

select count(*) from field_type ft where jurisdiction_id>0 and created_at<='yyyy-mm-dd';

select ct.id, ct.reference, ct.created_at, count(cf.*) as "Case Field Count"
from case_type ct, case_field cf
where
	ct.id = cf.case_type_id
    and (select count(*) from field_type ft where ft.id=cf.field_type_id)=0
group by 1, 2, 3
order by 2 desc, 3 asc

-- Query to return ALL case types for which a higher version already exists
-- These should therefore be historical records
SELECT ct.id, ct.created_at, ct.reference, ct.version FROM case_type ct INNER JOIN
        (SELECT reference, MAX("version") AS MaxVersion
        FROM case_type
        GROUP BY reference) grouped_ct
    ON ct.reference = grouped_ct.reference
	AND (ct.version != grouped_ct.MaxVersion)


