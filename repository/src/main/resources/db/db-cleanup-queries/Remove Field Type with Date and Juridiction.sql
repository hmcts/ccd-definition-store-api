
DECLARE
  fieldTypeCreateAt constant varchar := 'yyyy-mm-dd';

--list all field_type created on or before yyyy-mm-dd
SELECT COUNT(*) FROM field_type WHERE created_at<=fieldTypeCreateAt;

--list all field_type created on or before yyyy-mm-dd WHERE jurisdiction_id is not null
SELECT * FROM field_type ft WHERE ft.jurisdiction_id!=null AND created_at<=fieldTypeCreateAt;

--list all field_type created on or before yyyy-mm-dd WHERE jurisdiction_id is null
SELECT COUNT(*) FROM field_type ft WHERE ft.jurisdiction_id=null AND created_at<=fieldTypeCreateAt;

--aggregation of jurisdiction_id vs frequency
SELECT jurisdiction_id, COUNT(*) FROM field_type GROUP BY 1;

--aggregation of jurisdiction_id vs frequency created on or before yyyy-mm-dd
SELECT jurisdiction_id, COUNT(*) FROM field_type WHERE created_at<=fieldTypeCreateAt GROUP BY 1;

--list field_type used BY a jurisdiction created on or before yyyy-mm-dd
SELECT COUNT(*) FROM field_type ft WHERE jurisdiction_id>0 AND created_at<=fieldTypeCreateAt;

--list COUNT of rows IN table: field_type
SELECT COUNT(*) FROM field_type;

--list case_type id, reference AND created date WHERE field type not used
SELECT ct.id, ct.reference, ct.created_at, COUNT(cf.*) AS "Case Field Count"
FROM case_type ct, case_field cf
WHERE
	ct.id = cf.case_type_id
    AND (SELECT COUNT(*) FROM field_type ft WHERE ft.id=cf.field_type_id)=0
GROUP BY 1, 2, 3
order BY 2 desc, 3 asc

delete FROM field_type_list_item ftli
WHERE ftli.id IN
    (
        SELECT l.id FROM field_type_list_item l, field_type r
        WHERE
            l.field_type_id=r.id
            AND r.created_at<=fieldTypeCreateAt
            AND r.jurisdiction_id != null
    )

delete FROM field_type ft WHERE ft.jurisdiction_id!=null AND created_at<=fieldTypeCreateAt

delete FROM field_type ft WHERE jurisdiction_id>0 AND created_at<=fieldTypeCreateAt


