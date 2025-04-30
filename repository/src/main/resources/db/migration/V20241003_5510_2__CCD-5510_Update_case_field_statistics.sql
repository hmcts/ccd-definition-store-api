ALTER TABLE public.case_field ALTER COLUMN case_type_id SET STATISTICS 1000;

ANALYZE public.case_field;
