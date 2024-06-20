CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_workbasket_input_case_field_case_type_id ON public.workbasket_input_case_field USING btree (case_type_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_category_case_type_id ON public.category USING btree (case_type_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_case_type_get_case_webhook_id ON public.case_type USING btree (get_case_webhook_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_workbasket_case_field_case_type_id ON public.workbasket_case_field USING btree (case_type_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_search_input_case_field_case_type_id ON public.search_input_case_field USING btree (case_type_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_search_result_case_field_case_type_id ON public.search_result_case_field USING btree (case_type_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_role_to_access_profiles_case_type_id ON public.role_to_access_profiles USING btree (case_type_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_challenge_question_composite ON public.challenge_question USING btree (case_type_id, challenge_question_id);
