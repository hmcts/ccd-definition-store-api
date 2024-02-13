ALTER TABLE public.access_type_roles
    RENAME COLUMN organisation_policy_field TO case_assigned_role_field;

ALTER TABLE public.access_type_roles
    ALTER COLUMN case_assigned_role_field TYPE character varying(70);
