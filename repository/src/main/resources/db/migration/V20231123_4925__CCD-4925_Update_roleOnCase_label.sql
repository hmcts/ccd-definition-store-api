UPDATE complex_field SET label = 'Role On Case'
WHERE reference = 'roleOnCase'
  AND field_type_id = (SELECT id FROM field_type WHERE reference = 'Text' AND version = 1 AND jurisdiction_id IS NULL)
  AND complex_field_type_id = (SELECT id FROM field_type WHERE reference = 'Flags' AND version = 1 AND jurisdiction_id IS NULL);
