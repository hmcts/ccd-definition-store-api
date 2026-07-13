-- add column case_field_element_path
ALTER TABLE public.display_group_case_field
    ADD COLUMN IF NOT EXISTS case_field_element_path character varying(300);

ALTER TABLE ONLY public.display_group_case_field
    DROP CONSTRAINT IF EXISTS unique_display_group_case_field_display_group_id_case_field_id;

CREATE UNIQUE INDEX IF NOT EXISTS unique_dgcf_whole_case_field
    ON public.display_group_case_field USING btree (display_group_id, case_field_id)
    WHERE case_field_element_path IS NULL;

DROP INDEX IF EXISTS public.unique_dgcf_case_field_element_path;

CREATE UNIQUE INDEX unique_dgcf_case_field_element_path
    ON public.display_group_case_field USING btree (display_group_id, case_field_id, lower(case_field_element_path))
    WHERE case_field_element_path IS NOT NULL;
