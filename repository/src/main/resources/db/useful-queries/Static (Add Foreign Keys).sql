--These are the common 'add constraint' statements executed post a data cleanup exercise on defintion store
ALTER TABLE public."workbasket_input_case_field" ADD CONSTRAINT "fk_workbasket_input_case_field_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."workbasket_input_case_field" ADD CONSTRAINT "fk_workbasket_input_case_field_case_field_id" FOREIGN KEY (case_field_id) REFERENCES case_field(id);
ALTER TABLE public."workbasket_input_case_field" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."workbasket_case_field" ADD CONSTRAINT "fk_workbasket_case_field_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."workbasket_case_field" ADD CONSTRAINT "fk_workbasket_case_field_case_field_id" FOREIGN KEY (case_field_id) REFERENCES case_field(id);
ALTER TABLE public."workbasket_case_field" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."state_acl" ADD CONSTRAINT "fk_state_acl_state_id_state_id" FOREIGN KEY (state_id) REFERENCES state(id);
ALTER TABLE public."state_acl" ADD CONSTRAINT "fk_state_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."state" ADD CONSTRAINT "fk_state_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."search_result_case_field" ADD CONSTRAINT "fk_search_result_case_field_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."search_result_case_field" ADD CONSTRAINT "fk_search_result_case_field_case_field_id" FOREIGN KEY (case_field_id) REFERENCES case_field(id);
ALTER TABLE public."search_result_case_field" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."search_party" ADD CONSTRAINT "fk_case_field_search_party" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."search_input_case_field" ADD CONSTRAINT "fk_search_input_case_field_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."search_input_case_field" ADD CONSTRAINT "fk_search_input_case_field_case_field_id" FOREIGN KEY (case_field_id) REFERENCES case_field(id);
ALTER TABLE public."search_input_case_field" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."search_criteria" ADD CONSTRAINT "fk_case_field_search_criteria" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."search_cases_result_fields" ADD CONSTRAINT "fk_search_cases_result_fields_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."search_cases_result_fields" ADD CONSTRAINT "fk_search_cases_result_fields_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."search_cases_result_fields" ADD CONSTRAINT "fk_search_cases_result_fields_case_field_id_case_field_id" FOREIGN KEY (case_field_id) REFERENCES case_field(id);
ALTER TABLE public."search_alias_field" ADD CONSTRAINT "fk_search_alias_field_field_type_id" FOREIGN KEY (field_type_id) REFERENCES field_type(id);
ALTER TABLE public."search_alias_field" ADD CONSTRAINT "fk_search_alias_field_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."role_to_access_profiles" ADD CONSTRAINT "fk_case_field_role_to_access_profiles" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."role" ADD CONSTRAINT "fk_role_case_type_id_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."jurisdiction_ui_config" ADD CONSTRAINT "fk_jurisdiction_ui_config_jurisdiction_id" FOREIGN KEY (jurisdiction_id) REFERENCES jurisdiction(id);

--start -- Added as part of CCD-4327
ALTER TABLE public."complex_field" ADD CONSTRAINT fk_complex_field_complex_field_type_id FOREIGN KEY (complex_field_type_id) REFERENCES field_type(id);
ALTER TABLE public."complex_field" ADD CONSTRAINT fk_complex_field_field_type_id FOREIGN KEY (field_type_id) REFERENCES field_type(id);
ALTER TABLE public."field_type_list_item" ADD CONSTRAINT "fk_field_type_list_item_field_type_id" FOREIGN KEY (field_type_id) REFERENCES field_type(id);
ALTER TABLE public."field_type" ADD CONSTRAINT "fk_field_type_jurisdiction_id" FOREIGN KEY (jurisdiction_id) REFERENCES jurisdiction(id);
ALTER TABLE public."field_type" ADD CONSTRAINT "fk_field_type_collection_field_type_id" FOREIGN KEY (collection_field_type_id) REFERENCES field_type(id);
ALTER TABLE public."field_type" ADD CONSTRAINT "fk_field_type_base_field_type_id" FOREIGN KEY (base_field_type_id) REFERENCES field_type(id);
--end -- Added as part of CCD-4327

ALTER TABLE public."event_webhook" ADD CONSTRAINT "event_webhook_webhook_id_fkey" FOREIGN KEY (webhook_id) REFERENCES webhook(id);
ALTER TABLE public."event_webhook" ADD CONSTRAINT "event_webhook_event_id_fkey" FOREIGN KEY (event_id) REFERENCES event(id);
ALTER TABLE public."event_pre_state" ADD CONSTRAINT "fk_event_pre_state_state_id" FOREIGN KEY (state_id) REFERENCES state(id);
ALTER TABLE public."event_pre_state" ADD CONSTRAINT "fk_event_pre_state_event_id" FOREIGN KEY (event_id) REFERENCES event(id);
ALTER TABLE public."event_post_state" ADD CONSTRAINT "fk_event_post_state_case_event_id" FOREIGN KEY (case_event_id) REFERENCES event(id);
ALTER TABLE public."event_case_field_complex_type" ADD CONSTRAINT "fk_event_case_field_complex_type_event_case_field_id" FOREIGN KEY (event_case_field_id) REFERENCES event_case_field(id);
ALTER TABLE public."event_case_field" ADD CONSTRAINT "fk_event_case_field_event_id" FOREIGN KEY (event_id) REFERENCES event(id);
ALTER TABLE public."event_case_field" ADD CONSTRAINT "fk_event_case_field_case_field_id" FOREIGN KEY (case_field_id) REFERENCES case_field(id);
ALTER TABLE public."event_acl" ADD CONSTRAINT "fk_event_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."event_acl" ADD CONSTRAINT "fk_event_acl_event_id" FOREIGN KEY (event_id) REFERENCES event(id);
ALTER TABLE public."event" ADD CONSTRAINT "fk_event_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."display_group_case_field" ADD CONSTRAINT "fk_display_group_case_field_display_group_id" FOREIGN KEY (display_group_id) REFERENCES display_group(id);
ALTER TABLE public."display_group_case_field" ADD CONSTRAINT "fk_display_group_case_field_case_field_id" FOREIGN KEY (case_field_id) REFERENCES case_field(id);
ALTER TABLE public."display_group" ADD CONSTRAINT "fk_display_group_webhook_mid_event_id" FOREIGN KEY (webhook_mid_event_id) REFERENCES webhook(id);
ALTER TABLE public."display_group" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."display_group" ADD CONSTRAINT "fk_display_group_event_id" FOREIGN KEY (event_id) REFERENCES event(id);
ALTER TABLE public."display_group" ADD CONSTRAINT "fk_display_group_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."definition_designer" ADD CONSTRAINT "fk_definition_designer_jurisdiction_id" FOREIGN KEY (jurisdiction_id) REFERENCES jurisdiction(id);
ALTER TABLE public."complex_field_acl" ADD CONSTRAINT "fk_complex_field_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."complex_field_acl" ADD CONSTRAINT "fk_complex_field_acl_case_field_id_case_field_id" FOREIGN KEY (case_field_id) REFERENCES case_field(id);
ALTER TABLE public."challenge_question" ADD CONSTRAINT "fk_challenge_question_field_type_id" FOREIGN KEY (answer_field_type) REFERENCES field_type(id);
ALTER TABLE public."challenge_question" ADD CONSTRAINT "fk_challenge_question_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."category" ADD CONSTRAINT "fk_category_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."case_type_acl" ADD CONSTRAINT "fk_case_type_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."case_type_acl" ADD CONSTRAINT "fk_case_type_acl_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."case_type" ADD CONSTRAINT "fk_case_type_print_webhook_id" FOREIGN KEY (print_webhook_id) REFERENCES webhook(id);
ALTER TABLE public."case_type" ADD CONSTRAINT "fk_case_type_jurisdiction_id" FOREIGN KEY (jurisdiction_id) REFERENCES jurisdiction(id);
ALTER TABLE public."case_type" ADD CONSTRAINT "fk_case_type_get_case_webhook_id" FOREIGN KEY (get_case_webhook_id) REFERENCES webhook(id);
ALTER TABLE public."case_field_acl" ADD CONSTRAINT "fk_case_field_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."case_field_acl" ADD CONSTRAINT "fk_case_field_acl_case_field_id_case_field_id" FOREIGN KEY (case_field_id) REFERENCES case_field(id);
ALTER TABLE public."case_field" ADD CONSTRAINT "fk_case_field_field_type_id" FOREIGN KEY (field_type_id) REFERENCES field_type(id);
ALTER TABLE public."case_field" ADD CONSTRAINT "fk_case_field_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."banner" ADD CONSTRAINT "fk_banner_jurisdiction_id" FOREIGN KEY (jurisdiction_id) REFERENCES jurisdiction(id);
