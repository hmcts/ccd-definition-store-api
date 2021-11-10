--Before deleting anything, it's advised to list all existing jurisdictions and case types
--and double-check what should be deleted.
select DISTINCT j.reference as j_reference, ct.reference as ct_reference from jurisdiction as j, case_type as ct where j.id = ct.jurisdiction_id;
