-- Delete jurisdiction
DO $$
DECLARE
  jurisdictionId constant varchar := '???';
BEGIN

  delete from event_case_field_complex_type where event_case_field_id in (select id from event_case_field where case_field_id in
    (select id from case_field where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId))));

  delete from event_case_field where case_field_id in (select id from case_field where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId)));

  delete from display_group_case_field where case_field_id in (select id from case_field where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId)));

  delete from case_field_acl where case_field_id in (select id from case_field where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId)));

  delete from workbasket_case_field where case_field_id in (select id from case_field where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId)));

  delete from workbasket_input_case_field where case_field_id in (select id from case_field where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId)));

  delete from search_result_case_field where case_field_id in (select id from case_field where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId)));

  delete from search_input_case_field where case_field_id in (select id from case_field where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId)));

  delete from search_alias_field where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId));

  delete from complex_field_acl where case_field_id in (select id from case_field where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId)));

  delete from case_field where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId));

  delete from display_group where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId));

  delete from event_webhook where event_id in (select id from event where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId)));

  delete from event_pre_state where event_id in (select id from event where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId)));

  delete from event_acl where event_id in (select id from event where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId)));

  delete from event where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId));

  delete from state_acl where state_id in (select id from state where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId)));

  delete from state where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId));

  delete from case_type_acl where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId));

  delete from role where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId));

  delete from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId);

  delete from field_type_list_item where field_type_id in (select id from field_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId));

  delete from complex_field where complex_field_type_id in (select id from field_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId));

  delete from field_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId);

  delete from jurisdiction_ui_config where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId);

  delete from challenge_question where case_type_id in (select id from case_type where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId));

  delete from definition_designer where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId);

  delete from banner where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId);

  delete from state_test where case_type_test_id in (select id from case_type_test where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId));

  delete from webhook_test where id in (select print_webhook_test_id from case_type_test where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId));

  delete from case_type_test where jurisdiction_id = (select id from jurisdiction where reference = jurisdictionId);

  delete from jurisdiction where reference = jurisdictionId;
END $$;
