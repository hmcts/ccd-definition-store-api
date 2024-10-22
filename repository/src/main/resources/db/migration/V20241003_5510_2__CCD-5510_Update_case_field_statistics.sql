ALTER TABLE case_field ALTER COLUMN case_type_id SET STATISTICS 1000;

ANALYZE case_field;
