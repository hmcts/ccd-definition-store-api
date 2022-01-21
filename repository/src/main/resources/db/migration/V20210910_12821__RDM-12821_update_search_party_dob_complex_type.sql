UPDATE complex_field
SET field_type_id = (SELECT id FROM field_type WHERE reference = 'Date' AND version = 1 AND jurisdiction_id IS NULL)
WHERE complex_field_type_id = (SELECT id FROM field_type WHERE reference = 'SearchParty' AND version = 1 AND jurisdiction_id IS NULL)
AND reference = 'DateOfBirth'
