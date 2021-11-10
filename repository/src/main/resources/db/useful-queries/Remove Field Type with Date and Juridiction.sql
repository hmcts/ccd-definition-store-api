
DECLARE
  fieldTypeCreateAt constant varchar := 'yyyy-mm-dd';

--list all field_type created on or before yyyy-mm-dd
select count(*) from field_type where created_at<=fieldTypeCreateAt;

--list all field_type created on or before yyyy-mm-dd where jurisdiction_id is not null
select * from field_type ft where ft.jurisdiction_id!=null and created_at<=fieldTypeCreateAt;

--list all field_type created on or before yyyy-mm-dd where jurisdiction_id is null
select count(*) from field_type ft where ft.jurisdiction_id=null and created_at<=fieldTypeCreateAt;

--aggregation of jurisdiction_id vs frequency
select jurisdiction_id, count(*) from field_type group by 1;

--aggregation of jurisdiction_id vs frequency created on or before yyyy-mm-dd
select jurisdiction_id, count(*) from field_type where created_at<=fieldTypeCreateAt group by 1;

--list field_type used by a jurisdiction created on or before yyyy-mm-dd
select count(*) from field_type ft where jurisdiction_id>0 and created_at<=fieldTypeCreateAt;

--list count of rows in table: field_type
select count(*) from field_type;

--list case_type id, reference and created date where field type not used
select ct.id, ct.reference, ct.created_at, count(cf.*) as "Case Field Count"
from case_type ct, case_field cf
where
	ct.id = cf.case_type_id
    and (select count(*) from field_type ft where ft.id=cf.field_type_id)=0
group by 1, 2, 3
order by 2 desc, 3 asc

delete from field_type ft where ft.jurisdiction_id!=null and created_at<=fieldTypeCreateAt

delete from field_type ft where jurisdiction_id>0 and created_at<=fieldTypeCreateAt


