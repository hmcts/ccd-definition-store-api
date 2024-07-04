--These are the common 'drop constraint' statements executed prior to a data cleanup exercise on defintion store

--start -- in Prod but not in AAT
ALTER TABLE public."access_type" DROP CONSTRAINT "fk_access_type_case_type_id";
ALTER TABLE public."access_type_role" DROP CONSTRAINT "fk_access_type_role_case_type_id";
--end -- in Prod but not in AAT

ALTER TABLE public."banner" DROP CONSTRAINT "fk_banner_jurisdiction_id";
ALTER TABLE public."case_field" DROP CONSTRAINT "fk_case_field_case_type_id";
ALTER TABLE public."case_field" DROP CONSTRAINT "fk_case_field_field_type_id";
ALTER TABLE public."case_field_acl" DROP CONSTRAINT "fk_case_field_acl_case_field_id_case_field_id";
ALTER TABLE public."case_field_acl" DROP CONSTRAINT "fk_case_field_acl_role_id_role_id";
ALTER TABLE public."case_type" DROP CONSTRAINT "fk_case_type_get_case_webhook_id";
ALTER TABLE public."case_type" DROP CONSTRAINT "fk_case_type_jurisdiction_id";
ALTER TABLE public."case_type" DROP CONSTRAINT "fk_case_type_print_webhook_id";
ALTER TABLE public."case_type_acl" DROP CONSTRAINT "fk_case_type_acl_case_type_id";
ALTER TABLE public."case_type_acl" DROP CONSTRAINT "fk_case_type_acl_role_id_role_id";
ALTER TABLE public."category" DROP CONSTRAINT "fk_category_case_type_id";
ALTER TABLE public."challenge_question" DROP CONSTRAINT "fk_challenge_question_case_type_id";
ALTER TABLE public."challenge_question" DROP CONSTRAINT "fk_challenge_question_field_type_id";
ALTER TABLE public."complex_field_acl" DROP CONSTRAINT "fk_complex_field_acl_case_field_id_case_field_id";
ALTER TABLE public."complex_field_acl" DROP CONSTRAINT "fk_complex_field_acl_role_id_role_id";
ALTER TABLE public."definition_designer" DROP CONSTRAINT "fk_definition_designer_jurisdiction_id";
ALTER TABLE public."display_group" DROP CONSTRAINT "fk_display_group_case_type_id";
ALTER TABLE public."display_group" DROP CONSTRAINT "fk_display_group_event_id";
ALTER TABLE public."display_group" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."display_group" DROP CONSTRAINT "fk_display_group_webhook_mid_event_id";
ALTER TABLE public."display_group_case_field" DROP CONSTRAINT "fk_display_group_case_field_case_field_id";
ALTER TABLE public."display_group_case_field" DROP CONSTRAINT "fk_display_group_case_field_display_group_id";
ALTER TABLE public."event" DROP CONSTRAINT "fk_event_case_type_id";
ALTER TABLE public."event_acl" DROP CONSTRAINT "fk_event_acl_event_id";
ALTER TABLE public."event_acl" DROP CONSTRAINT "fk_event_acl_role_id_role_id";
ALTER TABLE public."event_case_field" DROP CONSTRAINT "fk_event_case_field_case_field_id";
ALTER TABLE public."event_case_field" DROP CONSTRAINT "fk_event_case_field_event_id";
ALTER TABLE public."event_case_field_complex_type" DROP CONSTRAINT "fk_event_case_field_complex_type_event_case_field_id";
ALTER TABLE public."event_post_state" DROP CONSTRAINT "fk_event_post_state_case_event_id";
ALTER TABLE public."event_pre_state" DROP CONSTRAINT "fk_event_pre_state_event_id";
ALTER TABLE public."event_pre_state" DROP CONSTRAINT "fk_event_pre_state_state_id";
ALTER TABLE public."event_webhook" DROP CONSTRAINT "event_webhook_event_id_fkey";
ALTER TABLE public."event_webhook" DROP CONSTRAINT "event_webhook_webhook_id_fkey";

--start -- Added as part of CCD-4327
ALTER TABLE public."complex_field" DROP CONSTRAINT "fk_complex_field_complex_field_type_id";
ALTER TABLE public."complex_field" DROP CONSTRAINT "fk_complex_field_field_type_id";
ALTER TABLE public."field_type" DROP CONSTRAINT "fk_field_type_base_field_type_id";
ALTER TABLE public."field_type" DROP CONSTRAINT "fk_field_type_collection_field_type_id";
ALTER TABLE public."field_type" DROP CONSTRAINT "fk_field_type_jurisdiction_id";
ALTER TABLE public."field_type_list_item" DROP CONSTRAINT "fk_field_type_list_item_field_type_id";
--end -- Added as part of CCD-4327

ALTER TABLE public."jurisdiction_ui_config" DROP CONSTRAINT "fk_jurisdiction_ui_config_jurisdiction_id";
ALTER TABLE public."role" DROP CONSTRAINT "fk_role_case_type_id_case_type_id";
ALTER TABLE public."role_to_access_profiles" DROP CONSTRAINT "fk_case_field_role_to_access_profiles";
ALTER TABLE public."search_alias_field" DROP CONSTRAINT "fk_search_alias_field_case_type_id";
ALTER TABLE public."search_alias_field" DROP CONSTRAINT "fk_search_alias_field_field_type_id";
ALTER TABLE public."search_cases_result_fields" DROP CONSTRAINT "fk_search_cases_result_fields_case_field_id_case_field_id";
ALTER TABLE public."search_cases_result_fields" DROP CONSTRAINT "fk_search_cases_result_fields_case_type_id";
ALTER TABLE public."search_cases_result_fields" DROP CONSTRAINT "fk_search_cases_result_fields_role_id_role_id";
ALTER TABLE public."search_criteria" DROP CONSTRAINT "fk_case_field_search_criteria";
ALTER TABLE public."search_input_case_field" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."search_input_case_field" DROP CONSTRAINT "fk_search_input_case_field_case_field_id";
ALTER TABLE public."search_input_case_field" DROP CONSTRAINT "fk_search_input_case_field_case_type_id";
ALTER TABLE public."search_party" DROP CONSTRAINT "fk_case_field_search_party";
ALTER TABLE public."search_result_case_field" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."search_result_case_field" DROP CONSTRAINT "fk_search_result_case_field_case_field_id";
ALTER TABLE public."search_result_case_field" DROP CONSTRAINT "fk_search_result_case_field_case_type_id";
ALTER TABLE public."state" DROP CONSTRAINT "fk_state_case_type_id";
ALTER TABLE public."state_acl" DROP CONSTRAINT "fk_state_acl_role_id_role_id";
ALTER TABLE public."state_acl" DROP CONSTRAINT "fk_state_acl_state_id_state_id";
ALTER TABLE public."workbasket_case_field" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."workbasket_case_field" DROP CONSTRAINT "fk_workbasket_case_field_case_field_id";
ALTER TABLE public."workbasket_case_field" DROP CONSTRAINT "fk_workbasket_case_field_case_type_id";
ALTER TABLE public."workbasket_input_case_field" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."workbasket_input_case_field" DROP CONSTRAINT "fk_workbasket_input_case_field_case_field_id";
ALTER TABLE public."workbasket_input_case_field" DROP CONSTRAINT "fk_workbasket_input_case_field_case_type_id";
