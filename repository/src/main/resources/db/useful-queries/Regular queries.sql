--Before deleting anything, it's advised to list all existing jurisdictions and case types
--and double-check what should be deleted.
SELECT DISTINCT j.reference AS j_reference, ct.reference AS ct_reference
FROM jurisdiction AS j, case_type AS ct WHERE j.id = ct.jurisdiction_id;
