--
-- Name: display_group_event_id_idx; Type: INDEX; Schema: display_group; Owner: -
--

CREATE INDEX CONCURRENTLY IF NOT EXISTS display_group_event_id_idx ON display_group USING btree (event_id);

--
-- Name: idx_workbasket_input_case_field_case_field_id; Type: INDEX; Schema: workbasket_input_case; Owner: -
--
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_workbasket_input_case_field_case_field_id ON workbasket_input_case_field USING btree (case_field_id);

--
-- Name: idx_workbasket_input_case_field_case_field_id; Type: INDEX; Schema: workbasket_case_field; Owner: -
--
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_workbasket_case_field_case_field_id ON workbasket_case_field USING btree (case_field_id);

--
-- Name: idx_search_result_case_field_case_field_id; Type: INDEX; Schema: search_result_case_field; Owner: -
--
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_search_result_case_field_case_field_id ON search_result_case_field USING btree (case_field_id);

--
-- Name: idx_search_input_case_field_case_field_id; Type: INDEX; Schema: search_input_case_field; Owner: -
--
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_search_input_case_field_case_field_id ON search_input_case_field USING btree (case_field_id);

--
-- Name: idx_search_cases_result_fields_case_field_id; Type: INDEX; Schema: search_cases_result_fields; Owner: -
--
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_search_cases_result_fields_case_field_id ON search_cases_result_fields USING btree (case_field_id);

--
-- Name: idx_display_group_case_field_case_field_id; Type: INDEX; Schema: display_group_case_field; Owner: -
--
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_display_group_case_field_case_field_id ON display_group_case_field USING btree (case_field_id);
