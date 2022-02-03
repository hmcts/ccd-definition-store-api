-- add column case_access_categories

ALTER TABLE public.role_to_access_profiles
ADD COLUMN case_access_categories character varying(1000);
