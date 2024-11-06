CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_state_case_type_id ON public.state USING btree (case_type_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_complex_field_acl_case_field_id ON public.complex_field_acl USING btree (case_field_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_event_acl_event_id ON public.event_acl USING btree (event_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_state_acl_state_id_id ON public.state_acl USING btree (state_id, id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_search_party_case_type_id ON public.search_party USING btree (case_type_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_case_type_lower_reference_version_desc ON public.case_type USING btree (LOWER(reference), version DESC);
