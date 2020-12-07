CREATE INDEX IF NOT EXISTS event_case_field_event_id_case_field_id_idx ON public.event_case_field USING btree (event_id, case_field_id);
